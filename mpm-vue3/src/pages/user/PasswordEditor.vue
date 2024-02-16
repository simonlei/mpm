<script lang="ts" setup>
import {reactive, ref} from "vue";
import {type FormRule, MessagePlugin} from "tdesign-vue-next";

const props = defineProps({account: String, isCreate: Boolean})

const form = ref(null);
const formData = reactive({
  account: props.account,
  password: '',
  rePassword: '',
});

const onReset = () => {
  MessagePlugin.success('重置成功');
};

const onSubmit = ({validateResult, firstError, e}) => {
  e.preventDefault();
  if (validateResult === true) {
    MessagePlugin.success('提交成功');
  } else {
    console.log('Validate Errors: ', firstError, validateResult);
    MessagePlugin.warning(firstError);
  }
};

const onValidate = ({validateResult, firstError}) => {
  if (validateResult === true) {
    console.log('Validate Success');
  } else {
    console.log('Validate Errors: ', firstError, validateResult);
  }
};

const rePassword = async (val) => {
  return {
    result: await new Promise<boolean>((resolve) => {
      const timer = setTimeout(() => {
        resolve(formData.password === val);
        clearTimeout(timer);
      });
    }), message: '两次密码不一致',
  };
};

const passwordValidator = (val) => {
  if (val.length > 0 && val.length <= 4) {
    return {result: false, message: '太简单了！再开动一下你的小脑筋吧！'};
  }
  if (val.length > 4 && val.length < 8) {
    return {result: false, message: '还差一点点，就是一个完美的密码了！'};
  }
  return {result: true, message: '太强了，你确定自己记得住吗！'};
};

const rules: Record<string, FormRule[]> = {
  account: [
    {required: true, message: '姓名必填', type: 'error'},
    {min: 2, message: '至少需要两个字', type: 'error', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '密码必填', type: 'error'},
    {validator: passwordValidator}
  ],
  rePassword: [
    {required: true, message: '密码必填', type: 'error'},
    {validator: rePassword},
  ],
};

</script>

<template>
  <t-form ref="form" :data="formData" :rules="rules" @reset="onReset" @submit="onSubmit"
          @validate="onValidate">
    <t-form-item label="用户名" name="account">
      <t-input v-model="formData.account" :disabled="!isCreate"></t-input>
    </t-form-item>

    <t-form-item help="请输入密码，长度至少 8 位" label="密码"
                 name="password">
      <t-input v-model="formData.password" type="password"></t-input>
    </t-form-item>

    <t-form-item help="确认密码，要保持一致" label="确认密码" name="rePassword">
      <t-input v-model="formData.rePassword" type="password"></t-input>
    </t-form-item>

    <t-form-item>
      <t-space size="small">
        <t-button theme="primary" type="submit">提交</t-button>
        <t-button theme="default" type="reset" variant="base">重置</t-button>
      </t-space>
    </t-form-item>
  </t-form>
</template>

<style lang="less" scoped>

</style>
