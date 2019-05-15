package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Docmosis {

    private final String templateName;
    private final String outputName;
    private Map<String, Object> data;

    @JsonCreator
    public Docmosis(@JsonProperty("templateName") final String templateName,
                      @JsonProperty("outputName") final String outputName,
                      @JsonProperty("data") final Map<String, Object> data) {
        this.templateName = templateName;
        this.outputName = outputName;
        this.data = data;
    }

}
