import { SeverityBadge } from './RoleBadge';

export default function ThreatDetailModal({ threat, onClose }) {
  if (!threat) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60" onClick={onClose}>
      <div
        className="w-[600px] rounded-lg border border-surface-border bg-surface-panel p-5"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-white">Threat detail</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            ✕
          </button>
        </div>

        <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
          <dt className="text-gray-500">ID</dt>
          <dd className="font-mono text-gray-200">{threat.id}</dd>
          <dt className="text-gray-500">Event ID</dt>
          <dd className="font-mono text-gray-200">{threat.eventId}</dd>
          <dt className="text-gray-500">Rule code</dt>
          <dd className="text-gray-200">{threat.ruleCode}</dd>
          <dt className="text-gray-500">Threat type</dt>
          <dd className="text-gray-200">{threat.threatType}</dd>
          <dt className="text-gray-500">Severity</dt>
          <dd><SeverityBadge severity={threat.severity} /></dd>
          <dt className="text-gray-500">Risk score</dt>
          <dd className="text-gray-200">{threat.riskScore}</dd>
          <dt className="text-gray-500">Source IP</dt>
          <dd className="font-mono text-gray-200">{threat.sourceIp}</dd>
          <dt className="text-gray-500">Detected at</dt>
          <dd className="text-gray-200">{threat.detectedAt}</dd>
        </dl>

        <div className="mt-4 text-sm text-gray-500">Description</div>
        <p className="mt-1 text-sm text-gray-300">{threat.description}</p>
      </div>
    </div>
  );
}