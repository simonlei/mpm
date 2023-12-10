<script lang="ts" setup>
import {photoFilterStore} from "@/store";

let filterStore = photoFilterStore();

function changeChecked(value) {
  filterStore.change({star: value});
}
</script>

<template>
  <t-checkbox :checked="filterStore.star" @change="changeChecked">
    只看星标
  </t-checkbox>
</template>

<style lang="less" scoped>

</style>
