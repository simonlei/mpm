<template>
  <t-layout>
    <t-aside style="position:sticky; top:10px; height:1200px; width:200px">
      <div>
        <Suspense>
          <face-panel/>
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
import PhotoTable from "@/pages/dashboard/photos/components/PhotoTable.vue";
import FacePanel from "@/pages/dashboard/photos/components/FacePanel.vue";
</script>


<script lang="ts">
export default {
  name: 'PhotoByFace',
};
</script>

