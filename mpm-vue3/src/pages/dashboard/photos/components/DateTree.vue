<template>
  <div>
    <t-tree :data="TREE_DATA" :keys="KEYSX" :onActive="treeActive" activable
            expand-on-click-node
            hover></t-tree>
  </div>

</template>

<script lang="ts" setup>

import {getPicsDateList} from "@/api/photos";
import {TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';
import {ref} from "vue";

const filterStore = photoFilterStore();
filterStore.path = null;

let TREE_DATA = ref(await getPicsDateList(filterStore.trashed));
const KEYSX = {value: 'id', label: 'title', children: 'months'};

filterStore.$onAction(async ({after}) => {
  after(async (result) => {
    TREE_DATA.value = await getPicsDateList(filterStore.trashed);
    console.log('data ' + TREE_DATA);
  })
});

function treeActive(value: Array<TreeNodeValue>) {
  console.log(value);
  let newDateKey = '' + value[0];
  if (filterStore.dateKey != newDateKey) {
    filterStore.change({dateKey: newDateKey});
  }
}

</script>

<script lang="ts">

export default {
  name: 'DateTree',
};
</script>
