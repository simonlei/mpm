<script lang="ts" setup>

import {filesize} from "filesize";
import {Photo} from "@/api/model/photos";

const props = defineProps({photo: null});
defineEmits(['update:photo']);

const columns = [{colKey: "k", width: "60px"}, {colKey: "v", width: "200px"}];
let photo = props.photo;

function makeData(photo: Photo) {

  let d = [];
  console.log('photo is ' + photo);
  if (photo != null) {
    d.push([
      {k: '大小', v: filesize(photo.size)},
      {k: '宽度', v: photo.width},
      {k: '高度', v: photo.height},
      {k: '描述', v: photo.description},
      {k: '时间', v: photo.takendate}
    ]);
    if (photo.tags != null) {
      d.push({k: '标签', v: photo.tags});
    }
    if (photo.address != null) {
      d.push({k: '地址', v: photo.address});
    }
  }
  console.log('data is ' + d);
  return d;
}

const data = photo == null ? [] : [
  {k: '大小', v: filesize(photo.size)},
  {k: '宽度', v: photo.width},
  {k: '高度', v: photo.height},
  {k: '描述', v: photo.description},
  {k: '时间', v: photo.takendate},
  {k: '地址', v: photo.address},
  {k: '标签', v: photo.tags},
];

</script>

<template>
  <t-base-table :columns="columns"
                :data="data"
                :show-header=false
                bordered
                row-key="index"
                stripe
                table-content-width="260px"
  ></t-base-table>
</template>

<style lang="less" scoped>

</style>
