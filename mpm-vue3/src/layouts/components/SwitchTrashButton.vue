<script lang="ts" setup>
import {photoFilterStore, photoModuleStore} from "@/store";

const photoStore = photoModuleStore();
const filterStore = photoFilterStore();

function switchTrash() {
  filterStore.change({trashed: !filterStore.trashed});
  console.log('now trashed ' + filterStore.trashed);
}

</script>

<template>
  <t-button :content="(filterStore.trashed ? '正常照片(' : '回收站(')+photoStore.otherCount+')'"
            class="t-button2" shape="square"
            theme="default"
            variant="text"
            @click="switchTrash">
  </t-button>
</template>

<style lang="less" scoped>

.t-button2 {
  min-width: 120px;
  max-width: 200px;
}

</style>
