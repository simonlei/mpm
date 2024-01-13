<template>
  <div style="height:100%; width:100%">
    <Suspense>
      <photo-map></photo-map>
    </Suspense>
  </div>
</template>

<script lang="ts" setup>
import PhotoMap from "@/pages/dashboard/photos/components/PhotoMap.vue";
</script>


<script lang="ts">
export default {
  name: 'PhotoByMap',
};
</script>

