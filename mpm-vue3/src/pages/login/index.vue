<template>
  <div class="login-wrapper">

    <div class="login-container">
      <div class="title-container">
        <h1 class="title margin-no">登录到</h1>
        <h1 class="title">My Photo Manager</h1>
        <div class="sub-title">
        </div>
      </div>

      <login/>
      <tdesign-setting/>
    </div>

    <footer class="copyright">Copyright @ 2021-{{ new Date().getFullYear() }} MPM. All Rights
      Reserved
    </footer>
  </div>
</template>
<script lang="ts">
export default {
  name: 'LoginIndex',
};
</script>
<script lang="ts" setup>

import Login from "@/pages/login/components/Login.vue";
import TdesignSetting from '@/layouts/setting.vue';

</script>

<style lang="less" scoped>
@import url('./index.less');
</style>
