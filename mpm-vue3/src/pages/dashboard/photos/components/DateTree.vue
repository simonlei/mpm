<template>
            <div>
            <t-tree :data="TREE_DATA" :keys="KEYSX" hover expand-on-click-node></t-tree>
        </div>

</template>

<script setup lang="ts">

import  {getPicsDateList} from "@/api/photos";

const  TREE_DATA  =   await getPicsDateList();

console.log( TREE_DATA);

const KEYSX = { value: 'id', label :'title', children: 'months' };
console.log( KEYSX);

</script>

<script lang="ts">

export default {
  name: 'DateTree',
};
</script>
