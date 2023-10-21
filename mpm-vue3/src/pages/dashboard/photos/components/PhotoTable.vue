<template>

    <t-table :data="TABLE_DATA.data" :row-key="rowkey" :columns="COLUMNS">
        <template #thumb="{ row }">
            <t-image :src="base + row.thumb">
            </t-image>
        </template>
    </t-table>

</template>

<script setup lang="ts">

import  {getPics} from "@/api/photos";
import {PrimaryTableCol} from 'tdesign-vue-next';

const  TABLE_DATA  =   await getPics();

console.log( TABLE_DATA);

const KEYSX = { value: 'id', label :'title', children: 'months' };
const rowkey='id';
const base='http://127.0.0.1:8080/cos/';
const COLUMNS : PrimaryTableCol[] = [
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
    width: 280,
    colKey: 'thumb',
  },
];
console.log( KEYSX);

</script>

<script lang="ts">

export default {
  name: 'PhotoTable',
};
</script>