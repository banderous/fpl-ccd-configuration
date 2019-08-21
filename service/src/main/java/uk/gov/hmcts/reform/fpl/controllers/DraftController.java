package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Directions;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/draft-SDO")
public class DraftController {

    private final ObjectMapper mapper;

    public DraftController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        List<Element<Direction>> directions = ImmutableList.of(
            Element.<Direction>builder()
                .id(UUID.randomUUID())
                .value(Direction.builder()
                    .title("Arrange an advocates' meeting")
                    .assignee("Cafcass")
                    .build())
                .build());

        caseDetails.getData().put("cmo", ImmutableList.of(Element.builder().value(
            ImmutableMap.of("directions", directions)).build()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Directions.DirectionsBuilder cafcassDirections = Directions.builder();

        List<Element<Directions>> cmo = caseData.getCmo();

        cmo.forEach(x -> {
            if (x.getValue().getDirections().get(0).getValue().getAssignee().equals("Cafcass")) {
                cafcassDirections.directions(x.getValue().getDirections()).build();
            }
        });

        caseDetails.getData().put("cafcassDirections", cafcassDirections.build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
