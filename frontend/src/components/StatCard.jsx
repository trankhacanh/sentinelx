export default function StatCard({ label, value, accent }) {
  return (
    <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
      <div className="text-sm text-gray-400">{label}</div>
      <div className={`mt-1 text-3xl font-bold ${accent ?? 'text-white'}`}>{value}</div>
    </div>
  );
}