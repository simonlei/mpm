<template>
  <div>
    <t-tree :data="TREE_DATA" :expand-level="1" :keys="KEYSX" :onActive="treeActive"
            activable expand-on-click-node
            hover></t-tree>
  </div>

</template>

<script lang="ts" setup>

import {getPicsDateList} from "@/api/photos";
import {TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';
import {onActivated, ref} from "vue";

const filterStore = photoFilterStore();
filterStore.path = null;
filterStore.faceId = null;
const root = {id: '', title: '全部', months: []};

async function loadDateTreeWithRoot() {
  let photosDates = await getPicsDateList(filterStore.trashed, filterStore.star);
  root.months = photosDates;
  return [root];
}

let TREE_DATA = ref(await loadDateTreeWithRoot());
const KEYSX = {value: 'id', label: 'title', children: 'months'};
let beforeIsTrashed = filterStore.trashed;

filterStore.$onAction(async ({after}) => {
  after(async (result) => {
    console.log("filter change result {}", result);
    if (result['dateKey'] == null) {
      beforeIsTrashed = filterStore.trashed;
      TREE_DATA.value = await loadDateTreeWithRoot();
      console.log('data ' + TREE_DATA);
    }
  })
});

function treeActive(value: Array<TreeNodeValue>) {
  console.log(value);
  let newDateKey = '' + value[0];
  if (filterStore.dateKey != newDateKey) {
    filterStore.change({dateKey: newDateKey});
  }
}

onActivated(() => {
  console.log('hello......date active');
  filterStore.path = null;
  filterStore.faceId = null;
});


</script>

<script lang="ts">

export default {
  name: 'DateTree',
};
</script>
