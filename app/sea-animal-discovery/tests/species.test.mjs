import assert from 'node:assert/strict';
import { afterEach, test } from 'node:test';
import { describeError, getSpecies, listSpecies } from '../src/species/api.ts';

const originalFetch = globalThis.fetch;
afterEach(() => { globalThis.fetch = originalFetch; });

const ochre = {
  id: 'sp-001',
  common_name: 'Ochre sea star',
  identification_details: 'Five thick arms.',
  habitat: 'Rocky intertidal zones.',
  conservation_information: 'Recovering.',
};

test('listSpecies requests the public catalog without a token', async () => {
  globalThis.fetch = async (url, init) => {
    assert.equal(url, 'http://localhost:8080/species');
    assert.equal(init.headers.has('Authorization'), false);
    return Response.json([ochre]);
  };
  assert.deepEqual(await listSpecies(), [ochre]);
});

test('listSpecies returns an empty list when the catalog is empty', async () => {
  globalThis.fetch = async () => Response.json([]);
  assert.deepEqual(await listSpecies(), []);
});

test('getSpecies requests one profile and encodes the id', async () => {
  globalThis.fetch = async (url) => {
    assert.equal(url, 'http://localhost:8080/species/sp-001');
    return Response.json(ochre);
  };
  assert.deepEqual(await getSpecies('sp-001'), ochre);
  globalThis.fetch = async (url) => {
    assert.equal(url, 'http://localhost:8080/species/a%2Fb');
    return Response.json(ochre);
  };
  await getSpecies('a/b');
});

test('getSpecies surfaces the API problem detail for a 404', async () => {
  globalThis.fetch = async () =>
    Response.json({ detail: 'No species with id sp-999.' }, { status: 404 });
  await assert.rejects(getSpecies('sp-999'), /No species with id sp-999/);
});

test('describeError shows API errors as-is and network failures in plain words', async () => {
  assert.equal(describeError(new Error('Request failed (502).')), 'Request failed (502).');
  globalThis.fetch = async () => { throw new TypeError('Failed to fetch'); };
  await assert.rejects(listSpecies(), (error) => {
    assert.match(describeError(error), /Can't reach the server/);
    return true;
  });
});
