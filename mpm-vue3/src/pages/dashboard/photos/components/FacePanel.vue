<script lang="ts" setup>

import {getFaces, updateFace} from "@/api/photos";
import {MoreIcon} from "tdesign-icons-vue-next";
import {dialogsStore, photoFilterStore} from "@/store";
import {onActivated, ref} from "vue";

const faces = ref(await getFaces());
const filterStore = photoFilterStore();
const dlgStore = dialogsStore();

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
      updateFace(data.face);
    });
  } else if (data.value == 2) {

  }
  // MessagePlugin.success(`选中【${data.content}】`);
};

function getPanelStyle() {
  return {height: `${window.innerHeight - 100}px`, overflow: "auto"};
}

onActivated(() => {
  console.log('hello......face active');
  filterStore.path = null;
  filterStore.dateKey = null;
});
</script>

<template>
  <div :style="getPanelStyle()" class="narrow-scrollbar">
    <t-card v-for="face in faces" :key="face.faceId" :bordered="true"
            :content="(face.name == null ? '未命名': face.name) + '('+face.count+')'"
            :shadow="true" title="">
      <template #avatar>
        <t-avatar :image="'/get_face_img/'+face.faceId" shape="round" size="64px"
                  @click="filterStore.change({faceId:face.faceId})"></t-avatar>
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
