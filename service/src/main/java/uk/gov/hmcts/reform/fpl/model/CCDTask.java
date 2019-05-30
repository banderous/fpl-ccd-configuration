package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDTask {
    private final String taskTitle;
    private final String assignee;
    private final String dueDate;
    private final String emailDate;
    private final String status;

    @JsonCreator
    public CCDTask(@JsonProperty("taskTitle") final String taskTitle,
                   @JsonProperty("assignee") final String assignee,
                   @JsonProperty("dueDate") final String dueDate,
                   @JsonProperty("emailDate") final String emailDate,
                   @JsonProperty("status") final String status) {
        this.taskTitle = taskTitle;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.emailDate = emailDate;
        this.status = status;
    }
}
