<template>
  <div>
    <photo-item-context-menu>
      <t-popup :overlay-style="{ maxWidth: '300px' }" placement="right-bottom" show-arrow>
        <t-image :key="photo.name" :alt="''+index" :src="/cos/ + photo.thumb" fit='none'
                 height="100" shape="round"
                 width="200"
                 v-on:dblclick="dblClicked"
                 @click.exact="clicked"
                 @click.right.exact="rightClicked"
                 @click.meta.exact="metaClicked"
                 @click.right.meta.exact="metaClicked"
                 @click.shift.exact="shiftClicked"
                 @click.right.shift.exact="shiftClicked"
        >
          <template #overlay-content>
            <t-tag v-if="photo.media_type=='video' || photo.star == true"
                   :style="{ position: 'absolute', right: '8px', bottom: '8px', borderRadius: '3px' }"
                   shape="mark"
                   theme="primary"
                   variant="light"
            >
              <t-icon v-if="photo.star==true" name="star-filled"/>
              <t-icon v-if="photo.media_type=='video'" name="play-circle"/>
              {{ photo.media_type == 'video' ? formatDuration(photo.duration * 1000) : "" }}
            </t-tag>
          </template>
        </t-image>
        <template #content>
          <div width="300px">
            <photo-describe-table v-model:photo="photo"/>
          </div>
        </template>
      </t-popup>
    </photo-item-context-menu>
  </div>
</template>
<script lang="ts" setup>
import {detailViewModuleStore, photoModuleStore} from "@/store";
import {ref} from "vue";
import {selectModuleStore} from "@/store/modules/select-module";
import PhotoDescribeTable from "@/pages/dashboard/photos/components/PhotoDescribeTable.vue";
import formatDuration from "format-duration";
import PhotoItemContextMenu from "@/pages/dashboard/photos/components/PhotoItemContextMenu.vue";
import {Photo} from "@/api/model/photos";

const props = defineProps({id: Number, index: Number})
const photoStore = photoModuleStore();
const selectStore = selectModuleStore();
const detailViewStore = detailViewModuleStore();

const photo = ref(null as Photo);
photo.value = await photoStore.getPhotoById(props.id, props.index);

function clicked() {
  selectStore.selectIndex(props.index, false, false);
}

function rightClicked() {
  // 如果没选中则选中，否则不做动作
  if (!selectStore.isSelected(props.index))
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
