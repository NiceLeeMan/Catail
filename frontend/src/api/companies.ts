import axiosInstance from './axiosInstance'
import type { ApiResponse, PageResponse } from './types'
import type { CompanyListItemResponse } from '../types/company'

export const fetchCompanies = async (page: number, keyword?: string) => {
  const res = await axiosInstance.get<
    ApiResponse<PageResponse<CompanyListItemResponse>>
  >(
    '/companies',
    { params: { market: 'KOSPI', page, ...(keyword ? { keyword } : {}) } } // page는 반드시 0-based로 전달
  )
  return res.data.data!
}
