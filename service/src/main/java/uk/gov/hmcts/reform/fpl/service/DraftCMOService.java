package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;


@Service
public class DraftCMOService {
    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;

    @Autowired
    public DraftCMOService(DateFormatterService dateFormatterService, ObjectMapper mapper) {
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
    }

    public DynamicList getHearingDateDynamicList(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        DynamicList hearingDatesDynamic = buildDynamicListFromHearingDetails(hearingDetails);

        if (isNotEmpty(caseData.getCaseManagementOrder())) {
            prePopulateHearingDateSelection(hearingDetails,
                hearingDatesDynamic,
                caseData.getCaseManagementOrder());
        }

        return hearingDatesDynamic;
    }

    private void prePopulateHearingDateSelection(List<Element<HearingBooking>> hearingDetails,
                                                 DynamicList hearingDatesDynamic,
                                                 CaseManagementOrder caseManagementOrder) {
        UUID hearingDateId = caseManagementOrder.getId();
        // There was a previous hearing date therefore we need to remap it
        String date = hearingDetails.stream()
            .filter(Objects::nonNull)
            .filter(element -> element.getId().equals(caseManagementOrder.getId()))
            .findFirst()
            .map(element -> formatLocalDateToMediumStyle(element.getValue().getDate()))
            .orElse("");

        DynamicListElement listElement = DynamicListElement.builder()
            .label(date)
            .code(hearingDateId)
            .build();

        hearingDatesDynamic.setValue(listElement);
    }

    public String createRespondentsKey(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        StringBuilder stringBuilder = new StringBuilder("");

        if (isNotEmpty(caseData.getRespondents1())) {
            for (int i = 0; i < caseData.getRespondents1().size(); i++) {
                RespondentParty respondentParty = caseData.getRespondents1().get(i).getValue().getParty();

                String key = String.format("Respondent %d: %s", i + 1, getRespondentsFullName(respondentParty));
                stringBuilder.append(key).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public String createOtherPartiesKey(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        System.out.println(caseData.getOthers());

        StringBuilder stringBuilder = new StringBuilder("");

        if (isNotEmpty(caseData.getOthers())) {
            for (int i = 0; i < caseData.getOthers().getAllOthers().size(); i++) {
                Other other = caseData.getOthers().getAllOthers().get(i);
                String key;

                if (i == 0) {
                    key = String.format("Person %d: %s", i + 1, defaultIfNull(other.getName(), ""));
                } else {
                    key = String.format("Other Person %d: %s", i + 1, defaultIfNull(other.getName(), ""));
                }

                stringBuilder.append(key).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    private String getRespondentsFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return firstName + " " + lastName;
    }

    public DynamicList buildDynamicListFromHearingDetails(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDateDynamicElement> hearingDates = hearingDetails
            .stream()
            .map(element -> new HearingDateDynamicElement(
                formatLocalDateToMediumStyle(element.getValue().getDate()), element.getId()))
            .collect(toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    public CaseManagementOrder getCaseManagementOrder(CaseDetails caseDetails) {
        DynamicList list = mapper.convertValue(caseDetails.getData().get("cmoHearingDateList"), DynamicList.class);

        return CaseManagementOrder.builder()
            .hearingDate(list.getValue().getLabel())
            .id(list.getValue().getCode())
            .build();
    }

    private String formatLocalDateToMediumStyle(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
