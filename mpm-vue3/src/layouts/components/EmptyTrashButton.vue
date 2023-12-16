<script lang="ts" setup>
import {photoFilterStore} from "@/store";
import {emptyTrash, getTaskProgress} from "@/api/photos";
import {ref} from "vue";

const filterStore = photoFilterStore();
let progressVisible = ref(false);
let progressPercent = ref(0);

async function doEmptyTrash() {
  const taskId = await emptyTrash();
  progressVisible.value = true;
  console.log("Task id is {}", taskId);
  let progress = await getTaskProgress(taskId.taskId);
  const interval = setInterval(async () => {
    if (progress.progress == 100) {
      progressVisible.value = false;
      filterStore.change({});
      clearInterval(interval);
    } else {
      progress = await getTaskProgress(taskId.taskId);
      progressPercent.value = progress.progress;
    }
  }, 100);
};

</script>

<template>
  <t-popconfirm content="清空后照片无法找回！确认清空回收站吗？" @confirm="doEmptyTrash">
    <t-button v-if="filterStore.trashed" theme="default" variant="text">清空回收站</t-button>
  </t-popconfirm>

  <t-dialog v-model:visible="progressVisible" :close-btn="false" :close-on-esc-keydown="false"
            :close-on-overlay-click="false" :footer="false">
    <t-progress :label="'正在清空回收站...'" :percentage="progressPercent" theme="plump"/>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
