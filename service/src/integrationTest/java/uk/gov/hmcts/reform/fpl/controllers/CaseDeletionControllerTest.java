package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.utils.MvcMakeRequestHelper.makeRequest;
import static uk.gov.hmcts.reform.fpl.enums.PostRequestMappings.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseDeletionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseDeletionControllerTest {
    private static final String CONTROLLER_URI = "case-deletion";

    @Test
    void shouldRemoveAllCaseDetailsWhenCalled() throws Exception {
        AboutToStartOrSubmitCallbackResponse response = makeRequest(callbackRequest(), CONTROLLER_URI, ABOUT_TO_SUBMIT);

        assertThat(response.getData()).isEmpty();
    }
}
