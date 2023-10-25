<template>
  <div>
    <t-tree :data="TREE_DATA" :keys="KEYSX" :onActive="treeActive" activable expand-on-click-node
            hover></t-tree>
  </div>

</template>

<script lang="ts" setup>

import {getPicsDateList} from "@/api/photos";
import {TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';

const TREE_DATA = await getPicsDateList();

console.log(TREE_DATA);

const KEYSX = {value: 'id', label: 'title', children: 'months'};
console.log(KEYSX);

function treeActive(value: Array<TreeNodeValue>) {
  console.log(value);
  const store = photoFilterStore();
  let newDateKey = '' + value[0];
  if (store.dateKey != newDateKey) {
    store.change({dateKey: newDateKey});
  }
}

</script>

<script lang="ts">

export default {
  name: 'DateTree',
};
</script>
