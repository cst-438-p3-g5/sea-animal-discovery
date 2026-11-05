import { StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GithubSignIn } from '@/components/github-sign-in';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';

export default function HomeScreen() {
  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.content}>
        <ThemedText type="title">Sea Animal Discovery</ThemedText>
        <ThemedText>Explore the ocean. Keep track of what you discover.</ThemedText>
        <GithubSignIn />
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  content: { width: '100%', maxWidth: 600, padding: 24, gap: 24 },
});
