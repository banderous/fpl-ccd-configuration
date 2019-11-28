package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(using = DirectionResponse.CustomSerializer.class)
public class DirectionResponse {
    private final UUID directionId;
    private final DirectionAssignee assignee;

    @JsonAlias("respondingOnBehalfOf")
    @JsonUnwrapped
    private RespondingOnBehalfOf respondingOnBehalfOf;

    private final String complied;
    private final String documentDetails;
    private final DocumentReference file;
    private final String cannotComplyReason;
    private final List<String> c2Uploaded;
    private final DocumentReference cannotComplyFile;

    public static class CustomSerializer extends JsonSerializer<DirectionResponse> {
        public void serialize(DirectionResponse value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();

            if (isNotEmpty(value.getRespondingOnBehalfOf().getRespondingOnBehalfOfOther())) {
                jgen.writeStringField("respondingOnBehalfOf", value.getRespondingOnBehalfOf().getRespondingOnBehalfOfOther());
            }

            if (isNotEmpty(value.getRespondingOnBehalfOf().getRespondingOnBehalfOfRespondent())) {
                jgen.writeStringField("respondingOnBehalfOf", value.getRespondingOnBehalfOf().getRespondingOnBehalfOfRespondent());
            }

            jgen.writeEndObject();
        }
    }
}
