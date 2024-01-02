<script lang="ts" setup>

import {dialogsStore} from "@/store";
import {ref} from "vue";

const dlgStore = dialogsStore();
const input = ref(null);
let inputValue = null;

function close() {
  dlgStore.textAreaValue = null;
  dlgStore.textAreaDlg = false;
}

function confirm() {
  dlgStore.textAreaValue = inputValue;
  dlgStore.textAreaDlg = false;
}

function valueChanged(value, context) {
  inputValue = value;
}

</script>

<template>
  <t-dialog :confirm-on-enter="true" :header="dlgStore.textAreaTitle" :on-close="close"
            :on-confirm="confirm"
            :visible="dlgStore.textAreaDlg"
            attach="body">
    <t-textarea ref="input" @change="valueChanged"></t-textarea>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
