package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.NoAssociatedUsersException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class LocalAuthorityUserService {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseAccessApi caseAccessApi;
    private final CaseUserApi caseUserApi;
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient client;

    @Autowired
    public LocalAuthorityUserService(CaseAccessApi caseAccessApi,
                                     LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration,
                                     AuthTokenGenerator authTokenGenerator,
                                     CaseUserApi caseUserApi,
                                     IdamClient idamClient) {
        this.caseAccessApi = caseAccessApi;
        this.localAuthorityUserLookupConfiguration = localAuthorityUserLookupConfiguration;
        this.authTokenGenerator = authTokenGenerator;
        this.caseUserApi = caseUserApi;
        this.client = idamClient;
    }

    public void grantUserAccess(String authorization, String creatorUserId, String caseId, String caseLocalAuthority) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<String> caseRoles = Set.of("[LASOLICITOR]");
        try {
            String authentication = client.authenticateUser("fpl-system-update@mailnesia.com", "Password12");
            caseUserApi.updateCaseRolesForUser(authentication, authTokenGenerator.generate(), caseId, creatorUserId,
                new CaseUser(creatorUserId, caseRoles));
            logger.info("Added case roles {} to user {}", caseRoles, creatorUserId);
        } catch (FeignException exception) {
            logger.warn(String.format("Error adding case roles %s to user %s", caseRoles, creatorUserId), exception);
        }


        findUserIds(caseLocalAuthority).stream()
            .filter(userId -> !Objects.equals(userId, creatorUserId))
            .forEach(userId -> {
                logger.debug("Granting user {} access to case {}", userId, caseId);

                try {
                    caseAccessApi.grantAccessToCase(
                        authorization,
                        authTokenGenerator.generate(),
                        creatorUserId,
                        JURISDICTION,
                        CASE_TYPE,
                        caseId,
                        new UserId(userId));

                } catch (Exception ex) {
                    logger.warn("Could not grant user {} access to case {}", userId, caseId, ex);
                }
            });
    }

    private List<String> findUserIds(String localAuthorityCode) {
        List<String> userIds = localAuthorityUserLookupConfiguration.getUserIds(localAuthorityCode);

        if (userIds.isEmpty()) {
            throw new NoAssociatedUsersException("No users found for the local authority '" + localAuthorityCode + "'");
        }

        return userIds;
    }
}
