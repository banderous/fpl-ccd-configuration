package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class StartInstanceResponse {
    private final ArrayList links;
    private final String id;
    private final String definitionId;
    private final String businessKey;
    private final String ended;
    private final String suspended;
    private final String tenantId;
    private final Task variables;

    @JsonCreator
    public StartInstanceResponse(@JsonProperty("links") final ArrayList links,
                                 @JsonProperty("id") final String id,
                                 @JsonProperty("definitionId") final String definitionId,
                                 @JsonProperty("businessKey") final String businessKey,
                                 @JsonProperty("ended") final String ended,
                                 @JsonProperty("suspended") final String suspended,
                                 @JsonProperty("tenantId") final String tenantId,
                                 @JsonProperty("variables") final Task variables) {
        this.links = links;
        this.id = id;
        this.definitionId = definitionId;
        this.businessKey = businessKey;
        this.ended = ended;
        this.suspended = suspended;
        this.tenantId = tenantId;
        this.variables = variables;
    }
}
