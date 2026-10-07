import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      // Backend luôn trả cùng message cho sai user/sai password (chống user enumeration, Phase 2)
      setError(err.response?.data?.message ?? 'Login failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex h-screen items-center justify-center bg-surface-base">
      <form
        onSubmit={handleSubmit}
        className="w-80 rounded-lg border border-surface-border bg-surface-panel p-6"
      >
        <h1 className="mb-6 text-center text-xl font-bold text-white">🛡️ SentinelX</h1>

        {error && (
          <div className="mb-4 rounded bg-red-900/50 px-3 py-2 text-sm text-red-300">{error}</div>
        )}

        <label className="mb-1 block text-sm text-gray-400">Username</label>
        <input
          className="mb-4 w-full rounded border border-surface-border bg-surface-base px-3 py-2 text-white"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoFocus
        />

        <label className="mb-1 block text-sm text-gray-400">Password</label>
        <input
          type="password"
          className="mb-6 w-full rounded border border-surface-border bg-surface-base px-3 py-2 text-white"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded bg-blue-600 py-2 font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
    </div>
  );
}