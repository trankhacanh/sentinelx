import { useEffect, useState } from 'react';
import { listAlerts } from '../api/alerts';
import { createIncident } from '../api/incidents';

const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function CreateIncidentModal({ onCreated, onClose }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [severity, setSeverity] = useState('MEDIUM');
  const [openAlerts, setOpenAlerts] = useState([]);
  const [selectedAlertIds, setSelectedAlertIds] = useState(new Set());
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    listAlerts({ status: 'OPEN', size: 50 })
      .then((res) => setOpenAlerts(res.data.data.content))
      .catch(() => setError('Failed to load open alerts'));
  }, []);

  function toggleAlert(id) {
    setSelectedAlertIds((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      const res = await createIncident({
        title,
        description,
        severity,
        alertIds: Array.from(selectedAlertIds),
      });
      onCreated(res.data.data.id);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to create incident');
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60" onClick={onClose}>
      <form
        onSubmit={handleSubmit}
        className="max-h-[85vh] w-[520px] overflow-y-auto rounded-lg border border-surface-border bg-surface-panel p-5"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="mb-4 text-lg font-semibold text-white">Create Incident</h2>

        {error && <div className="mb-3 text-sm text-red-400">{error}</div>}

        <label className="mb-1 block text-sm text-gray-400">Title</label>
        <input
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="mb-3 w-full rounded border border-surface-border bg-surface-base px-3 py-2 text-white"
        />

        <label className="mb-1 block text-sm text-gray-400">Description</label>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={3}
          className="mb-3 w-full rounded border border-surface-border bg-surface-base px-3 py-2 text-white"
        />

        <label className="mb-1 block text-sm text-gray-400">Severity</label>
        <select
          value={severity}
          onChange={(e) => setSeverity(e.target.value)}
          className="mb-4 w-full rounded border border-surface-border bg-surface-base px-3 py-2 text-white"
        >
          {SEVERITIES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>

        <div className="mb-1 text-sm text-gray-400">Link open alerts (optional)</div>
        <div className="mb-4 max-h-40 overflow-y-auto rounded border border-surface-border">
          {openAlerts.length === 0 ? (
            <div className="p-3 text-sm text-gray-500">No open alerts.</div>
          ) : (
            openAlerts.map((alert) => (
              <label
                key={alert.id}
                className="flex cursor-pointer items-center gap-2 border-b border-surface-border px-3 py-2 text-sm last:border-b-0 hover:bg-surface-border/30"
              >
                <input
                  type="checkbox"
                  checked={selectedAlertIds.has(alert.id)}
                  onChange={() => toggleAlert(alert.id)}
                />
                <span className="text-gray-200">{alert.title}</span>
              </label>
            ))
          )}
        </div>

        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded border border-surface-border px-3 py-1.5 text-sm text-gray-300"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="rounded bg-blue-600 px-3 py-1.5 text-sm text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Creating...' : 'Create'}
          </button>
        </div>
      </form>
    </div>
  );
}