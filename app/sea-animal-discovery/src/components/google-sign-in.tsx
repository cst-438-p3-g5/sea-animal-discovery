import { CodeChallengeMethod, makeRedirectUri, ResponseType, useAuthRequest } from 'expo-auth-session';
import * as Crypto from 'expo-crypto';
import * as WebBrowser from 'expo-web-browser';
import { Platform, Button, View } from 'react-native';
import { useState } from 'react';

import { exchangeGoogleCode, User } from '@/auth/api';
import { useAuth } from '@/auth/context';
import { ThemedText } from './themed-text';

WebBrowser.maybeCompleteAuthSession();

const discovery = { authorizationEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth' };
const webClientId = process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID
  ?? process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID ?? '';
const nativeClientId = process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID
  ?? process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID ?? '';
const nativeRedirectUri = process.env.EXPO_PUBLIC_GOOGLE_NATIVE_REDIRECT_URI ?? '';
const clientId = Platform.OS === 'web' ? webClientId : nativeClientId;
const redirectUri = Platform.OS === 'web'
  ? process.env.EXPO_PUBLIC_GOOGLE_WEB_REDIRECT_URI ?? ''
  : nativeRedirectUri || makeRedirectUri({ scheme: 'seaanimaldiscovery', path: 'oauthredirect' });

export function GoogleSignIn() {
  const auth = useAuth();
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [state, setState] = useState(() => Crypto.randomUUID());
  const [request, , promptAsync] = useAuthRequest({
    clientId,
    redirectUri,
    state,
    responseType: ResponseType.Code,
    scopes: ['openid', 'profile', 'email'],
    usePKCE: true,
    codeChallengeMethod: CodeChallengeMethod.S256,
  }, discovery);

  async function signIn() {
    if (!request?.codeVerifier || busy) return;
    setBusy(true);
    setMessage('');
    try {
      const result = await promptAsync();
      if (result.type === 'success' && result.params.code) {
        auth.signIn(await exchangeGoogleCode(result.params.code, request.codeVerifier, redirectUri));
      } else {
        setMessage(result.type === 'error' ? 'Google sign-in failed. Please try again.' : 'Sign-in was cancelled.');
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to sign in. Please try again.');
    } finally {
      setBusy(false);
      setState(Crypto.randomUUID());
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
          <ThemedText>Sign in with a verified Google email to get started.</ThemedText>
          <Button title={busy ? 'Signing in…' : 'Sign in with Google'} disabled={!clientId || !request || busy} onPress={signIn} />
          {!clientId && <ThemedText>Google sign-in has not been configured.</ThemedText>}
        </>
      )}
      {!!message && <ThemedText accessibilityLiveRegion="polite">{message}</ThemedText>}
    </View>
  );
}
