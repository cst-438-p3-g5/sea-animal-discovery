import { useState } from 'react';
import { Button, Platform, Pressable, StyleSheet, TextInput, View } from 'react-native';

import { signInAsTestUser } from '@/auth/api';
import { useAuth } from '@/auth/context';
import { ThemedText } from './themed-text';

const enabled = process.env.EXPO_PUBLIC_ENABLE_TEST_LOGIN === 'true';

export function TestUserSignIn() {
  const auth = useAuth();
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  if (!enabled) return null;

  if (auth.session) {
    if (Platform.OS === 'web' || auth.session.user.id !== 'local-test-user') return null;
    return (
      <View style={styles.container}>
        <ThemedText>Signed in as {auth.session.user.display_name}</ThemedText>
        <Button title="Sign out" onPress={auth.signOut} />
      </View>
    );
  }

  async function signIn() {
    if (busy) return;
    setBusy(true);
    setMessage('');
    try {
      auth.signIn(await signInAsTestUser(username, password));
      setPassword('');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to sign in as the test user.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <View style={styles.container}>
      <ThemedText type="subtitle" style={styles.heading}>Test user sign-in</ThemedText>
      <TextInput
        accessibilityLabel="Username"
        autoCapitalize="none"
        autoComplete="username"
        editable={!busy}
        onChangeText={setUsername}
        placeholder="Username"
        placeholderTextColor="#666"
        returnKeyType="next"
        style={styles.input}
        value={username}
      />
      <TextInput
        accessibilityLabel="Password"
        autoCapitalize="none"
        autoComplete="current-password"
        editable={!busy}
        onChangeText={setPassword}
        onSubmitEditing={signIn}
        placeholder="Password"
        placeholderTextColor="#666"
        returnKeyType="done"
        secureTextEntry
        style={styles.input}
        value={password}
      />
      <Pressable
        accessibilityRole="button"
        accessibilityState={{ disabled: busy }}
        disabled={busy}
        onPress={signIn}
        style={({ pressed }) => [styles.button, pressed && styles.pressed]}>
        <ThemedText style={styles.buttonText}>
          {busy ? 'Signing in…' : 'Sign in'}
        </ThemedText>
      </Pressable>
      {!!message && <ThemedText accessibilityLiveRegion="polite" style={styles.heading}>{message}</ThemedText>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { width: '100%', maxWidth: 420, gap: 12, alignItems: 'center' },
  heading: { textAlign: 'center' },
  input: {
    width: '100%',
    minHeight: 48,
    borderWidth: 1,
    borderColor: '#fff',
    borderRadius: 6,
    paddingHorizontal: 12,
    color: '#fff',
    backgroundColor: '#111',
  },
  button: {
    minHeight: 48,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#000',
    borderColor: '#fff',
    borderWidth: 1,
    borderRadius: 6,
    paddingHorizontal: 16,
  },
  buttonText: { color: '#fff' },
  pressed: { opacity: 0.7 },
});
