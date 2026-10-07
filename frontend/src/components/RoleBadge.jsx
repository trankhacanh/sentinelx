const SEVERITY_COLORS = {
  LOW: 'bg-severity-low',
  MEDIUM: 'bg-severity-medium',
  HIGH: 'bg-severity-high',
  CRITICAL: 'bg-severity-critical',
};

/** Dùng chung cho mọi nơi cần hiển thị severity -- một nơi duy nhất quyết định màu sắc. */
export function SeverityBadge({ severity }) {
  const color = SEVERITY_COLORS[severity] ?? 'bg-gray-500';
  return (
    <span className={`rounded px-2 py-0.5 text-xs font-semibold text-white ${color}`}>
      {severity}
    </span>
  );
}