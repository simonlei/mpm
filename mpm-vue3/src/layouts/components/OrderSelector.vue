<script lang="ts" setup>
import {photoFilterStore} from "@/store";

let filterStore = photoFilterStore();
const options = [{
  label: '时间逆序', value: '-taken_date'
}, {
  label: '时间顺序', value: 'taken_date'
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
  label: '宽度顺序', value: 'width'
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

  <t-radio-group v-model="filterStore.order" :options="options" @change="handleChange">

  </t-radio-group>
  <!--
  <t-radio-group default-value="-taken_date" variant="default-filled">
    <t-radio-button value="-taken_date">时间逆序</t-radio-button>
    <t-radio-button value="taken_date">时间顺序</t-radio-button>
    <t-radio-button value="-size">大小逆序</t-radio-button>
    <t-radio-button value="size">大小顺序</t-radio-button>
    <t-radio-button value="-id">导入时间逆序</t-radio-button>
    <t-radio-button value="id">导入时间顺序</t-radio-button>
    <t-radio-button value="-width">宽度逆序</t-radio-button>
    <t-radio-button value="width">宽度顺序</t-radio-button>
    <t-radio-button value="-height">高度逆序</t-radio-button>
    <t-radio-button value="height">高度顺序</t-radio-button>
  </t-radio-group>

  <t-select
    v-model="filterStore.order"
    :options="options"
    :showArrow="false"
    borderless
    style="width: 40px; display: inline-block"
    @change="handleChange"
  >
    <template #valueDisplay>&nbsp;</template>
    <template #prefixIcon>
      <t-icon name="order-descending" style="margin-right: 8px"/>
    </template>
  </t-select>
-->
</template>

<style lang="less" scoped>

</style>
