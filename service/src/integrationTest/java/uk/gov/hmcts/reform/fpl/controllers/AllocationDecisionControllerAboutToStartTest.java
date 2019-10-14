package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.AllocationDecision;
import uk.gov.hmcts.reform.fpl.model.AllocationProposal;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.utils.MvcMakeRequestHelper.makeRequest;
import static uk.gov.hmcts.reform.fpl.enums.PostRequestMappings.ABOUT_TO_START;
import static uk.gov.hmcts.reform.fpl.enums.PostRequestMappings.ABOUT_TO_SUBMIT;

@ActiveProfiles("integration-test")
@WebMvcTest(AllocationDecisionController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocationDecisionControllerAboutToStartTest {
    private static final String CONTROLLER_URI = "allocation-decision";

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldAddYesToMissingAllocationDecision() throws Exception {
        AllocationDecision currentAllocationDecision = AllocationDecision.builder()
            .proposal("test")
            .proposalReason("decision reason")
            .build();
        AllocationProposal allocationProposal = AllocationProposal.builder()
            .proposal("proposal")
            .proposalReason("reason")
            .build();

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("allocationProposal", allocationProposal)
                .put("allocationDecision", currentAllocationDecision)
                .build()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, CONTROLLER_URI, ABOUT_TO_START);

        AllocationDecision expectedDecision = AllocationDecision.builder()
            .proposal("test")
            .proposalReason("decision reason")
            .allocationProposalPresent("Yes")
            .build();

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);
        AllocationDecision actualAllocationDecision = caseData.getAllocationDecision();
        assertThat(actualAllocationDecision)
            .isEqualTo(expectedDecision);
    }

    @Test
    void shouldAddNoToMissingAllocationDecision() throws Exception {
        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .build()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, CONTROLLER_URI, ABOUT_TO_START);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(caseData.getAllocationDecision().getAllocationProposalPresent())
            .isEqualTo("No");
    }
}
