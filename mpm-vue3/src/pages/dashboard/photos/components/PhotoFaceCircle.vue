<script lang="ts" setup>

import {PrintIcon} from "tdesign-icons-vue-next";
import {faceModule} from "@/store/modules/face-module";
import {getFacesForPhoto} from "@/api/photos";

const photo = defineModel('photo', {type: Object});
const theImg = defineModel('theImg', {type: Object});

const faceStore = faceModule();
faceStore.photoFaces = await getFacesForPhoto(photo.value.id);

console.log('theImg ', theImg);
let ratioX = 1;
let ratioY = 1;
let deltaX = 0;
let deltaY = 0;
if (theImg != null && theImg.value != null) {
  // let rect = theImg.value.getBoundingClientRect();
  // console.log(rect);
  // small photo scale to 2560x1440
  const width = photo.value.width;
  const height = photo.value.height;

  const smallWidth = Math.min((width / height > 2560 / 1440) ? 2560 : width / height * 1440, width);
  const smallHeight = Math.min((width / height > 2560 / 1440) ? height / width * 2560 : 1440, height);
  const rect = theImg.value.$el.getBoundingClientRect();
  const realWidth = Math.min((smallWidth / smallHeight > rect.width / rect.height) ? rect.width : smallWidth / smallHeight * rect.height, rect.width);
  const realHeight = Math.min((smallWidth / smallHeight > rect.width / rect.height) ? smallHeight / smallWidth * rect.width : rect.height, rect.height);
  ratioX = Math.min(1, realWidth / smallWidth);
  ratioY = Math.min(1, realHeight / smallHeight);
  deltaX = (rect.width - realWidth) / 2;
  deltaY = (rect.height - realHeight) / 2;
  console.log('the value', width, height, smallWidth, smallHeight, realWidth, realHeight, ratioX, ratioY, deltaX, deltaY);
  // get width and height, then calc the real x,y,w,h
}
</script>

<template>
  <div v-if="faceStore.photoFaces.length>0 && faceStore.circleFace">
    <t-tag v-for="face in faceStore.photoFaces"
           :style="{ position: 'absolute', left: (deltaX+face.x*ratioX) + 'px', top: (deltaY+ face.y*ratioY) + 'px', width: face.width*ratioX+'px', height: face.height*ratioY+'px', borderRadius: '3px' }"
           shape="mark"
           theme="primary"
           variant="outline"
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
  </div>
</template>

<style lang="less" scoped>

</style>
