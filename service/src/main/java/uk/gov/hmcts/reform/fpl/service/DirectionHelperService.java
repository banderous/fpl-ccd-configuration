package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

/**
 * A service that helps with the sorting and editing of directions.
 */
@Service
public class DirectionHelperService {

    /**
     * Combines role directions into a single List of directions within an Order object.
     *
     * @param caseData data containing all the directions by role.
     * @return Order object.
     */
    public Order createOrder(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(filterDirectionsNotRequired(caseData.getAllParties()));

        directions.addAll(assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(filterDirectionsNotRequired(caseData.getLocalAuthorityDirections()));

        directions.addAll(assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY));

        directions.addAll(filterDirectionsNotRequired(caseData.getParentsAndRespondentsDirections()));

        directions.addAll(assignCustomDirections(caseData.getParentsAndRespondentsCustom(), PARENTS_AND_RESPONDENTS));

        directions.addAll(filterDirectionsNotRequired(caseData.getCafcassDirections()));

        directions.addAll(assignCustomDirections(caseData.getCafcassDirectionsCustom(), CAFCASS));

        directions.addAll(filterDirectionsNotRequired(caseData.getOtherPartiesDirections()));

        directions.addAll(assignCustomDirections(caseData.getOtherPartiesDirectionsCustom(), OTHERS));

        directions.addAll(filterDirectionsNotRequired(caseData.getCourtDirections()));

        directions.addAll(assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

        return Order.builder().directions(directions).build();
    }

    /**
     * Adds values that would otherwise be lost in CCD to directions.
     * Values include readOnly, directionRemovable and directionText.
     *
     * @param orderWithHiddenValues an order object that should be generated using original case data.
     * @param orderToAddValues      an order object that should be generated using case data edited through a ccd event.
     */
    public void persistHiddenDirectionValues(Order orderWithHiddenValues, Order orderToAddValues) {
        orderToAddValues.getDirections()
            .forEach(directionToAddValue -> orderWithHiddenValues.getDirections()
                .stream()
                .filter(direction -> direction.getId().equals(directionToAddValue.getId()))
                .forEach(direction -> {
                    directionToAddValue.getValue().setReadOnly(direction.getValue().getReadOnly());
                    directionToAddValue.getValue().setDirectionRemovable(direction.getValue().getDirectionRemovable());

                    if (!direction.getValue().getReadOnly().equals("No")) {
                        directionToAddValue.getValue().setDirectionText(direction.getValue().getDirectionText());
                    }
                }));
    }

    /**
     * Splits a list of directions into a map where the key is the role of the direction assignee and the value is the
     * list of directions belonging to the role.
     *
     * @param directions a list of directions with various assignees.
     * @return Map of role name, list of directions.
     */
    public Map<String, List<Element<Direction>>> sortDirectionsByAssignee(List<Element<Direction>> directions) {
        return directions.stream()
            .filter(x -> x.getValue().getCustom() == null)
            .collect(groupingBy(directionElement -> directionElement.getValue().getAssignee().getValue()));
    }

    /**
     * Iterates over a list of directions and adds numbers to the directionType starting from 2.
     *
     * @param directions a list of directions.
     * @return a list of directions with numbered directionType.
     */
    public List<Element<Direction>> numberDirections(List<Element<Direction>> directions) {
        AtomicInteger at = new AtomicInteger(2);

        return directions.stream()
            .map(direction -> {
                Direction.DirectionBuilder directionBuilder = direction.getValue().toBuilder();

                return Element.<Direction>builder()
                    .id(direction.getId())
                    .value(directionBuilder
                        .directionType(at.getAndIncrement() + ". " + direction.getValue().getDirectionType())
                        .build())
                    .build();
            })
            .collect(toList());
    }

    /**
     * Takes a direction from a configuration file and builds a CCD direction.
     *
     * @param direction  the direction taken from json config.
     * @param completeBy the date to be completed by. Can be null.
     * @return Direction to be stored in CCD.
     */
    public Element<Direction> constructDirectionForCCD(DirectionConfiguration direction, LocalDateTime completeBy) {
        return Element.<Direction>builder()
            .id(randomUUID())
            .value(Direction.builder()
                .directionType(direction.getTitle())
                .directionText(direction.getText())
                .assignee(direction.getAssignee())
                .directionRemovable(booleanToYesOrNo(direction.getDisplay().isDirectionRemovable()))
                .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
                .dateToBeCompletedBy(completeBy)
                .build())
            .build();
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }


    private List<Element<Direction>> assignCustomDirections(List<Element<Direction>> directions,
                                                            DirectionAssignee assignee) {
        if (!isNull(directions)) {
            return directions.stream()
                .map(element -> Element.<Direction>builder()
                    .id(element.getId())
                    .value(element.getValue().toBuilder()
                        .assignee(assignee)
                        .custom("Yes")
                        .readOnly("No")
                        .build())
                    .build())
                .collect(toList());
        } else {
            return emptyList();
        }
    }

    private List<Element<Direction>> filterDirectionsNotRequired(List<Element<Direction>> directions) {
        directions.removeIf(directionElement -> "No".equals(directionElement.getValue().getDirectionNeeded()));

        return directions;
    }
}
