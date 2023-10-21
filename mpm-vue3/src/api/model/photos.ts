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