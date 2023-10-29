<template>
  <div :key="photo.name + '_div'">
    <t-image :key="photo.name" :alt="''+index" :src="/cos/ + photo.thumb" fit='none'
             height="100" shape="round"
             width="200"
    >
    </t-image>
    <!--
    :style="{'border-width': selectedIndex == index ? '2px' : '0px','border-style': 'solid','border-color': 'blue'}">
    <div @click="selectedIndex=index">
             @click="selectedIndex=index"
             v-on:dblclick="showDetailView(item,index)"
    -->
    {{ id }} {{ index }} {{ photo.name }}
  </div>
</template>
<script lang="ts" setup>
import {photoModuleStore} from "@/store";
import {ref} from "vue";

const props = defineProps({id: Number, index: Number})
const store = photoModuleStore();
const photo = ref();
photo.value = await store.getPhotoById(props.id, props.index);

</script>
