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
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.controllers.utils.MvcMakeRequestHelper.makeRequest;
import static uk.gov.hmcts.reform.fpl.enums.PostRequestMappings.ABOUT_TO_SUBMIT;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerTest {
    private static final String CONTROLLER_URI = "case-initiation";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "10";
    private static final String CASE_ID = "1";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private CaseAccessApi caseAccessApi;

    @Test
    void shouldAddCaseLocalAuthorityToCaseData() throws Exception {
        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willReturn(
            new UserDetails(null, "user@example.gov.uk", null, null, null));

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("caseName", "title")
                .build()).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = makeRequest(request, CONTROLLER_URI, ABOUT_TO_SUBMIT);

        assertThat(response.getData())
            .containsEntry("caseName", "title")
            .containsEntry("caseLocalAuthority", "example");
    }

    @Test
    void shouldPopulateErrorsInResponseWhenDomainNameIsNotFound() throws Exception {
        AboutToStartOrSubmitCallbackResponse expectedResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .add("The email address was not linked to a known Local Authority")
                .build())
            .build();

        given(idamApi.retrieveUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails(null, "user@email.gov.uk", null, null, null));

        AboutToStartOrSubmitCallbackResponse response = makeRequest(
            CallbackRequest.builder().build(), CONTROLLER_URI, ABOUT_TO_SUBMIT);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void grantAccessShouldBeCalledOnceForEachOfThreeUsers() throws Exception {
        given(serviceAuthorisationApi.serviceToken(anyMap()))
            .willReturn(SERVICE_AUTH_TOKEN);

        assertThatCaseAccessApiIsCalledTimes(3);
    }

    @Test
    void shouldContinueAddingUsersAfterGrantAccessFailure() throws Exception {
        given(serviceAuthorisationApi.serviceToken(anyMap()))
            .willReturn(SERVICE_AUTH_TOKEN);

        doThrow(RuntimeException.class).when(caseAccessApi).grantAccessToCase(
            any(), any(), any(), any(), any(), any(), any()
        );

        assertThatCaseAccessApiIsCalledTimes(3);
    }

    private void assertThatCaseAccessApiIsCalledTimes(int timesCalled) throws Exception {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(Long.valueOf(CASE_ID))
                .data(ImmutableMap.of("caseLocalAuthority", "example"))
                .build())
            .build();

        mockMvc
            .perform(post("/callback/case-initiation/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        Thread.sleep(3000);

        verify(caseAccessApi, times(timesCalled)).grantAccessToCase(
            eq(AUTH_TOKEN), any(), eq(USER_ID), eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID), any()
        );
    }
}
