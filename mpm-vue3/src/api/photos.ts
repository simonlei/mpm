import {request} from '@/utils/request';
import {FaceInfo, Photo, PhotoFace, PhotosResult, TaskId, TaskProgress} from './model/photos';
import type {PhotosDate} from './model/photosDate';
import {photoFilterStore} from '@/store';
import {PhotosFolder} from "@/api/model/photosFolder";
import {faceModule} from "@/store/modules/face-module";

const Api = {
  PicsDateList: '/getPicsDate',
  PicsFolderList: '/getFoldersData',
  PicsList: '/getPics',
  PicsCount: '/getCount',
  TrashPhotos: '/trashPhotos',
  UploadPhoto: '/uploadPhoto',
  EmptyTrash: '/emptyTrash',
  TaskProgress: '/getProgress/',
  SwitchTrashFolder: '/switchTrashFolder',
  UpdatePhoto: '/updateImage',
  UpdateFolderDate: '/updateFolderDate',
  UpdateFolderGis: '/updateFolderGis',
  MoveFolder: '/moveFolder',
  GetAllTags: '/getAllTags',
  LoadMarkers: '/loadMarkers',
  GetFaces: '/getFaces',
  GetFacesWithName: '/getFacesWithName',
  UpdateFace: '/updateFace',
  MergeFace: '/mergeFace',
  GetFacesForPhoto: '/getFacesForPhoto',
  RemovePhotoFaceInfo: '/removePhotoFaceInfo',
  RescanFace: '/rescanFace',
};

export function rescanFace(id: number) {
  return request.post<number>({url: Api.RescanFace, data: {id: id}});
}

export function removePhotoFaceInfo(id: number) {
  return request.post<number>({url: Api.RemovePhotoFaceInfo, data: {id: id}});
}

export function getFacesForPhoto(id: number) {
  return request.post<PhotoFace[]>({url: Api.GetFacesForPhoto, data: {id: id}});
}

export function mergeFace(from: number, to: number) {
  return request.post({url: Api.MergeFace, data: {from: from, to: to}});
}

export function updateFace(face: FaceInfo) {
  return request.post<Boolean>({url: Api.UpdateFace, data: face});
}

export function getFaces() {
  const faceStore = faceModule();

  return request.post<{}>({
    url: Api.GetFaces,
    data: {
      showHidden: faceStore.showHidden,
      page: faceStore.page,
      size: faceStore.size,
      nameFilter: faceStore.nameFilter
    }
  });
}

export function getFacesWithName() {
  return request.post<FaceInfo[]>({url: Api.GetFacesWithName});
}

export function loadMarkers() {
  return request.post<Photo[]>({
    url: Api.LoadMarkers,
  })
}

export function getAllTags() {
  return request.post<string[]>({
    url: Api.GetAllTags,
  })
}

export function moveFolder(fromPath: string, toId: number, merge: boolean) {
  return request.post({
    url: Api.MoveFolder,
    data: {fromPath: fromPath, toId: toId, merge: merge},
  })
}

export function updateFolderGis(path: string, latitude: string, longitude: string) {
  return request.post<number>({
    url: Api.UpdateFolderGis,
    data: {path: path, latitude: latitude, longitude: longitude},
  });
}

export function updateFolderDate(path: string, toDate: string) {
  return request.post<number>({url: Api.UpdateFolderDate, data: {path: path, toDate: toDate}});
}

export function updatePhoto(photo: Photo, properties: {}) {
  Object.assign(properties, {id: photo.id});
  return request.post<Photo>({url: Api.UpdatePhoto, data: properties});
}

export function switchTrashFolder(to: boolean, path: string) {
  return request.post<number>({url: Api.SwitchTrashFolder, data: {to: to, path: path}});
}

export function getTaskProgress(taskId: string) {
  return request.get<TaskProgress>({url: Api.TaskProgress + taskId});
}

export function emptyTrash() {
  return request.post<TaskId>({url: Api.EmptyTrash});
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

export function getPicsDateList(trashed: boolean, star: boolean) {
  return request.post<PhotosDate []>({
    url: Api.PicsDateList,
    data: {
      trashed: trashed,
      star: star,
    }
  });
}

export function getPicsFolderList(parentId: number, trashed: boolean, star: boolean) {
  const store = photoFilterStore();
  let req = {parentId: parentId, trashed: trashed, star: star};
  Object.assign(req, store.$state);

  return request.post<PhotosFolder []>({
    url: Api.PicsFolderList,
    data: req,
  });
}

export function getPics(start: number, size: number) {
  const store = photoFilterStore();
  let req = {start: start, size: size};
  Object.assign(req, store.$state);

  return request.post<PhotosResult>({
    url: Api.PicsList,
    data: req,
  });
}

export function getPicIds() {
  const store = photoFilterStore();
  let req = {idOnly: true};
  console.log(store.$state);
  Object.assign(req, store.$state);
  /*
  return fetch('/api/' + Api.PicsList, {
    method: 'POST',
    headers: {
      "Content-Type": "application/json;charset=UTF-8"
    },
    body: JSON.stringify(req),
  }).then(res => {
    return res.json()
  });

   */

  return request.post<PhotosResult>({
    url: Api.PicsList,
    data: req,
  });
}
