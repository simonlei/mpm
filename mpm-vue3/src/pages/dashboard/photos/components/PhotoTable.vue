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
      <template v-slot="{ item, index }">
        <Suspense>
          <photo-item :id="item.id" :key="item.id" :index="index"
                      :style="{'border-width': selectStore.isSelected(index) ? '2px' : '0px','border-style': 'solid','border-color': 'blue'}"/>
        </Suspense>
      </template>

    </RecycleScroller>
  </div>

  <div>
    <PhotoViewer/>
  </div>
  <date-picker-dialog/>
  <text-input-dialog/>

</template>

<script lang="ts" setup>
import {getPicIds, getPicsCount} from "@/api/photos";
import {detailViewModuleStore, photoFilterStore, photoModuleStore} from '@/store';
import {onActivated, onDeactivated, onMounted, onUnmounted, ref} from "vue";
import {onKeyStroke, useActiveElement, useElementVisibility} from '@vueuse/core';
import PhotoItem from './PhotoItem.vue'
import {selectModuleStore} from "@/store/modules/select-module";
import PhotoViewer from "@/pages/dashboard/photos/components/PhotoDetailViewer.vue";
import DatePickerDialog from "@/layouts/components/DatePickerDialog.vue";
import TextInputDialog from "@/layouts/components/TextInputDialog.vue";

const gridItems = ref(10);
const photoStore = photoModuleStore();
const selectStore = selectModuleStore();
const detailViewStore = detailViewModuleStore();
const filterStore = photoFilterStore();
const scroller = ref(null);
const photogrid = ref(null);


photoStore.idList = (await getPicIds()).data;
photoStore.otherCount = await getPicsCount(!filterStore.trashed);

let oldWidth = 0;


function calcGridItems() {
  console.log("width:" + photogrid.value.clientWidth);
  return Math.floor((photogrid.value.clientWidth + 5) / (200 + 6));
}

function onResize() {
  if (oldWidth == photogrid.value.clientWidth) return;
  gridItems.value = calcGridItems();
  oldWidth = photogrid.value.clientWidth;
}

filterStore.$onAction(photosChanged());

function photosChanged() {
  return async ({name, store, args, after}) => {
    console.log('onAction store ' + store + ' name ' + name + ' args ' + args);
    after(async (result) => {
      console.log('store changed... ' + result);
      let newList = (await getPicIds()).data;
      photoStore.idList = newList;
      window.document.title = "My Photo Manager(" + photoStore.idList.length + ")";
      let otherCount = await getPicsCount(!filterStore.trashed);
      photoStore.otherCount = otherCount;
    })
  };
}


photoStore.$onAction(photosChanged());


selectStore.$onAction(async ({after}) => {
  after(async (result) => {
    if (selectStore.lastSelectedIndex == null || selectStore.lastSelectedIndex < 0) return;
    const start = scroller.value.getScroll().start;
    const end = scroller.value.getScroll().end;
    const row = Math.floor(selectStore.lastSelectedIndex / gridItems.value);
    console.log('start ' + start + ' end ' + end + ' row start ' + row * 156 + ' row end ' + (row + 1) * 156);
    if (row * 156 < start || (row + 1) * 156 > end) {
      scroller.value.scrollToItem(selectStore.lastSelectedIndex);
    }
  })
});

let key1: any;
let key2: any;
let key3: any;
let key4: any;
let key5: any;
let key6: any;

onMounted(async () => {
  gridItems.value = calcGridItems();
  console.log("grid items:" + gridItems.value);
  console.log("Setting up photo table...............");
  const activeElement = useActiveElement()
  const notUsingInput = () => {
    console.log(activeElement.value?.tagName);
    return keyAction && useElementVisibility(photogrid) && activeElement.value?.tagName != 'INPUT'
      && activeElement.value?.tagName != 'TEXTAREA';
  }

  key1 = onKeyStroke('d', async (e) => {
    if (selectStore.lastSelectedIndex < 0 || selectStore.lastSelectedIndex > photoStore.idList.length - 1) return;
    if (notUsingInput()) {
      await photoStore.deleteSelectedPhotos();
      if (detailViewStore.detailVisible) {
        await detailViewStore.showDetailView(selectStore.lastSelectedIndex);
      }
    }
  });
  key2 = onKeyStroke('Enter', async (e) => {
    if (selectStore.lastSelectedIndex < 0 || selectStore.lastSelectedIndex > photoStore.idList.length - 1) return;
    if (notUsingInput())
      await detailViewStore.showDetailView(selectStore.lastSelectedIndex);
  });
  key3 = onKeyStroke('ArrowLeft', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(-1, e.shiftKey);
  });
  key4 = onKeyStroke('ArrowRight', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(1, e.shiftKey);
  });
  key5 = onKeyStroke('ArrowUp', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(-gridItems.value, e.shiftKey);
  });
  key6 = onKeyStroke('ArrowDown', (e) => {
    if (notUsingInput())
      selectStore.changeSelectedIndex(gridItems.value, e.shiftKey);
  });

});
let keyAction = true;

onDeactivated(() => {
  console.log("do not actived");
  keyAction = false;
});
onActivated(() => {
  keyAction = true;
});

onUnmounted(() => {
  console.log("unmounted.......");
  key1();
  key2();
  key3();
  key4();
  key5();
  key6();
});

</script>

<script lang="ts">

export default {
  name: 'PhotoTable',
};
</script>


