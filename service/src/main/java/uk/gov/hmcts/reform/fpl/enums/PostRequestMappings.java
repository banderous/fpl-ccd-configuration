package uk.gov.hmcts.reform.fpl.enums;

public enum PostRequestMappings {
    ABOUT_TO_START("about-to-start"),
    MID__EVENT("mid-event"),
    ABOUT_TO_SUBMIT("about-to-submit"),
    SUBMITTED("submitted");

    private final String endPoint;

    PostRequestMappings(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }
}
