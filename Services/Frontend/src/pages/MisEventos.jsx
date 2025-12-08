import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import {
  ChartBarIcon,
  TicketIcon,
  CurrencyDollarIcon,
  UserGroupIcon
} from '@heroicons/react/24/outline';

const MisEventos = () => {
  const [eventos, setEventos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedStats, setExpandedStats] = useState({});

  useEffect(() => {
    fetchMisEventos();
  }, []);

  const fetchMisEventos = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const userStr = localStorage.getItem('user');
      
      if (!userStr) {
        setError('No se encontró información del usuario');
        setLoading(false);
        return;
      }

      const user = JSON.parse(userStr);
      console.log('Usuario desde localStorage:', user);

      // Obtener todos los eventos (incluyendo cancelados y finalizados)
      const response = await axios.get('/api/eventos?onlyActive=false', {
        headers: { Authorization: `Bearer ${token}` }
      });

      console.log('Todos los eventos:', response.data);

      // El userId puede estar en diferentes propiedades dependiendo de cómo se guardó
      const userId = user.id || user.userId;
      console.log('ID del usuario:', userId);

      // Filtrar solo los eventos del usuario actual
      const misEventos = response.data.filter(
        evento => evento.organizadorId === userId
      );

      console.log('Mis eventos filtrados:', misEventos);
      setEventos(misEventos);
      setError(null);
    } catch (err) {
      console.error('Error al cargar eventos:', err);
      setError('No se pudieron cargar tus eventos');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelarEvento = async (eventoId) => {
    if (!window.confirm('¿Estás seguro de que quieres cancelar este evento?')) {
      return;
    }

    try {
      const token = localStorage.getItem('token');
      await axios.delete(`/api/eventos/${eventoId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });

      alert('Evento cancelado exitosamente');
      fetchMisEventos(); // Recargar lista
    } catch (err) {
      console.error('Error al cancelar evento:', err);
      alert(err.response?.data?.mensaje || 'No se pudo cancelar el evento');
    }
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      ACTIVO: 'bg-green-100 text-green-800',
      CANCELADO: 'bg-red-100 text-red-800',
      FINALIZADO: 'bg-gray-100 text-gray-800'
    };
    return badges[estado] || 'bg-gray-100 text-gray-800';
  };

  const toggleStats = (eventoId) => {
    setExpandedStats(prev => ({
      ...prev,
      [eventoId]: !prev[eventoId]
    }));
  };

  const calcularEstadisticas = (evento) => {
    // Calcular entradas vendidas y disponibles
    const totalCapacidad = evento.capacidadTotal || 0;
    const disponibles = evento.entradasDisponibles || 0;
    const vendidas = totalCapacidad - disponibles;
    const porcentajeVendido = totalCapacidad > 0 ? (vendidas / totalCapacidad * 100) : 0;

    // Calcular ingresos por tipo de entrada
    let ingresosTotales = 0;
    let detallesPorTipo = [];

    if (evento.tiposEntrada && evento.tiposEntrada.length > 0) {
      detallesPorTipo = evento.tiposEntrada.map(tipo => {
        const cantidadTotal = tipo.cantidadTotal || tipo.cantidad || 0;
        const cantidadDisponible = tipo.cantidadDisponible || 0;
        const cantidadVendida = cantidadTotal - cantidadDisponible;
        const ingresoPorTipo = cantidadVendida * (tipo.precio || 0);
        ingresosTotales += ingresoPorTipo;

        return {
          nombre: tipo.nombre,
          precio: tipo.precio || 0,
          vendidas: cantidadVendida,
          disponibles: cantidadDisponible,
          total: cantidadTotal,
          ingresos: ingresoPorTipo,
          porcentaje: cantidadTotal > 0 ? (cantidadVendida / cantidadTotal * 100) : 0
        };
      });
    }

    return {
      vendidas,
      disponibles,
      totalCapacidad,
      porcentajeVendido,
      ingresosTotales,
      detallesPorTipo
    };
  };

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Mis Eventos</h1>
          <p className="mt-2 text-gray-600">Gestiona los eventos que has creado</p>
        </div>
        <Link
          to="/crear-evento"
          className="bg-indigo-600 text-white px-6 py-3 rounded-lg hover:bg-indigo-700 transition"
        >
          + Crear Evento
        </Link>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      {eventos.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
          <h3 className="mt-2 text-lg font-medium text-gray-900">No tienes eventos</h3>
          <p className="mt-1 text-gray-500">Comienza creando tu primer evento</p>
          <div className="mt-6">
            <Link
              to="/crear-evento"
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700"
            >
              + Crear Evento
            </Link>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6">
          {eventos.map((evento) => {
            const stats = calcularEstadisticas(evento);
            const isStatsExpanded = expandedStats[evento.id];

            return (
            <div
              key={evento.id}
              className="bg-white rounded-lg shadow hover:shadow-lg transition overflow-hidden"
            >
              <div className="p-6">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-xl font-semibold text-gray-900">
                        {evento.nombre}
                      </h3>
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-semibold ${getEstadoBadge(
                          evento.estado
                        )}`}
                      >
                        {evento.estado}
                      </span>
                    </div>
                    <p className="text-gray-600 mb-4">{evento.descripcion}</p>
                    
                    {/* Estadísticas resumidas */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                      <div className="bg-blue-50 rounded-lg p-3">
                        <div className="flex items-center gap-2 mb-1">
                          <TicketIcon className="h-5 w-5 text-blue-600" />
                          <p className="text-xs text-blue-600 font-medium">Entradas Vendidas</p>
                        </div>
                        <p className="text-2xl font-bold text-blue-900">{stats.vendidas}</p>
                        <p className="text-xs text-blue-700">{stats.porcentajeVendido.toFixed(1)}% del total</p>
                      </div>
                      
                      <div className="bg-green-50 rounded-lg p-3">
                        <div className="flex items-center gap-2 mb-1">
                          <CurrencyDollarIcon className="h-5 w-5 text-green-600" />
                          <p className="text-xs text-green-600 font-medium">Ingresos Totales</p>
                        </div>
                        <p className="text-2xl font-bold text-green-900">S/ {stats.ingresosTotales.toFixed(2)}</p>
                        <p className="text-xs text-green-700">De {stats.vendidas} tickets</p>
                      </div>
                      
                      <div className="bg-purple-50 rounded-lg p-3">
                        <div className="flex items-center gap-2 mb-1">
                          <UserGroupIcon className="h-5 w-5 text-purple-600" />
                          <p className="text-xs text-purple-600 font-medium">Disponibles</p>
                        </div>
                        <p className="text-2xl font-bold text-purple-900">{stats.disponibles}</p>
                        <p className="text-xs text-purple-700">De {stats.totalCapacidad} total</p>
                      </div>
                      
                      <div className="bg-orange-50 rounded-lg p-3">
                        <div className="flex items-center gap-2 mb-1">
                          <ChartBarIcon className="h-5 w-5 text-orange-600" />
                          <p className="text-xs text-orange-600 font-medium">Capacidad</p>
                        </div>
                        <p className="text-2xl font-bold text-orange-900">{stats.totalCapacidad}</p>
                        <p className="text-xs text-orange-700">Personas máximo</p>
                      </div>
                    </div>

                    {/* Botón para expandir estadísticas detalladas */}
                    <button
                      onClick={() => toggleStats(evento.id)}
                      className="w-full py-2 px-4 bg-gray-50 hover:bg-gray-100 rounded-lg transition flex items-center justify-center gap-2 text-sm font-medium text-gray-700 mb-4"
                    >
                      <ChartBarIcon className="h-4 w-4" />
                      {isStatsExpanded ? 'Ocultar estadísticas detalladas' : 'Ver estadísticas detalladas'}
                    </button>

                    {/* Estadísticas detalladas por tipo de entrada */}
                    {isStatsExpanded && stats.detallesPorTipo.length > 0 && (
                      <div className="bg-gray-50 rounded-lg p-4 mb-4">
                        <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                          <ChartBarIcon className="h-5 w-5 text-indigo-600" />
                          Desglose por Tipo de Entrada
                        </h4>
                        <div className="space-y-3">
                          {stats.detallesPorTipo.map((detalle, idx) => (
                            <div key={idx} className="bg-white rounded-lg p-3 border border-gray-200">
                              <div className="flex justify-between items-start mb-2">
                                <div>
                                  <p className="font-medium text-gray-900">{detalle.nombre}</p>
                                  <p className="text-sm text-gray-600">S/ {detalle.precio.toFixed(2)} por entrada</p>
                                </div>
                                <div className="text-right">
                                  <p className="font-bold text-indigo-600">S/ {detalle.ingresos.toFixed(2)}</p>
                                  <p className="text-xs text-gray-500">Ingresos</p>
                                </div>
                              </div>
                              
                              {/* Barra de progreso */}
                              <div className="mb-2">
                                <div className="flex justify-between text-xs text-gray-600 mb-1">
                                  <span>Vendidas: {detalle.vendidas}/{detalle.total}</span>
                                  <span>{detalle.porcentaje.toFixed(1)}%</span>
                                </div>
                                <div className="w-full bg-gray-200 rounded-full h-2">
                                  <div
                                    className="bg-indigo-600 h-2 rounded-full transition-all"
                                    style={{ width: `${detalle.porcentaje}%` }}
                                  ></div>
                                </div>
                              </div>
                              
                              <div className="grid grid-cols-3 gap-2 text-xs">
                                <div className="text-center bg-blue-50 rounded py-1">
                                  <p className="text-blue-900 font-semibold">{detalle.vendidas}</p>
                                  <p className="text-blue-600">Vendidas</p>
                                </div>
                                <div className="text-center bg-green-50 rounded py-1">
                                  <p className="text-green-900 font-semibold">{detalle.disponibles}</p>
                                  <p className="text-green-600">Disponibles</p>
                                </div>
                                <div className="text-center bg-gray-50 rounded py-1">
                                  <p className="text-gray-900 font-semibold">{detalle.total}</p>
                                  <p className="text-gray-600">Total</p>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                    
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                      <div>
                        <p className="text-gray-500">Fecha</p>
                        <p className="font-medium">
                          {new Date(evento.fechaEvento).toLocaleDateString('es-ES', {
                            day: '2-digit',
                            month: 'short',
                            year: 'numeric'
                          })}
                        </p>
                      </div>
                      <div>
                        <p className="text-gray-500">Ubicación</p>
                        <p className="font-medium">{evento.ubicacion}</p>
                      </div>
                      <div>
                        <p className="text-gray-500">Categoría</p>
                        <p className="font-medium">{evento.categoria}</p>
                      </div>
                      <div>
                        <p className="text-gray-500">Creado</p>
                        <p className="font-medium">
                          {new Date(evento.createdAt || evento.fechaEvento).toLocaleDateString('es-ES')}
                        </p>
                      </div>
                    </div>

                    {/* Tipos de entrada - versión compacta */}
                    {!isStatsExpanded && (
                      <div className="mt-4">
                        <p className="text-sm text-gray-500 mb-2">Tipos de entrada:</p>
                        <div className="flex flex-wrap gap-2">
                          {evento.tiposEntrada?.map((tipo) => (
                            <span
                              key={tipo.id}
                              className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-gray-100 text-gray-800"
                            >
                              {tipo.nombre} - S/ {tipo.precio.toFixed(2)}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Imagen del evento */}
                  {evento.imagenUrl && (
                    <img
                      src={evento.imagenUrl}
                      alt={evento.nombre}
                      className="w-32 h-32 object-cover rounded-lg ml-6"
                    />
                  )}
                </div>

                {/* Acciones */}
                <div className="mt-6 flex gap-3">
                  <Link
                    to={`/evento/${evento.id}`}
                    className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition"
                  >
                    Ver Detalles
                  </Link>
                  
                  {evento.estado === 'ACTIVO' && (
                    <>
                      <Link
                        to={`/editar-evento/${evento.id}`}
                        className="px-4 py-2 bg-indigo-100 text-indigo-700 rounded-lg hover:bg-indigo-200 transition"
                      >
                        Editar
                      </Link>
                      <button
                        onClick={() => handleCancelarEvento(evento.id)}
                        className="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition"
                      >
                        Cancelar Evento
                      </button>
                    </>
                  )}
                  
                  {evento.estado === 'ACTIVO' && new Date(evento.fechaEvento) < new Date() && (
                    <button
                      className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition"
                    >
                      Finalizar Evento
                    </button>
                  )}
                </div>
              </div>
            </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default MisEventos;
