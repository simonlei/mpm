<script lang="ts" setup>
import {switchTrashFolder} from "@/api/photos";
import {photoFilterStore} from "@/store";

const props = defineProps({node: null});
defineEmits(['update:node']);
const filterStore = photoFilterStore();

async function deleteSelectedFolder(value: string) {
  // TODO: confirm
  const result = await switchTrashFolder(!filterStore.trashed, value);
  console.log("delete {} result {}", value, result);
  if (result > 0) {
    filterStore.change({path: ''});
    // TREE_DATA.value = await getFolderTreeWithRoot();
  }
}

</script>

<template>
  <t-dropdown :max-column-width="250" trigger="context-menu">
    <div>{{ node.label }}</div>
    <t-dropdown-menu>
      <t-dropdown-item content="修改目录下所有照片时间..."></t-dropdown-item>
      <t-dropdown-item content="修改目录下所有照片GIS信息..."></t-dropdown-item>
      <t-dropdown-item :content="filterStore.trashed ? '恢复目录' : '删除目录'"
                       @click="deleteSelectedFolder(node.data.path)">
      </t-dropdown-item>
      <t-dropdown-item content="移动目录至..."></t-dropdown-item>
      <t-dropdown-item content="合并目录至..."></t-dropdown-item>
    </t-dropdown-menu>
  </t-dropdown>

</template>

<style lang="less" scoped>

</style>
