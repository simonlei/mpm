<template>
  <div>

    <t-tree :data="TREE_DATA" :expand-level="1" :keys="KEYSX" :load="load"
            :onActive="treeActive" activable
            expand-on-click-node hover>
      <template #label="{ node }">
        <folder-tree-context-menu :node="node"/>
      </template>
    </t-tree>
  </div>

</template>

<script lang="ts" setup>

import {getPicsFolderList} from "@/api/photos";
import {TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';
import {ref} from "vue";
import FolderTreeContextMenu from "@/pages/dashboard/photos/components/FolderTreeContextMenu.vue";

const filterStore = photoFilterStore();
filterStore.dateKey = null;

const root = {id: '', title: '全部', children: []};

async function getFolderTreeWithRoot() {
  let photosFolders = await getPicsFolderList(null, filterStore.trashed);
  photosFolders.forEach((value) => value.children = true);
  root.children = photosFolders;
  return [root];
}

let TREE_DATA = ref(await getFolderTreeWithRoot());

const KEYSX = {value: 'id', label: 'title'};
let beforeIsTrashed = filterStore.trashed;

function treeActive(value: Array<TreeNodeValue>, context) {
  console.log('tree active ' + value);
  let newPath = context.node.data.path;
  console.log('path ' + newPath);
  if (filterStore.path != newPath) {
    filterStore.change({path: newPath});
  }
}

filterStore.$onAction(async ({after}) => {
  after(async (result) => {
    if (beforeIsTrashed != filterStore.trashed || filterStore.path == '') {
      beforeIsTrashed = filterStore.trashed;

      TREE_DATA.value = await getFolderTreeWithRoot();
      console.log('data ' + TREE_DATA);
    }
  })
});

async function load(node) {
  return await getPicsFolderList(node.value, filterStore.trashed);
}

</script>

<script lang="ts">

export default {
  name: 'FolderTree',
};
</script>
