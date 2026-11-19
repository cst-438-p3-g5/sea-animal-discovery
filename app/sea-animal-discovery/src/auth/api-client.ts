export type User = { id: string; email: string; display_name: string };
export type Session = { access_token: string; token_type: 'Bearer'; user: User };

export type ApiRequest = <T>(path: string, token?: string, init?: RequestInit) => Promise<T>;

export function createApiClient(apiUrl: string) {
  const apiRequest: ApiRequest = async (path, token, init = {}) => {
    const headers = new Headers(init.headers);
    if (token) headers.set('Authorization', `Bearer ${token}`);
    const response = await fetch(`${apiUrl}${path}`, { ...init, headers });
    if (!response.ok) {
      const problem = await response.json().catch(() => null);
      throw new Error(problem?.detail ?? `Request failed (${response.status}).`);
    }
    return response.json();
  };

  function exchangeGithubCode(code: string, verifier: string, redirectUri: string) {
    return apiRequest<Session>('/auth/github', undefined, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, code_verifier: verifier, redirect_uri: redirectUri }),
    });
  }

  function signInAsTestUser(username: string, password: string) {
    return apiRequest<Session>('/auth/test', undefined, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
  }

  return { apiRequest, exchangeGithubCode, signInAsTestUser };
}
