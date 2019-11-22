package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final DraftCMOService draftCMOService;

    @Autowired
    public DraftCMOController(DraftCMOService draftCMOService) {
        this.draftCMOService = draftCMOService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("cmoHearingDateList", draftCMOService.getHearingDateDynamicList(caseDetails));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseManagementOrder caseManagementOrder = draftCMOService.getCaseManagementOrder(caseDetails);

        caseDetails.getData().remove("cmoHearingDateList");
        caseDetails.getData().put("caseManagementOrder", caseManagementOrder);

        // FIXME: 22/11/2019 NPE being thrown here, clearly something isn't being assigned properly
        switch (caseManagementOrder.getCmoStatus()) {
            case SEND_TO_JUDGE:
                // Currently do nothing but something will probably happen here in the future
                System.out.println("SEND");
                break;
            case PARTIES_REVIEW:
                // Move to new entry in case details that everyone has permissions to see
                caseDetails.getData().put("shareableCMO", caseManagementOrder);
                System.out.println("REVIEW");
                break;
            case SELF_REVIEW:
                // Remove the party review entry from case details if it exists
                caseDetails.getData().remove("shareableCMO"); // TODO: 22/11/2019 Change this name
                System.out.println("MINE");
                break;
            default:
                // Do nothing
                break;
        }


        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
