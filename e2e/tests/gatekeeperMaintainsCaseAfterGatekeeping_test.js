const config = require('../config.js');
const uploadDocs = require('../fragments/caseDocuments');
const setup = require('../fragments/caseSetup');

let caseId;

Feature('Case maintenance for Gatekeeper after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage) => {
  if (!caseId) {
    caseId = await setup.setupForGateKeeping(I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage);
    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('gatekeeper uploads documents', uploadDocs.uploadDocuments());
