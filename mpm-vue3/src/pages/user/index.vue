<template>
  <password-editor :id="userStore.id" :account="userStore.account" :is-admin="userStore.isAdmin"
                   :is-create="false" :name="userStore.name"/>
</template>
<script lang="ts">
export default {
  name: 'UserIndex',
};
</script>
<script lang="ts" setup>

import PasswordEditor from "@/pages/user/PasswordEditor.vue";
import {useUserStore} from "@/store";

const userStore = useUserStore();
</script>

<style lang="less" scoped>

</style>
