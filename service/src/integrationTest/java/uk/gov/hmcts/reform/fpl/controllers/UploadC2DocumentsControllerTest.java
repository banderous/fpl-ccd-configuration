package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsControllerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Test
    void shouldCreateC2DocumentBundleWithValidCaseDataAndNotifiyNonHmctsParty() throws Exception {

    }

    @Test
    void shouldCreateC2DocumentBundleWithValidCaseData() throws Exception {
        given(userDetailsService.getUserName(AUTH_TOKEN))
            .willReturn("Emma Taylor");

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(callbackRequest().getCaseDetails())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/upload-c2/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getTemporaryC2Document()).isNull();
        assertThat(caseData.getC2DocumentBundle()).hasSize(2);

        C2DocumentBundle existingC2Document = caseData.getC2DocumentBundle().get(0).getValue();
        C2DocumentBundle appendedC2Document = caseData.getC2DocumentBundle().get(1).getValue();

        assertC2BundleDocument(existingC2Document, "C2 document one");
        assertC2BundleDocument(appendedC2Document, "C2 document two");

        assertThat(appendedC2Document.getAuthor()).isEqualTo("Emma Taylor");
        assertThat(appendedC2Document.getUploadedDateTime()).isEqualTo(dateFormatterService
            .formatLocalDateTimeBaseUsingFormat(LocalDateTime.now(), "h:mma, d MMMM yyyy"));
    }

    private void assertC2BundleDocument(C2DocumentBundle documentBundle, String description) throws Exception {
        Document document = document();

        assertThat(documentBundle.getDocument().getUrl()).isEqualTo(document.links.self.href);
        assertThat(documentBundle.getDocument().getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(documentBundle.getDocument().getBinaryUrl()).isEqualTo(document.links.binary.href);
        assertThat(documentBundle.getDescription()).isEqualTo(description);
    }
}
