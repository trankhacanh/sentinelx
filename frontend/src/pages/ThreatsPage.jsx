import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { listThreats } from '../api/threats';
import { SeverityBadge } from '../components/RoleBadge';
import RiskScoreBar from '../components/RiskScoreBar';
import Pagination from '../components/Pagination';
import ThreatDetailModal from '../components/ThreatDetailModal';

const THREAT_TYPES = ['BRUTE_FORCE', 'PORT_SCAN', 'SUSPICIOUS_AUTHENTICATION', 'SQL_INJECTION', 'HIGH_REQUEST_RATE'];
const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function ThreatsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') ?? '0', 10);

  const [threatType, setThreatType] = useState('');
  const [severity, setSeverity] = useState('');
  const [sourceIp, setSourceIp] = useState('');

  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    const params = {
      page,
      size: 20,
      ...(threatType && { threatType }),
      ...(severity && { severity }),
      ...(sourceIp && { sourceIp }),
    };

    listThreats(params)
      .then((res) => setData(res.data.data))
      .catch((err) => setError(err.response?.data?.message ?? 'Failed to load threats'));
  }, [page, threatType, severity, sourceIp]);

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
      <h1 className="text-xl font-bold text-white">Threats</h1>

      <div className="flex flex-wrap gap-3 rounded-lg border border-surface-border bg-surface-panel p-4">
        <select
          value={threatType}
          onChange={(e) => handleFilterChange(setThreatType)(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        >
          <option value="">All threat types</option>
          {THREAT_TYPES.map((t) => (
            <option key={t} value={t}>{t}</option>
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
        <input
          placeholder="Source IP"
          value={sourceIp}
          onChange={(e) => handleFilterChange(setSourceIp)(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        />
      </div>

      {error && <div className="text-red-400">{error}</div>}

      {data && (
        <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500">
                <th className="pb-2">Detected at</th>
                <th className="pb-2">Type</th>
                <th className="pb-2">Severity</th>
                <th className="pb-2">Risk score</th>
                <th className="pb-2">Source IP</th>
                <th className="pb-2">Rule</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((threat) => (
                <tr
                  key={threat.id}
                  onClick={() => setSelected(threat)}
                  className="cursor-pointer border-t border-surface-border hover:bg-surface-border/40"
                >
                  <td className="py-1.5 text-gray-400">{threat.detectedAt}</td>
                  <td className="py-1.5 text-gray-200">{threat.threatType}</td>
                  <td className="py-1.5"><SeverityBadge severity={threat.severity} /></td>
                  <td className="py-1.5"><RiskScoreBar score={threat.riskScore} /></td>
                  <td className="py-1.5 font-mono text-gray-200">{threat.sourceIp}</td>
                  <td className="py-1.5 text-gray-400">{threat.ruleCode}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {data.content.length === 0 && (
            <div className="py-6 text-center text-sm text-gray-500">No threats found.</div>
          )}
          <Pagination page={page} totalPages={data.totalPages} onPageChange={goToPage} />
        </div>
      )}

      <ThreatDetailModal threat={selected} onClose={() => setSelected(null)} />
    </div>
  );
}