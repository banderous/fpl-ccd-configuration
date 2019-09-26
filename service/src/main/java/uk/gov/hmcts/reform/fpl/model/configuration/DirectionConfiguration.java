package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DirectionConfiguration {
    private final String title;
    private final String text;
    // full role name
    private final String assignee;

    private final Display display;

}