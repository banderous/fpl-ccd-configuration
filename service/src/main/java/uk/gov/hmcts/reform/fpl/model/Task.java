package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Task {
    private final Variable taskTitle;
    private final Variable dueDate;
    private final Variable emailDate;
    private final Variable status;

    @JsonCreator
    public Task(@JsonProperty("taskTitle") final Variable taskTitle,
                @JsonProperty("dueDate") final Variable dueDate,
                @JsonProperty("emailDate") final Variable emailDate,
                @JsonProperty("status") final Variable status) {
        this.taskTitle = taskTitle;
        this.dueDate = dueDate;
        this.emailDate = emailDate;
        this.status = status;
    }
}
