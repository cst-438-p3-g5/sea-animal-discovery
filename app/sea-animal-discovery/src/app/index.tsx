import { StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GithubSignIn } from '@/components/github-sign-in';
import { GoogleSignIn } from '@/components/google-sign-in';
import { AuthStatus } from '@/components/auth-status';
import { TestUserSignIn } from '@/components/test-user-sign-in';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';

export default function HomeScreen() {
  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.content}>
        <ThemedText type="title" style={styles.title}>Sea Animal Discovery</ThemedText>
        <AuthStatus />
        <GithubSignIn />
        <GoogleSignIn />
        <TestUserSignIn />
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  content: { width: '100%', maxWidth: 600, padding: 24, gap: 24, alignItems: 'center' },
  title: { textAlign: 'center' },
});
