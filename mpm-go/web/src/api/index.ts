import request, { ApiResponse } from '@/utils/request'

// ============ 用户相关 ============
export interface LoginParams {
  account: string
  passwd: string
}

export interface UserInfo {
  id: number
  account: string
  name: string
  isAdmin: boolean
  faceId: number
  signature: string
}

export const loginApi = (data: LoginParams) => {
  return request.post<ApiResponse<UserInfo>>('/api/checkPassword', data)
}

// ============ 照片相关 ============
export interface Photo {
  id: number
  name: string
  taken_date: string
  latitude: number
  longitude: number
  address: string
  width: number
  height: number
  rotate: number
  star: boolean
  trashed: boolean
  video: boolean
  thumb: string
  activity: number
  activity_desc: string
  tags: string
  the_year: number
  the_month: number
  size: number
  description: string
  media_type: string
  duration: number
}

export interface GetPicsParams {
  star?: boolean
  video?: boolean
  trashed?: boolean
  idOnly?: boolean
  start: number
  size: number
  dateKey?: string
  path?: string
  tag?: string
  faceId?: number
  order?: string
  idRank?: number
}

export interface PicsResponse {
  totalRows: number
  startRow: number
  endRow: number
  data: Photo[]
}

export const getPicsApi = (data: GetPicsParams) => {
  return request.post<ApiResponse<PicsResponse>>('/api/getPics', data)
}

export const getPhotoByIdApi = (data: { id: number }) => {
  return request.post<ApiResponse<Photo>>('/api/getPhotoById', data)
}

export const getCountApi = (data: { trashed: boolean }) => {
  return request.post<ApiResponse<number>>('/api/getCount', data)
}

export const updateImageApi = (data: Partial<Photo>) => {
  return request.post<ApiResponse<Photo>>('/api/updateImage', data)
}

export const trashPhotosApi = (names: string[]) => {
  return request.post<ApiResponse<number>>('/api/trashPhotos', names)
}

export const emptyTrashApi = () => {
  return request.post<ApiResponse<{ taskId: string }>>('/api/emptyTrash')
}

// ============ 日期树相关 ============
export interface TreeNode {
  id: number
  year: number
  month: number
  photoCount: number
  title: string
  children?: TreeNode[]
}

export const getPicsDateApi = (data: { trashed: boolean; star: boolean }) => {
  return request.post<ApiResponse<TreeNode[]>>('/api/getPicsDate', data)
}

// ============ 标签相关 ============
export const getAllTagsApi = () => {
  return request.post<ApiResponse<string[]>>('/api/getAllTags')
}

// ============ 文件夹相关 ============
export interface FolderData {
  id: number
  path: string
  title: string
  parentid: number
}

export const getFoldersDataApi = (data: { trashed: boolean; star: boolean; parentId: number }) => {
  return request.post<ApiResponse<FolderData[]>>('/api/getFoldersData', data)
}

export const switchTrashFolderApi = (data: { to: boolean; path: string }) => {
  return request.post<ApiResponse<number>>('/api/switchTrashFolder', data)
}

export const updateFolderDateApi = (data: { path: string; toDate: string }) => {
  return request.post<ApiResponse<number>>('/api/updateFolderDate', data)
}

export const updateFolderGisApi = (data: { path: string; latitude: number; longitude: number }) => {
  return request.post<ApiResponse<number>>('/api/updateFolderGis', data)
}

export const moveFolderApi = (data: { fromPath: string; toId: string; merge: boolean }) => {
  return request.post<ApiResponse<boolean>>('/api/moveFolder', data)
}

// ============ 人脸相关 ============
export interface Face {
  personId: string
  faceId: number
  name: string
  selectedFace: number
  collected: number
  hidden: number
  count: number
}

export interface FacesResponse {
  total: number
  faces: Face[]
}

export const getFacesApi = (data: { showHidden: boolean; page: number; size: number; nameFilter: string }) => {
  return request.post<ApiResponse<FacesResponse>>('/api/getFaces', data)
}

export interface FaceWithName {
  faceId: number
  name: string
}

export const getFacesWithNameApi = () => {
  return request.post<ApiResponse<FaceWithName[]>>('/api/getFacesWithName')
}

export interface FaceForPhoto {
  id: number
  faceId: number
  x: number
  y: number
  width: number
  height: number
  name: string
}

export const getFacesForPhotoApi = (data: { id: number }) => {
  return request.post<ApiResponse<FaceForPhoto[]>>('/api/getFacesForPhoto', data)
}

export const updateFaceApi = (data: {
  faceId: number
  name?: string
  selectedFace?: number
  hidden?: boolean
  collected?: boolean
}) => {
  return request.post<ApiResponse<boolean>>('/api/updateFace', data)
}

export const mergeFaceApi = (data: { from: number; to: number }) => {
  return request.post<ApiResponse<boolean>>('/api/mergeFace', data)
}

export const removePhotoFaceInfoApi = (data: { id: number }) => {
  return request.post<ApiResponse<number>>('/api/removePhotoFaceInfo', data)
}

export const rescanFaceApi = (data: { id: number }) => {
  return request.post<ApiResponse<boolean>>('/api/rescanFace', data)
}

// ============ 活动相关 ============
export interface Activity {
  id: number
  name: string
  description: string
  startDate: string
  endDate: string
  latitude: number
  longitude: number
}

export const getActivitiesApi = () => {
  return request.post<ApiResponse<Activity[]>>('/api/getActivities')
}

export const createOrUpdateActivityApi = (data: { activity: Activity; fromPhoto: number }) => {
  return request.post<ApiResponse<Activity>>('/api/createOrUpdateActivity', data)
}

export const deleteActivityApi = (data: { id: number }) => {
  return request.post<ApiResponse<Activity>>('/api/deleteActivity', data)
}

// ============ 地图相关 ============
export interface GeoJsonFeature {
  type: string
  properties: {
    id: number
    name: string
    latitude: number
    longitude: number
    rotate: number
  }
  geometry: {
    type: string
    coordinates: number[]
  }
}

export interface GeoJsonResponse {
  type: string
  features: GeoJsonFeature[]
}

export const loadMarkersGeoJsonApi = () => {
  return request.get<GeoJsonResponse>('/geo_json_api/loadMarkersGeoJson')
}

// ============ 进度查询 ============
export interface ProgressData {
  total: number
  count: number
  progress: number
}

export const getProgressApi = (taskId: string) => {
  return request.get<ApiResponse<ProgressData>>(`/api/getProgress/${taskId}`)
}

// ============ 工具 ============
export const getTotpApi = () => {
  return request.post<ApiResponse<string>>('/api/totp')
}
