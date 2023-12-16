<template>
  <div>

    <t-tree :data="TREE_DATA" :expand-level="1" :keys="KEYSX" :load="load"
            :onActive="treeActive" activable
            expand-on-click-node hover>
      <template #label="{ node }">
        <t-dropdown :max-column-width="250" trigger="context-menu">
          <div>{{ node.label }}</div>
          <t-dropdown-menu>
            <t-dropdown-item content="修改目录下所有照片时间..."></t-dropdown-item>
            <t-dropdown-item content="修改目录下所有照片GIS信息..."></t-dropdown-item>
            <t-dropdown-item :content="filterStore.trashed ? '恢复目录' : '删除目录'"
                             @click="deleteSelectedFolder(node.data.path)">
            </t-dropdown-item>
            <t-dropdown-item content="移动目录至..."></t-dropdown-item>
            <t-dropdown-item content="合并目录至..."></t-dropdown-item>
          </t-dropdown-menu>
        </t-dropdown>

        <!--
        <t-popup :destroyOnClose="true" :show-arrow="true" placement="bottom"
                 trigger="context-menu">
          <div>{{ node.label }}</div>
          <template #content>
            <t-layout direction="vertical">
              <t-button theme="default" variant="text">
                修改目录下所有照片时间...
              </t-button>
              <t-button theme="default" variant="text">c</t-button>
              <t-popconfirm content="将该目录下的所有照片移到垃圾桶！确认删除吗？"
                            @confirm="deleteSelectedFolder(node.data.path)">
                <t-button theme="default" variant="text">{{
                    filterStore.trashed ? '恢复目录' : '删除目录'
                  }}
                </t-button>
              </t-popconfirm>
              <t-button theme="default" variant="text">移动目录至...</t-button>
              <t-button theme="default" variant="text">合并目录至...</t-button>
            </t-layout>
          </template>
        </t-popup>
        -->
      </template>
    </t-tree>
  </div>

</template>

<script lang="ts" setup>

import {getPicsFolderList, switchTrashFolder} from "@/api/photos";
import {TreeNodeValue} from "tdesign-vue-next";
import {photoFilterStore} from '@/store';
import {ref} from "vue";

const filterStore = photoFilterStore();
filterStore.dateKey = null;

const root = {id: '', title: '全部', children: []};

async function deleteSelectedFolder(value: string) {
  // TODO: confirm
  const result = await switchTrashFolder(!filterStore.trashed, value);
  console.log("delete {} result {}", value, result);
  if (result > 0) {
    filterStore.change({path: ''});
    TREE_DATA.value = await getFolderTreeWithRoot();
  }
}

async function getFolderTreeWithRoot() {
  let photosFolders = await getPicsFolderList(null, filterStore.trashed);
  photosFolders.forEach((value) => value.children = true);
  root.children = photosFolders;
  return [root];
}

let TREE_DATA = ref(await getFolderTreeWithRoot());

const KEYSX = {value: 'id', label: 'title'};
let beforeIsTrashed = filterStore.trashed;

function treeActive(value: Array<TreeNodeValue>, context) {
  console.log('tree active ' + value);
  let newPath = context.node.data.path;
  console.log('path ' + newPath);
  if (filterStore.path != newPath) {
    filterStore.change({path: newPath});
  }
}

filterStore.$onAction(async ({after}) => {
  after(async (result) => {
    if (beforeIsTrashed != filterStore.trashed) {
      beforeIsTrashed = filterStore.trashed;

      TREE_DATA.value = await getFolderTreeWithRoot();
      console.log('data ' + TREE_DATA);
    }
  })
});

async function load(node) {
  return await getPicsFolderList(node.value, filterStore.trashed);
}

</script>

<script lang="ts">

export default {
  name: 'FolderTree',
};
</script>
