import api from './axios';
import type { ApiResponse, AuthResponse, Booking, Customer, Driver, Page, Route, SearchResult, Vehicle } from '../types';

// Auth
export const login = (email: string, password: string) =>
  api.post<ApiResponse<AuthResponse>>('/auth/login', { email, password });

export const register = (email: string, password: string, role: string) =>
  api.post<ApiResponse<AuthResponse>>('/auth/register', { email, password, role });

export const logout = () => api.post<ApiResponse<null>>('/auth/logout');

export const changePassword = (oldPassword: string, newPassword: string) =>
  api.put<ApiResponse<null>>('/auth/change-password', { oldPassword, newPassword });

export const forgotPassword = (email: string, newPassword: string) =>
  api.post<ApiResponse<null>>('/auth/forgot-password', { email, newPassword });

// Customers (admin-service)
export const adminGetAllCustomers = () =>
  api.get<ApiResponse<Customer[]>>('/admin/customers');

export const adminGetCustomer = (id: number) =>
  api.get<ApiResponse<Customer>>(`/admin/customers/${id}`);

export const adminCreateCustomer = (data: Partial<Customer> & { password: string }) =>
  api.post<ApiResponse<Customer>>('/admin/customers', data);

export const adminUpdateCustomer = (id: number, data: Partial<Customer>) =>
  api.put<ApiResponse<Customer>>(`/admin/customers/${id}`, data);

export const adminDeleteCustomer = (id: number) =>
  api.delete<ApiResponse<null>>(`/admin/customers/${id}`);

// Drivers
export const getAllDrivers = () =>
  api.get<ApiResponse<Driver[]>>('/admin/drivers');

export const getDriver = (id: number) =>
  api.get<ApiResponse<Driver>>(`/admin/drivers/${id}`);

export const createDriver = (data: Partial<Driver>) =>
  api.post<ApiResponse<Driver>>('/admin/drivers', data);

export const updateDriver = (id: number, data: Partial<Driver>) =>
  api.put<ApiResponse<Driver>>(`/admin/drivers/${id}`, data);

export const deleteDriver = (id: number) =>
  api.delete<ApiResponse<null>>(`/admin/drivers/${id}`);

// Vehicles
export const getAllVehicles = () =>
  api.get<ApiResponse<Vehicle[]>>('/admin/vehicles');

export const getAvailableVehicles = () =>
  api.get<ApiResponse<Vehicle[]>>('/admin/vehicles/available');

export const createVehicle = (data: Partial<Vehicle>) =>
  api.post<ApiResponse<Vehicle>>('/admin/vehicles', data);

export const updateVehicle = (id: number, data: Partial<Vehicle>) =>
  api.put<ApiResponse<Vehicle>>(`/admin/vehicles/${id}`, data);

export const deleteVehicle = (id: number) =>
  api.delete<ApiResponse<null>>(`/admin/vehicles/${id}`);

// Routes
export const getAllRoutes = () =>
  api.get<ApiResponse<Route[]>>('/admin/routes');

export const searchRoutes = (source: string, destination: string) =>
  api.get<ApiResponse<Route[]>>(`/admin/routes/search?source=${source}&destination=${destination}`);

export const createRoute = (data: Partial<Route>) =>
  api.post<ApiResponse<Route>>('/admin/routes', data);

export const updateRoute = (id: number, data: Partial<Route>) =>
  api.put<ApiResponse<Route>>(`/admin/routes/${id}`, data);

export const deleteRoute = (id: number) =>
  api.delete<ApiResponse<null>>(`/admin/routes/${id}`);

// Bookings
export const searchVehicles = (data: {
  source: string; destination: string; journeyDate: string;
  vehicleType?: string; maxFare?: number;
}) => api.post<ApiResponse<SearchResult[]>>('/booking/search', data);

export const createBooking = (data: {
  customerId: number; routeId: number; journeyDate: string;
  boardingPoint: string; dropPoint: string; passengerCount: number;
}) => api.post<ApiResponse<Booking>>('/booking', data);

export const cancelBooking = (id: number) =>
  api.put<ApiResponse<Booking>>(`/booking/${id}/cancel`);

export const getBooking = (id: number) =>
  api.get<ApiResponse<Booking>>(`/booking/${id}`);

export const getBookingsByCustomer = (customerId: number, page = 0, size = 10) =>
  api.get<ApiResponse<Page<Booking>>>(`/booking/customer/${customerId}?page=${page}&size=${size}`);

export const getAllBookings = (page = 0, size = 10) =>
  api.get<ApiResponse<Page<Booking>>>(`/booking?page=${page}&size=${size}`);

export const updateBookingStatus = (id: number, status: string) =>
  api.put<ApiResponse<Booking>>(`/booking/${id}/status?status=${status}`);

// Customer profile (customer-service)
export const getCustomerProfile = (email: string) =>
  api.get<ApiResponse<Customer>>(`/customers/profile?email=${encodeURIComponent(email)}`);

export const updateCustomerProfile = (id: number, data: { name: string; phone: string; address: string; gender: string; dob: string }) => {
  const payload = {
    name: data.name,
    phone: data.phone,
    address: data.address || null,
    gender: data.gender || null,
    dob: data.dob || null,
  };
  return api.put<ApiResponse<Customer>>(`/customers/${id}`, payload);
};

export const getCustomerBookings = (id: number) =>
  api.get<ApiResponse<object[]>>(`/customers/${id}/bookings`);
