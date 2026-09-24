export type SignalStatus = 'PENDING' | 'ADOPTED' | 'EXCLUDED';

export type QueryableSignalStatus = Extract<SignalStatus, 'PENDING' | 'EXCLUDED'>;
export type RequestableSignalStatus = Extract<SignalStatus, 'ADOPTED' | 'EXCLUDED'>;

export interface SignalListItemResponse {
  signalId: number;
  title: string;
  description: string;
  press: string | null;
  link: string;
  pubDate: string;
  relevanceReason: string | null;
  status: SignalStatus;
}

export interface SignalListResponse {
  signals: SignalListItemResponse[];
  nextCursor: string | null;
  hasNext: boolean;
}

export interface SignalStatusChangeRequest {
  status: RequestableSignalStatus;
}

export interface SignalStatusResponse {
  signalId: number;
  status: SignalStatus;
  updatedAt: string;
}

export interface SignalTimelineResponse {
  signals: SignalListItemResponse[];
}
