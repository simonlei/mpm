<template>
  <password-editor :is-create="true" account="xx"/>
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
