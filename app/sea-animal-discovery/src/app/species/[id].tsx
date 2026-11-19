import { Link, Stack, useLocalSearchParams } from 'expo-router';
import { useCallback } from 'react';
import { Platform, ScrollView, StyleSheet } from 'react-native';

import { StateMessage } from '@/components/state-message';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useLoad } from '@/hooks/use-load';
import { getSpecies, type Species } from '@/species/api';

export default function SpeciesProfileScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const load = useCallback(() => getSpecies(id), [id]);
  const { state, retry } = useLoad(load);

  return (
    <ThemedView style={styles.screen}>
      {state.status === 'success' && <Stack.Screen options={{ title: state.data.common_name }} />}
      <ScrollView contentContainerStyle={styles.content}>
        {Platform.OS === 'web' && (
          <Link href="/species" accessibilityRole="link">
            <ThemedText type="link">← All species</ThemedText>
          </Link>
        )}
        {state.status === 'loading' && <StateMessage kind="loading" message="Loading profile…" />}
        {state.status === 'error' && (
          <StateMessage kind="error" message={state.message} onRetry={retry} />
        )}
        {state.status === 'success' && <Profile species={state.data} />}
      </ScrollView>
    </ThemedView>
  );
}

function Profile({ species }: { species: Species }) {
  return (
    <ThemedView style={styles.profile}>
      <ThemedText type="subtitle" accessibilityRole="header">
        {species.common_name}
      </ThemedText>
      <Section title="How to identify it" body={species.identification_details} />
      <Section title="Where it lives" body={species.habitat} />
      <Section title="Conservation" body={species.conservation_information} />
    </ThemedView>
  );
}

function Section({ title, body }: { title: string; body: string }) {
  return (
    <ThemedView type="backgroundElement" style={styles.section}>
      <ThemedText type="smallBold" accessibilityRole="header">
        {title}
      </ThemedText>
      <ThemedText>{body}</ThemedText>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, alignItems: 'center' },
  content: {
    width: '100%',
    maxWidth: MaxContentWidth,
    padding: Spacing.three,
    gap: Spacing.three,
    paddingTop: Platform.select({ web: Spacing.six + Spacing.three }) ?? Spacing.three,
    paddingBottom: BottomTabInset + Spacing.three,
  },
  profile: { gap: Spacing.three },
  section: { padding: Spacing.three, borderRadius: Spacing.three, gap: Spacing.one },
});
