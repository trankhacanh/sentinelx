function colorFor(score) {
  if (score >= 80) return 'bg-severity-critical';
  if (score >= 60) return 'bg-severity-high';
  if (score >= 30) return 'bg-severity-medium';
  return 'bg-severity-low';
}

export default function RiskScoreBar({ score }) {
  return (
    <div className="flex items-center gap-2">
      <div className="h-1.5 w-20 overflow-hidden rounded-full bg-surface-border">
        <div className={`h-full ${colorFor(score)}`} style={{ width: `${score}%` }} />
      </div>
      <span className="text-xs text-gray-400">{score}</span>
    </div>
  );
}