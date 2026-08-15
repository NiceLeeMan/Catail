export type CatalystCategory =
  | 'FINANCIAL_PERFORMANCE'
  | 'REGULATION_POLICY'
  | 'SUPPLY_CHAIN'
  | 'COMPETITIVE_LANDSCAPE'
  | 'GOVERNANCE'
  | 'NEW_BUSINESS';

export type CatalystStatus = 'ACTIVE' | 'INACTIVE' | 'PAUSED';

export type CreatableCatalystStatus = Extract<CatalystStatus, 'ACTIVE' | 'INACTIVE'>;
export type RequestableCatalystStatus = Extract<CatalystStatus, 'ACTIVE' | 'PAUSED'>;

export interface CatalystListItemResponse {
  catalystId: number;
  title: string;
  category: CatalystCategory;
  detail: string;
  status: CatalystStatus;
  createdAt: string;
}

export interface CatalystListResponse {
  catalysts: CatalystListItemResponse[];
}

export interface CatalystCreateRequest {
  companyId: number;
  category: CatalystCategory;
  detail: string;
  status: CreatableCatalystStatus;
}

export interface CatalystCreateResponse {
  catalystId: number;
  title: string;
  market: string;
  stockCode: string;
  category: CatalystCategory;
  detail: string;
  status: CatalystStatus;
  createdAt: string;
}

export interface CatalystUpdateRequest {
  category: CatalystCategory;
  detail: string;
}

export interface CatalystUpdateResponse {
  catalystId: number;
  title: string;
  market: string;
  stockCode: string;
  category: CatalystCategory;
  detail: string;
  status: CatalystStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CatalystStatusChangeRequest {
  status: RequestableCatalystStatus;
}

export interface CatalystStatusResponse {
  catalystId: number;
  status: CatalystStatus;
  updatedAt: string;
}
