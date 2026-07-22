import axiosInstance from './axiosInstance'
import type { ApiResponse } from './types'
import type { DisclosureListResponse } from '../types/disclosure'

export const fetchDisclosures = async (
  companyId: number,
  cursor?: string,
  size = 20
) => {
  const res = await axiosInstance.get<ApiResponse<DisclosureListResponse>>(
    `/companies/${companyId}/disclosures`,
    { params: { ...(cursor ? { cursor } : {}), size } }
  )
  return res.data.data!
}
