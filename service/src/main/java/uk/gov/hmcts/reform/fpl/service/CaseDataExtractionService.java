package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CaseDataExtractionService {

    private DateFormatterService dateFormatterService;
    private HearingBookingService hearingBookingService;
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Value("classpath:assets/draftbackground.png")
    private Resource resourceFile;

    @Autowired
    public CaseDataExtractionService(DateFormatterService dateFormatterService,
                                     HearingBookingService hearingBookingService,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration) {
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

    public Map<String, String> getNoticeOfProceedingTemplateData(CaseData caseData) {
        HearingBooking hearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);

        // Validation within our frontend ensures that the following data is present
        Map<String, String> noticeProceedingData = new java.util.HashMap<>(Map.of(
            "courtName", hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName(),
            "familyManCaseNumber", caseData.getFamilyManCaseNumber(),
            "todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG),
            "applicantName", getFirstApplicantName(caseData),
            "orderTypes", getOrderTypes(caseData),
            "childrenNames", getAllChildrenNames(caseData),
            "hearingDate", dateFormatterService.formatLocalDateToString(hearingBooking.getDate(), FormatStyle.LONG),
            "hearingVenue", hearingBooking.getVenue(),
            "preHearingAttendance", hearingBooking.getPreHearingAttendance(),
            "hearingTime", hearingBooking.getTime()
        ));

        byte[] fileContent = new byte[0];
        try {
            fileContent = FileUtils.readFileToByteArray(resourceFile.getFile());
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

//            noticeProceedingData.put("draftBackground", String.format("image:base64:%1$s", encodedString));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return noticeProceedingData;
    }

    private String getOrderTypes(CaseData caseData) {
        return caseData.getOrders().getOrderType().stream()
            .map(OrderType::getLabel)
            .collect(Collectors.joining(", "));
    }

    private String getFirstApplicantName(CaseData caseData) {
        return caseData.getAllApplicants().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Applicant::getParty)
            .filter(Objects::nonNull)
            .map(ApplicantParty::getOrganisationName)
            .findFirst()
            .orElse("");
    }

    private String getAllChildrenNames(CaseData caseData) {
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(childParty -> (childParty.getFirstName()) + " " + (childParty.getLastName()))
                .collect(Collectors.joining(", "));
    }
}
