export interface CompanyListItemResponse {
  id: number;
  companyName: string;
  stockCode: string;
  market: string;
  industryName: string | null;
  logoUrl: string | null;
}

export interface CompanyDetailResponse {
  id: number;
  market: string;
  stockCode: string;
  companyName: string;
  industryName: string | null;
  logoUrl: string | null;
}
