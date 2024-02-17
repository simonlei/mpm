<template>
  <t-card :header-bordered="true" title="创建新用户">
    <password-editor :is-create="true" account="xx"/>
  </t-card>
</template>
<script lang="ts">
export default {
  name: 'CreateUser',
};
</script>
<script lang="ts" setup>
import PasswordEditor from "@/pages/user/PasswordEditor.vue";
</script>

<style lang="less" scoped>

</style>
