import type { Catalyst, CatalystStatus } from '../types/catalyst';

const TITLES = [
  '2차전지 소재 국산화 동향',
  'AI 반도체 수요 증가 추적',
  '바이오시밀러 규제 완화 모니터링',
  '전기차 충전 인프라 확대 정책',
  '클라우드 보안 규제 강화 흐름',
  '수소 에너지 상용화 로드맵',
  '메타버스 플랫폼 경쟁 구도',
  '조선업 친환경 선박 발주 동향',
  '핀테크 오픈뱅킹 확장 정책',
  '반도체 장비 수출 규제 변화',
  '헬스케어 디지털 전환 가속화',
  '로봇 자동화 산업 재편 흐름',
];

const INDUSTRY_TAG_SETS = [
  ['2차전지', '소재'],
  ['반도체', 'AI'],
  ['바이오', '제약'],
  ['모빌리티', '에너지'],
  ['IT', '보안'],
  ['에너지', '친환경'],
];

const STATUSES: CatalystStatus[] = ['ACTIVE', 'PAUSED', 'INACTIVE', 'ENDED'];

const MOCK_COUNT = 24;

export const MOCK_CATALYSTS: Catalyst[] = Array.from({ length: MOCK_COUNT }, (_, i) => {
  const id = i + 1;
  const hasMonitored = i % 5 !== 0;

  return {
    id,
    title: TITLES[i % TITLES.length],
    status: STATUSES[i % STATUSES.length],
    industryTags: INDUSTRY_TAG_SETS[i % INDUSTRY_TAG_SETS.length],
    pendingSignalCount: (i * 3) % 12,
    lastMonitoredAtLabel: hasMonitored ? '2025.06.23' : null,
  };
});
