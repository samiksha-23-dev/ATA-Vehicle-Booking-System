export interface AuthResponse {
  token: string;
  refreshToken: string;
  role: string;
  email: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface Customer {
  customerId: number;
  name: string;
  dob?: string;
  gender?: string;
  address?: string;
  phone: string;
  email: string;
  active: boolean;
}

export interface Driver {
  driverId: number;
  name: string;
  address: string;
  phone: string;
  licenseNumber: string;
  experience: number;
  availabilityStatus: 'AVAILABLE' | 'BUSY' | 'INACTIVE';
}

export interface Vehicle {
  vehicleId: number;
  vehicleNumber: string;
  vehicleName: string;
  vehicleType: string;
  seatingCapacity: number;
  farePerKm: number;
  status: 'AVAILABLE' | 'BOOKED' | 'MAINTENANCE';
  driverId?: number;
}

export interface Route {
  routeId: number;
  source: string;
  destination: string;
  distance: number;
  duration: string;
}

export interface Booking {
  bookingId: number;
  bookingDate: string;
  journeyDate: string;
  customerId: number;
  routeId: number;
  vehicleId: number;
  driverId: number;
  boardingPoint: string;
  dropPoint: string;
  passengerCount: number;
  fare: number;
  status: 'PENDING' | 'CONFIRMED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';
}

export interface SearchResult {
  route: Route;
  vehicle: Vehicle;
  estimatedFare: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
