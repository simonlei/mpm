<script lang="ts" setup>
import {detailViewModuleStore} from "@/store";
import {ref} from "vue";
import PhotoDescribeTable from "@/pages/dashboard/photos/components/PhotoDescribeTable.vue";

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

function getWidth() {
  return window.innerWidth;
}

function getHeight() {
  return window.innerHeight;
}

</script>

<template>
  <t-dialog ref="imageViewer" v-model:visible="detailViewStore.detailVisible" :close-btn="false"
            :footer="false" :header="false" :height="getHeight()" :width="getWidth()"
            attach="body" placement="center">
    <t-layout>

      <t-content v-if="detailViewStore.currentPhoto!=null">
        <t-image v-if="detailViewStore.currentPhoto.mediatype=='photo'"
                 :key="detailViewStore.currentPhotoName+'-small'"
                 :src="detailViewStore.currentPhotoSmallUrl"
                 :style="{ transform: 'rotate('+ getRotate() +'deg)'}"
        />
        <vue3VideoPlay v-else ref="videoPlayer"
                       :style="{ transform: 'rotate('+ detailViewStore.currentPhoto.rotate +'deg)'}"
                       poster="https://cdn.jsdelivr.net/gh/xdlumia/files/video-play/ironMan.jpg"
                       v-bind="config"
        />
      </t-content>
      <t-aside width="300px">
        <photo-describe-table :key="detailViewStore.currentPhotoName+'-detail'"
                              v-model:photo="detailViewStore.currentPhoto"/>
      </t-aside>

    </t-layout>
  </t-dialog>

</template>

<style lang="less" scoped>

</style>
