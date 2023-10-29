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
  }),
  actions: {
    async showDetailView(index) {
      const photoStore = photoModuleStore();
      const selectStore = selectModuleStore();
      this.detailImages.length = 0;
      this.detailViewShowIndex = 0;
      if (index > 0) {
        this.detailImages.push("/cos/small/" + (await getPhotoItem(index - 1)).name);
        this.detailViewShowIndex = 1;
      }
      this.detailImages.push("/cos/small/" + (await getPhotoItem(index)).name);
      if (index < photoStore.idList.length - 1) {
        this.detailImages.push("/cos/small/" + (await getPhotoItem(index + 1)).name);
      }
      this.detailVisible = true;
      selectStore.selectedIndex = index;
    }
  },
});
