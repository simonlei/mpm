<script lang="ts" setup>

import {PrintIcon} from "tdesign-icons-vue-next";
import {faceModule} from "@/store/modules/face-module";
import {getFacesForPhoto} from "@/api/photos";

const photo = defineModel('photo', {type: Object});
const theImg = defineModel('theImg', {type: Object});

const faceStore = faceModule();
faceStore.photoFaces = await getFacesForPhoto(photo.value.id);

console.log('theImg ', theImg);
if (theImg != null && theImg.value != null) {
  // let rect = theImg.value.getBoundingClientRect();
  // console.log(rect);
  console.log('the value', theImg.value.$el.getBoundingClientRect());
  // get width and height, then calc the real x,y,w,h
}
</script>

<template>
  <t-tag v-for="face in faceStore.photoFaces"
         :style="{ position: 'absolute', left: face.x + 'px', top: face.y + 'px', width: face.width+'px', height: face.height+'px', borderRadius: '3px' }"
         shape="mark"
         theme="primary"
         variant="light"
  >
    {{ face.name == null ? '未命名' : face.name }}
  </t-tag>
  <t-tag
    :style="{ position: 'absolute', right: '8px', bottom: '8px', borderRadius: '3px' }"
    shape="mark"
    theme="primary"
    variant="light"
  >
    <PrintIcon size="16"/>
    高清
  </t-tag>

</template>

<style lang="less" scoped>

</style>
