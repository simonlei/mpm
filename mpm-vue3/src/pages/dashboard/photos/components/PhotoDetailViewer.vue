<script lang="ts" setup>
import {detailViewModuleStore} from "@/store";
import {ref} from "vue";

const imageViewer = ref(null);
const detailViewStore = detailViewModuleStore();
const videoPlayer = ref(null);

const config = ref({
  src: `/cos/video_t/${detailViewStore.currentPhoto?.name}.mp4`,
  poster: `/cos/small/${detailViewStore.currentPhoto?.name}`,
});
detailViewStore.$subscribe((mutation, state) => {
  const photo = state.currentPhoto;
  config.value.src = `/cos/video_t/${photo?.name}.mp4`;
  config.value.poster = `/cos/small/${photo?.name}`;
  if (!state.detailVisible) {
    videoPlayer.value?.pause();
  }
});

function getRotate() {
  return (detailViewStore.currentPhoto.rotate + 360) % 360;
}

console.log('windows size {}  {}', window.innerWidth, window.innerHeight);

function getWidth() {
  return window.innerWidth + 'px';
}

function getHeight() {
  return window.innerHeight + 'px';
}

function getPhotoWidth() {
  let screenWidth = window.innerWidth - 350;
  let photo = detailViewStore.currentPhoto;
  console.log('screen width is {}', screenWidth);
  if (photo?.rotate % 180 != 90) return screenWidth;

  let screenHeight = window.innerHeight - 120;
  if (photo?.width / photo?.height > screenWidth / screenHeight) return screenWidth;
  return photo?.width * screenHeight / photo?.height;
}

function getPhotoHeight() {
  let screenHeight = window.innerHeight - 120;
  let p = detailViewStore.currentPhoto;
  if (p?.rotate % 180 != 90) return screenHeight;
  let screenWidth = window.innerWidth - 350;
  if (p?.width / p?.height < screenWidth / screenHeight) return screenHeight;
  return p?.height * screenWidth / p?.width;
}

</script>

<template>
  <Teleport to="body">
    <div v-if="detailViewStore.currentPhoto!=null"
         :height="detailViewStore.currentPhoto.rotate % 180 ==0 ? getPhotoHeight() : getPhotoWidth()"
         :style="{ transform: 'rotate('+ getRotate() +'deg)'}"
         :width="detailViewStore.currentPhoto.rotate % 180 ==0 ? getPhotoWidth() : getPhotoHeight()"
         class="mydialog"
    >
      <img v-if="detailViewStore.currentPhoto.mediatype=='photo'"
           :key="detailViewStore.currentPhotoName+'-small'"
           :height="getPhotoHeight()" :src="detailViewStore.currentPhotoSmallUrl"
           :width="getPhotoWidth()"
      />
      <vue3VideoPlay v-else ref="videoPlayer"
                     :style="{ transform: 'rotate('+ detailViewStore.currentPhoto.rotate +'deg)'}"
                     poster="https://cdn.jsdelivr.net/gh/xdlumia/files/video-play/ironMan.jpg"
                     v-bind="config"
      />
      <photo-describe-table :key="detailViewStore.currentPhotoName+'-detail'"
                            v-model:photo="detailViewStore.currentPhoto"/>
    </div>
  </Teleport>
  <!--
  <t-dialog ref="imageViewer" v-model:visible="detailViewStore.detailVisible" :close-btn="false"
            :footer="false" :header="false"
            attach="body"
            mode="full-screen">
    <t-layout>

      <t-content v-if="detailViewStore.currentPhoto!=null">
        <div
          :height="detailViewStore.currentPhoto.rotate % 180 ==0 ? getPhotoHeight() : getPhotoWidth()"
          :style="{ transform: 'rotate('+ getRotate() +'deg)'}"
          :width="detailViewStore.currentPhoto.rotate % 180 ==0 ? getPhotoWidth() : getPhotoHeight()">
          <img v-if="detailViewStore.currentPhoto.mediatype=='photo'"
               :key="detailViewStore.currentPhotoName+'-small'"
               :height="getPhotoHeight()" :src="detailViewStore.currentPhotoSmallUrl"
               :width="getPhotoWidth()"
          />
          <vue3VideoPlay v-else ref="videoPlayer"
                         :style="{ transform: 'rotate('+ detailViewStore.currentPhoto.rotate +'deg)'}"
                         poster="https://cdn.jsdelivr.net/gh/xdlumia/files/video-play/ironMan.jpg"
                         v-bind="config"
          />
        </div>
      </t-content>
      <t-aside width="300px">
        <photo-describe-table :key="detailViewStore.currentPhotoName+'-detail'"
                              v-model:photo="detailViewStore.currentPhoto"/>
      </t-aside>

    </t-layout>
  </t-dialog>
  -->

</template>

<style lang="less" scoped>
.mydialog {
  position: fixed;
  z-index: 99999;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>
