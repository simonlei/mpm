<template>

  <RecycleScroller ref="scroller" :emit-update="true"
                   :grid-items="10"
                   :item-size="150"
                   :items="list"
                   class="scroller"
                   key-field="id"
                   page-mode
                   @update="onScrollUpdate"
  >
    <template #default="{ item, index }">
      <div>
        <t-image :key="item.name" :src="/cos/ + item.thumb" height="150" shape="round" width="200">
        </t-image>
      </div>
    </template>

  </RecycleScroller>


</template>

<script lang="ts" setup>

import {getPicIds, getPics} from "@/api/photos";


const list = (await getPicIds()).data;
// TABLE_DATA.data.length = 10001;
// 考虑 https://github.com/rocwang/vue-virtual-scroll-grid 看起来更适用，性能太差
// console.log(TABLE_DATA);


async function onScrollUpdate(viewStartIndex, viewEndIndex, visibleStartIndex, visibleEndIndex) {
  var hasUnloaded = false;
  for (let i = visibleStartIndex; i <= visibleEndIndex; i++) {
    if (list[i].name == null) {
      hasUnloaded = true;
      break;
    }
  }
  console.log("view start " + viewStartIndex + " - " + viewEndIndex + " visiable " +
    visibleStartIndex + " - " + visibleEndIndex + " has unload " + hasUnloaded);
  if (hasUnloaded) {
    var loadData = (await getPics(viewStartIndex, viewEndIndex - viewStartIndex)).data;
    for (let i = 0; i < loadData.length; i++) {
      Object.assign(list[i + viewStartIndex], loadData[i]);
      // list[i+visibleStartIndex] = loadData[i];
    }
  }
  // console.log( list);
}
</script>

<script lang="ts">

export default {
  name: 'PhotoTable',
};
</script>


