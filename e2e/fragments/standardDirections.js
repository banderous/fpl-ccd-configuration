const config = require('../config');

module.exports = {
  addStandardDirections() {
    return async (I, caseViewPage, uploadStandardDirectionsDocumentEventPage) => {
      await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
      uploadStandardDirectionsDocumentEventPage.uploadStandardDirections(config.testFile);
      uploadStandardDirectionsDocumentEventPage.uploadAdditionalDocuments(config.testFile);
      await I.completeEvent('Save and continue');
      I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
      caseViewPage.selectTab(caseViewPage.tabs.documents);
      I.see('mockFile.txt');
      I.seeAnswerInTab('1', 'Other documents 1', 'Document name', 'Document 1');
      I.seeAnswerInTab('2', 'Other documents 1', 'Upload a file', 'mockFile.txt');
      I.seeAnswerInTab('1', 'Other documents 2', 'Document name', 'Document 2');
      I.seeAnswerInTab('2', 'Other documents 2', 'Upload a file', 'mockFile.txt');
    };
  },
};
