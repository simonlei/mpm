<script lang="ts" setup>
import {switchTrashFolder, updateFolderDate, updateFolderGis} from "@/api/photos";
import {dialogsStore, photoFilterStore} from "@/store";
import {DialogPlugin, MessagePlugin, NotifyPlugin} from "tdesign-vue-next";
import ContextMenu from "@imengyu/vue3-context-menu";
import {gisDateClipboardStore} from "@/store/modules/gis-date-clipboard";

const props = defineProps({node: null});
const dlgStore = dialogsStore();

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

function changeDateInFolder() {
  dlgStore.datePickerDlg = true;
  dlgStore.whenDateConfirmed(async (selectedDate: string) => {
    const count = await updateFolderDate(props.node.data.path, selectedDate);
    NotifyPlugin.info({title: `已设置目录下${count}张照片拍摄时间至 ${selectedDate}`});
  })
}

function changeGisInFolder() {
  dlgStore.textInputTitle = '请输入纬度,经度，例如 22.57765,113.9504277778';
  dlgStore.textInputDlg = true;
  dlgStore.whenInputConfirmed(async (inputValue: string) => {
    const values = inputValue.split(',');
    if (values == null || values.length != 2) {
      MessagePlugin.error('请按照 纬度,经度 模式来输入');
    } else {
      const count = await updateFolderGis(props.node.data.path, values[0], values[1]);
      NotifyPlugin.info({title: `已设置目录下${count}张照片GIS至 ${inputValue}`});
    }
  });
}

const gisDateClipboard = gisDateClipboardStore();

function changeGisAndDateInFolder() {
  const confirmDialog = DialogPlugin.confirm({
    header: '确定应用 GIS 及时间信息？',
    body: `GIS: ${gisDateClipboard.latitude}, ${gisDateClipboard.longitude}\n 时间：${gisDateClipboard.takendate}`,
    confirmBtn: '确定',
    onConfirm: async ({e}) => {
      let count = await updateFolderDate(props.node.data.path, gisDateClipboard.takendate.toString());
      NotifyPlugin.info({title: `已设置目录下${count}张照片拍摄时间至 ${gisDateClipboard.takendate}`});
      count = await updateFolderGis(props.node.data.path,
        gisDateClipboard.latitude.toString(), gisDateClipboard.longitude.toString());
      NotifyPlugin.info({title: `已设置目录下${count}张照片GIS至 ${gisDateClipboard.latitude},${gisDateClipboard.longitude}`});
      confirmDialog.hide();
    },
  });
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
        onClick: () => changeDateInFolder(),
      },
      {
        label: "修改目录下所有照片GIS信息...",
        onClick: () => changeGisInFolder(),
      },
      {
        label: "应用已复制的 GIS 和时间信息到目录下所有照片",
        onClick: () => changeGisAndDateInFolder(),
      },
      {
        label: `${filterStore.trashed ? '恢复目录' : '删除目录'}`,
        onClick: () =>
          deleteSelectedFolder(props.node.data.path)
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
