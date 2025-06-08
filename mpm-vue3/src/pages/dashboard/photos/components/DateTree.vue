<template>
  <div>
    <t-tree :data="TREE_DATA" :expand-level="1" :keys="KEYSX" :onActive="treeActive"
            activable expand-on-click-node style="height:1200px; overflow: auto" class="custom-scrollbar"
            hover></t-tree>
  </div>

</template>
<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px; /* 滚动条宽度 */
  height: 6px; /* 滚动条高度 */
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2); /* 滚动条滑块颜色 */
  border-radius: 3px; /* 滚动条滑块圆角 */
}

.custom-scrollbar::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05); /* 滚动条轨道颜色 */
}

/* Firefox */
.custom-scrollbar {
  scrollbar-width: thin; /* 滚动条宽度为 thin */
  scrollbar-color: rgba(0, 0, 0, 0.2) rgba(0, 0, 0, 0.05); /* 滑块颜色和轨道颜色 */
}
</style>
<script lang="ts" setup>

import {getPicsDateList} from "@/api/photos";
import {TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';
import {ref} from "vue";

const filterStore = photoFilterStore();
filterStore.path = null;
filterStore.faceId = null;
const root = {id: '', title: '全部', children: []};

async function loadDateTreeWithRoot() {
  let photosDates = await getPicsDateList(filterStore.trashed, filterStore.star);
  root.children = photosDates;
  return [root];
}

let TREE_DATA = ref(await loadDateTreeWithRoot());
const KEYSX = {value: 'id', label: 'title', children: 'children'};

filterStore.$onAction(async ({after}) => {
  after(async (result) => {
    console.log("filter change result {}", result);
    if (result['dateKey'] == null) {
      TREE_DATA.value = await loadDateTreeWithRoot();
      console.log('data ' + TREE_DATA);
    }
  })
});

function treeActive(value: Array<TreeNodeValue>) {
  console.log(value);
  let newDateKey = '' + value[0];
  if (filterStore.dateKey != newDateKey) {
    filterStore.change({path: null, faceId: null, dateKey: newDateKey});
  }
}

</script>

<script lang="ts">

export default {
  name: 'DateTree',
};
</script>
