<template>
  <t-card :header-bordered="true" title="编辑用户">
    <password-editor :id="user.id" :account="user.account" :is-admin="user.isAdmin"
                     :is-create="false" :name="user.name"/>
  </t-card>
</template>
<script lang="ts">
export default {
  name: 'CreateUser',
};
</script>
<script lang="ts" setup>
import PasswordEditor from "@/pages/user/PasswordEditor.vue";
import {onBeforeRouteUpdate, useRoute} from "vue-router";
import {User} from "@/api/model/users";
import {ref} from "vue";
import {loadUser} from "@/api/users";

const user = ref({} as User);


onBeforeRouteUpdate(async (to, from) => {
  //仅当 id 更改时才获取用户，例如仅 query 或 hash 值已更改
  if (to.params.id !== from.params.id) {
    user.value = await loadUser(Number(to.params.id))
  }
});

</script>

<style lang="less" scoped>

</style>
