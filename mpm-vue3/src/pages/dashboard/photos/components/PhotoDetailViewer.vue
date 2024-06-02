<script lang="ts" setup>
import {detailViewModuleStore} from "@/store";
import {ref} from "vue";
import PhotoDescribeTable from "@/pages/dashboard/photos/components/PhotoDescribeTable.vue";
import PhotoFaceCircle from "@/pages/dashboard/photos/components/PhotoFaceCircle.vue";
import {faceModule} from "@/store/modules/face-module";

const imageViewer = ref(null);
const detailViewStore = detailViewModuleStore();
const videoPlayer = ref(null);
const faceStore = faceModule();

const config = ref({
  src: `/cos/video_t/${detailViewStore.currentPhoto?.name}.mp4`,
  poster: `/cos/small/${detailViewStore.currentPhoto?.name}`,
});
detailViewStore.$subscribe((mutation, state) => {
  const photo = state.currentPhoto;
  config.value.src = `/cos/video_t/${photo?.name}.mp4`;
  config.value.poster = `/cos/small/${photo?.name}`;
  if (!state.detailVisible) {
    faceStore.circleFace = false;
    videoPlayer.value?.pause();
  }
});

console.log('windows size {}  {}', window.innerWidth, window.innerHeight);


function getMinWidth() {
  let photo = detailViewStore.currentPhoto;

  return Math.min((photo?.rotate % 180 == 0 ? photo?.width : photo?.height) + 300, window.innerWidth);
}

function getMinHeight() {
  let photo = detailViewStore.currentPhoto;

  return Math.min((photo?.rotate % 180 == 0 ? photo?.height : photo?.width) + 200, window.innerHeight);
}

function getImageStyle() {
  return {width: `(${getMinWidth() - 300})px`, height: `${getMinHeight() - 200}px`};
}

const theImg = ref(null);
</script>

<template>
  <t-dialog ref="imageViewer" v-model:visible="detailViewStore.detailVisible"
            :close-btn="false" :footer="false" :header="false"
            :width="getMinWidth()"
            attach="body"
            placement="center">
    <t-layout>
      <t-content v-if="detailViewStore.currentPhoto!=null">
        <t-image v-if="detailViewStore.currentPhoto.media_type=='photo'"
                 :key="detailViewStore.currentPhotoName+'-small'"
                 ref="theImg"
                 :src="detailViewStore.currentPhotoSmallUrl"
                 :style="getImageStyle()"
                 fit="contain"
                 overlay-trigger="always"
        >
          <template #overlayContent>
            <PhotoFaceCircle :key="detailViewStore.currentPhotoName"
                             v-model:photo="detailViewStore.currentPhoto"
                             v-model:theImg="theImg"/>
          </template>
        </t-image>
        <!-- :style="{ transform: 'rotate('+ detailViewStore.currentPhoto.rotate +'deg)'}" -->
        <vue3VideoPlay v-else ref="videoPlayer" v-bind="config"/>
      </t-content>
      <t-aside width="300px">
        <PhotoDescribeTable :key="detailViewStore.currentPhotoName+'-detail'"
                            v-model:photo="detailViewStore.currentPhoto"/>
      </t-aside>
    </t-layout>
  </t-dialog>

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
