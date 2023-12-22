<script lang="ts" setup>

import {dialogsStore} from "@/store";
import {ref} from "vue";

const dlgStore = dialogsStore();
const datePicker = ref(null);
let selectedDate = null;

function close() {
  dlgStore.datePickerDlg = false;
  dlgStore.datePicked = null;
}

function confirm() {
  dlgStore.datePickerDlg = false;
  dlgStore.datePicked = selectedDate;
}

function dateChange(value, context) {
  selectedDate = value;
}

</script>

<template>
  <t-dialog :confirm-on-enter="true" :on-close="close" :on-confirm="confirm"
            :visible="dlgStore.datePickerDlg"
            attach="body"
            header="请选择日期">
    <t-date-picker ref="datePicker" @change="dateChange"></t-date-picker>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
