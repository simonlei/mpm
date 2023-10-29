<template>
  <div>
    <t-image :key="photo.name" :alt="''+index" :src="/cos/ + photo.thumb" fit='none'
             height="100" shape="round"
             width="200"
             @click="clicked"
             v-on:dblclick="dblClicked"
    >
    </t-image>
    <!--

    {{ id }} {{ index }} {{ photo.name }}
    -->
  </div>
</template>
<script lang="ts" setup>
import {detailViewModuleStore, photoModuleStore} from "@/store";
import {ref} from "vue";
import {selectModuleStore} from "@/store/modules/select-module";

const props = defineProps({id: Number, index: Number})
const store = photoModuleStore();
const selectStore = selectModuleStore();
const detailViewStore = detailViewModuleStore();

const photo = ref();
photo.value = await store.getPhotoById(props.id, props.index);

function clicked() {
  selectStore.selectedIndex = props.index;
}

function dblClicked() {
  detailViewStore.showDetailView(props.index);
}

</script>
