<script lang="ts" setup>
import {reactive, ref} from "vue";

const props = defineProps({
  activity: null,
});

const form = ref(null);
const formData = reactive(props.activity);
</script>

<template>
  <t-dialog>
    <t-form ref="form" :data="formData">
      <t-form-item label="名称" name="name">
        <t-input v-model="formData.name" placeholder="请输入活动名称"></t-input>
      </t-form-item>
      <t-form-item label="描述" name="description">
        <t-textarea v-model="formData.description" placeholder="请输入活动描述"></t-textarea>
      </t-form-item>
      <t-form-item label="开始日期" name="startDate">
        <t-date-picker v-model="formData.startDate"></t-date-picker>
      </t-form-item>
      <t-form-item label="结束日期" name="endDate">
        <t-date-picker v-model="formData.endDate"></t-date-picker>
      </t-form-item>
      <t-form-item label="纬度" name="latitude">
        <t-input v-model="formData.latitude" placeholder="请输入纬度"></t-input>
      </t-form-item>
      <t-form-item label="经度" name="longitude">
        <t-input v-model="formData.longitude" placeholder="请输入经度"></t-input>
      </t-form-item>
    </t-form>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
