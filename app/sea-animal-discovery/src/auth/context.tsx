import { createContext, PropsWithChildren, useContext, useState } from 'react';
import { apiRequest, Session } from './api';

type Auth = {
  session: Session | null;
  signIn: (session: Session) => void;
  signOut: () => void;
  request: <T>(path: string, init?: RequestInit) => Promise<T>;
};
const AuthContext = createContext<Auth | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  // Keep the JWT in memory, out of URLs and persistent browser storage.
  const [session, setSession] = useState<Session | null>(null);
  return (
    <AuthContext value={{
      session,
      signIn: setSession,
      signOut: () => setSession(null),
      request: <T,>(path: string, init?: RequestInit) => apiRequest<T>(path, session?.access_token, init),
    }}>
      {children}
    </AuthContext>
  );
}

export function useAuth() {
  const auth = useContext(AuthContext);
  if (!auth) throw new Error('AuthProvider is required.');
  return auth;
}
