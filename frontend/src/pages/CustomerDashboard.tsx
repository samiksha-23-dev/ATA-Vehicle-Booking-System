import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { searchVehicles, createBooking, cancelBooking, getBookingsByCustomer } from '../api/services';
import type { Booking, Page, SearchResult } from '../types';
import { StatusBadge, Spinner, PaginationBar } from '../components';
import { Search, Plus, List, MapPin, Calendar, Users } from 'lucide-react';

type Tab = 'search' | 'bookings';

export default function CustomerDashboard() {
  const { customerProfile } = useAuth();
  const [tab, setTab] = useState<Tab>('search');

  // Search state
  const [searchForm, setSearchForm] = useState({ source: '', destination: '', journeyDate: '', vehicleType: '', maxFare: '' });
  const [results, setResults] = useState<SearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState('');

  // Booking form state
  const [bookingTarget, setBookingTarget] = useState<SearchResult | null>(null);
  const [bookingForm, setBookingForm] = useState({ boardingPoint: '', dropPoint: '', passengerCount: 1 });
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingMsg, setBookingMsg] = useState('');

  // My bookings state
  const [bookings, setBookings] = useState<Page<Booking> | null>(null);
  const [bookingsPage, setBookingsPage] = useState(0);
  const [bookingsLoading, setBookingsLoading] = useState(false);

  const setS = (k: string, v: string) => setSearchForm(f => ({ ...f, [k]: v }));

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setSearchError('');
    setSearching(true);
    setBookingTarget(null);
    try {
      const res = await searchVehicles({
        source: searchForm.source,
        destination: searchForm.destination,
        journeyDate: searchForm.journeyDate || undefined as any,
        vehicleType: searchForm.vehicleType || undefined,
        maxFare: searchForm.maxFare ? Number(searchForm.maxFare) : undefined,
      });
      setResults(res.data.data ?? []);
    } catch (err: any) {
      setSearchError(err.response?.data?.message ?? 'Search failed');
    } finally {
      setSearching(false);
    }
  };

  const handleBook = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!bookingTarget || !customerProfile) return;
    setBookingLoading(true);
    setBookingMsg('');
    try {
      await createBooking({
        customerId: customerProfile.customerId,
        routeId: bookingTarget.route.routeId,
        journeyDate: searchForm.journeyDate,
        boardingPoint: bookingForm.boardingPoint,
        dropPoint: bookingForm.dropPoint,
        passengerCount: bookingForm.passengerCount,
      });
      setBookingMsg('Booking confirmed!');
      setBookingTarget(null);
      setTab('bookings');
    } catch (err: any) {
      setBookingMsg(err.response?.data?.message ?? 'Booking failed');
    } finally {
      setBookingLoading(false);
    }
  };

  const loadBookings = async (page: number) => {
    if (!customerProfile) return;
    setBookingsLoading(true);
    try {
      const res = await getBookingsByCustomer(customerProfile.customerId, page);
      setBookings(res.data.data);
    } catch { /* ignore */ }
    finally { setBookingsLoading(false); }
  };

  useEffect(() => {
    if (tab === 'bookings') loadBookings(bookingsPage);
  }, [tab, bookingsPage, customerProfile]);

  const handleCancel = async (id: number) => {
    if (!confirm('Cancel this booking?')) return;
    try {
      await cancelBooking(id);
      loadBookings(bookingsPage);
    } catch (err: any) {
      alert(err.response?.data?.message ?? 'Cancel failed');
    }
  };

  return (
    <div className="max-w-5xl mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Welcome, {customerProfile?.name ?? 'Customer'}</h1>
        <p className="text-gray-500 text-sm">Search and book vehicles for your journey</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6 border-b">
        {([['search', 'Search & Book', Search], ['bookings', 'My Bookings', List]] as const).map(([key, label, Icon]) => (
          <button key={key} onClick={() => setTab(key)}
            className={`flex items-center gap-2 px-4 py-2 text-sm font-medium border-b-2 transition -mb-px ${tab === key ? 'border-blue-600 text-blue-700' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
            <Icon size={16} />{label}
          </button>
        ))}
      </div>

      {/* Search Tab */}
      {tab === 'search' && (
        <div className="space-y-6">
          <form onSubmit={handleSearch} className="bg-white rounded-xl shadow p-5 grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">From</label>
              <div className="relative">
                <MapPin size={16} className="absolute left-3 top-2.5 text-gray-400" />
                <input required value={searchForm.source} onChange={e => setS('source', e.target.value)}
                  placeholder="Source city" className="w-full border rounded-lg pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">To</label>
              <div className="relative">
                <MapPin size={16} className="absolute left-3 top-2.5 text-gray-400" />
                <input required value={searchForm.destination} onChange={e => setS('destination', e.target.value)}
                  placeholder="Destination city" className="w-full border rounded-lg pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Journey Date</label>
              <div className="relative">
                <Calendar size={16} className="absolute left-3 top-2.5 text-gray-400" />
                <input type="date" value={searchForm.journeyDate} onChange={e => setS('journeyDate', e.target.value)}
                  className="w-full border rounded-lg pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Vehicle Type (optional)</label>
              <input value={searchForm.vehicleType} onChange={e => setS('vehicleType', e.target.value)}
                placeholder="e.g. SUV, Sedan" className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Max Fare (₹, optional)</label>
              <input type="number" value={searchForm.maxFare} onChange={e => setS('maxFare', e.target.value)}
                placeholder="e.g. 2000" className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div className="flex items-end">
              <button type="submit" disabled={searching}
                className="w-full bg-blue-700 hover:bg-blue-800 text-white font-semibold py-2 rounded-lg flex items-center justify-center gap-2 disabled:opacity-60">
                <Search size={16} />{searching ? 'Searching...' : 'Search'}
              </button>
            </div>
          </form>

          {searchError && <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2 text-sm">{searchError}</div>}

          {results.length > 0 && !bookingTarget && (
            <div className="space-y-3">
              <h2 className="font-semibold text-gray-700">{results.length} result(s) found</h2>
              {results.map((r, i) => (
                <div key={i} className="bg-white rounded-xl shadow p-4 flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-gray-800">{r.vehicle.vehicleName} <span className="text-gray-400 text-sm">({r.vehicle.vehicleType})</span></p>
                    <p className="text-sm text-gray-500">{r.route.source} → {r.route.destination} · {r.route.distance} km · {r.route.duration}</p>
                    <p className="text-sm text-gray-500">Seats: {r.vehicle.seatingCapacity} · ₹{r.vehicle.farePerKm}/km</p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold text-blue-700">₹{r.estimatedFare.toFixed(2)}</p>
                    <button onClick={() => setBookingTarget(r)}
                      className="mt-2 bg-blue-700 hover:bg-blue-800 text-white text-sm px-4 py-1.5 rounded-lg flex items-center gap-1">
                      <Plus size={14} /> Book
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {bookingTarget && (
            <div className="bg-white rounded-xl shadow p-5">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-semibold text-gray-800">Complete Booking — {bookingTarget.vehicle.vehicleName}</h2>
                <button onClick={() => setBookingTarget(null)} className="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
              </div>
              <form onSubmit={handleBook} className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Boarding Point</label>
                  <input required value={bookingForm.boardingPoint} onChange={e => setBookingForm(f => ({ ...f, boardingPoint: e.target.value }))}
                    className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Drop Point</label>
                  <input required value={bookingForm.dropPoint} onChange={e => setBookingForm(f => ({ ...f, dropPoint: e.target.value }))}
                    className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Passengers</label>
                  <div className="relative">
                    <Users size={16} className="absolute left-3 top-2.5 text-gray-400" />
                    <input type="number" min={1} max={bookingTarget.vehicle.seatingCapacity} required
                      value={bookingForm.passengerCount} onChange={e => setBookingForm(f => ({ ...f, passengerCount: Number(e.target.value) }))}
                      className="w-full border rounded-lg pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>
                <div className="flex items-end">
                  <div className="w-full bg-blue-50 rounded-lg p-3 text-sm">
                    <p className="text-gray-500">Estimated Fare</p>
                    <p className="text-xl font-bold text-blue-700">
                      ₹{(bookingTarget.route.distance * bookingTarget.vehicle.farePerKm * bookingForm.passengerCount).toFixed(2)}
                    </p>
                  </div>
                </div>
                {bookingMsg && <div className={`col-span-2 text-sm px-3 py-2 rounded-lg ${bookingMsg.includes('confirmed') ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>{bookingMsg}</div>}
                <div className="col-span-2">
                  <button type="submit" disabled={bookingLoading}
                    className="w-full bg-green-600 hover:bg-green-700 text-white font-semibold py-2 rounded-lg disabled:opacity-60">
                    {bookingLoading ? 'Booking...' : 'Confirm Booking'}
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}

      {/* My Bookings Tab */}
      {tab === 'bookings' && (
        <div>
          {bookingsLoading ? <Spinner /> : (
            <>
              {!bookings || bookings.content.length === 0 ? (
                <div className="text-center py-12 text-gray-400">No bookings found.</div>
              ) : (
                <div className="space-y-3">
                  {bookings.content.map(b => (
                    <div key={b.bookingId} className="bg-white rounded-xl shadow p-4 flex items-center justify-between">
                      <div>
                        <p className="font-semibold text-gray-800">Booking #{b.bookingId}</p>
                        <p className="text-sm text-gray-500">{b.boardingPoint} → {b.dropPoint}</p>
                        <p className="text-sm text-gray-500">Journey: {b.journeyDate} · {b.passengerCount} passenger(s)</p>
                        <p className="text-sm font-medium text-blue-700 mt-1">₹{b.fare.toFixed(2)}</p>
                      </div>
                      <div className="flex flex-col items-end gap-2">
                        <StatusBadge status={b.status} />
                        {(b.status === 'CONFIRMED' || b.status === 'PENDING') && (
                          <button onClick={() => handleCancel(b.bookingId)}
                            className="text-xs text-red-600 hover:text-red-800 border border-red-300 px-2 py-1 rounded">
                            Cancel
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
              <PaginationBar page={bookingsPage} totalPages={bookings?.totalPages ?? 0} onChange={setBookingsPage} />
            </>
          )}
        </div>
      )}
    </div>
  );
}
