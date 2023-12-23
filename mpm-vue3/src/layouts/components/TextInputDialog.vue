<script lang="ts" setup>

import {dialogsStore} from "@/store";
import {ref} from "vue";

const dlgStore = dialogsStore();
const input = ref(null);
let inputValue = null;

function close() {
  dlgStore.textInputValue = null;
  dlgStore.textInputDlg = false;
}

function confirm() {
  dlgStore.textInputValue = inputValue;
  dlgStore.textInputDlg = false;
}

function valueChanged(value, context) {
  inputValue = value;
}

</script>

<template>
  <t-dialog :confirm-on-enter="true" :header="dlgStore.textInputTitle" :on-close="close"
            :on-confirm="confirm"
            :visible="dlgStore.textInputDlg"
            attach="body">
    <t-input ref="input" @change="valueChanged"></t-input>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
