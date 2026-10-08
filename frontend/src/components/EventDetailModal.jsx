export default function EventDetailModal({ event, onClose }) {
  if (!event) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60" onClick={onClose}>
      <div
        className="max-h-[80vh] w-[600px] overflow-y-auto rounded-lg border border-surface-border bg-surface-panel p-5"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-white">Event detail</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            ✕
          </button>
        </div>

        <dl className="mb-4 grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
          <dt className="text-gray-500">ID</dt>
          <dd className="font-mono text-gray-200">{event.id}</dd>
          <dt className="text-gray-500">Timestamp</dt>
          <dd className="text-gray-200">{event.timestamp}</dd>
          <dt className="text-gray-500">Event type</dt>
          <dd className="text-gray-200">{event.eventType}</dd>
          <dt className="text-gray-500">Source</dt>
          <dd className="text-gray-200">{event.source}</dd>
          <dt className="text-gray-500">Source IP</dt>
          <dd className="font-mono text-gray-200">{event.sourceIp}</dd>
          <dt className="text-gray-500">Destination IP</dt>
          <dd className="font-mono text-gray-200">{event.destinationIp ?? '—'}</dd>
          <dt className="text-gray-500">Username</dt>
          <dd className="text-gray-200">{event.username ?? '—'}</dd>
          <dt className="text-gray-500">Severity</dt>
          <dd className="text-gray-200">{event.severity}</dd>
        </dl>

        <div className="mb-1 text-sm text-gray-500">Payload</div>
        <pre className="overflow-x-auto rounded bg-surface-base p-3 text-xs text-gray-300">
          {event.payload ? JSON.stringify(event.payload, null, 2) : 'null'}
        </pre>
      </div>
    </div>
  );
}