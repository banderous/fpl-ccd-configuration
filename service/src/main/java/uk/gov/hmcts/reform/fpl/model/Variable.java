package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Variable {
    private final String type;
    private final String value;
    private final Object valueInfo;

    @JsonCreator
    public Variable(@JsonProperty("type") final String type,
                    @JsonProperty("value") final String value,
                    @JsonProperty("valueInfo") final Object valueInfo) {
        this.type = type;
        this.value = value;
        this.valueInfo = valueInfo;
    }
}
