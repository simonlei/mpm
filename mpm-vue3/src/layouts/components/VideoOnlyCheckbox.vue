<script lang="ts" setup>

import {photoFilterStore} from "@/store";

let filterStore = photoFilterStore();

function changeChecked(value) {
  filterStore.change({video: value});
}
</script>

<template>
  <t-checkbox :checked="filterStore.video" @change="changeChecked">
    只看视频
  </t-checkbox>
</template>

<style lang="less" scoped>

</style>
