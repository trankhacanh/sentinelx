import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', roles: ['ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER', 'VIEWER'] },
  { to: '/events', label: 'Security Events', roles: ['ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER'] },
  { to: '/threats', label: 'Threats', roles: ['ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER'] },
  { to: '/alerts', label: 'Alerts', roles: ['ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER', 'VIEWER'] },
  { to: '/incidents', label: 'Incidents', roles: ['ADMIN', 'SOC_ANALYST', 'SECURITY_ENGINEER', 'VIEWER'] },
  { to: '/detection-rules', label: 'Detection Rules', roles: ['ADMIN', 'SECURITY_ENGINEER'] },
];

export default function AppLayout() {
  const { user, logout } = useAuth();
  const roles = user?.roles ?? [];

  return (
    <div className="flex h-screen">
      <aside className="w-60 flex-shrink-0 border-r border-surface-border bg-surface-panel p-4">
        <div className="mb-6 text-lg font-bold text-white">🛡️ SentinelX</div>
        <nav className="flex flex-col gap-1">
          {NAV_ITEMS.filter((item) => item.roles.some((r) => roles.includes(r))).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `rounded px-3 py-2 text-sm ${
                  isActive ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-surface-border'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-surface-border bg-surface-panel px-6 py-3">
          <div className="text-sm text-gray-400">
            {user?.username} · <span className="text-gray-500">{roles.join(', ')}</span>
          </div>
          <button
            onClick={logout}
            className="rounded bg-surface-border px-3 py-1 text-sm text-gray-200 hover:bg-gray-700"
          >
            Log out
          </button>
        </header>

        <main className="flex-1 overflow-y-auto bg-surface-base p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}