import {defineStore} from 'pinia';

export const dialogsStore = defineStore('dialogsStore', {
  state: () => {
    return {
      datePickerDlg: false,
      datePicked: null,
    }
  },
  getters: {},
  actions: {
    whenDateConfirmed(callback) {
      const unsubscribe = this.$subscribe((mutation, state) => {
        if (!state.datePickerDlg) {
          if (state.datePicked != null) {
            // console.log("Picked date is {}", state.datePicked);
            callback(state.datePicked);
          }
          unsubscribe();
        }
      });
    },
  },
  persist: false,
});
