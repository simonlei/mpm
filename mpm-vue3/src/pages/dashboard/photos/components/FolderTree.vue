<template>
  <div>

    <t-tree ref="tree" :data="TREE_DATA" :expand-level="1" :keys="KEYSX" :load="load"
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
const KEYSX = {value: 'path', label: 'title'};
const tree = ref(null);

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
    filterStore.change({path: newPath});
  }
}

filterStore.$onAction(async ({after}) => {
  after(async (result) => {
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
