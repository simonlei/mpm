<script lang="ts" setup>

import {getFaces, getFacesWithName, mergeFace, updateFace} from "@/api/photos";
import {
  GitMergeIcon,
  HeartFilledIcon,
  HeartIcon,
  UserCheckedIcon,
  UserInvisibleIcon,
  UserVisibleIcon
} from "tdesign-icons-vue-next";
import {dialogsStore, photoFilterStore} from "@/store";
import {faceModule} from "@/store/modules/face-module";
import {FaceInfo} from "@/api/model/photos";
import {ref} from "vue";
import {MessagePlugin} from "tdesign-vue-next";

const filterStore = photoFilterStore();
const faceStore = faceModule();
const dlgStore = dialogsStore();
filterStore.path = null;
filterStore.dateKey = null;
faceStore.faces = await getFaces();
const showNameSelectDlg = ref(false);
const names = ref([] as FaceInfo[]);
const selectedFaceId = ref(null as number);
const currentFace = ref(null as number);

// console.log("faces is", faces);

function changeFaceName(face: FaceInfo) {
  dlgStore.textInputTitle = '请输入对应的人名';
  dlgStore.textInputValue = face.name;
  dlgStore.textInputDlg = true;
  dlgStore.whenInputConfirmed((inputValue: string) => {
    face.name = inputValue;
    updateFace({faceId: face.faceId, name: inputValue});
  });
}

async function showMergeFaceDlg(face: FaceInfo) {
  currentFace.value = face.faceId;
  names.value = await getFacesWithName();
  showNameSelectDlg.value = true;
}

function getPanelStyle() {
  return {height: `${window.innerHeight - 100}px`, overflow: "auto"};
}

function changeFace(face: FaceInfo) {
  filterStore.change({dateKey: null, path: null, faceId: face.faceId});
}

function revertFaceCollected(face: FaceInfo) {
  updateFace({faceId: face.faceId, collected: !face.collected}).then(async () => {
    faceStore.faces = await getFaces();
  });
}

function revertFaceHidden(face: FaceInfo) {
  updateFace({faceId: face.faceId, hidden: !face.hidden}).then(async () => {
    faceStore.faces = await getFaces();
  });
}

function closeDlg() {
  showNameSelectDlg.value = false;
}

function confirmDlg() {
  showNameSelectDlg.value = false;
  // console.log('selected ', selectedFaceId.value);
  mergeFace(currentFace.value, selectedFaceId.value).then(() => {
    MessagePlugin.success(`已合并人脸数据`);
    filterStore.change({dateKey: null, path: null, faceId: selectedFaceId.value});
    faceStore.changeSelectedFace();
  });
}

function changeShowHidden() {
  faceStore.showHidden = !faceStore.showHidden;
  faceStore.changeSelectedFace();
}

</script>

<template>
  <t-layout>
    <t-header>
      <t-checkbox :checked="faceStore.showHidden" @change="changeShowHidden">查看隐藏人脸
      </t-checkbox>
    </t-header>
    <t-content>
      <div :style="getPanelStyle()" class="narrow-scrollbar">
        <t-card v-for="face in faceStore.faces" :key="face.faceId"
                :bordered="face.faceId == filterStore.faceId"
                :shadow="true"
                :subtitle="'('+face.count+')'"
                :title="(face.name == null ? '未命名': face.name)">
          <template #avatar>
            <t-avatar
              :image="'/get_face_img/'+face.faceId + '/' + (face.selectedFace==null?-1:face.selectedFace)"
              shape="round" size="64px"
              @click="changeFace(face)"></t-avatar>
          </template>
          <template #footer>
            <t-row :align="'middle'" justify="center" style="gap: 1px">
              <t-col flex="auto" style="display: inline-flex; justify-content: center">
                <t-button :style="{ 'margin': '1px' }" shape="square" variant="text"
                          @click="changeFaceName(face)">
                  <user-checked-icon/>
                </t-button>
              </t-col>
              <t-col flex="auto" style="display: inline-flex; justify-content: center">
                <t-button :style="{ 'margin': '1px' }" shape="square" variant="text"
                          @click="revertFaceCollected(face)">
                  <heart-filled-icon v-if="face.collected"/>
                  <heart-icon v-else/>
                </t-button>
              </t-col>
              <t-col flex="auto" style="display: inline-flex; justify-content: center">
                <t-popconfirm :content="'请确认' + (face.hidden ? '展示' : '隐藏') + '人脸？'"
                              @confirm="revertFaceHidden(face)">
                  <t-button :style="{ 'margin': '1px' }" shape="square" variant="text">
                    <user-invisible-icon v-if="face.hidden"/>
                    <user-visible-icon v-else/>
                  </t-button>
                </t-popconfirm>
              </t-col>
              <t-col flex="auto" style="display: inline-flex; justify-content: center">
                <t-button :style="{ 'margin': '1px' }" shape="square" variant="text"
                          @click="showMergeFaceDlg(face)">
                  <git-merge-icon/>
                </t-button>
              </t-col>
            </t-row>
          </template>
        </t-card>
      </div>
    </t-content>
  </t-layout>

  <t-dialog :confirm-on-enter="true" :on-close="closeDlg" :on-confirm="confirmDlg"
            :visible="showNameSelectDlg"
            attach="body"
            header="请选择要合并到的目标人名">
    <t-select v-model:value="selectedFaceId">
      <t-option v-for="face in names" :key="face.faceId" :label="face.name" :value="face.faceId">

      </t-option>
    </t-select>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
