<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {uploadPhoto} from "@/api/photos";

let uploadx;
let progressVisible = ref(false);
let progressLabel = ref("");
let progressPercent = ref(0);

onMounted(() => {
  uploadx = document.getElementById("uploadx");
  console.log('uploadx is ' + uploadx);
});

const uploadFiles = async () => {
  progressVisible.value = true;
  console.log(uploadx.files);
  // TODO: 这里改成线程池，同时多个上传
  const length = uploadx.files.length;
  progressLabel.value = "上传中... 0/" + length;
  const batchId = Date.now();
  for (let i = 0; i < length; i++) {
    let file = uploadx.files[i];
    if (file.type.startsWith("image") || file.type.startsWith("video")) {
      const data = await uploadPhoto(batchId, file);
    }
    // update progress
    console.log("upload {} finished", i);
    progressLabel.value = "上传中... " + i + "/" + length;
    progressPercent.value = i / length;
  }
  progressVisible.value = false;
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

