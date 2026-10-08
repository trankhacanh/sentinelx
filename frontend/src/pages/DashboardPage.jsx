import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import StatCard from '../components/StatCard';
import { SeverityBadge } from '../components/RoleBadge';
import { listEvents } from '../api/events';
import { getThreatStatistics } from '../api/threats';
import { listAlerts } from '../api/alerts';
import { listIncidents } from '../api/incidents';

const SEVERITY_ORDER = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

function sevenDaysAgoIso() {
  const date = new Date();
  date.setDate(date.getDate() - 7);
  return date.toISOString();
}

/**
 * Bọc một lời gọi API, trả về { ok: true, value } nếu thành công hoặc { ok: false } nếu thất bại
 * (ví dụ 403 do thiếu quyền). Dùng thay cho Promise.all "được tất cả hoặc mất tất cả": một role
 * như VIEWER có quyền xem Alerts/Incidents nhưng KHÔNG xem được Events/Threats (RBAC khác nhau
 * theo thiết kế Phase 3-6) -- dashboard vẫn phải hiển thị được phần họ CÓ quyền, không sập toàn
 * trang chỉ vì một phần bị từ chối.
 */
function safeCall(promise) {
  return promise.then((res) => ({ ok: true, value: res.data.data })).catch(() => ({ ok: false }));
}

export default function DashboardPage() {
  const [state, setState] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    Promise.all([
      safeCall(listEvents({ size: 1 })),
      safeCall(getThreatStatistics({ from: sevenDaysAgoIso() })),
      safeCall(listAlerts({ status: 'OPEN', size: 1 })),
      safeCall(listIncidents({ status: 'OPEN', size: 1 })),
      safeCall(listAlerts({ size: 5 })),
    ]).then(([events, stats, openAlerts, openIncidents, recentAlerts]) => {
      if (cancelled) return;
      setState({ events, stats, openAlerts, openIncidents, recentAlerts });
      setLoading(false);
    });

    return () => {
      cancelled = true;
    };
  }, []);

  if (loading) return <div className="text-gray-400">Loading dashboard...</div>;

  const { events, stats, openAlerts, openIncidents, recentAlerts } = state;
  const severityData = stats.ok
    ? SEVERITY_ORDER.map((sev) => ({ severity: sev, count: stats.value.bySeverity[sev] ?? 0 }))
    : [];
  const typeData = stats.ok ? Object.entries(stats.value.byType).map(([type, count]) => ({ type, count })) : [];

  return (
    <div className="flex flex-col gap-6">
      <div className="grid grid-cols-4 gap-4">
        <StatCard label="Total Events" value={events.ok ? events.value.totalElements : '—'} />
        <StatCard label="Total Threats (7 days)" value={stats.ok ? stats.value.totalThreats : '—'} />
        <StatCard
          label="Open Alerts"
          value={openAlerts.ok ? openAlerts.value.totalElements : '—'}
          accent="text-severity-high"
        />
        <StatCard
          label="Open Incidents"
          value={openIncidents.ok ? openIncidents.value.totalElements : '—'}
          accent="text-severity-critical"
        />
      </div>

      {!events.ok && !stats.ok && (
        <div className="rounded border border-surface-border bg-surface-panel px-4 py-2 text-sm text-gray-500">
          Events and threat statistics require SOC Analyst, Security Engineer or Admin access.
        </div>
      )}

      {stats.ok && (
        <div className="grid grid-cols-2 gap-4">
          <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
            <h2 className="mb-3 text-sm font-semibold text-gray-300">Threats by Severity (7 days)</h2>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={severityData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis dataKey="severity" stroke="#9ca3af" />
                <YAxis stroke="#9ca3af" allowDecimals={false} />
                <Tooltip contentStyle={{ background: '#121821', border: '1px solid #1f2937' }} />
                <Bar dataKey="count" fill="#f97316" />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
            <h2 className="mb-3 text-sm font-semibold text-gray-300">Threats by Type (7 days)</h2>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={typeData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis dataKey="type" stroke="#9ca3af" tick={{ fontSize: 11 }} />
                <YAxis stroke="#9ca3af" allowDecimals={false} />
                <Tooltip contentStyle={{ background: '#121821', border: '1px solid #1f2937' }} />
                <Bar dataKey="count" fill="#3b82f6" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      <div className="grid grid-cols-2 gap-4">
        {stats.ok && (
          <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
            <h2 className="mb-3 text-sm font-semibold text-gray-300">Top Source IPs (7 days)</h2>
            {stats.value.topSourceIps.length === 0 ? (
              <div className="text-sm text-gray-500">No data</div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-gray-500">
                    <th className="pb-2">IP</th>
                    <th className="pb-2">Threat count</th>
                  </tr>
                </thead>
                <tbody>
                  {stats.value.topSourceIps.map((row) => (
                    <tr key={row.sourceIp} className="border-t border-surface-border">
                      <td className="py-1.5 font-mono text-gray-200">{row.sourceIp}</td>
                      <td className="py-1.5 text-gray-400">{row.count}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
          <h2 className="mb-3 text-sm font-semibold text-gray-300">Recent Alerts</h2>
          {!recentAlerts.ok ? (
            <div className="text-sm text-gray-500">Unable to load alerts.</div>
          ) : recentAlerts.value.content.length === 0 ? (
            <div className="text-sm text-gray-500">No alerts</div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500">
                  <th className="pb-2">Title</th>
                  <th className="pb-2">Severity</th>
                  <th className="pb-2">Status</th>
                </tr>
              </thead>
              <tbody>
                {recentAlerts.value.content.map((alert) => (
                  <tr key={alert.id} className="border-t border-surface-border">
                    <td className="py-1.5 text-gray-200">{alert.title}</td>
                    <td className="py-1.5">
                      <SeverityBadge severity={alert.severity} />
                    </td>
                    <td className="py-1.5 text-gray-400">{alert.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}