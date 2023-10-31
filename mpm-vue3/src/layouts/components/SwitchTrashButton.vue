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
  <t-tooltip content="切换回收站状态" placement="bottom">
    <t-button class="t-button2" shape="square" theme="default" variant="text" width="1200"
              @click="switchTrash">
      回收站({{ photoStore.otherCount }})
    </t-button>
  </t-tooltip>
</template>

<style lang="less" scoped>

.t-button2 {
  min-width: 80px;
}

</style>
