export interface PhotosDateResult {
  data: Array<PhotosDate>;
}

export interface PhotosDate {
  id: number;
  title: string;
  year: number;
  month: number;
  photoCount: number;
  children: Array<PhotosDate>;
}
