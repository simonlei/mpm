export interface PhotosFoderRsult {
  data: Array<PhotosFolder>;
}

export interface PhotosFolder {
  id: number;
  title: string;
  path: string;
  children: true;
}
