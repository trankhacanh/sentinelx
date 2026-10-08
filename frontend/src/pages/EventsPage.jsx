import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { listEvents } from '../api/events';
import client from '../api/client';
import Pagination from '../components/Pagination';
import EventDetailModal from '../components/EventDetailModal';

const EVENT_TYPES = [
  'LOGIN_SUCCESS', 'LOGIN_FAILED', 'HTTP_REQUEST', 'FILE_ACCESS',
  'PORT_SCAN', 'SQL_INJECTION', 'HIGH_REQUEST_RATE',
];
const SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

function searchEvents(q, params) {
  return client.get('/api/events/search', { params: { q, ...params } });
}

export default function EventsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') ?? '0', 10);

  const [eventType, setEventType] = useState('');
  const [severity, setSeverity] = useState('');
  const [sourceIp, setSourceIp] = useState('');
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');

  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);

  // Debounce 400ms: tránh gọi API trên từng phím gõ của ô tìm kiếm.
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query), 400);
    return () => clearTimeout(timer);
  }, [query]);

  useEffect(() => {
    const params = {
      page,
      size: 20,
      ...(eventType && { eventType }),
      ...(severity && { severity }),
      ...(sourceIp && { sourceIp }),
    };

    const request =
      debouncedQuery.trim().length >= 3
        ? searchEvents(debouncedQuery.trim(), params)
        : listEvents(params);

    request
      .then((res) => setData(res.data.data))
      .catch((err) => setError(err.response?.data?.message ?? 'Failed to load events'));
  }, [page, eventType, severity, sourceIp, debouncedQuery]);

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
      <h1 className="text-xl font-bold text-white">Security Events</h1>

      <div className="flex flex-wrap gap-3 rounded-lg border border-surface-border bg-surface-panel p-4">
        <input
          placeholder="Search (min 3 chars)..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        />
        <select
          value={eventType}
          onChange={(e) => handleFilterChange(setEventType)(e.target.value)}
          className="rounded border border-surface-border bg-surface-base px-3 py-1.5 text-sm text-white"
        >
          <option value="">All event types</option>
          {EVENT_TYPES.map((t) => (
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
                <th className="pb-2">Timestamp</th>
                <th className="pb-2">Type</th>
                <th className="pb-2">Source</th>
                <th className="pb-2">Source IP</th>
                <th className="pb-2">Username</th>
                <th className="pb-2">Severity</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((event) => (
                <tr
                  key={event.id}
                  onClick={() => setSelected(event)}
                  className="cursor-pointer border-t border-surface-border hover:bg-surface-border/40"
                >
                  <td className="py-1.5 text-gray-400">{event.timestamp}</td>
                  <td className="py-1.5 text-gray-200">{event.eventType}</td>
                  <td className="py-1.5 text-gray-400">{event.source}</td>
                  <td className="py-1.5 font-mono text-gray-200">{event.sourceIp}</td>
                  <td className="py-1.5 text-gray-400">{event.username ?? '—'}</td>
                  <td className="py-1.5 text-gray-400">{event.severity}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {data.content.length === 0 && (
            <div className="py-6 text-center text-sm text-gray-500">No events found.</div>
          )}
          <Pagination page={page} totalPages={data.totalPages} onPageChange={goToPage} />
        </div>
      )}

      <EventDetailModal event={selected} onClose={() => setSelected(null)} />
    </div>
  );
}