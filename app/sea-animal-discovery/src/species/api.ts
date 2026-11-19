import { apiRequest } from '../auth/api';
import { createSpeciesApi, describeError } from './api-client';

export type { Species } from './api-client';
export { describeError };

export const { listSpecies, getSpecies } = createSpeciesApi(apiRequest);
