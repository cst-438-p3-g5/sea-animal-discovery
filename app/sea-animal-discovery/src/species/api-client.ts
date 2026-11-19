import type { ApiRequest } from '../auth/api-client';

export type Species = {
  id: string;
  common_name: string;
  identification_details: string;
  habitat: string;
  conservation_information: string;
};

export function createSpeciesApi(apiRequest: ApiRequest) {
  return {
    listSpecies() {
      return apiRequest<Species[]>('/species');
    },
    getSpecies(id: string) {
      return apiRequest<Species>(`/species/${encodeURIComponent(id)}`);
    },
  };
}

// API errors should be shown as returned; network failures get a short prompt.
export function describeError(error: unknown) {
  if (error instanceof Error && !(error instanceof TypeError)) return error.message;
  return "Can't reach the server. Check your connection and try again.";
}
