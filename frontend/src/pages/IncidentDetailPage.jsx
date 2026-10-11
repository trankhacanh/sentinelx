import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  getIncident, updateIncidentStatus, assignIncident, addIncidentNote,
} from '../api/incidents';
import { SeverityBadge } from '../components/RoleBadge';
import RiskScoreBar from '../components/RiskScoreBar';
import { useAuth } from '../context/AuthContext';

const STATUSES = ['OPEN', 'INVESTIGATING', 'CONTAINED', 'RESOLVED', 'CLOSED'];

export default function IncidentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const canWrite = user?.roles?.some((r) => ['ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER'].includes(r));

  const [incident, setIncident] = useState(null);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [noteText, setNoteText] = useState('');
  const [assigneeInput, setAssigneeInput] = useState('');

  function fetchIncident() {
    getIncident(id)
      .then((res) => setIncident(res.data.data))
      .catch((err) => setError(err.response?.data?.message ?? 'Failed to load incident'));
  }

  useEffect(fetchIncident, [id]);

  async function handleStatusChange(newStatus) {
    setActionError('');
    try {
      await updateIncidentStatus(id, newStatus);
      fetchIncident();
    } catch (err) {
      setActionError(err.response?.data?.message ?? 'Failed to update status');
    }
  }

  async function handleAssign(e) {
    e.preventDefault();
    setActionError('');
    try {
      await assignIncident(id, assigneeInput.trim() === '' ? null : assigneeInput.trim());
      setAssigneeInput('');
      fetchIncident();
    } catch (err) {
      setActionError(err.response?.data?.message ?? 'Failed to assign incident');
    }
  }

  async function handleAddNote(e) {
    e.preventDefault();
    if (!noteText.trim()) return;
    setActionError('');
    try {
      await addIncidentNote(id, noteText.trim());
      setNoteText('');
      fetchIncident();
    } catch (err) {
      setActionError(err.response?.data?.message ?? 'Failed to add note');
    }
  }

  if (error) return <div className="text-red-400">{error}</div>;
  if (!incident) return <div className="text-gray-400">Loading...</div>;

  return (
    <div className="flex flex-col gap-4">
      <button onClick={() => navigate('/incidents')} className="w-fit text-sm text-gray-400 hover:text-white">
        ← Back to Incidents
      </button>

      <div className="rounded-lg border border-surface-border bg-surface-panel p-5">
        <div className="mb-3 flex items-center justify-between">
          <h1 className="text-xl font-bold text-white">{incident.title}</h1>
          <SeverityBadge severity={incident.severity} />
        </div>
        <p className="mb-4 text-sm text-gray-400">{incident.description}</p>

        {actionError && <div className="mb-3 text-sm text-red-400">{actionError}</div>}

        <div className="flex flex-wrap items-center gap-6 text-sm">
          <div>
            <span className="text-gray-500">Status: </span>
            {canWrite ? (
              <select
                value={incident.status}
                onChange={(e) => handleStatusChange(e.target.value)}
                className="rounded border border-surface-border bg-surface-base px-2 py-1 text-white"
              >
                {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            ) : (
              <span className="text-gray-200">{incident.status}</span>
            )}
          </div>
          <div>
            <span className="text-gray-500">Resolved at: </span>
            <span className="text-gray-300">{incident.resolvedAt ?? '—'}</span>
          </div>
          <div>
            <span className="text-gray-500">Assigned to: </span>
            <span className="font-mono text-gray-300">
              {incident.assignedTo ? incident.assignedTo.slice(0, 8) : '—'}
            </span>
          </div>
        </div>

        {canWrite && (
          <form onSubmit={handleAssign} className="mt-3 flex gap-2">
            <input
              placeholder="User ID to assign (empty = unassign)"
              value={assigneeInput}
              onChange={(e) => setAssigneeInput(e.target.value)}
              className="flex-1 rounded border border-surface-border bg-surface-base px-3 py-1.5 font-mono text-sm text-white"
            />
            <button type="submit" className="rounded border border-surface-border px-3 py-1.5 text-sm text-gray-300">
              Assign
            </button>
          </form>
        )}
      </div>

      <div className="rounded-lg border border-surface-border bg-surface-panel p-5">
        <h2 className="mb-3 text-sm font-semibold text-gray-300">Linked Alerts ({incident.alerts.length})</h2>
        {incident.alerts.length === 0 ? (
          <div className="text-sm text-gray-500">No alerts linked.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500">
                <th className="pb-2">Title</th>
                <th className="pb-2">Severity</th>
                <th className="pb-2">Risk</th>
                <th className="pb-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {incident.alerts.map((alert) => (
                <tr key={alert.id} className="border-t border-surface-border">
                  <td className="py-1.5 text-gray-200">{alert.title}</td>
                  <td className="py-1.5"><SeverityBadge severity={alert.severity} /></td>
                  <td className="py-1.5"><RiskScoreBar score={alert.riskScore} /></td>
                  <td className="py-1.5 text-gray-400">{alert.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="rounded-lg border border-surface-border bg-surface-panel p-5">
        <h2 className="mb-3 text-sm font-semibold text-gray-300">Investigation Notes</h2>

        {incident.notes.length === 0 ? (
          <div className="mb-3 text-sm text-gray-500">No notes yet.</div>
        ) : (
          <div className="mb-3 flex flex-col gap-3 border-l-2 border-surface-border pl-4">
            {incident.notes.map((note) => (
              <div key={note.id}>
                <div className="text-xs text-gray-500">
                  {note.createdAt} · author {note.authorId.slice(0, 8)}
                </div>
                <div className="text-sm text-gray-200">{note.content}</div>
              </div>
            ))}
          </div>
        )}

        {canWrite && (
          <form onSubmit={handleAddNote} className="flex gap-2">
            <input
              placeholder="Add an investigation note..."
              value={noteText}
              onChange={(e) => setNoteText(e.target.value)}
              className="flex-1 rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
            />
            <button type="submit" className="rounded bg-blue-600 px-3 py-1.5 text-sm text-white hover:bg-blue-700">
              Add
            </button>
          </form>
        )}
      </div>
    </div>
  );
}