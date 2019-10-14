package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingBooking {
    private final String type;
    private final String typeDetails;
    private final String venue;
    private final LocalDate date;
    private final String preHearingAttendance;
    private final String time;
    private final List<String> hearingNeedsBooked;
    private final String hearingNeedsDetails;
    private final String judgeTitle;
    private final String judgeName;
    private final DynamicList venueList;
}
