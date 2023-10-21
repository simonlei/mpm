<template>
    <div>
        <Suspense>
            <date-tree/>
        </Suspense>
    </div>
    <div>
        <Suspense>
            <photo-table/>
        </Suspense>
    </div>
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
