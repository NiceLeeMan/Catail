import type { CatalystCategory, CatalystStatus, RequestableCatalystStatus } from '../types/catalyst';

export const CATALYST_MAX_COUNT = 3;

export const CATALYST_CATEGORIES = [
  'FINANCIAL_PERFORMANCE',
  'REGULATION_POLICY',
  'SUPPLY_CHAIN',
  'COMPETITIVE_LANDSCAPE',
  'GOVERNANCE',
  'NEW_BUSINESS',
] as const satisfies readonly CatalystCategory[];

export const CATALYST_CATEGORY_LABELS: Record<CatalystCategory, string> = {
  FINANCIAL_PERFORMANCE: '실적/재무',
  REGULATION_POLICY: '규제/정책',
  SUPPLY_CHAIN: '공급망',
  COMPETITIVE_LANDSCAPE: '경쟁구도',
  GOVERNANCE: '경영권/지배구조',
  NEW_BUSINESS: '신사업/전략',
};

export const CATALYST_STATUS_LABELS: Record<CatalystStatus, string> = {
  ACTIVE: '활성',
  INACTIVE: '비활성',
  PAUSED: '일시중지',
};

interface CatalystStatusAction {
  label: string;
  target: RequestableCatalystStatus;
}

const STATUS_ACTIONS: Record<CatalystStatus, CatalystStatusAction | null> = {
  INACTIVE: { label: '활성화', target: 'ACTIVE' },
  ACTIVE: { label: '일시중지', target: 'PAUSED' },
  PAUSED: { label: '재개', target: 'ACTIVE' },
};

export function getCatalystStatusAction(status: CatalystStatus): CatalystStatusAction | null {
  return STATUS_ACTIONS[status];
}
