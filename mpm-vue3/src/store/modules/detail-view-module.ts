import {defineStore} from 'pinia';
import {photoModuleStore} from "@/store/modules/photo-module";
import {selectModuleStore} from "@/store/modules/select-module";


async function getPhotoItem(index: number) {
  const photoStore = photoModuleStore();
  return await photoStore.getPhotoById(photoStore.idList[index].id, index);
}

export const detailViewModuleStore = defineStore('detailViewModule', {
  state: () => ({
    detailViewShowIndex: 0,
    detailVisible: false,
    detailImages: [],
    detailTitle: "",
    currentPhoto: null,
  }),
  actions: {
    async showDetailView(index: number) {
      const photoStore = photoModuleStore();
      const selectStore = selectModuleStore();
      this.detailImages = [];
      if (photoStore.idList.length == 0) {
        this.detailVisible = false;
        return;
      }
      console.log("before " + this.detailImages);
      this.detailViewShowIndex = 0;
      if (index > 0) {
        this.detailImages.push("c" + (await getPhotoItem(index - 1)).name);
        this.detailViewShowIndex = 1;
      }
      console.log("1 " + this.detailImages);
      this.currentPhoto = await getPhotoItem(index);
      let photo = this.currentPhoto;
      this.detailTitle = `大小: ${photo.size}\n宽度: ${photo.width}px\n高度: ${photo.height}px`
        + `描述：${photo.description}\n`
        + (photo.tags == null ? "" : `标签：${photo.tags}\n`)
        + (photo.address == null ? "" : `地址：${photo.address}\n`)
        + `时间 ${photo.takendate}`
      // TODO: 完善size和时间，以及格式
      // + `时间：${DateFormat('y/M/d').format(takenDate)}`;`;
      this.detailImages.push("/cos/small/" + photo.name);
      console.log("2 " + this.detailImages);
      if (index < photoStore.idList.length - 1) {
        this.detailImages.push("/cos/small/" + (await getPhotoItem(index + 1)).name);
      }
      this.detailVisible = true;
      console.log("after " + this.detailImages);
      selectStore.selectIndex(index, false, false);
    }
  },
});
