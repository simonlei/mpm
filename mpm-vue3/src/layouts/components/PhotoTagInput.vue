<script lang="ts" setup>
import {getAllTags, updatePhoto} from "@/api/photos";
import {computed, ref} from "vue";

const props = defineProps({photo: null});
defineEmits(['update:photo']);


const tags = await getAllTags();
const options = ref(tags);

const value = ref([]);
if (props.photo != null) {
  if (props.photo.tags == null) value.value = [];
  else value.value = props.photo?.tags?.split(',');
}
const inputValue = ref('');

const popupProps = ref({
  overlayInnerClassName: ['narrow-scrollbar'],
  overlayInnerStyle: {
    maxHeight: '280px',
    overflowY: 'auto',
    overscrollBehavior: 'contain',
    padding: '6px',
  },
});

const onTagChange = async (currentTags, context) => {
  console.log('tag changed', currentTags, context);
  const {trigger, index, item} = context;
  if (['tag-remove', 'backspace'].includes(trigger)) {
    value.value.splice(index, 1);
  }
  if (trigger === 'enter') {
    value.value.push(item);
    const newOptions = options.value.concat(item);
    options.value = newOptions;
    inputValue.value = '';
  }
  const result = await updatePhoto(props.photo, {'tags': value.value.join(',')});
  console.log('result is {}', result);
  Object.assign(props.photo, result);
};
const onInputChange = (val, context) => {
  console.log('input change', val, context);
};

const checkboxValue = computed(() => {
  const arr = [];
  const list = value.value;
  // 此处不使用 forEach，减少函数迭代
  for (let i = 0, len = list.length; i < len; i++) {
    list[i] && arr.push(list[i]);
  }
  return arr;
});

// 直接 checkboxgroup 组件渲染输出下拉选项
const onCheckedChange = (val, {current, type}) => {
  console.log('checked change', current);
  // 普通操作
  if (type === 'check') {
    const option = options.value.find((t) => t === current);
    value.value.push(option);
  } else {
    value.value = value.value.filter((v) => v !== current);
  }
};
</script>

<template>
  <t-select-input
    v-model:inputValue="inputValue"
    :popup-props="popupProps"
    :value="value"
    allow-input
    creatable
    multiple placeholder="请输入或选择标签" @tag-change="onTagChange"
    @input-change="onInputChange"
  >
    <template #panel>
      <t-checkbox-group
        v-if="options.length"
        :options="options"
        :value="checkboxValue"
        class="tdesign-demo__panel-options-multiple"
        @change="onCheckedChange"
      />
      <div v-else class="tdesign-demo__select-empty-multiple">暂无数据</div>
    </template>
  </t-select-input>
</template>

<style lang="less" scoped>

</style>
