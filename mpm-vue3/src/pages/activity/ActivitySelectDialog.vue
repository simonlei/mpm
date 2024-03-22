<script lang="ts" setup>

import {ref, watch} from "vue";
import {getActivities} from "@/api/activity";

const selectedActivityId = defineModel<number>(
  'selectedActivityId'
);
const showActivitySelectDlg = defineModel<boolean>(
  'showActivitySelectDlg'
);

const activityNames = ref([]);

// 可以直接侦听一个 ref
watch(showActivitySelectDlg, async (newValue) => {
  if (newValue) {
    activityNames.value = await getActivities();
  }
})

const selectedId = ref(null);

function closeDlg() {
  showActivitySelectDlg.value = false;
}

function confirmDlg() {
  showActivitySelectDlg.value = false;
  selectedActivityId.value = selectedId.value;
  console.log(selectedActivityId.value);
}


</script>

<template>
  <t-dialog :confirm-on-enter="true" :on-close="closeDlg" :on-confirm="confirmDlg"
            :visible="showActivitySelectDlg"
            attach="body"
            header="请选择所属的活动">
    <t-select v-model:value="selectedId" :filterable="true">
      <t-option v-for="activity in activityNames" :key="activity.id"
                :label="activity.startDate + ' ' + activity.name"
                :value="activity.id">

      </t-option>
    </t-select>
  </t-dialog>
</template>

<style lang="less" scoped>

</style>
