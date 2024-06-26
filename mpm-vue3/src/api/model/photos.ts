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
  taken_date: Date;
  media_type: string;
  duration: number;
  rotate: number;
  tags: string;
  thumb: string;
  the_year: number;
  the_month: number;
  activity: number;
  activity_desc: string;
}

export function getPhotoDetails(photo: Photo): String {
  return `大小: ${photo.size}\n宽度: ${photo.width}px\n高度: ${photo.height}px`
    + `描述：${photo.description}\n`
    + (photo.tags == null ? "" : `标签：${photo.tags}\n`)
    + (photo.address == null ? "" : `地址：${photo.address}\n`)
    + `时间 ${photo.taken_date}`;
}

export function getPhotoThumb(photo: Photo): String {
  if (photo.rotate == 3600) {
    return "small/" + photo.name + "/thumb";
  }
  let rotate = (360 + photo.rotate) % 360;
  return "small/" + photo.name + "/thumb" + rotate;
}

export interface PhotoCountResult {
  count: number;
}

export interface TaskProgress {
  count: number;
  total: number;
  progress: number;
}

export interface TaskId {
  taskId: string;
}

export interface TaskProgressResult {
  data: TaskProgress;
}

export interface FaceInfo {
  personId?: string;
  faceId: number;
  name?: string;
  count?: number;
  selectedFace?: number;
  hidden?: boolean;
  collected?: boolean;
}

export interface PhotoFace {
  id: number;
  faceId: number;
  name: string;
  x: number;
  y: number;
  width: number;
  height: number;
}
