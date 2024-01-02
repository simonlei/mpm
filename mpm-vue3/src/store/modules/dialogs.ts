import {defineStore} from 'pinia';

export const dialogsStore = defineStore('dialogsStore', {
  state: () => {
    return {
      datePickerDlg: false,
      datePicked: null,
      textInputDlg: false,
      textInputTitle: '',
      textInputValue: null,
      textAreaDlg: false,
      textAreaTitle: '',
      textAreaValue: null,
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
    whenInputConfirmed(callback) {
      const unsubscribe = this.$subscribe((mutation, state) => {
        if (!state.textInputDlg) {
          if (state.textInputValue != null) {
            // console.log("Picked date is {}", state.textInputValue);
            callback(state.textInputValue);
          }
          unsubscribe();
        }
      });
    },
    whenTextAreaConfirmed(callback) {
      const unsubscribe = this.$subscribe((mutation, state) => {
        if (!state.textAreaDlg) {
          if (state.textAreaValue != null) {
            // console.log("Picked date is {}", state.textInputValue);
            callback(state.textAreaValue);
          }
          unsubscribe();
        }
      });
    },
  },
  persist: false,
});
