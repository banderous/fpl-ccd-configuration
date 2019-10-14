package uk.gov.hmcts.reform.fpl.controllers.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.enums.PostRequestMappings;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class MvcMakeRequestHelper {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    private static ObjectMapper mapper;
    private static MockMvc mockMvc;

    @Autowired
    private void setUp(ObjectMapper mapper, MockMvc mockMvc) {
        MvcMakeRequestHelper.mapper = mapper;
        MvcMakeRequestHelper.mockMvc = mockMvc;
    }

    public static AboutToStartOrSubmitCallbackResponse makeRequest(CallbackRequest request,
                                                                   String controllerUri,
                                                                   PostRequestMappings endPoint) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/" + controllerUri + "/" + endPoint.getEndPoint())
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);
    }
}

