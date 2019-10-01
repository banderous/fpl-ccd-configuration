const config = require('../config.js');
const uploadDocs = require('../fragments/caseDocuments');
const setup = require('../fragments/caseSetup');

let caseId;

Feature('Case maintenance for Judiciary after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage) => {
  if (!caseId) {
    caseId = await setup.setupForGateKeeping(I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage);
    await I.signIn(config.judiciaryEmail, config.judiciaryPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('judiciary uploads documents', uploadDocs.uploadDocuments());
