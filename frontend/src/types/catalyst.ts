export type CatalystStatus = 'ACTIVE' | 'PAUSED' | 'INACTIVE' | 'ENDED';

export const STATUS_META: Record<CatalystStatus, { bg: string; text: string; label: string }> = {
  ACTIVE: { bg: 'bg-status-active-bg', text: 'text-status-active', label: 'ACTIVE' },
  PAUSED: { bg: 'bg-status-paused-bg', text: 'text-status-paused', label: 'PAUSED' },
  INACTIVE: { bg: 'bg-status-inactive-bg', text: 'text-status-inactive', label: 'INACTIVE' },
  ENDED: { bg: 'bg-status-ended-bg', text: 'text-status-ended', label: 'ENDED' },
};

// 모니터링 상태변경 API의 목표 상태로 선택 가능한 값. INACTIVE로의 전이는 허용되지 않는다.
export const TARGET_STATUS_OPTIONS: CatalystStatus[] = ['ACTIVE', 'PAUSED', 'ENDED'];

export interface Catalyst {
  id: number;
  title: string;
  status: CatalystStatus;
  industryTags: string[];
  pendingSignalCount: number;
  lastMonitoredAtLabel: string | null;
}

export interface CatalystListItemResponse {
  id: number;
  title: string;
  status: CatalystStatus;
  industryTags: string[];
  pendingSignalCount: number;
  createdAt: string; // ISO datetime string
}

export interface CreateCatalystPayload {
  title: string;
  content: string;
  industryIds: number[];
  status: 'ACTIVE' | 'INACTIVE';
}

export interface CatalystDetailResponse {
  id: number;
  title: string;
  content: string;
  status: CatalystStatus;
  industryTags: string[];
  createdAt: string; // ISO datetime string
}

export interface CatalystBasicInfo {
  title: string;
  content: string;
  industries: string[];
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
}

export interface CatalystMonitoringOperation {
  status: CatalystStatus;
  searchConditions: string[];
  searchIntervalHours: number;
  lastSearchedAt: string | null; // ISO datetime string
  activatedAt: string | null; // ISO datetime string
}

export interface CatalystInfoResponse {
  basicInfo: CatalystBasicInfo;
  monitoringOperation: CatalystMonitoringOperation;
}

export interface UpdateCatalystBasicInfoPayload {
  title: string;
  content: string;
  industryIds: number[];
}

export interface CatalystUpdateResponse {
  title: string;
  content: string;
  industries: string[];
  updatedAt: string; // ISO datetime string
}
