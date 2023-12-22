<script lang="ts" setup>

import {selectModuleStore} from "@/store/modules/select-module";
import {MessagePlugin} from "tdesign-vue-next";
import {dialogsStore} from "@/store";

const selectModule = selectModuleStore();
const dlgStore = dialogsStore();

function changePhotoDate() {
  if (selectModule.lastSelectedIndex == -1) {
    MessagePlugin.warning('请选中照片后再试');
    return;
  }
  dlgStore.datePickerDlg = true;
  dlgStore.whenDateConfirmed((selectedDate: string) => {
    console.log("Picked date is {}", selectedDate);
  })

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
</template>

<style lang="less" scoped>

</style>
