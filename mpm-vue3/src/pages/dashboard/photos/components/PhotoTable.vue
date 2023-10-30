<template>
  <div ref="photogrid">
    <RecycleScroller ref="scroller"
                     :grid-items="gridItems"
                     :item-size="156"
                     :itemSecondarySize="206"
                     :items="photoStore.idList"
                     class="scroller"
                     key-field="id"
                     page-mode
                     @resize="onResize"
    >
      <template #default="{ item, index }">
        <Suspense>
          <photo-item :id="item.id" :key="item.id" :index="index"
                      :style="{'border-width': selectStore.selectedIndexes.indexOf(index)>=0 ? '2px' : '0px','border-style': 'solid','border-color': 'blue'}"/>
        </Suspense>
      </template>

    </RecycleScroller>
  </div>

  <div>
    <t-image-viewer ref="imageViewer"
                    v-model:index="detailViewStore.detailViewShowIndex"
                    v-model:visible="detailViewStore.detailVisible"
                    :images="detailViewStore.detailImages"
                    :onClose="onDetailViewClosed"
                    :onIndexChange="onDetailViewIndexChange"
    >
    </t-image-viewer>
  </div>

</template>

<script lang="ts" setup>
import {getPicIds} from "@/api/photos";
import {detailViewModuleStore, photoFilterStore, photoModuleStore} from '@/store';
import {onMounted, ref} from "vue";
import {onKeyStroke} from '@vueuse/core';
import PhotoItem from './PhotoItem.vue'
import {selectModuleStore} from "@/store/modules/select-module";

const gridItems = ref(10);
const imageViewer = ref(null);
const photoStore = photoModuleStore();
const selectStore = selectModuleStore();
const detailViewStore = detailViewModuleStore();
const filterStore = photoFilterStore();
const scroller = ref(null);
const photogrid = ref(null);


photoStore.idList = (await getPicIds()).data;
let oldWidth = 0;

console.log("Setting up photo table...............");


onKeyStroke('Enter', async (e) => {
  if (selectStore.lastSelectedIndex < 0 || selectStore.lastSelectedIndex > photoStore.idList.length - 1) return;
  await detailViewStore.showDetailView(selectStore.lastSelectedIndex);
})
onKeyStroke('ArrowLeft', (e) => {
  if (detailViewStore.detailVisible) return;
  changeSelectedIndex(-1, e.shiftKey);
  e.preventDefault();
})
onKeyStroke('ArrowRight', (e) => {
  if (detailViewStore.detailVisible) return;
  changeSelectedIndex(1, e.shiftKey);
  e.preventDefault();
})
onKeyStroke('ArrowUp', (e) => {
  changeSelectedIndex(-gridItems.value, e.shiftKey);
  e.preventDefault();
})
onKeyStroke('ArrowDown', (e) => {
  changeSelectedIndex(gridItems.value, e.shiftKey);
  e.preventDefault();
})


function onDetailViewClosed() {
  console.log('detail view closed:' + selectStore.lastSelectedIndex);
  scroller.value.scrollToItem(selectStore.lastSelectedIndex);
}

function onDetailViewIndexChange(index, context) {
  let delta = context.trigger == 'prev' ? -1 : context.trigger == 'next' ? 1 : 0;
  changeSelectedIndex(delta, false);
}

async function changeSelectedIndex(delta: number, shiftKey: boolean) {
  console.log("from " + selectStore.lastSelectedIndex + " with delta " + delta);
  let nextIndex = selectStore.lastSelectedIndex + delta;
  if (nextIndex < 0) nextIndex = 0;
  if (nextIndex > photoStore.idList.length - 1) nextIndex = photoStore.idList.length - 1;
  console.log("changed to " + nextIndex);

  if (detailViewStore.detailVisible)
    await detailViewStore.showDetailView(nextIndex);

  selectStore.selectIndex(nextIndex, false, shiftKey);

  const start = scroller.value.getScroll().start;
  const end = scroller.value.getScroll().end;
  const row = Math.floor(nextIndex / gridItems.value);
  console.log('start ' + start + ' end ' + end + ' row start ' + row * 156 + ' row end ' + (row + 1) * 156);
  if (row * 156 < start || (row + 1) * 156 > end)
    scroller.value.scrollToItem(selectStore.lastSelectedIndex);

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

filterStore.$onAction(async ({
                               name, // action 名称
                               store, // store 实例，类似 `someStore`
                               args, // 传递给 action 的参数数组
                               after, // 在 action 返回或解决后的钩子
                               onError, // action 抛出或拒绝的钩子
                             }) => {
  after(async (result) => {
    console.log('store changed... ' + result);
    let newList = (await getPicIds()).data;
    selectStore.clearSelect();
    photoStore.idList.length = newList.length;
    window.document.title = "My Photo Manager(" + photoStore.idList.length + ")";
    for (let i = 0; i < newList.length; i++) {
      photoStore.idList[i] = newList[i];
    }

    if (scroller.value != null) {
      scroller.value.updateVisibleItems(true);
      scroller.value.scrollToPosition(0);
    }
  })
});

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


