<script lang="ts" setup>

import {dialogsStore} from "@/store";

const dlgStore = dialogsStore();

function close() {
  dlgStore.textAreaValue = null;
  dlgStore.textAreaDlg = false;
}

function confirm() {
  dlgStore.textAreaDlg = false;
}

</script>

<template>
  <t-dialog :confirm-on-enter="true" :header="dlgStore.textAreaTitle" :on-close="close"
            :on-confirm="confirm"
            :visible="dlgStore.textAreaDlg"
            attach="body">
    <t-textarea v-model:value="dlgStore.textAreaValue"></t-textarea>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
