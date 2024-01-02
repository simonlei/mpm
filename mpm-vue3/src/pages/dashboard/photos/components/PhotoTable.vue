<template>
  <div ref="photogrid">
    <RecycleScroller ref="scroller"
                     :grid-items="photoStore.gridItems"
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
  <text-area-dialog/>
</template>

<script lang="ts" setup>
import {getPicIds, getPicsCount} from "@/api/photos";
import {photoFilterStore, photoModuleStore} from '@/store';
import {onMounted, ref} from "vue";
import PhotoItem from './PhotoItem.vue'
import {selectModuleStore} from "@/store/modules/select-module";
import PhotoViewer from "@/pages/dashboard/photos/components/PhotoDetailViewer.vue";
import DatePickerDialog from "@/layouts/components/DatePickerDialog.vue";
import TextInputDialog from "@/layouts/components/TextInputDialog.vue";
import TextAreaDialog from "@/layouts/components/TextAreaDialog.vue";

const photoStore = photoModuleStore();
const selectStore = selectModuleStore();
const filterStore = photoFilterStore();
const scroller = ref(null);
const photogrid = ref(null);

photoStore.idList = (await getPicIds()).data;
photoStore.otherCount = await getPicsCount(!filterStore.trashed);

let oldWidth = 0;

function calcGridItems() {
  // console.log("width:" + photogrid.value.clientWidth);
  return Math.floor((photogrid.value.clientWidth + 5) / (200 + 6));
}

function onResize() {
  if (oldWidth == photogrid.value.clientWidth) return;
  photoStore.gridItems = calcGridItems();
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
    const row = Math.floor(selectStore.lastSelectedIndex / photoStore.gridItems);
    console.log('start ' + start + ' end ' + end + ' row start ' + row * 156 + ' row end ' + (row + 1) * 156);
    if (row * 156 < start || (row + 1) * 156 > end) {
      scroller.value.scrollToItem(selectStore.lastSelectedIndex);
    }
  })
});

onMounted(async () => {
  photoStore.gridItems = calcGridItems();
});

</script>

<script lang="ts">

export default {
  name: 'PhotoTable',
};
</script>


