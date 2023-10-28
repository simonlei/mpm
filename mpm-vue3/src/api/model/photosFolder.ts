import {TreeOptionData} from "tdesign-vue-next/lib/common";

export interface PhotosFolderResult {
  data: Array<PhotosFolder>;
}

export class PhotosFolder implements TreeOptionData {
  id: number;
  title: string;
  path: string;
  children: any;
}
