package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Schedule {
    private final String includeSchedule;
    private final String allocation;
    private final String application;
    private final String todaysHearing;
    private final String childrensCurrentArrangement;
    private final String timetableForProceedings;
    private final String timetableForChildren;
    private final String alternativeCarers;
    private final String threshold;
    private final String keyIssues;
    private final String partiesPositions;
}
