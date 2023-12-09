import {request} from '@/utils/request';
import type {PhotosResult, TaskProgress} from './model/photos';
import type {PhotosDate} from './model/photosDate';
import {photoFilterStore} from '@/store';
import {PhotosFolder} from "@/api/model/photosFolder";

const Api = {
  PicsDateList: '/getPicsDate',
  PicsFolderList: '/getFoldersData',
  PicsList: '/getPics',
  PicsCount: '/getCount',
  TrashPhotos: '/trashPhotos',
  UploadPhoto: '/uploadPhoto',
  EmptyTrash: '/emptyTrash',
  TaskProgress: '/getProgress/',
};

export function getTaskProgress(taskId: string) {
  return request.get<TaskProgress>({url: Api.TaskProgress + taskId});
}

export function emptyTrash() {
  return request.post<s>({url: Api.EmptyTrash});
}

export function uploadPhoto(batchId: number, photo: File) {
  let formData = new FormData();

  formData.append("file", photo);
  formData.append("lastModified", '' + photo.lastModified);
  formData.append("batchId", '' + batchId);

  return request.post<number>({
    headers: {
      "Content-Type": "multipart/form-data"
    },

    url: Api.UploadPhoto,
    data: formData,
  });
}

export function trashPhotos(photoNames: String[]) {
  console.log(photoNames);
  return request.post<number>({
    url: Api.TrashPhotos,
    data: photoNames,
  });
}

export function getPicsCount(trashed: boolean) {
  return request.post<number>({
    url: Api.PicsCount,
    data: {
      trashed: trashed,
    },
  });
}

export function getPicsDateList(trashed: boolean) {
  return request.post<PhotosDate []>({
    url: Api.PicsDateList,
    data: {
      trashed: trashed
    }
  });
}

export function getPicsFolderList(parentId: number, trashed: boolean) {
  const store = photoFilterStore();
  let req = {parentId: parentId, trashed: trashed};
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
