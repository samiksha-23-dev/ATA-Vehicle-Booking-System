import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { Customer } from '../types';
import api from '../api/axios';

interface AuthUser {
  email: string;
  role: string;
}

interface AuthContextType {
  user: AuthUser | null;
  customerProfile: Customer | null;
  profileLoading: boolean;
  login: (token: string, refreshToken: string, email: string, role: string) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const email = localStorage.getItem('email');
    const role = localStorage.getItem('role');
    return email && role ? { email, role } : null;
  });
  const [customerProfile, setCustomerProfile] = useState<Customer | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);

  const refreshProfile = useCallback(async () => {
    const email = localStorage.getItem('email');
    const role = localStorage.getItem('role');
    const token = localStorage.getItem('token');
    if (role !== 'CUSTOMER' || !email || !token) return;
    setProfileLoading(true);
    try {
      const res = await api.get(`/customers/profile?email=${encodeURIComponent(email)}`);
      setCustomerProfile(res.data.data);
    } catch {
      setCustomerProfile(null);
    } finally {
      setProfileLoading(false);
    }
  }, []);

  // Load profile on mount if already logged in as customer
  useEffect(() => {
    if (user?.role === 'CUSTOMER') {
      refreshProfile();
    }
  }, []); // only on mount

  const login = async (token: string, refreshToken: string, email: string, role: string) => {
    // Store everything first so axios interceptor picks up the token
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('email', email);
    localStorage.setItem('role', role);
    setUser({ email, role });
    // Fetch profile immediately after token is stored
    if (role === 'CUSTOMER') {
      setProfileLoading(true);
      try {
        const res = await api.get(`/customers/profile?email=${encodeURIComponent(email)}`);
        setCustomerProfile(res.data.data);
      } catch {
        setCustomerProfile(null);
      } finally {
        setProfileLoading(false);
      }
    }
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    setCustomerProfile(null);
  };

  return (
    <AuthContext.Provider value={{ user, customerProfile, profileLoading, login, logout, refreshProfile }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
