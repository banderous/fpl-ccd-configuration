package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.utils.MvcMakeRequestHelper.makeRequest;
import static uk.gov.hmcts.reform.fpl.enums.PostRequestMappings.ABOUT_TO_START;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantAboutToStartControllerTest {
    private static final String CONTROLLER_URI = "enter-applicant";

    @Test
    void shouldPrepopulateApplicantDataWhenNoApplicantExists() throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("data", "some data"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, CONTROLLER_URI, ABOUT_TO_START);

        assertThat(response.getData()).containsKey("applicants");
    }
}
