<script lang="ts" setup>

import {getFacesWithName, mergeFace, updateFace} from "@/api/photos";
import {photoFilterStore} from "@/store";
import {faceModule} from "@/store/modules/face-module";
import {FaceInfo} from "@/api/model/photos";
import {ref} from "vue";
import {MessagePlugin} from "tdesign-vue-next";
import {
  GitMergeIcon,
  HeartFilledIcon,
  HeartIcon,
  SearchIcon,
  UserCheckedIcon,
  UserInvisibleIcon,
  UserVisibleIcon
} from "tdesign-icons-vue-next";
import {changeFaceName} from "@/pages/dashboard/photos/components/faceFunction";

const filterStore = photoFilterStore();
const faceStore = faceModule();
filterStore.path = null;
filterStore.dateKey = null;
faceStore.changeSelectedFace();
const showNameSelectDlg = ref(false);
const names = ref([] as FaceInfo[]);
const selectedFaceId = ref(null as number);
const currentFace = ref(null as number);

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
    faceStore.changeSelectedFace();
  });
}

function revertFaceHidden(face: FaceInfo) {
  updateFace({faceId: face.faceId, hidden: !face.hidden}).then(async () => {
    faceStore.changeSelectedFace();
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

function onPageChange(pageInfo) {
  console.log(pageInfo);
  faceStore.page = pageInfo.current;
  faceStore.changeSelectedFace();
}

function changeFilterName(value) {
  console.log("filter value " + value);
  faceStore.nameFilter = value;
  faceStore.changeSelectedFace();
}

</script>

<template>
  <t-list :stripe="true" :style="getPanelStyle()" size="small">
    <template #header>
      <t-checkbox :checked="faceStore.showHidden" @change="changeShowHidden">查看隐藏人脸
      </t-checkbox>
      <t-input placeholder="输入名字过滤" @change="changeFilterName">
        <template #suffixIcon>
          <search-icon :style="{ cursor: 'pointer' }"/>
        </template>
      </t-input>
    </template>
    <t-list-item v-for="(face,index) in faceStore.faces" :key="`stripe${index}`"
                 class="small-list-item">
      <t-card :key="face.faceId" :bordered="face.faceId == filterStore.faceId"
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
    </t-list-item>
    <template #footer>
      <t-pagination
        :page-size="faceStore.size"
        :show-page-number="false"
        :show-page-size="false"
        :total="faceStore.total"
        :total-content="false"
        size="small"
        theme="simple"
        @change="onPageChange"
      />
    </template>
  </t-list>

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
.small-list-item {
  padding: 1px 1px !important;
}
</style>
