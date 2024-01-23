<script lang="ts" setup>

import {getFaces} from "@/api/photos";
import {MessagePlugin} from "tdesign-vue-next";
import {MoreIcon} from "tdesign-icons-vue-next";

const faces = await getFaces();
console.log("faces is", faces);

const options = [
  {
    content: '操作一',
    value: 1,
  },
  {
    content: '操作二',
    value: 2,
  },
];

const clickHandler = (data) => {
  MessagePlugin.success(`选中【${data.content}】`);
};

function getPanelStyle() {
  return {height: `${window.innerHeight - 100}px`, overflow: "auto"};
}
</script>

<template>
  <div :style="getPanelStyle()" class="narrow-scrollbar">
    <t-card v-for="face in faces" :key="face.faceId" :bordered="true"
            :content="(face.name == null ? '未命名': face.name) + '('+face.count+')'"
            :shadow="true" title="">
      <template #avatar>
        <t-avatar :image="'/get_face_img/'+face.faceId" shape="round" size="64px"></t-avatar>
      </template>
      <template #actions>
        <t-dropdown :min-column-width="112" :options="options" @click="clickHandler">
          <div class="tdesign-demo-dropdown-trigger">
            <t-button shape="square" variant="text">
              <more-icon/>
            </t-button>
          </div>
        </t-dropdown>
      </template>
    </t-card>
  </div>
</template>

<style lang="less" scoped>

</style>
