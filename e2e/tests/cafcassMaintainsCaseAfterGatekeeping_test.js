const config = require('../config.js');
const uploadDocs = require('../fragments/caseDocuments');
const setup = require('../fragments/caseSetup');

let caseId;

Feature('Case maintenance for CAFCASS after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage) => {
  if (!caseId) {
    caseId = await setup.setupForGateKeeping(I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage);
    await I.signIn(config.cafcassEmail, config.cafcassPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('cafcass uploads documents', uploadDocs.uploadDocuments());
