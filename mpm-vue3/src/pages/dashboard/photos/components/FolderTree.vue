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
import {ref} from "vue";

const filterStore = photoFilterStore();
filterStore.dateKey = null;

const TREE_DATA = ref(await getPicsFolderList(null, filterStore.trashed));
TREE_DATA.value.forEach((value) => value.children = true);

const KEYSX = {value: 'id', label: 'title'};

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
    TREE_DATA.value = await getPicsFolderList(null, filterStore.trashed);
    console.log('data ' + TREE_DATA);
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
