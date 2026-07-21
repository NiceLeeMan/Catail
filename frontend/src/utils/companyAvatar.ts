const AVATAR_PALETTE = [
  '#3B82F6',
  '#EF4444',
  '#22C55E',
  '#6366F1',
  '#14B8A6',
  '#EAB308',
  '#EC4899',
  '#0EA5E9',
  '#F97316',
  '#DC2626',
  '#F59E0B',
  '#2563EB',
  '#DB2777',
  '#0284C7',
  '#7C3AED',
];

export function getCompanyInitials(companyName: string): string {
  const asciiPrefix = companyName.match(/^[A-Za-z]+/)?.[0] ?? '';
  if (asciiPrefix.length >= 2) {
    return asciiPrefix.slice(0, 2).toUpperCase();
  }
  return companyName.slice(0, 1).toUpperCase();
}

export function getAvatarColor(seed: string): string {
  let hash = 0;
  for (let i = 0; i < seed.length; i += 1) {
    hash = (hash * 31 + seed.charCodeAt(i)) | 0;
  }
  const index = Math.abs(hash) % AVATAR_PALETTE.length;
  return AVATAR_PALETTE[index];
}
