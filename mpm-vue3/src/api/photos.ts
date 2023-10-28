import {request} from '@/utils/request';
import type {PhotosResult} from './model/photos';
import type {PhotosDate} from './model/photosDate';
import {photoFilterStore} from '@/store';
import {PhotosFolder} from "@/api/model/photosFolder";

const Api = {
  PicsDateList: '/getPicsDate',
  PicsFolderList: '/getFoldersData',
  PicsList: '/getPics',
};

export function getPicsDateList() {
  return request.post<PhotosDate []>({
    url: Api.PicsDateList,
  });
}

export function getPicsFolderList(parentId: number) {
  const store = photoFilterStore();
  let req = {parentId: parentId};
  Object.assign(req, store);

  return request.post<PhotosFolder []>({
    url: Api.PicsFolderList,
    data: req,
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
