<script lang="ts" setup>

import {dialogsStore} from "@/store";

const dlgStore = dialogsStore();

function close() {
  dlgStore.textInputValue = null;
  dlgStore.textInputDlg = false;
}

function confirm() {
  dlgStore.textInputDlg = false;
}

</script>

<template>
  <t-dialog :confirm-on-enter="true" :header="dlgStore.textInputTitle" :on-close="close"
            :on-confirm="confirm"
            :visible="dlgStore.textInputDlg"
            attach="body">
    <t-input v-model:value="dlgStore.textInputValue"></t-input>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
