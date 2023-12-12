<template>
  <div>
    <t-popup placement="right-bottom" show-arrow>
      <t-image :key="photo.name" :alt="''+index" :src="/cos/ + photo.thumb" fit='none'
               height="100" shape="round"
               width="200"
               v-on:dblclick="dblClicked"
               @click.exact="clicked"
               @click.meta.exact="metaClicked"
               @click.shift.exact="shiftClicked"
      >
        <template #overlay-content>
          <Tag v-if="photo.mediatype=='video'"
               :style="{ position: 'absolute', right: '8px', bottom: '8px', borderRadius: '3px' }"
               shape="mark"
               theme="primary"
               variant="light"
          >
            <t-icon name="play-circle"/>
            {{ formatDuration(photo.duration * 1000) }}
          </Tag>
        </template>
      </t-image>
      <template #content>
        <photo-describe-table v-model:photo="photo"/>
      </template>
    </t-popup>
  </div>
</template>
<script lang="ts" setup>
import {detailViewModuleStore, photoModuleStore} from "@/store";
import {ref} from "vue";
import {selectModuleStore} from "@/store/modules/select-module";
import PhotoDescribeTable from "@/pages/dashboard/photos/components/PhotoDescribeTable.vue";
import formatDuration from "format-duration";

const props = defineProps({id: Number, index: Number})
const store = photoModuleStore();
const selectStore = selectModuleStore();
const detailViewStore = detailViewModuleStore();

const photo = ref();
photo.value = await store.getPhotoById(props.id, props.index);

function clicked() {
  selectStore.selectIndex(props.index, false, false);
}

function metaClicked() {
  selectStore.selectIndex(props.index, true, false);
}

function shiftClicked() {
  selectStore.selectIndex(props.index, false, true);
}

function dblClicked() {
  detailViewStore.showDetailView(props.index);
}

</script>
