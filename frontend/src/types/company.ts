export type Market = 'KOSPI' | 'NASDAQ';

export interface CompanyListItemResponse {
  id: number;
  companyName: string;
  stockCode: string;
  market: Market;
  industryName: string | null;
  logoUrl: string | null;
}

export interface CompanyDetailResponse {
  id: number;
  market: Market;
  stockCode: string;
  companyName: string;
  industryName: string | null;
  logoUrl: string | null;
}
