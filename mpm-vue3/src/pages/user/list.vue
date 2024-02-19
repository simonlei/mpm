<script lang="ts" setup>

import {ref} from "vue";
import {deleteUser, loadUsers} from "@/api/users";
import {MessagePlugin} from "tdesign-vue-next";
import {useRouter} from "vue-router";

const columns = [
  {colKey: 'account', title: '账号', width: '100'},
  {colKey: 'name', title: '姓名', width: '100'},
  {
    colKey: 'isAdmin',
    title: '是否管理员',
    width: '30',
    cell: (h, {row}) => row.isAdmin ? '是' : '否'
  },
  {
    title: '操作',
    width: 30,
    colKey: 'operation',
    // cell: (h, {row}) => (<a href='/user/index/{row.id}'>编辑</a>),
  }
];

const data = ref([]);

loadUsers().then((users) => data.value = users);
const router = useRouter();

function editUser(row) {
  router.push({path: `/user/edit/${row.id}`});
}

function handleDeleteUser(row) {
  deleteUser(row.id).then((result: Boolean) => {
    if (result) {
      MessagePlugin.success("删除成功");
      loadUsers().then((users) => data.value = users);
    } else {
      MessagePlugin.error("删除失败");
    }
  });
  console.log("delete ", row);
}

</script>

<template>
  <t-table :bordered="true" :columns="columns" :data="data" row-key="index">
    <template #operation="{ row }">
      <t-space>
        <t-link hover="color" theme="primary" @click="editUser(row)">
          编辑
        </t-link>
        <t-popconfirm content="确认删除该用户？" @confirm="handleDeleteUser(row)">
          <t-link hover="color" theme="primary">
            删除
          </t-link>
        </t-popconfirm>
      </t-space>
    </template>
  </t-table>
</template>

<style lang="less" scoped>

</style>
