import { useState, useEffect } from 'react';
import {
  getAllBookings, updateBookingStatus,
  getAllVehicles, createVehicle, updateVehicle, deleteVehicle,
  getAllDrivers, createDriver, updateDriver, deleteDriver,
  getAllRoutes, createRoute, updateRoute, deleteRoute,
  adminGetAllCustomers, adminDeleteCustomer,
} from '../api/services';
import type { Booking, Customer, Driver, Page, Route, Vehicle } from '../types';
import { StatusBadge, Spinner, PaginationBar, Modal, FormInput, FormSelect } from '../components';
import { Car, Users, MapPin, List, UserCheck } from 'lucide-react';

type Tab = 'bookings' | 'vehicles' | 'drivers' | 'routes' | 'customers';

export default function AdminDashboard() {
  const [tab, setTab] = useState<Tab>('bookings');

  const tabs: [Tab, string, React.ReactNode][] = [
    ['bookings', 'Bookings', <List size={16} />],
    ['vehicles', 'Vehicles', <Car size={16} />],
    ['drivers', 'Drivers', <UserCheck size={16} />],
    ['routes', 'Routes', <MapPin size={16} />],
    ['customers', 'Customers', <Users size={16} />],
  ];

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Admin Dashboard</h1>
      <div className="flex gap-1 mb-6 border-b flex-wrap">
        {tabs.map(([key, label, icon]) => (
          <button key={key} onClick={() => setTab(key)}
            className={`flex items-center gap-2 px-4 py-2 text-sm font-medium border-b-2 transition -mb-px ${tab === key ? 'border-blue-600 text-blue-700' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
            {icon}{label}
          </button>
        ))}
      </div>
      {tab === 'bookings' && <BookingsTab />}
      {tab === 'vehicles' && <VehiclesTab />}
      {tab === 'drivers' && <DriversTab />}
      {tab === 'routes' && <RoutesTab />}
      {tab === 'customers' && <CustomersTab />}
    </div>
  );
}

// ─── Bookings ────────────────────────────────────────────────────────────────
function BookingsTab() {
  const [data, setData] = useState<Page<Booking> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const load = async (p: number) => {
    setLoading(true);
    try { const r = await getAllBookings(p); setData(r.data.data); }
    catch { /* ignore */ } finally { setLoading(false); }
  };

  useEffect(() => { load(page); }, [page]);

  const handleStatus = async (id: number, status: string) => {
    try { await updateBookingStatus(id, status); load(page); }
    catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  if (loading) return <Spinner />;
  return (
    <div>
      <div className="overflow-x-auto rounded-xl shadow">
        <table className="w-full text-sm bg-white">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['ID', 'Customer', 'Journey Date', 'Route', 'Passengers', 'Fare', 'Status', 'Actions'].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {data?.content.map(b => (
              <tr key={b.bookingId} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">#{b.bookingId}</td>
                <td className="px-4 py-3">{b.customerId}</td>
                <td className="px-4 py-3">{b.journeyDate}</td>
                <td className="px-4 py-3">{b.boardingPoint} → {b.dropPoint}</td>
                <td className="px-4 py-3">{b.passengerCount}</td>
                <td className="px-4 py-3 font-medium">₹{b.fare.toFixed(2)}</td>
                <td className="px-4 py-3"><StatusBadge status={b.status} /></td>
                <td className="px-4 py-3">
                  <select value={b.status} onChange={e => handleStatus(b.bookingId, e.target.value)}
                    className="border rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-500">
                    {['PENDING','CONFIRMED','ONGOING','COMPLETED','CANCELLED'].map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <PaginationBar page={page} totalPages={data?.totalPages ?? 0} onChange={setPage} />
    </div>
  );
}

// ─── Vehicles ────────────────────────────────────────────────────────────────
function VehiclesTab() {
  const [items, setItems] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState<'add' | 'edit' | null>(null);
  const [selected, setSelected] = useState<Vehicle | null>(null);
  const empty = { vehicleNumber: '', vehicleName: '', vehicleType: '', seatingCapacity: 4, farePerKm: 10, status: 'AVAILABLE', driverId: '' };
  const [form, setForm] = useState<any>(empty);

  const load = async () => { setLoading(true); try { const r = await getAllVehicles(); setItems(r.data.data); } catch { } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);

  const openEdit = (v: Vehicle) => { setSelected(v); setForm({ ...v, driverId: v.driverId ?? '' }); setModal('edit'); };
  const openAdd = () => { setSelected(null); setForm(empty); setModal('add'); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = { ...form, driverId: form.driverId ? Number(form.driverId) : undefined };
    try {
      if (modal === 'edit' && selected) await updateVehicle(selected.vehicleId, payload);
      else await createVehicle(payload);
      setModal(null); load();
    } catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this vehicle?')) return;
    try { await deleteVehicle(id); load(); } catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  if (loading) return <Spinner />;
  return (
    <div>
      <div className="flex justify-end mb-3">
        <button onClick={openAdd} className="bg-blue-700 hover:bg-blue-800 text-white px-4 py-2 rounded-lg text-sm">+ Add Vehicle</button>
      </div>
      <div className="overflow-x-auto rounded-xl shadow">
        <table className="w-full text-sm bg-white">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['Number', 'Name', 'Type', 'Seats', '₹/km', 'Status', 'Driver', 'Actions'].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {items.map(v => (
              <tr key={v.vehicleId} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{v.vehicleNumber}</td>
                <td className="px-4 py-3">{v.vehicleName}</td>
                <td className="px-4 py-3">{v.vehicleType}</td>
                <td className="px-4 py-3">{v.seatingCapacity}</td>
                <td className="px-4 py-3">{v.farePerKm}</td>
                <td className="px-4 py-3"><StatusBadge status={v.status} /></td>
                <td className="px-4 py-3">{v.driverId ?? '—'}</td>
                <td className="px-4 py-3 flex gap-2">
                  <button onClick={() => openEdit(v)} className="text-blue-600 hover:underline text-xs">Edit</button>
                  <button onClick={() => handleDelete(v.vehicleId)} className="text-red-600 hover:underline text-xs">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {modal && (
        <Modal title={modal === 'add' ? 'Add Vehicle' : 'Edit Vehicle'} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmit} className="space-y-3">
            <FormInput label="Vehicle Number" required value={form.vehicleNumber} onChange={e => setForm((f: any) => ({ ...f, vehicleNumber: e.target.value }))} />
            <FormInput label="Vehicle Name" required value={form.vehicleName} onChange={e => setForm((f: any) => ({ ...f, vehicleName: e.target.value }))} />
            <FormInput label="Vehicle Type" required value={form.vehicleType} onChange={e => setForm((f: any) => ({ ...f, vehicleType: e.target.value }))} />
            <FormInput label="Seating Capacity" type="number" min={1} required value={form.seatingCapacity} onChange={e => setForm((f: any) => ({ ...f, seatingCapacity: Number(e.target.value) }))} />
            <FormInput label="Fare Per Km (₹)" type="number" min={1} required value={form.farePerKm} onChange={e => setForm((f: any) => ({ ...f, farePerKm: Number(e.target.value) }))} />
            <FormSelect label="Status" value={form.status} onChange={e => setForm((f: any) => ({ ...f, status: e.target.value }))}>
              {['AVAILABLE','BOOKED','MAINTENANCE'].map(s => <option key={s}>{s}</option>)}
            </FormSelect>
            <FormInput label="Driver ID (optional)" type="number" value={form.driverId} onChange={e => setForm((f: any) => ({ ...f, driverId: e.target.value }))} />
            <button type="submit" className="w-full bg-blue-700 hover:bg-blue-800 text-white py-2 rounded-lg text-sm font-semibold">Save</button>
          </form>
        </Modal>
      )}
    </div>
  );
}

// ─── Drivers ─────────────────────────────────────────────────────────────────
function DriversTab() {
  const [items, setItems] = useState<Driver[]>([]);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState<'add' | 'edit' | null>(null);
  const [selected, setSelected] = useState<Driver | null>(null);
  const empty = { name: '', address: '', phone: '', licenseNumber: '', experience: 1, availabilityStatus: 'AVAILABLE' };
  const [form, setForm] = useState<any>(empty);

  const load = async () => { setLoading(true); try { const r = await getAllDrivers(); setItems(r.data.data); } catch { } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);

  const openEdit = (d: Driver) => { setSelected(d); setForm({ ...d }); setModal('edit'); };
  const openAdd = () => { setSelected(null); setForm(empty); setModal('add'); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (modal === 'edit' && selected) await updateDriver(selected.driverId, form);
      else await createDriver(form);
      setModal(null); load();
    } catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this driver?')) return;
    try { await deleteDriver(id); load(); } catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  if (loading) return <Spinner />;
  return (
    <div>
      <div className="flex justify-end mb-3">
        <button onClick={openAdd} className="bg-blue-700 hover:bg-blue-800 text-white px-4 py-2 rounded-lg text-sm">+ Add Driver</button>
      </div>
      <div className="overflow-x-auto rounded-xl shadow">
        <table className="w-full text-sm bg-white">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['Name', 'Phone', 'License', 'Experience', 'Status', 'Actions'].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {items.map(d => (
              <tr key={d.driverId} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{d.name}</td>
                <td className="px-4 py-3">{d.phone}</td>
                <td className="px-4 py-3">{d.licenseNumber}</td>
                <td className="px-4 py-3">{d.experience} yr(s)</td>
                <td className="px-4 py-3"><StatusBadge status={d.availabilityStatus} /></td>
                <td className="px-4 py-3 flex gap-2">
                  <button onClick={() => openEdit(d)} className="text-blue-600 hover:underline text-xs">Edit</button>
                  <button onClick={() => handleDelete(d.driverId)} className="text-red-600 hover:underline text-xs">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {modal && (
        <Modal title={modal === 'add' ? 'Add Driver' : 'Edit Driver'} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmit} className="space-y-3">
            <FormInput label="Name" required value={form.name} onChange={e => setForm((f: any) => ({ ...f, name: e.target.value }))} />
            <FormInput label="Address" required value={form.address} onChange={e => setForm((f: any) => ({ ...f, address: e.target.value }))} />
            <FormInput label="Phone (10 digits)" required pattern="[0-9]{10}" value={form.phone} onChange={e => setForm((f: any) => ({ ...f, phone: e.target.value }))} />
            <FormInput label="License Number" required value={form.licenseNumber} onChange={e => setForm((f: any) => ({ ...f, licenseNumber: e.target.value }))} />
            <FormInput label="Experience (years)" type="number" min={0} required value={form.experience} onChange={e => setForm((f: any) => ({ ...f, experience: Number(e.target.value) }))} />
            <FormSelect label="Availability Status" value={form.availabilityStatus} onChange={e => setForm((f: any) => ({ ...f, availabilityStatus: e.target.value }))}>
              {['AVAILABLE','BUSY','INACTIVE'].map(s => <option key={s}>{s}</option>)}
            </FormSelect>
            <button type="submit" className="w-full bg-blue-700 hover:bg-blue-800 text-white py-2 rounded-lg text-sm font-semibold">Save</button>
          </form>
        </Modal>
      )}
    </div>
  );
}

// ─── Routes ──────────────────────────────────────────────────────────────────
function RoutesTab() {
  const [items, setItems] = useState<Route[]>([]);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState<'add' | 'edit' | null>(null);
  const [selected, setSelected] = useState<Route | null>(null);
  const empty = { source: '', destination: '', distance: 0, duration: '' };
  const [form, setForm] = useState<any>(empty);

  const load = async () => { setLoading(true); try { const r = await getAllRoutes(); setItems(r.data.data); } catch { } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);

  const openEdit = (r: Route) => { setSelected(r); setForm({ ...r }); setModal('edit'); };
  const openAdd = () => { setSelected(null); setForm(empty); setModal('add'); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (modal === 'edit' && selected) await updateRoute(selected.routeId, form);
      else await createRoute(form);
      setModal(null); load();
    } catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this route?')) return;
    try { await deleteRoute(id); load(); } catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  if (loading) return <Spinner />;
  return (
    <div>
      <div className="flex justify-end mb-3">
        <button onClick={openAdd} className="bg-blue-700 hover:bg-blue-800 text-white px-4 py-2 rounded-lg text-sm">+ Add Route</button>
      </div>
      <div className="overflow-x-auto rounded-xl shadow">
        <table className="w-full text-sm bg-white">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['Source', 'Destination', 'Distance (km)', 'Duration', 'Actions'].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {items.map(r => (
              <tr key={r.routeId} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{r.source}</td>
                <td className="px-4 py-3">{r.destination}</td>
                <td className="px-4 py-3">{r.distance}</td>
                <td className="px-4 py-3">{r.duration}</td>
                <td className="px-4 py-3 flex gap-2">
                  <button onClick={() => openEdit(r)} className="text-blue-600 hover:underline text-xs">Edit</button>
                  <button onClick={() => handleDelete(r.routeId)} className="text-red-600 hover:underline text-xs">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {modal && (
        <Modal title={modal === 'add' ? 'Add Route' : 'Edit Route'} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmit} className="space-y-3">
            <FormInput label="Source" required value={form.source} onChange={e => setForm((f: any) => ({ ...f, source: e.target.value }))} />
            <FormInput label="Destination" required value={form.destination} onChange={e => setForm((f: any) => ({ ...f, destination: e.target.value }))} />
            <FormInput label="Distance (km)" type="number" min={1} required value={form.distance} onChange={e => setForm((f: any) => ({ ...f, distance: Number(e.target.value) }))} />
            <FormInput label="Duration (e.g. 2h 30m)" required value={form.duration} onChange={e => setForm((f: any) => ({ ...f, duration: e.target.value }))} />
            <button type="submit" className="w-full bg-blue-700 hover:bg-blue-800 text-white py-2 rounded-lg text-sm font-semibold">Save</button>
          </form>
        </Modal>
      )}
    </div>
  );
}

// ─── Customers ───────────────────────────────────────────────────────────────
function CustomersTab() {
  const [items, setItems] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);

  const load = async () => { setLoading(true); try { const r = await adminGetAllCustomers(); setItems(r.data.data); } catch { } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this customer?')) return;
    try { await adminDeleteCustomer(id); load(); } catch (err: any) { alert(err.response?.data?.message ?? 'Failed'); }
  };

  if (loading) return <Spinner />;
  return (
    <div className="overflow-x-auto rounded-xl shadow">
      <table className="w-full text-sm bg-white">
        <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
          <tr>{['ID', 'Name', 'Email', 'Phone', 'Active', 'Actions'].map(h => <th key={h} className="px-4 py-3 text-left">{h}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {items.map(c => (
            <tr key={c.customerId} className="hover:bg-gray-50">
              <td className="px-4 py-3">{c.customerId}</td>
              <td className="px-4 py-3 font-medium">{c.name}</td>
              <td className="px-4 py-3">{c.email}</td>
              <td className="px-4 py-3">{c.phone}</td>
              <td className="px-4 py-3"><StatusBadge status={c.active ? 'AVAILABLE' : 'INACTIVE'} /></td>
              <td className="px-4 py-3">
                <button onClick={() => handleDelete(c.customerId)} className="text-red-600 hover:underline text-xs">Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
