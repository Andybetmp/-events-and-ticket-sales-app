import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { 
  ShieldCheckIcon, 
  UserGroupIcon, 
  TicketIcon, 
  CalendarIcon,
  ChartBarIcon
} from '@heroicons/react/24/outline';
import { isAdmin } from '../utils/roleUtils';

export default function AdminPanel() {
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    totalUsuarios: 0,
    totalEventos: 0,
    totalTickets: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Verificar que el usuario sea admin
    if (!isAdmin()) {
      navigate('/');
      return;
    }

    loadStats();
  }, [navigate]);

  const loadStats = async () => {
    try {
      const token = localStorage.getItem('token');
      const headers = { Authorization: `Bearer ${token}` };

      // Cargar estadísticas básicas
      const [eventosRes, ticketsRes] = await Promise.all([
        axios.get('/api/eventos', { headers }),
        axios.get('/api/orchestration/my-tickets', { headers }).catch(() => ({ data: [] }))
      ]);

      setStats({
        totalEventos: eventosRes.data.length || 0,
        totalTickets: ticketsRes.data.length || 0,
        totalUsuarios: 0 // Pendiente endpoint
      });
    } catch (error) {
      console.error('Error cargando estadísticas:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-gradient-to-r from-amber-500 to-amber-600 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex items-center space-x-3">
            <ShieldCheckIcon className="h-10 w-10" />
            <div>
              <h1 className="text-3xl font-bold">Panel de Administración</h1>
              <p className="text-amber-100 mt-1">Gestión y estadísticas del sistema</p>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* Total Usuarios */}
          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-blue-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-medium">Total Usuarios</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.totalUsuarios}</p>
              </div>
              <UserGroupIcon className="h-12 w-12 text-blue-500 opacity-75" />
            </div>
          </div>

          {/* Total Eventos */}
          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-purple-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-medium">Total Eventos</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.totalEventos}</p>
              </div>
              <CalendarIcon className="h-12 w-12 text-purple-500 opacity-75" />
            </div>
          </div>

          {/* Total Tickets */}
          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-green-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-medium">Tickets Vendidos</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.totalTickets}</p>
              </div>
              <TicketIcon className="h-12 w-12 text-green-500 opacity-75" />
            </div>
          </div>

          {/* Ingresos */}
          <div className="bg-white rounded-xl shadow-md p-6 border-l-4 border-amber-500">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-medium">Sistema</p>
                <p className="text-lg font-bold text-gray-900 mt-2">Activo</p>
              </div>
              <ChartBarIcon className="h-12 w-12 text-amber-500 opacity-75" />
            </div>
          </div>
        </div>

        {/* Admin Actions */}
        <div className="bg-white rounded-xl shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Acciones de Administrador</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button
              onClick={() => navigate('/')}
              className="flex items-center space-x-3 p-4 bg-gradient-to-r from-purple-500 to-purple-600 text-white rounded-lg hover:from-purple-600 hover:to-purple-700 transition"
            >
              <CalendarIcon className="h-6 w-6" />
              <div className="text-left">
                <p className="font-semibold">Gestionar Eventos</p>
                <p className="text-sm text-purple-100">Ver y editar todos los eventos</p>
              </div>
            </button>

            <button
              onClick={() => alert('Próximamente: Gestión de usuarios')}
              className="flex items-center space-x-3 p-4 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 transition"
            >
              <UserGroupIcon className="h-6 w-6" />
              <div className="text-left">
                <p className="font-semibold">Gestionar Usuarios</p>
                <p className="text-sm text-blue-100">Administrar cuentas de usuario</p>
              </div>
            </button>

            <button
              onClick={() => alert('Próximamente: Reportes')}
              className="flex items-center space-x-3 p-4 bg-gradient-to-r from-green-500 to-green-600 text-white rounded-lg hover:from-green-600 hover:to-green-700 transition"
            >
              <ChartBarIcon className="h-6 w-6" />
              <div className="text-left">
                <p className="font-semibold">Reportes</p>
                <p className="text-sm text-green-100">Ver estadísticas detalladas</p>
              </div>
            </button>

            <button
              onClick={() => navigate('/mis-tickets')}
              className="flex items-center space-x-3 p-4 bg-gradient-to-r from-amber-500 to-amber-600 text-white rounded-lg hover:from-amber-600 hover:to-amber-700 transition"
            >
              <TicketIcon className="h-6 w-6" />
              <div className="text-left">
                <p className="font-semibold">Ver Tickets</p>
                <p className="text-sm text-amber-100">Revisar tickets del sistema</p>
              </div>
            </button>
          </div>
        </div>

        {/* Información del Sistema */}
        <div className="bg-white rounded-xl shadow-md p-6 mt-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Permisos de Administrador</h2>
          <div className="space-y-3 text-gray-700">
            <div className="flex items-start space-x-3">
              <div className="flex-shrink-0 mt-0.5">
                <div className="h-2 w-2 bg-green-500 rounded-full"></div>
              </div>
              <p><span className="font-semibold">Gestión Total:</span> Crear, editar y eliminar cualquier evento</p>
            </div>
            <div className="flex items-start space-x-3">
              <div className="flex-shrink-0 mt-0.5">
                <div className="h-2 w-2 bg-green-500 rounded-full"></div>
              </div>
              <p><span className="font-semibold">Usuarios:</span> Ver listado completo de usuarios registrados</p>
            </div>
            <div className="flex items-start space-x-3">
              <div className="flex-shrink-0 mt-0.5">
                <div className="h-2 w-2 bg-green-500 rounded-full"></div>
              </div>
              <p><span className="font-semibold">Compras:</span> Acceso a todos los tickets y transacciones</p>
            </div>
            <div className="flex items-start space-x-3">
              <div className="flex-shrink-0 mt-0.5">
                <div className="h-2 w-2 bg-green-500 rounded-full"></div>
              </div>
              <p><span className="font-semibold">Sistema:</span> Eliminar usuarios y contenido inapropiado</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
