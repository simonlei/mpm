<script lang="ts" setup>
import {switchTrashFolder} from "@/api/photos";
import {photoFilterStore} from "@/store";
import {DialogPlugin, MessagePlugin} from "tdesign-vue-next";
import ContextMenu from "@imengyu/vue3-context-menu";

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

async function onContextMenu(e: MouseEvent) {
  //prevent the browser's default menu
  e.preventDefault();
  await filterStore.change({path: props.node.data.path});

  //show our menu
  ContextMenu.showContextMenu({
    x: e.x,
    y: e.y,
    items: [
      {
        label: "修改目录下所有照片时间...",
        onClick: () => {
          alert("You click a menu item");
        }
      },
      {
        label: "修改目录下所有照片GIS信息...",
        onClick: () => {
          alert("You click a menu item");
        }
      },
      {
        label: `${filterStore.trashed ? '恢复目录' : '删除目录'}`,
        onClick: () =>
          deleteSelectedFolder(props.node.data.path)
      },
      {
        label: "移动目录至...",
        onClick: () => {
          alert("You click a menu item");
        }
      },
      {
        label: "合并目录至...",
        onClick: () => {
          alert("You click a menu item");
        }
      },
    ]
  });
}


</script>

<template>
  <div @contextmenu="onContextMenu($event)">{{ node.label }}</div>
</template>

<style lang="less" scoped>

</style>
