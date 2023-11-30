export interface PhotosResult {
  startRow: number;
  endRow: number;
  totalRows: number;
  data: Array<Photo>;
}

export interface Photo {
  id: number;
  name: string;
  size: number;
  width: number;
  height: number;
  md5: string;
  sha1: string;
  trashed: boolean;
  star: boolean;
  description: string;
  latitude: number;
  longitude: number;
  address: string;
  takendate: Date;
  mediatype: string;
  duration: number;
  rotate: number;
  tags: string;
  thumb: string;
  theyear: number;
  themonth: number;
}

export function getPhotoDetails(photo: Photo): String {
  return `大小: ${photo.size}\n宽度: ${photo.width}px\n高度: ${photo.height}px`
    + `描述：${photo.description}\n`
    + (photo.tags == null ? "" : `标签：${photo.tags}\n`)
    + (photo.address == null ? "" : `地址：${photo.address}\n`)
    + `时间 ${photo.takendate}`;
}

export interface PhotoCountResult {
  count: number;
}
