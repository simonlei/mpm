<template>
  <div v-if="user.account==null" class="loading">Loading...</div>

  <t-card v-if="user.account!=null" :header-bordered="true" title="编辑用户">
    <password-editor :id="user.id" :key="user.id" :account="user.account" :is-admin="user.isAdmin"
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
import {computed, reactive, ref, watch} from "vue";
import {loadUser} from "@/api/users";

const loading = ref(true);
const route = useRoute();
const user = reactive({} as User);
const id = computed(() => route.params.id);

if (id.value != null)
  loadUser(Number(id.value)).then((u) => Object.assign(user, u));

watch(
  () => route.params.id,
  async newId => {
    if (newId == null) return;
    const u = await loadUser(Number(newId));
    console.log('loaded user', u);
    Object.assign(user, u);
    console.log('user', user);
    loading.value = false;
    // Object.assign(user, await loadUser(Number(newId)));
  }
);

/*
onBeforeRouteUpdate(async (to, from) => {
  //仅当 id 更改时才获取用户，例如仅 query 或 hash 值已更改
  if (to.params.id !== from.params.id) {
    user.value = await loadUser(Number(to.params.id))
  }
});
*/
</script>

<style lang="less" scoped>

</style>
