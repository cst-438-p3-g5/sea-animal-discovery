import { apiRequest } from '../auth/api.ts';

export type Species = {
  id: string;
  common_name: string;
  identification_details: string;
  habitat: string;
  conservation_information: string;
};

export function listSpecies() {
  return apiRequest<Species[]>('/species');
}

export function getSpecies(id: string) {
  return apiRequest<Species>(`/species/${encodeURIComponent(id)}`);
}

// apiRequest throws an Error carrying the API's problem detail for HTTP errors.
// A failed fetch (server down, no network) throws a TypeError instead.
export function describeError(error: unknown) {
  if (error instanceof Error && !(error instanceof TypeError)) return error.message;
  return "Can't reach the server. Check your connection and try again.";
}
