import type { SignalListItemResponse } from '../types/signal';

export interface SignalTimelineGroup {
  key: string;
  dateLabel: string;
  items: SignalListItemResponse[];
}

export function formatTimelineDateLabel(iso: string): string {
  const date = new Date(iso);
  const now = new Date();
  const options: Intl.DateTimeFormatOptions = { month: 'long', day: 'numeric', weekday: 'long' };
  if (date.getFullYear() !== now.getFullYear()) {
    options.year = 'numeric';
  }
  return new Intl.DateTimeFormat('ko-KR', options).format(date);
}

export function groupSignalsByDate(signals: SignalListItemResponse[]): SignalTimelineGroup[] {
  const groups = new Map<string, SignalListItemResponse[]>();

  for (const signal of signals) {
    const date = new Date(signal.pubDate);
    const key = `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`;
    const bucket = groups.get(key);
    if (bucket) {
      bucket.push(signal);
    } else {
      groups.set(key, [signal]);
    }
  }

  return Array.from(groups.entries()).map(([key, items]) => ({
    key,
    dateLabel: formatTimelineDateLabel(items[0].pubDate),
    items,
  }));
}
