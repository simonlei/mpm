<script lang="ts" setup>

import {onMounted} from "vue";
import {onKeyStroke, useActiveElement} from "@vueuse/core";
import {detailViewModuleStore, photoModuleStore} from "@/store";
import {selectModuleStore} from "@/store/modules/select-module";
import {copyImageToClipboard} from "copy-image-clipboard";
import {MessagePlugin} from "tdesign-vue-next";

const photoStore = photoModuleStore();
const selectStore = selectModuleStore();
const detailViewStore = detailViewModuleStore();

onMounted(() => {

  console.log("key board listener...... mounted");

  console.log("grid items:" + photoStore.gridItems);
  console.log("Setting up photo table...............");
  const activeElement = useActiveElement()
  const notUsingInput = () => {
    console.log(activeElement.value?.tagName);
    return activeElement.value?.tagName != 'INPUT' && activeElement.value?.tagName != 'TEXTAREA';
  }
  onKeyStroke('c', (e) => {
    if (selectStore.lastSelectedIndex < 0 || selectStore.lastSelectedIndex > photoStore.idList.length - 1) return;
    if (notUsingInput()) {
      let index = selectStore.lastSelectedIndex;
      photoStore.getPhotoById(photoStore.idList[index].id, index).then(photo => {
        let imageSource = '/cos/small/' + photo.name;
        copyImageToClipboard(imageSource);
        MessagePlugin.closeAll();
        MessagePlugin.success('复制图片成功');
      });
    }
  });
  onKeyStroke('d', async (e) => {
    if (selectStore.lastSelectedIndex < 0 || selectStore.lastSelectedIndex > photoStore.idList.length - 1) return;
    if (notUsingInput()) {
      await photoStore.deleteSelectedPhotos();
      if (detailViewStore.detailVisible) {
        await detailViewStore.showDetailView(selectStore.lastSelectedIndex);
      }
    }
  });
  onKeyStroke('r', async (e) => {
    if (selectStore.lastSelectedIndex < 0 || selectStore.lastSelectedIndex > photoStore.idList.length - 1) return;
    if (notUsingInput()) {
      await photoStore.rotateSelectedPhotos();
      if (detailViewStore.detailVisible) {
        await detailViewStore.showDetailView(selectStore.lastSelectedIndex);
      }
    }
  });
  onKeyStroke('Enter', async (e) => {
    if (selectStore.lastSelectedIndex < 0 || selectStore.lastSelectedIndex > photoStore.idList.length - 1) return;
    if (notUsingInput())
      await detailViewStore.showDetailView(selectStore.lastSelectedIndex);
  });
  onKeyStroke('ArrowLeft', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(-1, e.shiftKey);
  });
  onKeyStroke('ArrowRight', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(1, e.shiftKey);
  });
  onKeyStroke('ArrowUp', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(-photoStore.gridItems, e.shiftKey);
  });
  onKeyStroke('ArrowDown', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(photoStore.gridItems, e.shiftKey);
  });
});
</script>

<template>
  <div></div>
</template>

<style lang="less" scoped>

</style>
