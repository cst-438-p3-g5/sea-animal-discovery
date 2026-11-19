import Constants from 'expo-constants';
import { Platform } from 'react-native';

export type User = { id: string; email: string; display_name: string };
export type Session = { access_token: string; token_type: 'Bearer'; user: User };

const metroHost = Constants.expoConfig?.hostUri?.split(':')[0];
const defaultApiUrl = Platform.OS === 'web'
  ? 'http://localhost:8080'
  : `http://${metroHost ?? '10.0.2.2'}:8080`;
const apiUrl = process.env.EXPO_PUBLIC_API_URL?.trim() || defaultApiUrl;

export async function apiRequest<T>(path: string, token?: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(`${apiUrl}${path}`, { ...init, headers });
  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    throw new Error(problem?.detail ?? `Request failed (${response.status}).`);
  }
  return response.json() as Promise<T>;
}

export function exchangeGithubCode(code: string, verifier: string, redirectUri: string) {
  return apiRequest<Session>('/auth/github', undefined, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, code_verifier: verifier, redirect_uri: redirectUri }),
  });
}

export function signInAsTestUser(username: string, password: string) {
  return apiRequest<Session>('/auth/test', undefined, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
}
