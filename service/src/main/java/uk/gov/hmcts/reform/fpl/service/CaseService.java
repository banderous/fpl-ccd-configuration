package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseUserApi caseUserApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final RequestData requestData;

    @Autowired
    public CaseService(CaseUserApi caseUserApi, AuthTokenGenerator authTokenGenerator, RequestData requestData) {
        this.caseUserApi = caseUserApi;
        this.authTokenGenerator = authTokenGenerator;
        this.requestData = requestData;
    }

    public void addUser(String caseId, String userId, Set<CaseRole> caseRoles) {
        Set<String> formattedCaseRoles = caseRoles.stream().map(CaseRole::formattedName).collect(Collectors.toSet());
        CaseUser caseUser = new CaseUser(userId, formattedCaseRoles);
        caseUserApi.updateCaseRolesForUser(
            requestData.authorisation(),
            authTokenGenerator.generate(),
            caseId,
            userId,
            caseUser);
    }
}
