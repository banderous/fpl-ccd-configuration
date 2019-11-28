package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;

@Data
class RespondingOnBehalfOf {
    private final String respondingOnBehalfOfRespondent;
    private final String respondingOnBehalfOfOther;
}
