import Constants from 'expo-constants';
import { Platform } from 'react-native';

import { createApiClient } from './api-client';
export type { Session, User } from './api-client';

const metroHost = Constants.expoConfig?.hostUri?.split(':')[0];
const defaultApiUrl = Platform.OS === 'web'
  ? 'http://localhost:8080'
  : `http://${metroHost ?? '10.0.2.2'}:8080`;
const apiUrl = process.env.EXPO_PUBLIC_API_URL?.trim() || defaultApiUrl;

export const { apiRequest, exchangeGithubCode, exchangeGoogleCode, signInAsTestUser } = createApiClient(apiUrl);
