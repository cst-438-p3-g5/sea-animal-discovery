import { ActivityIndicator, Pressable, StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

type Props =
  | { kind: 'loading'; message?: string }
  | { kind: 'empty'; message: string }
  | { kind: 'error'; message: string; onRetry: () => void };

/** One clear message for the loading, empty, and error states of a screen. */
export function StateMessage(props: Props) {
  const theme = useTheme();

  if (props.kind === 'loading') {
    return (
      <ThemedView style={styles.container} accessibilityLiveRegion="polite">
        <ActivityIndicator size="large" color={theme.text} />
        <ThemedText themeColor="textSecondary">{props.message ?? 'Loading…'}</ThemedText>
      </ThemedView>
    );
  }

  if (props.kind === 'empty') {
    return (
      <ThemedView style={styles.container}>
        <ThemedText type="subtitle" style={styles.centerText}>
          Nothing here yet
        </ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.centerText}>
          {props.message}
        </ThemedText>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container} accessibilityRole="alert">
      <ThemedText type="subtitle" style={styles.centerText}>
        Something went wrong
      </ThemedText>
      <ThemedText themeColor="textSecondary" style={styles.centerText}>
        {props.message}
      </ThemedText>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel="Try again"
        onPress={props.onRetry}
        style={({ pressed }) => pressed && styles.pressed}>
        <ThemedView type="backgroundSelected" style={styles.button}>
          <ThemedText type="smallBold">Try again</ThemedText>
        </ThemedView>
      </Pressable>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    gap: Spacing.three,
    padding: Spacing.four,
  },
  centerText: { textAlign: 'center' },
  button: {
    paddingVertical: Spacing.two,
    paddingHorizontal: Spacing.four,
    borderRadius: Spacing.three,
  },
  pressed: { opacity: 0.7 },
});
