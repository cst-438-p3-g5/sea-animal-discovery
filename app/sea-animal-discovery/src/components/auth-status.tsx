import { Button, StyleSheet, View } from 'react-native';
import { useState } from 'react';

import { type User } from '@/auth/api';
import { useAuth } from '@/auth/context';
import { ThemedText } from './themed-text';

export function AuthStatus() {
  const auth = useAuth();
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');

  if (!auth.session) return null;

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
    <View style={styles.container}>
      <ThemedText style={styles.centered}>Signed in as {auth.session.user.display_name}</ThemedText>
      <Button title={busy ? 'Loading…' : 'Check my profile'} disabled={busy} onPress={checkProfile} />
      <Button title="Sign out" disabled={busy} onPress={() => { auth.signOut(); setMessage(''); }} />
      {!!message && <ThemedText accessibilityLiveRegion="polite" style={styles.centered}>{message}</ThemedText>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { gap: 12, width: '100%', maxWidth: 420 },
  centered: { textAlign: 'center' },
});
