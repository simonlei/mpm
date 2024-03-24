<script lang="ts" setup>

import {filesize} from "filesize";
import useClipboard from "vue-clipboard3";
import {MessagePlugin} from "tdesign-vue-next";
import {reactive, ref} from "vue";
import {getFacesForPhoto, rescanFace, updateFace, updatePhoto} from "@/api/photos";
import {
  DownloadIcon,
  FaceRetouchingIcon,
  HappyIcon,
  LocationIcon,
  RotateIcon,
  StarFilledIcon,
  StarIcon,
  UserSearchIcon
} from "tdesign-icons-vue-next";
import PhotoTagInput from "@/layouts/components/PhotoTagInput.vue";
import {detailViewModuleStore, photoFilterStore} from "@/store";
import {faceModule} from "@/store/modules/face-module";
import {gisDateClipboardStore} from "@/store/modules/gis-date-clipboard";
import ActivityEditDialog from "@/pages/activity/ActivityEditDialog.vue";
import {ActivityModel} from "@/api/model/activityModel";

const filterStore = photoFilterStore();
const faceStore = faceModule();
const detailViewStore = detailViewModuleStore();
const gisDateClipboard = gisDateClipboardStore();
const props = defineProps({photo: null});
defineEmits(['update:photo']);

const columns = [{colKey: "k", width: "60px"}, {colKey: "v", width: "200px"}];
let photo = props.photo;

const data = ref([]);

if (photo != null) {
  data.value.push({k: '大小', v: filesize(photo.size)});
  data.value.push({k: '宽度', v: photo.width});
  data.value.push({k: '高度', v: photo.height});
  data.value.push({k: '描述', v: photo.description});
  data.value.push({k: '时间', v: photo.takendate});

  if (photo.tags != null) {
    data.value.push({k: '标签', v: photo.tags});
  }
  if (photo.address != null) {
    data.value.push({k: '地址', v: photo.address});
  }
  if (photo.activitydesc != null) {
    data.value.push({k: '所属活动', v: photo.activitydesc});
  }
}

function copyGisLocation() {
  const {toClipboard} = useClipboard();
  gisDateClipboard.latitude = photo.latitude;
  gisDateClipboard.longitude = photo.longitude;
  gisDateClipboard.takendate = photo.takendate;

  toClipboard(`${photo?.latitude},${photo?.longitude}`)
  .then(() => {
    MessagePlugin.closeAll();
    MessagePlugin.success('复制GIS及日期成功');
  })
}

async function rotatePhoto() {
  let rotate = (photo.rotate + 90) % 360;
  const result = await updatePhoto(photo, {'rotate': rotate});
  console.log('result is {}', result);
  Object.assign(photo, result);
}

async function starPhoto() {
  const result = await updatePhoto(photo, {'star': !photo.star});
  console.log('result is {}', result);
  Object.assign(photo, result);
}

async function setDefaultFace() {
  const result = await updateFace({faceId: filterStore.faceId, selectedFace: photo.id});
  console.log('result is {}', result);
  const faceStore = faceModule();
  await faceStore.changeSelectedFace();
  MessagePlugin.success('设置人脸成功');
}

function changeCircleFace() {
  faceStore.circleFace = !faceStore.circleFace;
}

function rescanFaceInPhoto() {
  rescanFace(photo.id).then(async () => {
    console.log("rescan face done.");
    faceStore.photoFaces = await getFacesForPhoto(photo.id);
    MessagePlugin.success('重新扫描人脸成功');
  });
}

const activity = reactive({} as ActivityModel);

function createActivity() {
  activity.startDate = photo.takendate;
  activity.endDate = photo.takendate;
  activity.latitude = photo.latitude;
  activity.longitude = photo.longitude;
  activity.fromPhoto = photo.id;
}

</script>

<template>
  <t-layout>
    <t-content>
      <t-base-table :key="photo?.id"
                    :columns="columns"
                    :data="data"
                    :show-header=false
                    bordered
                    row-key="index"
                    stripe
      ></t-base-table>
    </t-content>
    <t-footer>
      <t-space align="center" direction="vertical">
        <t-space>
          <t-button shape="circle" variant="outline" @click="starPhoto">
            <template #icon>
              <star-filled-icon v-if="photo?.star"/>
              <star-icon v-else/>
            </template>
          </t-button>

          <t-button shape="circle" variant="outline" @click="rotatePhoto">
            <rotate-icon slot="icon"/>
          </t-button>
          <t-button :href="'/cos/origin/'+photo?.name" shape="circle" target="_blank"
                    variant="outline">
            <download-icon slot="icon"/>
          </t-button>
          <t-popup content="以当前照片创建活动" trigger="hover">
            <t-button :disabled="photo?.activity != null" shape="circle" variant="outline"
                      @click="createActivity">
              <location-icon slot="icon"/>
            </t-button>
          </t-popup>
          <t-popup v-if="filterStore.faceId!=null" content="设置当前为默认人脸" trigger="hover">
            <t-button shape="circle" variant="outline"
                      @click="setDefaultFace">
              <happy-icon slot="icon"/>
            </t-button>
          </t-popup>
        </t-space>
        <t-space>
          <t-popup v-if="detailViewStore.detailVisible" content="圈人" trigger="hover">
            <t-button shape="circle" variant="outline"
                      @click="changeCircleFace">
              <face-retouching-icon slot="icon"/>
            </t-button>
          </t-popup>
          <t-popup v-if="detailViewStore.detailVisible" content="重新识别人脸" trigger="hover">
            <t-button shape="circle" variant="outline"
                      @click="rescanFaceInPhoto">
              <user-search-icon slot="icon"/>
            </t-button>
          </t-popup>
        </t-space>
        <photo-tag-input v-model:photo="photo"/>
      </t-space>
    </t-footer>
  </t-layout>
  <activity-edit-dialog ref="edit-dlg" v-model:activity="activity"/>
</template>

<style lang="less" scoped>

</style>
