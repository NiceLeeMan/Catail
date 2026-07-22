export interface DisclosureItemResponse {
  id: number;
  provider: string;
  reportName: string;
  submitterName: string;
  receivedDate: string;
  remarkCodes: string[];
  sourceUrl: string;
}

export interface DisclosureListResponse {
  items: DisclosureItemResponse[];
  nextCursor: string | null;
  hasNext: boolean;
  lastSyncedAt: string | null;
  initialSyncCompleted: boolean;
}
