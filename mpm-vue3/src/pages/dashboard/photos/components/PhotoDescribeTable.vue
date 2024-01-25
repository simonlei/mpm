<script lang="ts" setup>

import {filesize} from "filesize";
import useClipboard from "vue-clipboard3";
import {MessagePlugin} from "tdesign-vue-next";
import {ref} from "vue";
import {updateFace, updatePhoto} from "@/api/photos";
import {
  DownloadIcon,
  HappyIcon,
  LocationIcon,
  RotateIcon,
  StarFilledIcon,
  StarIcon
} from "tdesign-icons-vue-next";
import PhotoTagInput from "@/layouts/components/PhotoTagInput.vue";
import {photoFilterStore} from "@/store";

const filterStore = photoFilterStore();
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
}

function copyGisLocation() {
  const {toClipboard} = useClipboard();

  toClipboard(`${photo?.latitude},${photo?.longitude}`)
  .then(() => {
    MessagePlugin.closeAll();
    MessagePlugin.success('复制GIS位置成功');
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
  const result = await updateFace({faceId: filterStore.faceId, selectedPhotoFace: photo.id});
  console.log('result is {}', result);
  MessagePlugin.success('设置人脸成功');
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
          <t-popup content="复制当前 GIS 位置信息" trigger="hover">
            <t-button v-if="photo?.latitude!=null" shape="circle" variant="outline"
                      @click="copyGisLocation">
              <location-icon slot="icon"/>
            </t-button>
          </t-popup>
          <t-popup content="设置当前为默认人脸" trigger="hover">
            <t-button v-if="filterStore.faceId!=null" shape="circle" variant="outline"
                      @click="setDefaultFace">
              <happy-icon slot="icon"/>
            </t-button>
          </t-popup>
        </t-space>
        <photo-tag-input v-model:photo="photo"/>
      </t-space>
    </t-footer>
  </t-layout>
</template>

<style lang="less" scoped>

</style>
