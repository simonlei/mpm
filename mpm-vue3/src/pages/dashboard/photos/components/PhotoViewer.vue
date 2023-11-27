<script lang="ts" setup>
import {detailViewModuleStore} from "@/store";
import {ref} from "vue";
import {selectModuleStore} from "@/store/modules/select-module";

const imageViewer = ref(null);
const detailViewStore = detailViewModuleStore();
const selectStore = selectModuleStore();

function onDetailViewIndexChange(index, context) {
  let delta = context.trigger == 'prev' ? -1 : context.trigger == 'next' ? 1 : 0;
  selectStore.changeSelectedIndex(delta, false);
}

</script>

<template>
  <t-image-viewer ref="imageViewer"
                  v-model:index="detailViewStore.detailViewShowIndex"
                  v-model:visible="detailViewStore.detailVisible"
                  :images="detailViewStore.detailImages"
                  :onIndexChange="onDetailViewIndexChange"
                  :title="detailViewStore.detailTitle"
  >
  </t-image-viewer>
</template>

<style lang="less" scoped>

</style>
