package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class DirectionResponseTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSerialiseRespondingOnBehalfOfToCorrectStringValue() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(DirectionResponse.builder()
            .respondingOnBehalfOf(new RespondingOnBehalfOf("", "blah"))
            .build());

        System.out.println("serialised = " + serialised);
    }

    @Test
    void shouldDeserialiseRespondingOnBehalfOf() throws JsonProcessingException {
        DirectionResponse deserialised =
            mapper.readValue("{\"respondingOnBehalfOf\":{\"respondingOnBehalfOfRespondent\":\"RESPONDENT\"}}" , DirectionResponse.class);

        System.out.println("deserialised = " + deserialised);
    }
}
