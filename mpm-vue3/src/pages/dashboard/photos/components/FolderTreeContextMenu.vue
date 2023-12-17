<script lang="ts" setup>
import {switchTrashFolder} from "@/api/photos";
import {photoFilterStore} from "@/store";
import {DialogPlugin, MessagePlugin} from "tdesign-vue-next";

const props = defineProps({node: null});
defineEmits(['update:node']);
const filterStore = photoFilterStore();

async function deleteSelectedFolder(value: string) {
  const confirmDia = DialogPlugin({
    header: filterStore.trashed ? '确认恢复目录？' : '确认删除目录？',
    body: filterStore.trashed ? '将目录下所有照片恢复？' : '将目录下所有照片移入回收站？',
    confirmBtn: '确认',
    cancelBtn: '取消',
    onConfirm: async ({e}) => {
      const result = await switchTrashFolder(!filterStore.trashed, value);
      console.log("delete {} result {}", value, result);
      if (result > 0) {
        filterStore.change({path: ''});
        MessagePlugin.success(`已处理照片${result}`);
      }
      confirmDia.hide();
    },
    onClose: ({e, trigger}) => {
      confirmDia.hide();
    },
  });
  confirmDia.show();
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
