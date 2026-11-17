import { useAuth } from '../context/AuthContext';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { logout as apiLogout } from '../api/services';
import { LogOut, Car, User, LayoutDashboard } from 'lucide-react';

export function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    try { await apiLogout(); } catch { /* ignore */ }
    logout();
    navigate('/login');
  };

  const isActive = (path: string) =>
    location.pathname.startsWith(path) ? 'bg-blue-700' : 'hover:bg-blue-700';

  return (
    <nav className="bg-blue-800 text-white px-6 py-3 flex items-center justify-between shadow-lg">
      <div className="flex items-center gap-2 font-bold text-xl">
        <Car size={24} />
        <span>ATA Booking</span>
      </div>
      {user && (
        <div className="flex items-center gap-2">
          {user.role === 'CUSTOMER' && (
            <>
              <Link to="/dashboard" className={`px-3 py-1.5 rounded text-sm flex items-center gap-1 ${isActive('/dashboard')}`}>
                <LayoutDashboard size={16} /> Dashboard
              </Link>
              <Link to="/profile" className={`px-3 py-1.5 rounded text-sm flex items-center gap-1 ${isActive('/profile')}`}>
                <User size={16} /> Profile
              </Link>
            </>
          )}
          {user.role === 'ADMIN' && (
            <Link to="/admin" className={`px-3 py-1.5 rounded text-sm flex items-center gap-1 ${isActive('/admin')}`}>
              <LayoutDashboard size={16} /> Admin Panel
            </Link>
          )}
          <span className="text-sm text-blue-200 ml-2">{user.email}</span>
          <button onClick={handleLogout} className="ml-2 px-3 py-1.5 bg-red-600 hover:bg-red-700 rounded text-sm flex items-center gap-1">
            <LogOut size={16} /> Logout
          </button>
        </div>
      )}
    </nav>
  );
}

export function StatusBadge({ status }: { status: string }) {
  const colors: Record<string, string> = {
    CONFIRMED: 'bg-green-100 text-green-800',
    PENDING: 'bg-yellow-100 text-yellow-800',
    ONGOING: 'bg-blue-100 text-blue-800',
    COMPLETED: 'bg-gray-100 text-gray-800',
    CANCELLED: 'bg-red-100 text-red-800',
    AVAILABLE: 'bg-green-100 text-green-800',
    BOOKED: 'bg-orange-100 text-orange-800',
    MAINTENANCE: 'bg-red-100 text-red-800',
    BUSY: 'bg-orange-100 text-orange-800',
    INACTIVE: 'bg-gray-100 text-gray-800',
  };
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${colors[status] ?? 'bg-gray-100 text-gray-700'}`}>
      {status}
    </span>
  );
}

export function Spinner() {
  return (
    <div className="flex justify-center items-center py-12">
      <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
    </div>
  );
}

export function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-4 border-b">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-2xl leading-none">&times;</button>
        </div>
        <div className="p-4">{children}</div>
      </div>
    </div>
  );
}

export function PaginationBar({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (p: number) => void }) {
  if (totalPages <= 1) return null;
  return (
    <div className="flex items-center justify-center gap-2 mt-4">
      <button disabled={page === 0} onClick={() => onChange(page - 1)}
        className="px-3 py-1 rounded border disabled:opacity-40 hover:bg-gray-100 text-sm">Prev</button>
      <span className="text-sm text-gray-600">Page {page + 1} of {totalPages}</span>
      <button disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}
        className="px-3 py-1 rounded border disabled:opacity-40 hover:bg-gray-100 text-sm">Next</button>
    </div>
  );
}

export function FormInput({ label, ...props }: { label: string } & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input {...props} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
    </div>
  );
}

export function FormSelect({ label, children, ...props }: { label: string } & React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <select {...props} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
        {children}
      </select>
    </div>
  );
}
