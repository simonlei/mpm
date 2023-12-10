<script lang="ts" setup>
import {detailViewModuleStore} from "@/store";
import {ref} from "vue";
import PhotoDescribeTable from "@/pages/dashboard/photos/components/PhotoDescribeTable.vue";

const imageViewer = ref(null);
const detailViewStore = detailViewModuleStore();

const config = ref({
  src: `/cos/video_t/${detailViewStore.currentPhoto?.name}.mp4`,
  poster: `/cos/small/${detailViewStore.currentPhoto?.name}`,
});
detailViewStore.$subscribe((mutation, state) => {
  const photo = state.currentPhoto;
  config.value.src = `/cos/video_t/${photo?.name}.mp4`;
  config.value.poster = `/cos/small/${photo?.name}`;
});

</script>

<template>
  <t-dialog ref="imageViewer" v-model:visible="detailViewStore.detailVisible" :close-btn="false"
            :footer="false" :header="false" placement="center" width="1000px">
    <t-layout>

      <t-content v-if="detailViewStore.currentPhoto!=null">
        <t-image v-if="detailViewStore.currentPhoto.mediatype=='photo'"
                 :key="detailViewStore.currentPhotoName+'-small'"
                 :src="detailViewStore.currentPhotoSmallUrl"
        />
        <customVideo v-else
                     :key="detailViewStore.currentPhotoName+'-video'"
                     :src="`/cos/video_t/${detailViewStore.currentPhoto?.name}.mp4`"
                     :videoConfig="config"
                     width="800px"
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
