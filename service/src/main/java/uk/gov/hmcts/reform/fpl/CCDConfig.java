package uk.gov.hmcts.reform.fpl;

import ccd.sdk.types.BaseCCDConfig;
import ccd.sdk.types.DisplayContext;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;

public class CCDConfig extends BaseCCDConfig<CaseData, State, UserRole> {
    @Override
    protected void configure() {

        event("uploadDocuments")
            .forAllStates()
            .explicitGrants()
            .grant("CRU", LOCAL_AUTHORITY)
            .name("Documents")
            .description("Upload documents")
            .displayOrder(14)
            .midEventWebhook()
            .fields()
                .label("uploadDocuments_paragraph_1", "You must upload these documents if possible. Give the reason and date you expect to provide it if you don’t have a document yet.")
                .optional(CaseData::getSocialWorkChronologyDocument)
                .optional(CaseData::getSocialWorkStatementDocument)
                .optional(CaseData::getSocialWorkAssessmentDocument)
                .optional(CaseData::getSocialWorkCarePlanDocument)
                .optional(CaseData::getSocialWorkEvidenceTemplateDocument)
                .optional(CaseData::getThresholdDocument)
                .optional(CaseData::getChecklistDocument)
                .field("[STATE]", DisplayContext.ReadOnly, "courtBundle = \"DO_NOT_SHOW\"")
                .field("courtBundle", DisplayContext.Optional, "[STATE] != \"Open\"", "CourtBundle", null, "8. Court bundle")
                .label("documents_socialWorkOther_border_top", "-------------------------------------------------------------------------------------------------------------")
                .optional(CaseData::getOtherSocialWorkDocuments)
                .label("documents_socialWorkOther_border_bottom", "-------------------------------------------------------------------------------------------------------------");
    }
}
