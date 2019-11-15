const { I } = inject();
const draftDirections = require('../../fragments/draftDirections');

module.exports = {
  fields: {
    cmoHearingDateList: '#cmoHearingDateList',
  },

  associateHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.selectOption(this.fields.cmoHearingDateList, date);
  },

  validatePreviousSelectedHearingDate(date) {
    I.waitForElement(this.fields.cmoHearingDateList);
    I.see(date, this.fields.cmoHearingDateList);
  },

  async enterDirection(direction) {
    await I.addAnotherElementToCollection();
    await draftDirections.enterTitleAndDescription('allParties', direction);
    await draftDirections.enterDate('allParties', direction);
  },
};
