package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final DraftCMOService draftCMOService;
    private final ObjectMapper mapper;

    @Autowired
    public DraftCMOController(DraftCMOService draftCMOService, ObjectMapper mapper) {
        this.draftCMOService = draftCMOService;
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        final CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.putAll(draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseData.getCaseManagementOrder(), caseData.getHearingDetails()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final Map<String, Object> data = caseDetails.getData();

        CaseManagementOrder caseManagementOrder = draftCMOService.getCaseManagementOrder(data);

        draftCMOService.prepareCaseDetails(data, caseManagementOrder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
