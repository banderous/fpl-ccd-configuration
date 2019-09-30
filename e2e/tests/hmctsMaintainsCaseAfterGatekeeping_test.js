const config = require('../config.js');
const uploadDocs = require('../fragments/caseDocuments');
const setup = require('../fragments/caseSetup');
const hearingDetails = require('../fragments/hearingDetails');
const standardDirections = require('../fragments/standardDirections');

let caseId;

Feature('Case maintenance for HMCTS after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage) => {
  if (!caseId) {
    caseId = await setup.setupForGateKeeping(I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage);
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('hmcts uploads documents', uploadDocs.uploadDocuments());

Scenario('HMCTS admin enters hearing details and submits', hearingDetails.addHearingDetails());

Scenario('HMCTS admin uploads standard directions with other documents', standardDirections.addStandardDirections());
