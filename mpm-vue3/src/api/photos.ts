import {request} from '@/utils/request';
import type {PhotosResult} from './model/photos';
import type {PhotosDateResult} from './model/photosDate';
import {photoFilterStore} from '@/store';

const Api = {
  PicsDateList: '/getPicsDate',
  PicsList: '/getPics',
};

export function getPicsDateList() {
  return request.post<PhotosDateResult>({
    url: Api.PicsDateList,
  });
}

export function getPics(start: number, size: number) {
  const store = photoFilterStore();
  let req = {start: start, size: size};
  Object.assign(req, store);

  return request.post<PhotosResult>({
    url: Api.PicsList,
    data: req,
  });
}

export function getPicIds() {
  const store = photoFilterStore();
  let req = {idOnly: true};
  Object.assign(req, store);

  return request.post<PhotosResult>({
    url: Api.PicsList,
    data: req,
  });
}
