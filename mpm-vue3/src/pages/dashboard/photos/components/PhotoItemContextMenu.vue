<script lang="ts" setup>

import {selectModuleStore} from "@/store/modules/select-module";
import {MessagePlugin} from "tdesign-vue-next";
import {dialogsStore, photoModuleStore} from "@/store";

const selectModule = selectModuleStore();
const photoModule = photoModuleStore();
const dlgStore = dialogsStore();

function changePhotoDate() {
  if (selectModule.lastSelectedIndex == -1) {
    MessagePlugin.warning('请选中照片后再试');
    return;
  }

  let index = selectModule.lastSelectedIndex;
  photoModule.getPhotoById(photoModule.idList[index].id, index).then(photo => {
    dlgStore.datePicked = photo.takendate;
    dlgStore.datePickerDlg = true;
    dlgStore.whenDateConfirmed((selectedDate: string) => {
      photoModule.updateSelectedPhotos({"takenDate": selectedDate}, ` 拍摄时间到 ${selectedDate}`);
    });
  });
}

function changePhotoGis() {
  if (selectModule.lastSelectedIndex == -1) {
    MessagePlugin.warning('请选中照片后再试');
    return;
  }
  dlgStore.textInputTitle = '请输入纬度,经度，例如 22.57765,113.9504277778';
  dlgStore.textInputDlg = true;
  dlgStore.whenInputConfirmed((inputValue: string) => {
    const values = inputValue.split(',');
    if (values == null || values.length != 2) {
      MessagePlugin.error('请按照 纬度,经度 模式来输入');
    } else {
      photoModule.updateSelectedPhotos(
        {'latitude': values[0], 'longitude': values[1]}, ` GIS信息到 ${inputValue}`);
    }
    // console.log("input value is {}", inputValue);
  });
}

function changePhotoDesc() {
  if (selectModule.lastSelectedIndex == -1) {
    MessagePlugin.warning('请选中照片后再试');
    return;
  }
  dlgStore.textAreaTitle = '请输入描述信息';
  dlgStore.textAreaDlg = true;
  dlgStore.whenTextAreaConfirmed((inputValue: string) => {
    photoModule.updateSelectedPhotos({'description': inputValue}, ` 描述信息到 ${inputValue}`);
  });
}

</script>

<template>
  <t-dropdown :max-column-width="250" :show-arror="true" placement="right" trigger="context-menu">
    <slot></slot>
    <t-dropdown-menu>
      <t-dropdown-item content="修改选中照片的时间" @click="changePhotoDate"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的描述信息" @click="changePhotoDesc"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的GIS信息" @click="changePhotoGis"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的标签"></t-dropdown-item>
      <!--
      <t-dropdown-item content="跳转到文件夹"></t-dropdown-item>
      -->
    </t-dropdown-menu>
  </t-dropdown>
</template>

<style lang="less" scoped>

</style>
