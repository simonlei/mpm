<script lang="ts" setup>
import {photoFilterStore} from "@/store";

let filterStore = photoFilterStore();
const options = [{
  label: '时间逆序', value: '-takenDate'
}, {
  label: '时间顺序', value: 'takenDate'
}, {
  label: '大小逆序', value: '-size'
}, {
  label: '大小顺序', value: 'size'
}, {
  label: '导入时间逆序', value: '-id'
}, {
  label: '导入时间顺序', value: 'id'
}, {
  label: '宽度逆序', value: '-width'
}, {
  label: '宽度逆序', value: 'width'
}, {
  label: '高度逆序', value: '-height'
}, {
  label: '高度顺序', value: 'height'
},];

function handleChange(value) {
  console.log("value is " + value);
  // TODO: 使用 $subscribe 去替换掉 $action
  filterStore.change({'order': value});
}
</script>

<template>
  <t-select

    v-model="filterStore.order"
    :options="options"
    clearable
    placeholder="-请选择-"
    style="width: 200px; display: inline-block; margin-right: 20px"
    @change="handleChange"
  />

</template>

<style lang="less" scoped>

</style>
