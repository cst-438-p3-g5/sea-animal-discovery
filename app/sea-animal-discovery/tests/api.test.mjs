import assert from 'node:assert/strict';
import { afterEach, test } from 'node:test';
import { apiRequest, exchangeGithubCode } from '../src/auth/api.ts';

const originalFetch = globalThis.fetch;
afterEach(() => { globalThis.fetch = originalFetch; });

test('code exchange sends PKCE data to API without a provider secret', async () => {
  globalThis.fetch = async (url, init) => {
    assert.equal(url, 'http://localhost:8080/auth/github');
    assert.equal(init.method, 'POST');
    assert.equal(init.headers.get('Content-Type'), 'application/json');
    assert.equal(init.headers.has('Authorization'), false);
    assert.deepEqual(JSON.parse(init.body), { code: 'code', code_verifier: 'verifier', redirect_uri: 'http://localhost:8081' });
    return Response.json({ access_token: 'app-jwt', token_type: 'Bearer', user: { id: '1' } });
  };
  assert.equal((await exchangeGithubCode('code', 'verifier', 'http://localhost:8081')).access_token, 'app-jwt');
});

test('authenticated requests send JWT and preserve other headers', async () => {
  globalThis.fetch = async (url, init) => {
    assert.equal(url, 'http://localhost:8080/users/me');
    assert.equal(init.headers.get('Authorization'), 'Bearer app-jwt');
    assert.equal(init.headers.get('Accept'), 'application/json');
    return Response.json({ id: '1' });
  };
  assert.deepEqual(await apiRequest('/users/me', 'app-jwt', { headers: { Accept: 'application/json' } }), { id: '1' });
});

test('shows API problem detail and handles non-JSON failures', async () => {
  globalThis.fetch = async () => Response.json({ detail: 'A verified email is required.' }, { status: 401 });
  await assert.rejects(exchangeGithubCode('bad', 'verifier', 'http://localhost:8081'), /A verified email is required/);
  globalThis.fetch = async () => new Response('gateway unavailable', { status: 502 });
  await assert.rejects(apiRequest('/users/me', 'expired'), /Request failed \(502\)/);
});
