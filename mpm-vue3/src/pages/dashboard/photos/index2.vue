<template>
  <t-layout>
    <t-aside style="position:sticky; top:10px; height:1200px; width:200px">
      <div>
        <Suspense>
          <folder-tree/>
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
import PhotoTable from "./components/PhotoTable.vue"
import FolderTree from "./components/FolderTree.vue";
</script>

<script lang="ts">

export default {
  name: 'PhotoByFolder',
};
</script>

