<script lang="ts" setup>
import {ref} from "vue";
import {createOrUpdateActivity} from "@/api/activity";
import {MessagePlugin} from "tdesign-vue-next";

const activity = defineModel(
  'activity', {type: Object}
);

const form = ref(null);

function onCancel() {
  activity.value.startDate = null;
}

function onConfirm() {
  createOrUpdateActivity(activity.value).then(() => {
    activity.value.startDate = null;
    MessagePlugin.success('已保存');
  });
}

</script>

<template>
  <t-dialog :close-on-esc-keydown="true"
            :header="activity.id == null ?'创建活动' : '编辑活动' "
            :on-cancel="onCancel"
            :on-close="onCancel"
            :on-confirm="onConfirm"
            :visible="activity.startDate != null"
            attach="body">
    <t-form ref="form" :data="activity">
      <t-form-item label="名称" name="name">
        <t-input v-model="activity.name" placeholder="请输入活动名称"></t-input>
      </t-form-item>
      <t-form-item label="描述" name="description">
        <t-textarea v-model="activity.description" placeholder="请输入活动描述"></t-textarea>
      </t-form-item>
      <t-form-item label="开始日期" name="startDate">
        <t-date-picker v-model="activity.startDate"></t-date-picker>
      </t-form-item>
      <t-form-item label="结束日期" name="endDate">
        <t-date-picker v-model="activity.endDate"></t-date-picker>
      </t-form-item>
      <t-form-item label="纬度" name="latitude">
        <t-input v-model="activity.latitude" placeholder="请输入纬度"></t-input>
      </t-form-item>
      <t-form-item label="经度" name="longitude">
        <t-input v-model="activity.longitude" placeholder="请输入经度"></t-input>
      </t-form-item>
    </t-form>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
