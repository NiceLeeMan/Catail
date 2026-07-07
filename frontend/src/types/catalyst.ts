export type CatalystStatus = 'ACTIVE' | 'PAUSED' | 'INACTIVE' | 'ENDED';

export const STATUS_META: Record<CatalystStatus, { bg: string; text: string; label: string }> = {
  ACTIVE: { bg: 'bg-status-active-bg', text: 'text-status-active', label: 'ACTIVE' },
  PAUSED: { bg: 'bg-status-paused-bg', text: 'text-status-paused', label: 'PAUSED' },
  INACTIVE: { bg: 'bg-status-inactive-bg', text: 'text-status-inactive', label: 'INACTIVE' },
  ENDED: { bg: 'bg-status-ended-bg', text: 'text-status-ended', label: 'ENDED' },
};

export interface Catalyst {
  id: number;
  title: string;
  status: CatalystStatus;
  industryTags: string[];
  pendingSignalCount: number;
  lastMonitoredAtLabel: string | null;
}
