import { Stack } from 'expo-router';
import { Platform } from 'react-native';

// A Stack inside the tabs lets the profile screen push on top of the catalog.
// On web the tab bar is a custom overlay, so screens draw their own heading.
export default function SpeciesLayout() {
  return (
    <Stack screenOptions={{ headerShown: Platform.OS !== 'web' }}>
      <Stack.Screen name="index" options={{ title: 'Species' }} />
      <Stack.Screen name="[id]" options={{ title: 'Species profile' }} />
    </Stack>
  );
}
