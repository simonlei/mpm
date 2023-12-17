<script lang="ts" setup>

import {selectModuleStore} from "@/store/modules/select-module";
import {MessagePlugin} from "tdesign-vue-next";
import {ref} from "vue";

const selectModule = selectModuleStore();
const dateSelectDialog = ref(null);
let dateSelectDialogVisible = ref(false);

function changePhotoDate() {
  if (selectModule.lastSelectedIndex == -1) {
    MessagePlugin.warning('请选中照片后再试');
    return;
  }
  //dateSelectDialog.show();
  dateSelectDialogVisible.value = true;
  console.log("dialog " + dateSelectDialog);
}

</script>

<template>
  <t-dropdown :max-column-width="250" :show-arror="true" placement="right" trigger="context-menu">
    <slot></slot>
    <t-dropdown-menu>
      <t-dropdown-item content="修改选中照片的时间" @click="changePhotoDate"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的描述信息"></t-dropdown-item>
      <t-dropdown-item content="拷贝选中照片的GIS信息"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的GIS信息"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的标签"></t-dropdown-item>
      <!--
      <t-dropdown-item content="跳转到文件夹"></t-dropdown-item>
      -->
    </t-dropdown-menu>
  </t-dropdown>

  <t-dialog ref="dateSelectDialog" v-model:visible="dateSelectDialogVisible" header="请选择日期"
            height="300px"
            width="300px">
    hello...
    <t-button>xzxx</t-button>
    <t-date-picker allowInput/>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
