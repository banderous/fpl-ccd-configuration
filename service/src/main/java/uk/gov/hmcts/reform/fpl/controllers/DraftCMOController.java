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
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;

import static java.util.Objects.isNull;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;
    private final DirectionHelperService directionHelperService;


    @Autowired
    public DraftCMOController(ObjectMapper mapper,
                              DraftCMOService draftCMOService,
                              DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
        this.directionHelperService = directionHelperService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isNull(caseData.getCaseManagementOrder())) {
            directionHelperService.sortDirectionsByAssignee(caseData.getCaseManagementOrder().getDirections())
                .forEach(caseDetails.getData()::put);
        } else {
            removeExistingDirectionsFromCaseDetails(caseDetails);
        }

        caseDetails.getData().put("cmoHearingDateList", draftCMOService.getHearingDateDynamicList(caseDetails));

        setCustomDirectionDropdownKeys(caseDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseManagementOrder caseManagementOrder = draftCMOService.getCaseManagementOrder(caseDetails);

        caseDetails.getData().remove("cmoHearingDateList");
        caseDetails.getData().put("caseManagementOrder", caseManagementOrder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private CaseDetails setCustomDirectionDropdownKeys(CaseDetails caseDetails) {
        caseDetails.getData().put("otherPartiesDropdownKeyCMO",
            draftCMOService.createOtherPartiesAssigneeDropdownKey(caseDetails));

        caseDetails.getData().put("parentsAndRespondentsDropdownKeyCMO",
            draftCMOService.createParentsAndRespondentAssigneeDropdownKey(caseDetails));

        return caseDetails;
    }

    private CaseDetails removeExistingDirectionsFromCaseDetails(CaseDetails caseDetails) {
        caseDetails.getData().remove("parentsAndRespondentsCustom");
        caseDetails.getData().remove("otherPartiesDirectionsCustom");

        return caseDetails;
    }
}
