import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login as apiLogin, register as apiRegister } from '../api/services';
import api from '../api/axios';
import { Car } from 'lucide-react';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [isRegister, setIsRegister] = useState(false);
  const [form, setForm] = useState({ email: '', password: '', role: 'CUSTOMER', name: '', phone: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (isRegister) {
        // Step 1: register in auth-service
        const res = await apiRegister(form.email, form.password, form.role);
        const { token, refreshToken, email, role } = res.data.data;

        // Step 2: if CUSTOMER, create profile in customer-service
        // Token must be in localStorage before this call
        localStorage.setItem('token', token);
        if (role === 'CUSTOMER') {
          await api.post('/customers/register', {
            name: form.name,
            phone: form.phone,
            email: form.email,
            password: form.password,
          });
        }

        // Step 3: complete login (fetches profile)
        await login(token, refreshToken, email, role);
        navigate(role === 'ADMIN' ? '/admin' : '/dashboard');
      } else {
        const res = await apiLogin(form.email, form.password);
        const { token, refreshToken, email, role } = res.data.data;
        await login(token, refreshToken, email, role);
        navigate(role === 'ADMIN' ? '/admin' : '/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.message ?? 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 to-blue-600 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
        <div className="flex flex-col items-center mb-8">
          <div className="bg-blue-100 p-3 rounded-full mb-3">
            <Car size={36} className="text-blue-700" />
          </div>
          <h1 className="text-2xl font-bold text-gray-800">ATA Vehicle Booking</h1>
          <p className="text-gray-500 text-sm mt-1">{isRegister ? 'Create your account' : 'Sign in to continue'}</p>
        </div>

        {error && <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2 text-sm mb-4">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          {isRegister && form.role === 'CUSTOMER' && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
                <input type="text" required value={form.name} onChange={e => set('name', e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Phone (10 digits)</label>
                <input type="tel" required pattern="[0-9]{10}" value={form.phone} onChange={e => set('phone', e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </>
          )}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input type="email" required value={form.email} onChange={e => set('email', e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input type="password" required minLength={8} value={form.password} onChange={e => set('password', e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          {isRegister && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
              <select value={form.role} onChange={e => set('role', e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="CUSTOMER">Customer</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          )}
          <button type="submit" disabled={loading}
            className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold py-2.5 rounded-lg transition disabled:opacity-60">
            {loading ? 'Please wait...' : isRegister ? 'Register' : 'Login'}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          {isRegister ? 'Already have an account?' : "Don't have an account?"}{' '}
          <button onClick={() => { setIsRegister(!isRegister); setError(''); setForm({ email: '', password: '', role: 'CUSTOMER', name: '', phone: '' }); }}
            className="text-blue-700 font-semibold hover:underline">
            {isRegister ? 'Login' : 'Register'}
          </button>
        </p>
      </div>
    </div>
  );
}
