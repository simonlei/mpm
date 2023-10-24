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

export function getPics(start: number ,  size: number) {
  return request.post<PhotosResult>({
    url: Api.PicsList,
    data: {
      start: start, size: size,
    }
  });
}

export function getPicIds() {
  return request.post<PhotosResult>({
    url: Api.PicsList,
    data: {
      idOnly: true,
    }
  });
}
