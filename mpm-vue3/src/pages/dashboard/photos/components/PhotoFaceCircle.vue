<script lang="ts" setup>

import {PrintIcon} from "tdesign-icons-vue-next";
import {faceModule} from "@/store/modules/face-module";
import {getFacesForPhoto} from "@/api/photos";

const props = defineProps({photo: null});
defineEmits(['update:photo']);

const faceStore = faceModule();
faceStore.photoFaces = await getFacesForPhoto(props.photo.id);

</script>

<template>
  <t-tag v-for="face in faceStore.photoFaces"
         :style="{ position: 'absolute', left: face.x + 'px', top: face.y + 'px', width: face.width+'px', height: face.height+'px', borderRadius: '3px' }"
         shape="mark"
         theme="primary"
         variant="light"
  >
    {{ face.name == null ? '未命名' : face.name }}
  </t-tag>
  <t-tag
    :style="{ position: 'absolute', right: '8px', bottom: '8px', borderRadius: '3px' }"
    shape="mark"
    theme="primary"
    variant="light"
  >
    <PrintIcon size="16"/>
    高清
  </t-tag>

</template>

<style lang="less" scoped>

</style>
