<template>
  <div ref="photogrid">
    <RecycleScroller ref="scroller"
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
        <Suspense>
          <photo-item :id="item.id" :key="item.id" :index="index"/>
        </Suspense>
      </template>

    </RecycleScroller>
  </div>

  <div>
    <t-image-viewer ref="imageViewer"
                    v-model:index="detailViewShowIndex"
                    v-model:visible="detailVisible"
                    :images="detailImages"
                    :onClose="onDetailViewClosed"
                    :onIndexChange="onDetailViewIndexChange"
    >
    </t-image-viewer>
  </div>

</template>

<script lang="ts" setup>
import {getPicIds, getPics} from "@/api/photos";
import {photoFilterStore} from '@/store';
import {onMounted, ref} from "vue";
import {onKeyStroke} from '@vueuse/core';
import AsyncLock from 'async-lock';
import PhotoItem from './PhotoItem.vue'


const gridItems = ref(10);
const selectedIndex = ref(0);
const detailVisible = ref(false);
const detailViewShowIndex = ref(0);
const detailImages = ref([]);
const imageViewer = ref(null);

let list = (await getPicIds()).data;
let oldWidth = 0;

console.log("Setting up photo table...............");

onKeyStroke('Enter', (e) => {
  if (list == null || selectedIndex.value < 0 || selectedIndex.value > list.length - 1) return;
  showDetailView(list[selectedIndex.value], selectedIndex.value);
})
onKeyStroke('ArrowLeft', (e) => {
  if (detailVisible.value) return;
  changeSelectedIndex(-1);
  e.preventDefault();
})
onKeyStroke('ArrowRight', (e) => {
  if (detailVisible.value) return;
  changeSelectedIndex(1);
  e.preventDefault();
})
onKeyStroke('ArrowUp', (e) => {
  changeSelectedIndex(-gridItems.value);
  e.preventDefault();
})
onKeyStroke('ArrowDown', (e) => {
  changeSelectedIndex(gridItems.value);
  e.preventDefault();
})


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

function onDetailViewClosed() {
  console.log('detail view closed:' + selectedIndex.value);
  scroller.value.scrollToItem(selectedIndex.value);
}

function onDetailViewIndexChange(index, context) {
  let delta = context.trigger == 'prev' ? -1 : context.trigger == 'next' ? 1 : 0;
  changeSelectedIndex(delta);
}

function changeSelectedIndex(delta: number) {
  console.log("from " + selectedIndex.value + " with delta " + delta);
  let nextIndex = selectedIndex.value + delta;
  if (nextIndex < 0) nextIndex = 0;
  if (nextIndex > list.length - 1) nextIndex = list.length - 1;
  console.log("changed to " + nextIndex);

  if (detailVisible.value)
    showDetailView(list[nextIndex], nextIndex);
  else {
    selectedIndex.value = nextIndex;
    const start = scroller.value.getScroll().start;
    const end = scroller.value.getScroll().end;
    const row = Math.floor(nextIndex / gridItems.value);
    console.log('start ' + start + ' end ' + end + ' row start ' + row * 206 + ' row end ' + (row + 1) * 206);
    if (row * 206 - 300 < start || (row + 1) * 206 - 300 > end)
      scroller.value.scrollToItem(selectedIndex.value);
  }
}

async function onScrollUpdate(viewStartIndex, viewEndIndex, visibleStartIndex, visibleEndIndex) {
  // const AsyncLock = require('async-lock');
  const lock = new AsyncLock();

  lock.acquire('scrollUpdate', async function () {

    // TODO: 展示大图时，左右切换，页面会有抖动
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
      const result = (await getPics(viewStartIndex, Math.max(viewEndIndex - viewStartIndex, 100)));
      const loadData = result.data;
      list.length = result.totalRows;
      for (let i = 0; i < loadData.length; i++) {
        Object.assign(list[i + viewStartIndex], loadData[i]);
      }
      console.log('fetch end' + viewEndIndex);
    }
  });
  // console.log(list);
}

function calcGridItems() {
  console.log("width:" + photogrid.value.clientWidth);
  return Math.floor((photogrid.value.clientWidth + 5) / (200 + 6));
}

function onResize() {
  if (oldWidth == photogrid.value.clientWidth) return;
  gridItems.value = calcGridItems();
  oldWidth = photogrid.value.clientWidth;
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
    console.log('store changed... ' + result);
    let newList = (await getPicIds()).data;
    list.length = newList.length;
    window.document.title = "My Photo Manager(" + list.length + ")";
    for (let i = 0; i < newList.length; i++) {
      list[i] = newList[i];
    }

    if (scroller.value != null) {
      scroller.value.updateVisibleItems(true);
      scroller.value.scrollToPosition(0);
    }
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


