package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.controllers.utils.MvcMakeRequestHelper.makeRequest;
import static uk.gov.hmcts.reform.fpl.enums.PostRequestMappings.ABOUT_TO_START;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToStartTest {
    private static final String CONTROLLER_URI = "case-submission";
    private static final String AUTH_TOKEN = "Bearer token";

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void mockUserNameRetrieval() {
        given(userDetailsService.getUserName(AUTH_TOKEN)).willReturn("Emma Taylor");
    }

    @Test
    void shouldAddConsentLabelToCaseDetails() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("caseName", "title"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(request, CONTROLLER_URI, ABOUT_TO_START);

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("submissionConsentLabel",
                "I, Emma Taylor, believe that the facts stated in this application are true.");
    }

    @Nested
    class LocalAuthorityValidation {
        @Test
        void shouldReturnErrorWhenCaseBelongsToSmokeTestLocalAuthority() throws Exception {
            AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
                prepareCaseBelongingTo("FPLA"), CONTROLLER_URI, ABOUT_TO_START);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", "FPLA");
            assertThat(callbackResponse.getErrors()).contains("Test local authority cannot submit cases");
        }

        @Test
        void shouldReturnNoErrorWhenCaseBelongsToRegularLocalAuthority() throws Exception {
            AboutToStartOrSubmitCallbackResponse callbackResponse = makeRequest(
                prepareCaseBelongingTo("SA"), CONTROLLER_URI, ABOUT_TO_START);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", "SA");
            assertThat(callbackResponse.getErrors()).isEmpty();
        }

        private CallbackRequest prepareCaseBelongingTo(String localAuthority) {
            return CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .data(ImmutableMap.of("caseLocalAuthority", localAuthority))
                    .build())
                .build();
        }
    }
}
