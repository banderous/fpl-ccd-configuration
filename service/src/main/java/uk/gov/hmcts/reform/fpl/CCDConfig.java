package uk.gov.hmcts.reform.fpl;

import ccd.sdk.types.BaseCCDConfig;
import ccd.sdk.types.CaseData;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.UserRole;

public class CCDConfig extends BaseCCDConfig<CaseData, State, UserRole> {
    @Override
    protected void configure() {

    }
}
