import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { listAllUsers } from '../api/users';

export default function AssignModal({ currentAssignee, onAssign, onClose }) {
  const { user } = useAuth();
  const isAdmin = user?.roles?.includes('ADMIN');

  const [users, setUsers] = useState([]);
  const [selectedId, setSelectedId] = useState(currentAssignee ?? '');
  const [manualId, setManualId] = useState(currentAssignee ?? '');
  const [error, setError] = useState('');

  useEffect(() => {
    if (isAdmin) {
      listAllUsers({ size: 100 })
        .then((res) => setUsers(res.data.data.content))
        .catch(() => setError('Failed to load user list'));
    }
  }, [isAdmin]);

  function handleSubmit(e) {
    e.preventDefault();
    const targetId = isAdmin ? selectedId : manualId.trim();
    onAssign(targetId === '' ? null : targetId);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60" onClick={onClose}>
      <form
        onSubmit={handleSubmit}
        className="w-96 rounded-lg border border-surface-border bg-surface-panel p-5"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="mb-4 text-lg font-semibold text-white">Assign to</h2>

        {error && <div className="mb-3 text-sm text-red-400">{error}</div>}

        {isAdmin ? (
          <select
            value={selectedId}
            onChange={(e) => setSelectedId(e.target.value)}
            className="mb-4 w-full rounded border border-surface-border bg-surface-base px-3 py-2 text-white"
          >
            <option value="">Unassigned</option>
            {users
              .filter((u) => !u.roles.includes('VIEWER') || u.roles.length > 1)
              .map((u) => (
                <option key={u.id} value={u.id}>
                  {u.username} ({u.roles.join(', ')})
                </option>
              ))}
          </select>
        ) : (
          <div className="mb-4">
            <label className="mb-1 block text-sm text-gray-400">User ID (UUID)</label>
            <input
              value={manualId}
              onChange={(e) => setManualId(e.target.value)}
              placeholder="Leave empty to unassign"
              className="w-full rounded border border-surface-border bg-surface-base px-3 py-2 font-mono text-sm text-white"
            />
            <p className="mt-1 text-xs text-gray-500">
              Only Admins can browse the user list. Ask your Admin for the user ID, or leave empty to unassign.
            </p>
          </div>
        )}

        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded border border-surface-border px-3 py-1.5 text-sm text-gray-300"
          >
            Cancel
          </button>
          <button type="submit" className="rounded bg-blue-600 px-3 py-1.5 text-sm text-white hover:bg-blue-700">
            Save
          </button>
        </div>
      </form>
    </div>
  );
}