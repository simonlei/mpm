<template>
  <div>

    <t-tree ref="tree" :data="TREE_DATA" :expand-level="1" :keys="KEYSX"
            :load="load" :onActive="treeActive" activable draggable
            expand-on-click-node
            hover @drop="dropFolder">
      <template #label="{ node }">
        <folder-tree-context-menu :node="node"/>
      </template>
    </t-tree>
  </div>

</template>

<script lang="ts" setup>

import {getPicsFolderList, moveFolder} from "@/api/photos";
import {DialogPlugin, TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';
import {ref} from "vue";
import FolderTreeContextMenu from "@/pages/dashboard/photos/components/FolderTreeContextMenu.vue";

const filterStore = photoFilterStore();
filterStore.faceId = null;
filterStore.dateKey = null;

const root = {id: '-1', title: '全部', children: []};
const KEYSX = {value: 'id', label: 'title'};
const tree = ref(null);

function dropFolder(ctx: {}) {
  const fromNode = ctx['dragNode'];
  const toNode = ctx['dropNode'];
  console.log("from {} ", fromNode.data.path);
  console.log("from {} ", fromNode.data.id);
  const confirmDialog = DialogPlugin.confirm({
    header: '移动文件夹还是合并文件夹？',
    body: '请选择是移动文件夹还是合并文件夹',
    confirmBtn: '移动',
    cancelBtn: '合并',
    onConfirm: async ({e}) => {
      await moveFolder(fromNode.data.path, toNode.data.id, false);
      TREE_DATA.value = await getFolderTreeWithRoot();
      confirmDialog.hide();
    },
    onCancel: async ({e}) => {
      await moveFolder(fromNode.data.path, toNode.data.id, true);
      TREE_DATA.value = await getFolderTreeWithRoot();
      confirmDialog.hide();
    },
    onClose: async ({e, trigger}) => {
      TREE_DATA.value = await getFolderTreeWithRoot();
      confirmDialog.hide();
    },
  });
  console.log("Drop node is: {}", ctx['dropNode']);
}

async function getFolderTreeWithRoot() {
  let photosFolders = await getPicsFolderList(null, filterStore.trashed, filterStore.star);
  photosFolders.forEach((value) => value.children = true);
  root.children = photosFolders;
  return [root];
}

let TREE_DATA = ref(await getFolderTreeWithRoot());

let beforeIsTrashed = filterStore.trashed;

function treeActive(value: Array<TreeNodeValue>, context) {
  console.log('tree active ' + value);
  let newPath = context.node.data.path;
  console.log('path ' + newPath);
  if (filterStore.path != newPath) {
    filterStore.change({faceId: null, dateKey: null, path: newPath});
  }
}

filterStore.$onAction(async ({after}) => {
  after(async (result) => {
    if (tree.value == null) return;
    console.log("filter change result {}, {}", result, result['path']);
    if (result['path'] == null || filterStore.path == '') {
      beforeIsTrashed = filterStore.trashed;

      TREE_DATA.value = await getFolderTreeWithRoot();
      console.log('data ' + TREE_DATA);
    }
    const node = tree.value.getItem(filterStore.path)
    if (node != null) {
      tree.value.setItem(node.value, {actived: true});
    }
  })
});

async function load(node) {
  return await getPicsFolderList(node.data.id, filterStore.trashed, filterStore.star);
}

</script>

<script lang="ts">

export default {
  name: 'FolderTree',
};
</script>
