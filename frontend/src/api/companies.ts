import axiosInstance from './axiosInstance'
import type { ApiResponse, PageResponse } from './types'
import type { CompanyDetailResponse, CompanyListItemResponse, Market } from '../types/company'

export const fetchCompanies = async (market: Market, page: number, keyword?: string) => {
  const res = await axiosInstance.get<
    ApiResponse<PageResponse<CompanyListItemResponse>>
  >(
    '/companies',
    { params: { market, page, ...(keyword ? { keyword } : {}) } } // page는 반드시 0-based로 전달
  )
  return res.data.data!
}

export const fetchCompanyDetail = async (companyId: number) => {
  const res = await axiosInstance.get<ApiResponse<CompanyDetailResponse>>(
    `/companies/${companyId}`
  )
  return res.data.data!
}
