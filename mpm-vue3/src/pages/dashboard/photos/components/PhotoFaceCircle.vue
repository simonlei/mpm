<script lang="ts" setup>

import {faceModule} from "@/store/modules/face-module";
import {getFacesForPhoto, removePhotoFaceInfo} from "@/api/photos";
import {DialogPlugin} from "tdesign-vue-next";
import {dialogsStore} from "@/store";
import {changeFaceName} from "@/pages/dashboard/photos/components/FacePanel.vue";

const photo = defineModel('photo', {type: Object});
const theImg = defineModel('theImg', {type: Object});

const faceStore = faceModule();
const dlgStore = dialogsStore();
faceStore.photoFaces = await getFacesForPhoto(photo.value.id);

console.log('theImg ', theImg);
let ratioX = 1;
let ratioY = 1;
let deltaX = 0;
let deltaY = 0;

function getRatioAndDelta() {
  if (theImg != null && theImg.value != null && theImg.value.$el != null) {
    // let rect = theImg.value.getBoundingClientRect();
    // console.log(rect);
    // small photo scale to 2560x1440
    const width = photo.value.width;
    const height = photo.value.height;

    const smallWidth = (width / height > 2560 / 1440) ? 2560 : width / height * 1440;
    const smallHeight = (width / height > 2560 / 1440) ? height / width * 2560 : 1440;
    const rect = theImg.value.$el.getBoundingClientRect();
    console.log("rect is ", rect);
    const realWidth = Math.min((smallWidth / smallHeight > rect.width / rect.height) ? rect.width : smallWidth / smallHeight * rect.height, rect.width);
    const realHeight = Math.min((smallWidth / smallHeight > rect.width / rect.height) ? smallHeight / smallWidth * rect.width : rect.height, rect.height);
    ratioX = Math.min(1, realWidth / smallWidth);
    ratioY = Math.min(1, realHeight / smallHeight);
    deltaX = (rect.width - realWidth) / 2;
    deltaY = (rect.height - realHeight) / 2;
    console.log('the value', width, height, smallWidth, smallHeight, realWidth, realHeight, ratioX, ratioY, deltaX, deltaY);
    // get width and height, then calc the real x,y,w,h
  }
}

function calcFaceStyle(face) {
  getRatioAndDelta();
  console.log("Face is ", face);
  return {
    position: 'absolute',
    left: (deltaX + face.x * ratioX) + 'px',
    top: (deltaY + face.y * ratioY) + 'px',
    width: face.width * ratioX + 'px',
    height: face.height * ratioY + 'px',
    borderRadius: '3px'
  };
}

function handleClose(index, face) {
  const confirmDia = DialogPlugin({
    header: '删除这个人脸？',
    body: '确定删除当前人脸？',
    confirmBtn: '确认',
    cancelBtn: '取消',
    onConfirm: ({e}) => {
      console.log('confirm delete ', index, face);
      faceStore.photoFaces.splice(index, 1);
      removePhotoFaceInfo(face.id);
      confirmDia.hide();
    },
    onClose: ({e, trigger}) => {
      confirmDia.hide();
    },
  });
}

</script>

<template>
  <div v-if="faceStore.photoFaces.length>0 && faceStore.circleFace">
    <t-tag v-for="(face,index) in faceStore.photoFaces"
           :closable="true"
           :style="calcFaceStyle(face)"
           shape="mark"
           theme="primary"
           variant="outline"
           @click="changeFaceName(face)"
           @close="handleClose(index, face)"
    >
      {{ face.name == null ? '未命名' : face.name }}
    </t-tag>
  </div>
</template>

<style lang="less" scoped>

</style>
