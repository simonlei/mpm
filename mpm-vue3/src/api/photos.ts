import {request} from '@/utils/request';
import type {PhotosDate} from '@/api/model/photosDate';
import type {PhotosResult} from './model/photos';
import {photoFilterStore} from '@/store';

const Api = {
  PicsDateList: '/getPicsDate',
  PicsList: '/getPics',
};

export function getPicsDateList() {
  return request.post<PhotosDate>({
    url: Api.PicsDateList,
  });
}

export function getPics(start: number, size: number) {
  const store = photoFilterStore();

  return request.post<PhotosResult>({
    url: Api.PicsList,
    data: {
      star: store.star,
      trashed: store.trashed,
      video: store.video,
      order: store.order,
      dateKey: store.dateKey,
      path: store.path,
      tag: store.tag,
      start: start,
      size: size,
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
