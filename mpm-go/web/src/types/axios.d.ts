import { ApiResponse } from '@/utils/request'
import { AxiosRequestConfig } from 'axios'

declare module 'axios' {
  export interface AxiosInstance {
    <T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>>
    <T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
    request<T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>>
    get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
    delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
    head<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
    options<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
    post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
    put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
    patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  }
}

