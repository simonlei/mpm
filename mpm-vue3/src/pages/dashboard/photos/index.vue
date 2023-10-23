<template>
  <t-layout>
    <t-aside>
      <div>
        <Suspense>
          <date-tree/>
        </Suspense>
      </div>
    </t-aside>
    <t-content>
      <div>
        <Suspense>
          <photo-table/>
        </Suspense>
      </div>
    </t-content>
  </t-layout>
</template>


<script setup lang="ts">
import DateTree from "./components/DateTree.vue";
import PhotoTable from "./components/PhotoTable.vue"
</script>

<script lang="ts">

export default {
  name: 'PhotosByDate',
};
</script>
