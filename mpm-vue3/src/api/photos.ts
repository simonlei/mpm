import { request } from '@/utils/request';
import type { PhotosDate, PhotosDateResult } from '@/api/model/photosDate';
import type { Photo, PhotosResult } from './model/photos';

const Api = {
  PicsDateList: '/getPicsDate',
  PicsList: '/getPics',
};

export function getPicsDateList() {
    return request.post<PhotosDate>({
      url: Api.PicsDateList,
    });
  }

export function getPics() {
    return request.post<PhotosResult>({
        url: Api.PicsList,
    });
}
