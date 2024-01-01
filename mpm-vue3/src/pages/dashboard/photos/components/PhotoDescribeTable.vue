<script lang="ts" setup>

import {filesize} from "filesize";
import useClipboard from "vue-clipboard3";
import {MessagePlugin} from "tdesign-vue-next";
import {ref} from "vue";
import {updatePhoto} from "@/api/photos";

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

</script>

<template>
  <t-base-table :key="photo?.id"
                :columns="columns"
                :data="data"
                :show-header=false
                bordered
                row-key="index"
                stripe
                table-content-width="260px"
  ></t-base-table>
  <t-button v-if="photo?.latitude!=null" variant="outline" @click="copyGisLocation">复制GIS位置
  </t-button>
  <t-button variant="outline" @click="rotatePhoto">
    右转 90 度
  </t-button>
</template>

<style lang="less" scoped>

</style>
