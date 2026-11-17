import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { updateCustomerProfile, changePassword } from '../api/services';
import { User, Lock } from 'lucide-react';
import { Spinner } from '../components';

export default function ProfilePage() {
  const { customerProfile, profileLoading, refreshProfile } = useAuth();
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({
    name: '',
    phone: '',
    address: '',
    gender: '',
    dob: '',
  });
  const [pwForm, setPwForm] = useState({ oldPassword: '', newPassword: '' });
  const [msg, setMsg] = useState('');
  const [pwMsg, setPwMsg] = useState('');
  const [loading, setLoading] = useState(false);

  // Populate form whenever customerProfile loads or changes
  useEffect(() => {
    if (customerProfile) {
      setForm({
        name: customerProfile.name ?? '',
        phone: customerProfile.phone ?? '',
        address: customerProfile.address ?? '',
        gender: customerProfile.gender ?? '',
        dob: customerProfile.dob ?? '',
      });
    }
  }, [customerProfile]);

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customerProfile) return;
    setLoading(true);
    setMsg('');
    try {
      await updateCustomerProfile(customerProfile.customerId, form);
      await refreshProfile();
      setMsg('Profile updated successfully!');
      setEditing(false);
    } catch (err: any) {
      setMsg(err.response?.data?.message ?? 'Update failed');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setPwMsg('');
    try {
      await changePassword(pwForm.oldPassword, pwForm.newPassword);
      setPwMsg('Password changed successfully!');
      setPwForm({ oldPassword: '', newPassword: '' });
    } catch (err: any) {
      setPwMsg(err.response?.data?.message ?? 'Failed to change password');
    }
  };

  if (profileLoading) return <div className="p-6"><Spinner /></div>;
  if (!customerProfile) return <div className="p-6 text-gray-500">Could not load profile. Please try refreshing.</div>;

  return (
    <div className="max-w-2xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">My Profile</h1>

      {/* Profile Card */}
      <div className="bg-white rounded-xl shadow p-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="bg-blue-100 p-4 rounded-full">
            <User size={32} className="text-blue-700" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-gray-800">{customerProfile.name}</h2>
            <p className="text-gray-500 text-sm">{customerProfile.email}</p>
          </div>
        </div>

        {!editing ? (
          <div className="grid grid-cols-2 gap-4 text-sm">
            {([['Phone', customerProfile.phone], ['Gender', customerProfile.gender], ['Date of Birth', customerProfile.dob], ['Address', customerProfile.address]] as [string, string | undefined][]).map(([label, value]) => (
              <div key={label}>
                <p className="text-gray-400 text-xs uppercase tracking-wide">{label}</p>
                <p className="text-gray-700 font-medium">{value || '—'}</p>
              </div>
            ))}
            <div className="col-span-2 mt-2">
              <button onClick={() => { setEditing(true); setMsg(''); }}
                className="bg-blue-700 hover:bg-blue-800 text-white px-4 py-2 rounded-lg text-sm">
                Edit Profile
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleUpdate} className="grid grid-cols-2 gap-4">
            {([['Name', 'name', 'text'], ['Phone', 'phone', 'tel'], ['Gender', 'gender', 'text'], ['Date of Birth', 'dob', 'date'], ['Address', 'address', 'text']] as [string, string, string][]).map(([label, key, type]) => (
              <div key={key} className={key === 'address' ? 'col-span-2' : ''}>
                <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
                <input
                  type={type}
                  value={(form as any)[key]}
                  onChange={e => set(key, e.target.value)}
                  className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            ))}
            {msg && (
              <div className={`col-span-2 text-sm px-3 py-2 rounded-lg ${msg.includes('success') ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
                {msg}
              </div>
            )}
            <div className="col-span-2 flex gap-2">
              <button type="submit" disabled={loading}
                className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm disabled:opacity-60">
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
              <button type="button" onClick={() => { setEditing(false); setMsg(''); }}
                className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg text-sm">
                Cancel
              </button>
            </div>
          </form>
        )}
      </div>

      {/* Change Password */}
      <div className="bg-white rounded-xl shadow p-6">
        <div className="flex items-center gap-2 mb-4">
          <Lock size={20} className="text-gray-600" />
          <h2 className="font-semibold text-gray-800">Change Password</h2>
        </div>
        <form onSubmit={handlePasswordChange} className="space-y-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Current Password</label>
            <input type="password" required value={pwForm.oldPassword}
              onChange={e => setPwForm(f => ({ ...f, oldPassword: e.target.value }))}
              className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">New Password</label>
            <input type="password" required minLength={8} value={pwForm.newPassword}
              onChange={e => setPwForm(f => ({ ...f, newPassword: e.target.value }))}
              className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          {pwMsg && (
            <div className={`text-sm px-3 py-2 rounded-lg ${pwMsg.includes('success') ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
              {pwMsg}
            </div>
          )}
          <button type="submit" className="bg-blue-700 hover:bg-blue-800 text-white px-4 py-2 rounded-lg text-sm">
            Change Password
          </button>
        </form>
      </div>
    </div>
  );
}
