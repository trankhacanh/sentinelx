import { useEffect, useState } from 'react';
import { listDetectionRules, setRuleEnabled } from '../api/detectionRules';
import { SeverityBadge } from '../components/RoleBadge';

export default function DetectionRulesPage() {
  const [rules, setRules] = useState(null);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  function fetchRules() {
    listDetectionRules()
      .then((res) => setRules(res.data.data))
      .catch((err) => setError(err.response?.data?.message ?? 'Failed to load detection rules'));
  }

  useEffect(fetchRules, []);

  async function handleToggle(rule) {
    setActionError('');
    try {
      await setRuleEnabled(rule.id, !rule.enabled);
      fetchRules();
    } catch (err) {
      setActionError(err.response?.data?.message ?? 'Failed to update rule');
    }
  }

  if (error) return <div className="text-red-400">{error}</div>;
  if (!rules) return <div className="text-gray-400">Loading...</div>;

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-bold text-white">Detection Rules</h1>

      {actionError && <div className="text-red-400">{actionError}</div>}

      <div className="rounded-lg border border-surface-border bg-surface-panel p-4">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-gray-500">
              <th className="pb-2">Rule code</th>
              <th className="pb-2">Name</th>
              <th className="pb-2">Threat type</th>
              <th className="pb-2">Severity</th>
              <th className="pb-2">Base risk</th>
              <th className="pb-2">Enabled</th>
            </tr>
          </thead>
          <tbody>
            {rules.map((rule) => (
              <tr key={rule.id} className="border-t border-surface-border">
                <td className="py-2 font-mono text-gray-200">{rule.ruleCode}</td>
                <td className="py-2 text-gray-200">{rule.name}</td>
                <td className="py-2 text-gray-400">{rule.threatType}</td>
                <td className="py-2"><SeverityBadge severity={rule.severity} /></td>
                <td className="py-2 text-gray-400">{rule.baseRiskScore}</td>
                <td className="py-2">
                  <button
                    onClick={() => handleToggle(rule)}
                    className={`relative h-6 w-11 rounded-full transition-colors ${
                      rule.enabled ? 'bg-green-600' : 'bg-surface-border'
                    }`}
                  >
                    <span
                      className={`absolute top-0.5 h-5 w-5 rounded-full bg-white transition-transform ${
                        rule.enabled ? 'translate-x-5' : 'translate-x-0.5'
                      }`}
                    />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}