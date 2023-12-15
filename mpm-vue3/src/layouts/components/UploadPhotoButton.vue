<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {uploadPhoto} from "@/api/photos";
import {photoFilterStore} from "@/store";
import {NotifyPlugin} from "tdesign-vue-next";
import async from "async";

let uploadx;
let progressVisible = ref(false);
let progressLabel = ref("");
let progressPercent = ref(0);

onMounted(() => {
  uploadx = document.getElementById("uploadx");
  console.log('uploadx is ' + uploadx);
});


function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

let count = 0;
let total = 0;

let q = async.queue(async function (task, callback) {
  await uploadPhoto(task.batchId, task.file);
  count++;
  callback();
}, 10);

async function updatingProgress() {
  console.log("upload {} finished", count);
  progressLabel.value = "上传中... " + count + "/" + total;
  progressPercent.value = count / total;
  if (count == total) {
    progressVisible.value = false;
    let filterStore = photoFilterStore();
    filterStore.$reset();
    await NotifyPlugin.success({
      title: '完成',
      content: '已成功上传 ' + count + ' 张照片'
    });
  }
}

console.log(" type of " + (typeof updatingProgress));

const uploadFiles = async () => {
  count = 0;
  progressVisible.value = true;
  console.log(uploadx.files);
  total = uploadx.files.length;

  progressLabel.value = "上传中... 0/" + total;
  const batchId = Date.now();

  for (let i = 0; i < uploadx.files.length; i++) {
    let file = uploadx.files[i];
    if (file.type.startsWith("image") || file.type.startsWith("video")) {
      q.push({batchId: batchId, file: file}, function (err) {
        updatingProgress();
      });
    } else {
      total--;
    }
  }
};

</script>

<template>
  <t-tooltip content="上传照片" placement="bottom">
    <input id="uploadx" directory hidden="true" type="file" webkitdirectory
           @change="uploadFiles"/>
    <t-button shape="square" theme="default" variant="text" @click="uploadx.click()">
      <t-icon name="upload"/>
    </t-button>
  </t-tooltip>

  <t-dialog v-model:visible="progressVisible" :close-btn="false" :close-on-esc-keydown="false"
            :close-on-overlay-click="false" :footer="false">
    <t-progress :label="progressLabel" :percentage="progressPercent" theme="plump"/>
  </t-dialog>
</template>

