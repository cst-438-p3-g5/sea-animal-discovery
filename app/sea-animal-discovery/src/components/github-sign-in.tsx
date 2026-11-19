import { useAuth } from '@/auth/context';
import { ThemedText } from './themed-text';

export function GithubSignIn() {
  const auth = useAuth();
  if (auth.session) return null;
  return <ThemedText>GitHub sign-in is currently available on the web app.</ThemedText>;
}
