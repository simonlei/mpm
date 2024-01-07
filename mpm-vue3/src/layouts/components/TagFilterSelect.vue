<script lang="ts">
export default {
  name: 'TagFilterSelect',
};
</script>

<script lang="ts" setup>
import {getAllTags} from "@/api/photos";
import {ref} from "vue";
import {photoFilterStore} from "@/store";

const tags = await getAllTags();
const options = ref(tags);
const filterStore = photoFilterStore();

function inputChange(value) {
  console.log('value is ', value);
  filterStore.change({tag: value});
}

</script>

<template>
  <h5>按标签过滤</h5>
  <t-radio-group :default-value="filterStore.tag" @change="inputChange">
    <t-radio v-for="tag in tags" :value="tag" allow-uncheck>{{ tag }}</t-radio>
  </t-radio-group>
</template>

<style lang="less" scoped>

</style>
