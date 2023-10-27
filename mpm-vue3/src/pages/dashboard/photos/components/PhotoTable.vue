<template>
  <div ref="photogrid">
    <RecycleScroller :key="Date.now()"
                     ref="scroller" :emit-update="true"
                     :grid-items="gridItems"
                     :item-size="156"
                     :itemSecondarySize="206"
                     :items="list"
                     class="scroller"
                     key-field="id"
                     page-mode
                     @resize="onResize"
                     @update="onScrollUpdate"
    >
      <template #default="{ item, index }">
        <div
          :style="{'border-width': selectedIndex == index ? '2px' : '0px','border-style': 'solid','border-color': 'blue'}">
          <t-image :key="item.name" :src="/cos/ + item.thumb" fit='none' height="150"
                   shape="round" width="200"
                   @click="selectedIndex=index"
                   @doubleclick="console.log('double')"
                   v-on:dblclick="showDetailView(item,index)"
          >
          </t-image>
        </div>
      </template>

    </RecycleScroller>
  </div>

  <div>
    <t-image-viewer ref="imageViewer"
                    v-model:index="detailViewShowIndex"
                    v-model:visible="detailVisible"
                    :images="detailImages"
                    :onIndexChange="onDetailViewIndexChange"
    >
    </t-image-viewer>
  </div>

</template>

<script lang="ts" setup>

import {getPicIds, getPics} from "@/api/photos";
import {photoFilterStore} from '@/store';
import {onMounted, ref} from "vue";

const gridItems = ref(10);
const selectedIndex = ref(0);
const detailVisible = ref(false);
const detailViewShowIndex = ref(0);
const detailImages = ref([]);
const imageViewer = ref(null);

let list = (await getPicIds()).data;

function showDetailView(item, index) {
  detailImages.value.length = 0;
  detailViewShowIndex.value = 0;
  if (index > 0) {
    detailImages.value.push("/cos/small/" + list[index - 1].name);
    detailViewShowIndex.value = 1;
  }
  detailImages.value.push("/cos/small/" + list[index].name);
  if (index < list.length - 1) {
    detailImages.value.push("/cos/small/" + list[index + 1].name);
  }
  detailVisible.value = true;
  selectedIndex.value = index;
}

function onDetailViewIndexChange(index, context) {
  let delta = context.trigger == 'prev' ? -1 : context.trigger == 'next' ? 1 : 0;
  let nextIndex = selectedIndex.value + delta;
  if (nextIndex < 0) nextIndex = 0;
  if (nextIndex > list.length - 1) nextIndex = list.length - 1;
  console.log("changed to " + nextIndex);
  showDetailView(list[nextIndex], nextIndex);
}

async function onScrollUpdate(viewStartIndex, viewEndIndex, visibleStartIndex, visibleEndIndex) {
  var hasUnloaded = false;
  for (let i = viewStartIndex; i <= viewEndIndex; i++) {
    if (list[i] == null) continue;
    if (list[i].name == null) {
      hasUnloaded = true;
      break;
    }
  }
  console.log("view start " + viewStartIndex + " - " + viewEndIndex + " visiable " +
    visibleStartIndex + " - " + visibleEndIndex + " has unload " + hasUnloaded);
  if (hasUnloaded) {
    const result = (await getPics(viewStartIndex, viewEndIndex - viewStartIndex));
    const loadData = result.data;
    list.length = result.totalRows;
    for (let i = 0; i < loadData.length; i++) {
      Object.assign(list[i + viewStartIndex], loadData[i]);
    }
  }
  // console.log(list);
}

function calcGridItems() {
  console.log("width:" + photogrid.value.clientWidth);
  return Math.floor((photogrid.value.clientWidth + 5) / (200 + 6));
}

function onResize() {
  gridItems.value = calcGridItems();
}

const store = photoFilterStore();
const scroller = ref(null);

store.$onAction(async ({
                         name, // action 名称
                         store, // store 实例，类似 `someStore`
                         args, // 传递给 action 的参数数组
                         after, // 在 action 返回或解决后的钩子
                         onError, // action 抛出或拒绝的钩子
                       }) => {
  after(async (result) => {
    let newList = (await getPicIds()).data;
    list.length = newList.length;
    for (let i = 0; i < newList.length; i++) {
      list[i] = newList[i];
    }


    scroller.value.updateVisibleItems(true);
    scroller.value.scrollToPosition(0);
  })
});
const photogrid = ref(null);


onMounted(async () => {
  gridItems.value = calcGridItems();
  console.log("grid items:" + gridItems.value);
});
</script>

<script lang="ts">

export default {
  name: 'PhotoTable',
};
</script>


