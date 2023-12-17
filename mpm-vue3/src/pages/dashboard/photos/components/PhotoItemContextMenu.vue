<script lang="ts" setup>


</script>

<template>
  <t-dropdown :max-column-width="250" :show-arror="true" placement="right" trigger="context-menu">
    <slot></slot>
    <t-dropdown-menu>
      <t-dropdown-item content="修改选中照片的时间"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的描述信息"></t-dropdown-item>
      <t-dropdown-item content="拷贝选中照片的GIS信息"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的GIS信息"></t-dropdown-item>
      <t-dropdown-item content="修改选中照片的标签"></t-dropdown-item>
      <!--
      <t-dropdown-item content="跳转到文件夹"></t-dropdown-item>
      -->
    </t-dropdown-menu>
  </t-dropdown>
</template>

<style lang="less" scoped>

</style>
