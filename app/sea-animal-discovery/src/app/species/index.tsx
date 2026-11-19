import { Link } from 'expo-router';
import { FlatList, Platform, Pressable, StyleSheet } from 'react-native';

import { StateMessage } from '@/components/state-message';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useLoad } from '@/hooks/use-load';
import { listSpecies, type Species } from '@/species/api';
import { useTheme } from '@/hooks/use-theme';

export default function SpeciesCatalogScreen() {
  const theme = useTheme();
  const { state, retry } = useLoad(listSpecies);

  return (
    <ThemedView style={styles.screen}>
      {state.status === 'loading' && <StateMessage kind="loading" message="Loading species…" />}
      {state.status === 'error' && (
        <StateMessage kind="error" message={state.message} onRetry={retry} />
      )}
      {state.status === 'success' && state.data.length === 0 && (
        <StateMessage kind="empty" message="No species have been added to the catalog." />
      )}
      {state.status === 'success' && state.data.length > 0 && (
        <FlatList
          data={state.data}
          keyExtractor={(species) => species.id}
          contentContainerStyle={styles.list}
          ListHeaderComponent={
            <ThemedView style={styles.header}>
              <ThemedText type="subtitle" accessibilityRole="header">
                Species
              </ThemedText>
              <ThemedText themeColor="textSecondary">
                {state.data.length} species to discover. Select one to see its profile.
              </ThemedText>
            </ThemedView>
          }
          renderItem={({ item }) => <SpeciesRow species={item} textColor={theme.textSecondary} />}
        />
      )}
    </ThemedView>
  );
}

function SpeciesRow({ species, textColor }: { species: Species; textColor: string }) {
  return (
    <Link href={{ pathname: '/species/[id]', params: { id: species.id } }} asChild>
      <Pressable
        accessibilityRole="link"
        accessibilityLabel={`${species.common_name}. Open profile.`}
        style={({ pressed }) => pressed && styles.pressed}>
        <ThemedView type="backgroundElement" style={styles.card}>
          <ThemedText type="default" style={styles.name}>
            {species.common_name}
          </ThemedText>
          <ThemedText type="small" style={{ color: textColor }} numberOfLines={2}>
            {species.habitat}
          </ThemedText>
        </ThemedView>
      </Pressable>
    </Link>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, alignItems: 'center' },
  list: {
    width: '100%',
    maxWidth: MaxContentWidth,
    padding: Spacing.three,
    gap: Spacing.two,
    paddingTop: Platform.select({ web: Spacing.six + Spacing.three }) ?? Spacing.three,
    paddingBottom: BottomTabInset + Spacing.three,
  },
  header: { gap: Spacing.one, paddingBottom: Spacing.two },
  card: { padding: Spacing.three, borderRadius: Spacing.three, gap: Spacing.one },
  name: { fontWeight: 700 },
  pressed: { opacity: 0.7 },
});
