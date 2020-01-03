package uk.gov.hmcts.reform.fpl;

import ccd.sdk.types.BaseCCDConfig;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;

public class CCDConfig extends BaseCCDConfig<CaseData, State, UserRole> {
    @Override
    protected void configure() {

    }
}
