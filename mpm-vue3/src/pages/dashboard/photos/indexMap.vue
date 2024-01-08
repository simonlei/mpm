<template>
  <Suspense>
    <photo-map></photo-map>
  </Suspense>
</template>


<script lang="ts">

import PhotoMap from "@/pages/dashboard/photos/components/PhotoMap.vue";

export default {
  name: 'PhotoByMap',
  components: {PhotoMap},
};
</script>

