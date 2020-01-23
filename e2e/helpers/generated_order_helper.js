const createBlankOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.title);
  await createOrderEventPage.enterC21OrderDetails();
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.judgeAndLegalAdvisorTitleId);
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.completeEvent('Save and continue');
};

const createCareOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
};

const createSupervisionOrder = async (I, createOrderEventPage, order) => {
  await createOrderEventPage.selectType(order.type, order.subtype);
  if (order.subtype === 'Final') {
    await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.months);
    await createOrderEventPage.enterNumberOfMonths(order.months);
  }
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName, order.judgeAndLegalAdvisor.judgeTitle);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
};

const createEmergencyProtectionOrder = async (I, createOrderEventPage, order) => {
  const tomorrow = new Date(Date.now() + (3600 * 1000 * 24));

  await createOrderEventPage.selectType(order.type);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.childrenDescription.radioGroup);
  await createOrderEventPage.enterChildrenDescription(order.childrenDescription);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.type);
  createOrderEventPage.selectEpoType(order.epoType);
  createOrderEventPage.enterRemovalAddress(order.removalAddress);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.includePhrase);
  createOrderEventPage.includePhrase(order.includePhrase);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.epo.endDate.id);
  createOrderEventPage.enterEpoEndDate(tomorrow);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.judgeAndLegalAdvisorTitleId);
  await createOrderEventPage.enterJudgeAndLegalAdvisor(order.judgeAndLegalAdvisor.judgeLastName, order.judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), createOrderEventPage.fields.directionsNeeded.id);
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
};

module.exports = {
  async createOrder(I, createOrderEventPage, order) {
    switch (order.type) {
      case 'Blank order (C21)':
        await createBlankOrder(I, createOrderEventPage, order);
        break;
      case 'Care order':
        await createCareOrder(I, createOrderEventPage, order);
        break;
      case 'Supervision order':
        await createSupervisionOrder(I, createOrderEventPage, order);
        break;
      case 'Emergency protection order':
        await createEmergencyProtectionOrder(I, createOrderEventPage, order);
        break;
    }
  },

  async assertOrder(I, caseViewPage, order, orderNum) {
    const orderHeading = 'Order ' + orderNum;
    caseViewPage.selectTab(caseViewPage.tabs.orders);
    I.seeAnswerInTab(1, orderHeading, 'Type of order', order.fullType);

    if (order.type === 'Blank order (C21)') {
      I.seeAnswerInTab(2, orderHeading, 'Order title', order.title);
      I.seeAnswerInTab(4, orderHeading, 'Order document', order.document);
    } else {
      I.seeAnswerInTab(2, orderHeading, 'Order document', order.document);
    }

    I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', order.judgeAndLegalAdvisor.judgeTitle);
    I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', order.judgeAndLegalAdvisor.judgeLastName);
    I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name', order.judgeAndLegalAdvisor.legalAdvisorName);
  },
};