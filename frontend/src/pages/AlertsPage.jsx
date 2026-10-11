import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { listAlerts, updateAlertStatus, assignAlert } from '../api/alerts';
import { SeverityBadge } from '../components/RoleBadge';
import RiskScoreBar from '../components/RiskScoreBar';
import Pagination from '../components/Pagination';
import AssignModal from '../components/AssignModal';

const STATUSES = ['OPEN', 'ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED', 'FALSE_POSITIVE'];
const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function AlertsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') ?? '0', 10);

  const [status, setStatus] = useState('');
  const [severity, setSeverity] = useState('');

  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [assigningAlert, setAssigningAlert] = useState(null);
  const [actionError, setActionError] = useState('');

  function fetchAlerts() {
    const params = {
      page,
      size: 20,
      ...(status && { status }),
      ...(severity && { severity }),
    };
    listAlerts(params)
      .then((res) => setData(res.data.data))
      .catch((err) => setError(err.response?.data?.message ?? 'Failed to load alerts'));
  }

  useEffect(fetchAlerts, [page, status, severity]);

  function goToPage(newPage) {
    setSearchParams({ page: String(newPage) });
  }

  function handleFilterChange(setter) {
    return (value) => {
      setter(value);
      setSearchParams({ page: '0' });
    };
  }

  async function handleStatusChange(alertId, newStatus) {
    setActionError('');
    try {
      await updateAlertStatus(alertId, newStatus);
      fetchAlerts();
    } catch (err) {
      setActionError(err.response?.data?.message ?? 'Failed to update status');
    }
  }

  async function handleAssign(assigneeId) {
    setActionError('');
    try {
      await assignAlert(assigningAlert.id, assigneeId);
      setAssigningAlert(null);
      fetchAlerts();
    } catch (err) {
      setActionError(err.response?.data?.message ?? 'Failed to assign alert');
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-bold text-white">Alerts</h1>

      <div className="flex flex-wrap gap-3 rounded-lg border border-surface-border bg-surface-panel p-4">
        <select
          value={status}
          onChange={(e) => handleFilterChange(setStatus)(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        >
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
        <select
          value={severity}
          onChange={(e) => handleFilterChange(setSeverity)(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        >
          <option value="">All severities</option>
          {SEVERITIES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      {error && <div className="text-red-400">{error}</div>}
      {actionError && <div className="text-red-400">{actionError}</div>}

      {data && (
        <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500">
                <th className="pb-2">Title</th>
                <th className="pb-2">Severity</th>
                <th className="pb-2">Risk</th>
                <th className="pb-2">Status</th>
                <th className="pb-2">Assigned to</th>
                <th className="pb-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((alert) => (
                <tr key={alert.id} className="border-t border-surface-border">
                  <td className="py-1.5 text-gray-200">{alert.title}</td>
                  <td className="py-1.5"><SeverityBadge severity={alert.severity} /></td>
                  <td className="py-1.5"><RiskScoreBar score={alert.riskScore} /></td>
                  <td className="py-1.5">
                    <select
                      value={alert.status}
                      onChange={(e) => handleStatusChange(alert.id, e.target.value)}
                      className="rounded border border-surface-border bg-surface-base px-2 py-1 text-xs text-white"
                    >
                      {STATUSES.map((s) => (
                        <option key={s} value={s}>{s}</option>
                      ))}
                    </select>
                  </td>
                  <td className="py-1.5 font-mono text-gray-400">
                    {alert.assignedTo ? alert.assignedTo.slice(0, 8) : '—'}
                  </td>
                  <td className="py-1.5">
                    <button
                      onClick={() => setAssigningAlert(alert)}
                      className="rounded border border-surface-border px-2 py-1 text-xs text-gray-300 hover:bg-surface-border"
                    >
                      Assign
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {data.content.length === 0 && (
            <div className="py-6 text-center text-sm text-gray-500">No alerts found.</div>
          )}
          <Pagination page={page} totalPages={data.totalPages} onPageChange={goToPage} />
        </div>
      )}

      {assigningAlert && (
        <AssignModal
          currentAssignee={assigningAlert.assignedTo}
          onAssign={handleAssign}
          onClose={() => setAssigningAlert(null)}
        />
      )}
    </div>
  );
}