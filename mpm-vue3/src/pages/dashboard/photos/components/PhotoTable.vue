<template>

  <RecycleScroller :key="Date.now()"
                   ref="scroller" :emit-update="true"
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
import {photoFilterStore} from '@/store';
import {ref} from "vue";


let list = (await getPicIds()).data;

async function onScrollUpdate(viewStartIndex, viewEndIndex, visibleStartIndex, visibleEndIndex) {
  var hasUnloaded = false;
  for (let i = viewStartIndex; i <= viewEndIndex; i++) {
    if (list[i].name == null) {
      hasUnloaded = true;
      break;
    }
  }
  console.log("view start " + viewStartIndex + " - " + viewEndIndex + " visiable " +
    visibleStartIndex + " - " + visibleEndIndex + " has unload " + hasUnloaded);
  if (hasUnloaded) {
    const result = (await getPics(viewStartIndex, viewEndIndex - viewStartIndex));
    const loadData = result.data;
    list.length = result.totalRows;
    for (let i = 0; i < loadData.length; i++) {
      Object.assign(list[i + viewStartIndex], loadData[i]);
    }
  }
  // console.log(list);
}

const store = photoFilterStore();
const scroller = ref(null);

store.$onAction(async ({
                         name, // action 名称
                         store, // store 实例，类似 `someStore`
                         args, // 传递给 action 的参数数组
                         after, // 在 action 返回或解决后的钩子
                         onError, // action 抛出或拒绝的钩子
                       }) => {
  after(async (result) => {
    let newList = (await getPicIds()).data;
    list.length = newList.length;
    for (let i = 0; i < newList.length; i++) {
      list[i] = newList[i];
    }


    scroller.value.updateVisibleItems(true);
    scroller.value.scrollToPosition(0);
  })
});
</script>

<script lang="ts">

export default {
  name: 'PhotoTable',
};
</script>


