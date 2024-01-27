<script lang="ts" setup>

import {getFaces, updateFace} from "@/api/photos";
import {MoreIcon} from "tdesign-icons-vue-next";
import {dialogsStore, photoFilterStore} from "@/store";
import {faceModule} from "@/store/modules/face-module";
import {FaceInfo} from "@/api/model/photos";

const filterStore = photoFilterStore();
const faceStore = faceModule();
const dlgStore = dialogsStore();
filterStore.path = null;
filterStore.dateKey = null;
faceStore.faces = await getFaces();

// console.log("faces is", faces);

function getOptions(face) {
  return [{
    content: '修改名字',
    value: 1,
    face: face,
  }, {
    content: '合并人脸至...',
    value: 2,
    face: face,
  }, {
    content: '删除人脸',
    value: 3,
    face: face,
  },];
}

const clickHandler = (data) => {
  if (data.value == 1) {
    dlgStore.textInputTitle = '请输入对应的人名';
    dlgStore.textInputValue = data.face.name;
    dlgStore.textInputDlg = true;
    dlgStore.whenInputConfirmed((inputValue: string) => {
      data.face.name = inputValue;
      updateFace({faceId: data.face.faceId, name: inputValue});
    });
  } else if (data.value == 2) {

  }
  // MessagePlugin.success(`选中【${data.content}】`);
};

function getPanelStyle() {
  return {height: `${window.innerHeight - 100}px`, overflow: "auto"};
}

function changeFace(face: FaceInfo) {
  filterStore.change({dateKey: null, path: null, faceId: face.faceId});
}

</script>

<template>
  <div :style="getPanelStyle()" class="narrow-scrollbar">
    <t-card v-for="face in faceStore.faces" :key="face.faceId"
            :bordered="face.faceId == filterStore.faceId"
            :content="(face.name == null ? '未命名': face.name) + '('+face.count+')'"
            :shadow="true" title="">
      <template #avatar>
        <t-avatar
          :image="'/get_face_img/'+face.faceId + '/' + (face.selectedFace==null?-1:face.selectedFace)"
          shape="round" size="64px"
          @click="changeFace(face)"></t-avatar>
      </template>
      <template #actions>
        <t-dropdown :min-column-width="112" :options="getOptions(face)" @click="clickHandler">
          <div class="tdesign-demo-dropdown-trigger">
            <t-button shape="square" variant="text">
              <more-icon/>
            </t-button>
          </div>
        </t-dropdown>
      </template>
    </t-card>
  </div>
</template>

<style lang="less" scoped>

</style>
