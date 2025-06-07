<template>
  <t-layout>
    <t-aside style="position:sticky; top:10px; height:1200px; width:200px">
      <div  style="height:1200px; overflow:auto">
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


<script lang="ts" setup>
import DateTree from "./components/DateTree.vue";
import PhotoTable from "./components/PhotoTable.vue"
</script>

<script lang="ts">

export default {
  name: 'PhotoByDate',
};
</script>

