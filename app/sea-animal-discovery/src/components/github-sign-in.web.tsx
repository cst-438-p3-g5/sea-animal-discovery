import { CodeChallengeMethod, ResponseType, useAuthRequest } from 'expo-auth-session';
import * as Crypto from 'expo-crypto';
import * as WebBrowser from 'expo-web-browser';
import { useState } from 'react';
import { Button, View } from 'react-native';

import { exchangeGithubCode, User } from '@/auth/api';
import { useAuth } from '@/auth/context';
import { ThemedText } from './themed-text';

WebBrowser.maybeCompleteAuthSession();

const discovery = { authorizationEndpoint: 'https://github.com/login/oauth/authorize' };
const clientId = process.env.EXPO_PUBLIC_GITHUB_CLIENT_ID ?? '';
const redirectUri = process.env.EXPO_PUBLIC_GITHUB_REDIRECT_URI ?? 'http://localhost:8081';

export function GithubSignIn() {
  const auth = useAuth();
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [state, setState] = useState(() => Crypto.randomUUID());
  const [request, , promptAsync] = useAuthRequest({
    clientId, redirectUri, state,
    responseType: ResponseType.Code,
    scopes: ['read:user', 'user:email'],
    usePKCE: true,
    codeChallengeMethod: CodeChallengeMethod.S256,
  }, discovery);

  async function signIn() {
    if (!request?.codeVerifier || busy) return;
    setBusy(true);
    setMessage('');
    try {
      // AuthSession validates the returned state before reporting success.
      const result = await promptAsync();
      if (result.type === 'success' && result.params.code) {
        auth.signIn(await exchangeGithubCode(result.params.code, request.codeVerifier, redirectUri));
      } else {
        setMessage(result.type === 'error' ? 'GitHub sign-in failed. Please try again.' : 'Sign-in was cancelled.');
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to sign in. Please try again.');
    } finally {
      setBusy(false);
      setState(Crypto.randomUUID()); // New state and verifier for every attempt.
    }
  }

  async function checkProfile() {
    setBusy(true);
    setMessage('');
    try {
      const profile = await auth.request<User>('/users/me');
      setMessage(`Authenticated as ${profile.display_name} (${profile.email}).`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to load your profile.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <View style={{ gap: 12, width: '100%', maxWidth: 420 }}>
      {auth.session ? (
        <>
          <ThemedText>Signed in as {auth.session.user.display_name}</ThemedText>
          <Button title={busy ? 'Loading…' : 'Check my profile'} disabled={busy} onPress={checkProfile} />
          <Button title="Sign out" disabled={busy} onPress={() => { auth.signOut(); setMessage(''); }} />
        </>
      ) : (
        <>
          <ThemedText>Sign in with a verified GitHub email to get started.</ThemedText>
          <Button title={busy ? 'Signing in…' : 'Sign in with GitHub'} disabled={!clientId || !request || busy} onPress={signIn} />
          {!clientId && <ThemedText>GitHub sign-in has not been configured.</ThemedText>}
        </>
      )}
      {!!message && <ThemedText accessibilityLiveRegion="polite">{message}</ThemedText>}
    </View>
  );
}
