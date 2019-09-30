package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String GATEKEEPER_EMAIL = "FamilyPublicLaw+gatekeeper@gmail.com";

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldReturnPopulatedDirectionsByRoleInAboutToSubmit() throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/notify-gatekeeper/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(callbackRequest())))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getAllParties()).isNotEmpty();
        assertThat(caseData.getLocalAuthorityDirections()).isNotEmpty();
        assertThat(caseData.getParentsAndRespondentsDirections()).isNotEmpty();
        assertThat(caseData.getCafcassDirections()).isNotEmpty();
        assertThat(caseData.getOtherPartiesDirections()).isNotEmpty();
        assertThat(caseData.getCourtDirections()).isNotEmpty();
    }

    @Test
    void shouldBuildGatekeeperNotificationTemplateWithCompleteValues() throws Exception {
        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");
        Map<String, Object> expectedGatekeeperParameters = ImmutableMap.<String, Object>builder()
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "Same day")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        mockMvc
            .perform(post("/callback/notify-gatekeeper/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("core-case-data-store-api/callback-request.json")))
            .andExpect(status().isOk());

        verify(notificationClient, times(1)).sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, GATEKEEPER_EMAIL,
            expectedGatekeeperParameters, "12345");
    }
}
