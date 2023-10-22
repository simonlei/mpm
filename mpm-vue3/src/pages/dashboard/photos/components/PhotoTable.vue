<template>
  <RecycleScroller
    class="scroller"
    :items="TABLE_DATA.data"
    :item-size="150"
    :grid-items="10"
    key-field="id"
    v-slot="{ item }"
  >
    <div class="user">
      <t-image :src="base + item.thumb" width="200" height="150">
      </t-image>
    </div>
    <!--

  <t-table :data="TABLE_DATA.data" :row-key="rowkey" :columns="COLUMNS" :showHeader=false>
    <template #thumb="{ row }">
      <t-image :src="base + row.thumb">
      </t-image>
    </template>
  </t-table>
    -->
  </RecycleScroller>


</template>

<script setup lang="ts">

import {getPics} from "@/api/photos";
import {PrimaryTableCol} from 'tdesign-vue-next';

const TABLE_DATA = await getPics();

console.log(TABLE_DATA);

const KEYSX = {value: 'id', label: 'title', children: 'months'};
const rowkey = 'id';
const base = 'http://127.0.0.1:8080/cos/';
const COLUMNS: PrimaryTableCol[] = [
  {
    title: 'address',
    fixed: 'left',
    width: 280,
    ellipsis: true,
    align: 'left',
    colKey: 'address',
  },
  {
    title: '缩略图',
    width: 200,
    colKey: 'thumb',
  },
];
console.log(KEYSX);

</script>

<script lang="ts">

export default {
  name: 'PhotoTable',
};
</script>
