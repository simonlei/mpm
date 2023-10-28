<template>
  <div>
    <t-tree :data="TREE_DATA" :keys="KEYSX" :load="load" :onActive="treeActive"
            activable
            expand-on-click-node hover></t-tree>
  </div>

</template>

<script lang="ts" setup>

import {getPicsFolderList} from "@/api/photos";
import {TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';

const store = photoFilterStore();
store.dateKey = null;

const TREE_DATA = await getPicsFolderList(null);
TREE_DATA.forEach((value) => value.children = true);

const KEYSX = {value: 'id', label: 'title'};

function treeActive(value: Array<TreeNodeValue>, context) {
  console.log('tree active ' + value);
  let newPath = context.node.data.path;
  console.log('path ' + newPath);
  if (store.path != newPath) {
    store.change({path: newPath});
  }
}

async function load(node) {
  return await getPicsFolderList(node.value);
}

</script>

<script lang="ts">

export default {
  name: 'FolderTree',
};
</script>
