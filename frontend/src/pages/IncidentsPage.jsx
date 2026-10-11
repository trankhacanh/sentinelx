import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { listIncidents } from '../api/incidents';
import { SeverityBadge } from '../components/RoleBadge';
import Pagination from '../components/Pagination';
import CreateIncidentModal from '../components/CreateIncidentModal';
import { useAuth } from '../context/AuthContext';

const STATUSES = ['OPEN', 'INVESTIGATING', 'CONTAINED', 'RESOLVED', 'CLOSED'];
const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function IncidentsPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const canWrite = user?.roles?.some((r) => ['ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER'].includes(r));

  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') ?? '0', 10);

  const [status, setStatus] = useState('');
  const [severity, setSeverity] = useState('');
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);

  function fetchIncidents() {
    const params = { page, size: 20, ...(status && { status }), ...(severity && { severity }) };
    listIncidents(params)
      .then((res) => setData(res.data.data))
      .catch((err) => setError(err.response?.data?.message ?? 'Failed to load incidents'));
  }

  useEffect(fetchIncidents, [page, status, severity]);

  function goToPage(newPage) {
    setSearchParams({ page: String(newPage) });
  }

  function handleFilterChange(setter) {
    return (value) => {
      setter(value);
      setSearchParams({ page: '0' });
    };
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-white">Incidents</h1>
        {canWrite && (
          <button
            onClick={() => setShowCreate(true)}
            className="rounded bg-blue-600 px-3 py-1.5 text-sm text-white hover:bg-blue-700"
          >
            + Create Incident
          </button>
        )}
      </div>

      <div className="flex flex-wrap gap-3 rounded-lg border border-surface-border bg-surface-panel p-4">
        <select
          value={status}
          onChange={(e) => handleFilterChange(setStatus)(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        >
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
        <select
          value={severity}
          onChange={(e) => handleFilterChange(setSeverity)(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        >
          <option value="">All severities</option>
          {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      {error && <div className="text-red-400">{error}</div>}

      {data && (
        <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500">
                <th className="pb-2">Title</th>
                <th className="pb-2">Severity</th>
                <th className="pb-2">Status</th>
                <th className="pb-2">Alerts</th>
                <th className="pb-2">Created</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((incident) => (
                <tr
                  key={incident.id}
                  onClick={() => navigate(`/incidents/${incident.id}`)}
                  className="cursor-pointer border-t border-surface-border hover:bg-surface-border/40"
                >
                  <td className="py-1.5 text-gray-200">{incident.title}</td>
                  <td className="py-1.5"><SeverityBadge severity={incident.severity} /></td>
                  <td className="py-1.5 text-gray-400">{incident.status}</td>
                  <td className="py-1.5 text-gray-400">{incident.alertCount}</td>
                  <td className="py-1.5 text-gray-400">{incident.createdAt}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {data.content.length === 0 && (
            <div className="py-6 text-center text-sm text-gray-500">No incidents found.</div>
          )}
          <Pagination page={page} totalPages={data.totalPages} onPageChange={goToPage} />
        </div>
      )}

      {showCreate && (
        <CreateIncidentModal
          onCreated={(id) => navigate(`/incidents/${id}`)}
          onClose={() => setShowCreate(false)}
        />
      )}
    </div>
  );
}