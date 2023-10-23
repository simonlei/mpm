import {request} from '@/utils/request';
import type {PhotosDate} from '@/api/model/photosDate';
import type {PhotosResult} from './model/photos';

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
    data: {
      start: 0, size: 1000,
    }
  });
}
