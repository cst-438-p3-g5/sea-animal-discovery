import { Tabs, TabList, TabSlot, TabTrigger } from 'expo-router/ui';

export default function AppTabs() {
  return (
    <Tabs>
      <TabSlot style={{ height: '100%' }} />
      <TabList style={{ display: 'none' }}>
        <TabTrigger name="home" href="/" />
        <TabTrigger name="species" href="/species" />
        <TabTrigger name="explore" href="/explore" />
      </TabList>
    </Tabs>
  );
}
