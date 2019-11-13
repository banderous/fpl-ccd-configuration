const config = require('../config.js');
const response = require('../fixtures/response');
const directions = require('../fixtures/directions');

let caseId;

Feature('Comply with directions');

Before(async (I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage, draftStandardDirectionsEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();

    //hmcts login and send to gatekeeper
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
    caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
    sendCaseToGatekeeperEventPage.enterEmail();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
    I.signOut();

    //gatekeeper login, draft sdo and select issued
    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
    await draftStandardDirectionsEventPage.enterJudgeAndLegalAdvisor('Smith', 'Bob Ross');
    await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
    draftStandardDirectionsEventPage.markAsFinal();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);
    I.signOut();
  }
});

Scenario('local authority complies with directions', async (I, caseViewPage, complyWithDirectionsEventPage) => {
  await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  await I.navigateToCaseDetails(caseId);
  caseViewPage.goToNewActions(config.applicationActions.complyWithDirections);
  await complyWithDirectionsEventPage.canComplyWithDirection('localAuthorityDirections', 0, response, config.testFile);
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.applicationActions.complyWithDirections);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Compliance 1', 'Party', 'Local Authority');
  I.seeAnswerInTab(3, 'Compliance 1', 'Has this direction been complied with?', 'Yes');
  I.seeAnswerInTab(4, 'Compliance 1', 'Give details', response.complied.yes.documentDetails);
  I.seeAnswerInTab(5, 'Compliance 1', 'Upload file', 'mockFile.txt');
});