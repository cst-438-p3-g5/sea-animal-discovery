import { useEffect, useState } from 'react';

import { describeError } from '@/species/api';

export type LoadState<T> =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'success'; data: T };

/** Runs `load` on mount (and again on retry), tracking loading, error, and success. */
export function useLoad<T>(load: () => Promise<T>) {
  const [attempt, setAttempt] = useState(0);
  const [state, setState] = useState<LoadState<T>>({ status: 'loading' });

  useEffect(() => {
    let cancelled = false;
    load().then(
      (data) => {
        if (!cancelled) setState({ status: 'success', data });
      },
      (error: unknown) => {
        if (!cancelled) setState({ status: 'error', message: describeError(error) });
      },
    );
    return () => {
      cancelled = true;
    };
  }, [load, attempt]);

  const retry = () => {
    setState({ status: 'loading' });
    setAttempt((n) => n + 1);
  };

  return { state, retry };
}
