import type { CatalystStatus } from '../types/catalyst';

export interface CatalystDetailMock {
  id: number;
  title: string;
  content: string;
  status: CatalystStatus;
  industryTags: string[];
  createdAtLabel: string;
  updatedAtLabel: string;
  searchConditions: string[];
  monitoring: {
    status: CatalystStatus;
    intervalLabel: string;
    lastMonitoredAtLabel: string;
  };
}

export function getMockCatalystDetail(id: number): CatalystDetailMock {
  return {
    id,
    title: '미국의 대중 반도체 수출 규제 강화',
    content:
      '미국 상무부가 반도체 및 AI 데이터센터 관련 품목에 대한 대중국 수출 통제를 지속적으로 강화하고 있다. 관련 정책 발표와 산업 동향을 추적한다.',
    status: 'ACTIVE',
    industryTags: ['반도체', 'AI 데이터센터'],
    createdAtLabel: '2025.06.20',
    updatedAtLabel: '2025.06.24 09:15',
    searchConditions: [
      'ai agent enterprise adoption trend',
      'ai agent use case in industry',
      'generative ai agent business application',
      'ai agent automation solution case study',
      'ai agent market growth forecast',
    ],
    monitoring: {
      status: 'ACTIVE',
      intervalLabel: '6시간',
      lastMonitoredAtLabel: '2025.05.20 08:12',
    },
  };
}

export function formatCatalystId(id: number): string {
  return `CAT-2025-${String(id).padStart(4, '0')}`;
}