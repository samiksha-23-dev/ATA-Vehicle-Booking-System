import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components';
import LoginPage from './pages/LoginPage';
import CustomerDashboard from './pages/CustomerDashboard';
import ProfilePage from './pages/ProfilePage';
import AdminDashboard from './pages/AdminDashboard';
import type { ReactNode } from 'react';

function RequireAuth({ children, role }: { children: ReactNode; role?: string }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
  return <>{children}</>;
}

function AppRoutes() {
  const { user } = useAuth();
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/login" element={user ? <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace /> : <LoginPage />} />
        <Route path="/dashboard" element={<RequireAuth role="CUSTOMER"><CustomerDashboard /></RequireAuth>} />
        <Route path="/profile" element={<RequireAuth role="CUSTOMER"><ProfilePage /></RequireAuth>} />
        <Route path="/admin" element={<RequireAuth role="ADMIN"><AdminDashboard /></RequireAuth>} />
        <Route path="*" element={<Navigate to={user ? (user.role === 'ADMIN' ? '/admin' : '/dashboard') : '/login'} replace />} />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
