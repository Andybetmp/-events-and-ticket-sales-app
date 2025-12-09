import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { logger } from '../utils/logger';
import {
  CalendarIcon,
  MapPinIcon,
  ClockIcon,
  TicketIcon,
  UserGroupIcon,
  TagIcon,
  MinusIcon,
  PlusIcon
} from '@heroicons/react/24/outline';

export default function EventoDetalle() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [evento, setEvento] = useState(null);
  const [tiposEntrada, setTiposEntrada] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cantidades, setCantidades] = useState({});
  
  const MAX_ENTRADAS_POR_COMPRA = 5; // Límite máximo de entradas por transacción

  useEffect(() => {
    cargarEvento();
    cargarTiposEntrada();
  }, [id]);

  const cargarEvento = async () => {
    try {
      const response = await axios.get(`/api/eventos/${id}`);
      setEvento(response.data);
    } catch (err) {
      logger.error('Error cargando evento:', err);
      setError('No se pudo cargar el evento');
    } finally {
      setLoading(false);
    }
  };

  const cargarTiposEntrada = async () => {
    try {
      const response = await axios.get(`/api/eventos/${id}/tipos-entrada`);
      setTiposEntrada(response.data);
      
      // Inicializar cantidades en 0
      const cantidadesIniciales = {};
      response.data.forEach(tipo => {
        cantidadesIniciales[tipo.id] = 0;
      });
      setCantidades(cantidadesIniciales);
    } catch (err) {
      console.error('Error cargando tipos de entrada:', err);
    }
  };

  const incrementarCantidad = (tipoId, cantidadDisponible) => {
    const totalActual = getTotalTickets();
    
    // Verificar si ya se alcanzó el límite máximo
    if (totalActual >= MAX_ENTRADAS_POR_COMPRA) {
      alert(`Máximo ${MAX_ENTRADAS_POR_COMPRA} entradas por compra`);
      return;
    }
    
    setCantidades(prev => ({
      ...prev,
      [tipoId]: Math.min((prev[tipoId] || 0) + 1, cantidadDisponible)
    }));
  };

  const decrementarCantidad = (tipoId) => {
    setCantidades(prev => ({
      ...prev,
      [tipoId]: Math.max((prev[tipoId] || 0) - 1, 0)
    }));
  };

  const calcularTotal = () => {
    return tiposEntrada.reduce((total, tipo) => {
      const cantidad = cantidades[tipo.id] || 0;
      return total + (tipo.precio * cantidad);
    }, 0);
  };

  const getTotalTickets = () => {
    return Object.values(cantidades).reduce((sum, cant) => sum + cant, 0);
  };

  const handleComprar = async () => {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user'));
    
    if (!token || !user) {
      navigate('/login');
      return;
    }

    const totalTickets = getTotalTickets();
    if (totalTickets === 0) {
      alert('Por favor selecciona al menos un ticket');
      return;
    }

    setLoading(true);

    try {
      // Preparar datos de selección
      const seleccion = tiposEntrada
        .filter(tipo => cantidades[tipo.id] > 0)
        .map(tipo => ({
          tipoEntradaId: tipo.id,
          nombre: tipo.nombre,
          cantidad: cantidades[tipo.id],
          precio: tipo.precio,
          subtotal: tipo.precio * cantidades[tipo.id]
        }));

      // CREAR RESERVAS para cada tipo de entrada seleccionado
      console.log('Creando reservas temporales...');
      const reservas = [];
      
      for (const item of seleccion) {
        try {
          const response = await axios.post(
            '/api/reservas/crear',
            {
              tipoEntradaId: item.tipoEntradaId,
              usuarioId: user.id,
              cantidad: item.cantidad
            },
            {
              headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
              }
            }
          );
          
          reservas.push(response.data);
          console.log(`Reserva creada para ${item.nombre}: ID=${response.data.id}`);
        } catch (error) {
          console.error(`Error creando reserva para ${item.nombre}:`, error);
          
          // Si falla alguna reserva, liberar las que ya se crearon
          if (reservas.length > 0) {
            console.log('Liberando reservas ya creadas...');
            for (const reserva of reservas) {
              try {
                await axios.put(
                  `/api/reservas/${reserva.id}/liberar`,
                  {},
                  {
                    headers: {
                      'Authorization': `Bearer ${token}`
                    }
                  }
                );
              } catch (liberarError) {
                console.error('Error liberando reserva:', liberarError);
              }
            }
          }
          
          throw new Error(
            error.response?.data?.message || 
            error.response?.data?.error || 
            'No se pudo reservar las entradas. Por favor intenta nuevamente.'
          );
        }
      }

      console.log(`${reservas.length} reservas creadas exitosamente`);

      // Guardar en sessionStorage con las reservas creadas y navegar a checkout
      const checkoutData = {
        evento,
        seleccion,
        reservas,  // IDs y datos de las reservas
        total: calcularTotal()
      };
      
      console.log('Guardando datos en sessionStorage:', checkoutData);
      sessionStorage.setItem('checkout-data', JSON.stringify(checkoutData));
      
      console.log('Navegando a /checkout');
      navigate('/checkout');
      
    } catch (error) {
      console.error('Error en proceso de compra:', error);
      alert(error.message || 'Error al procesar la compra. Por favor intenta nuevamente.');
      setLoading(false);
    }
  };

  const formatearFecha = (fecha) => {
    return new Date(fecha).toLocaleDateString('es-PE', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-black mx-auto mb-4"></div>
          <p className="text-gray-600">Cargando evento...</p>
        </div>
      </div>
    );
  }

  if (error || !evento) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 text-lg mb-4">{error || 'Evento no encontrado'}</p>
          <button
            onClick={() => navigate('/')}
            className="bg-black text-white px-6 py-2 rounded-lg hover:bg-gray-800 transition"
          >
            Volver a Eventos
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Imagen Principal */}
      <div className="relative h-[400px] bg-gradient-to-r from-teal-600 to-gray-900">
        {evento.imagenUrl && evento.imagenUrl.trim() !== '' ? (
          <img 
            src={evento.imagenUrl} 
            alt={evento.nombre}
            className="w-full h-full object-cover"
            onError={(e) => {
              e.target.style.display = 'none';
            }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <TicketIcon className="w-32 h-32 text-white opacity-20" />
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-32 relative z-10 pb-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Contenido Principal */}
          <div className="lg:col-span-2 space-y-6">
            {/* Título y Badge */}
            <div className="bg-white rounded-lg shadow-md p-8">
              <button
                onClick={() => navigate('/')}
                className="text-gray-600 mb-4 hover:text-gray-900 text-sm"
              >
                ← Volver
              </button>
              
              <span className="inline-block px-3 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded mb-4">
                <span className="inline-block w-2 h-2 bg-green-600 rounded-full mr-2"></span>
                {evento.categoria || 'General'}
              </span>

              <h1 className="text-3xl font-black text-gray-900 mb-4">
                {evento.nombre}
              </h1>

              <div className="text-sm text-gray-600">
                {formatearFecha(evento.fechaEvento)}
              </div>
            </div>

            {/* Descripción */}
            <div className="bg-white rounded-lg shadow-md p-8">
              <h2 className="text-xl font-bold text-gray-900 mb-4">{evento.nombre}</h2>
              <p className="text-gray-700 leading-relaxed whitespace-pre-line">
                {evento.descripcion || 'Descripción no disponible'}
              </p>

              <button className="mt-4 text-teal-600 font-semibold text-sm hover:underline">
                Leer más ↓
              </button>
            </div>

            {/* Ubicación */}
            <div className="bg-white rounded-lg shadow-md p-8">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Ubicación</h2>
              
              <div className="flex items-start space-x-3 mb-4">
                <MapPinIcon className="h-5 w-5 text-gray-600 mt-1 flex-shrink-0" />
                <div>
                  <p className="font-semibold text-gray-900">{evento.ubicacion?.split(',')[0] || evento.ubicacion}</p>
                  <p className="text-sm text-gray-600">{evento.ubicacion}</p>
                </div>
              </div>

              <button className="flex items-center space-x-2 text-gray-900 font-semibold text-sm border border-gray-300 rounded px-4 py-2 hover:bg-gray-50 transition">
                <MapPinIcon className="h-4 w-4" />
                <span>¿Cómo llegar?</span>
              </button>
            </div>

            {/* Organiza */}
            {evento.organizador && (
              <div className="bg-white rounded-lg shadow-md p-8">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Organiza</h2>
                <div className="flex items-center space-x-4">
                  <div className="w-12 h-12 bg-teal-500 rounded-full flex items-center justify-center text-white font-bold text-xl">
                    {evento.organizador.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900">{evento.organizador}</p>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Sidebar - Fecha y Entradas */}
          <div className="lg:col-span-1">
            <div className="sticky top-24 space-y-4">
              {/* Card de Fecha y Hora */}
              <div className="bg-white rounded-lg shadow-md p-6">
                <h3 className="font-bold text-gray-900 mb-4 flex items-center">
                  <CalendarIcon className="h-5 w-5 mr-2" />
                  Fecha y Hora
                </h3>
                
                {/* Selector de fecha (simulado por ahora) */}
                <div className="space-y-2 mb-4">
                  <button className="w-full text-left px-4 py-3 border-2 border-teal-500 bg-teal-50 rounded-lg">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-xs text-gray-600">
                          {new Date(evento.fechaEvento).toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short', year: '2-digit' }).toUpperCase()}
                        </p>
                        <p className="text-sm font-semibold text-gray-900">
                          {new Date(evento.fechaEvento).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
                        </p>
                      </div>
                      <div className="w-5 h-5 bg-teal-500 rounded-full flex items-center justify-center">
                        <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/>
                        </svg>
                      </div>
                    </div>
                  </button>
                </div>

                <button className="w-full text-teal-600 font-semibold text-sm hover:underline text-left">
                  Ver más opciones →
                </button>
              </div>

              {/* Card de Entradas */}
              <div className="bg-white rounded-lg shadow-md p-6">
                <h3 className="font-bold text-gray-900 mb-4 flex items-center justify-between">
                  <span className="flex items-center">
                    <TicketIcon className="h-5 w-5 mr-2" />
                    Entradas
                  </span>
                  <button className="text-teal-600 text-xs font-semibold hover:underline">
                    ¿Tienes un código?
                  </button>
                </h3>

                {evento.estado !== 'ACTIVO' ? (
                  <div className="bg-yellow-50 border border-yellow-200 rounded p-4 text-center">
                    <p className="text-sm text-yellow-800 font-semibold">
                      Evento {evento.estado}
                    </p>
                  </div>
                ) : tiposEntrada.length === 0 ? (
                  <div className="text-center py-8">
                    <TicketIcon className="h-12 w-12 text-gray-400 mx-auto mb-2" />
                    <p className="text-sm text-gray-600">No hay entradas disponibles</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {tiposEntrada.map((tipo) => (
                      <div key={tipo.id} className="border border-gray-200 rounded-lg p-4">
                        <div className="flex items-start justify-between mb-3">
                          <div className="flex-1">
                            <p className="font-bold text-gray-900 flex items-center">
                              <TicketIcon className="h-4 w-4 mr-1" />
                              {tipo.nombre}
                            </p>
                            <p className="text-xs text-gray-600 mt-1">
                              {tipo.descripcion || 'Entrada estándar'}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-lg font-bold text-gray-900">
                              S/ {tipo.precio.toFixed(2)}
                            </p>
                            <p className="text-xs text-gray-500">
                              Disponibles: {tipo.cantidadDisponible}
                            </p>
                          </div>

                          {tipo.cantidadDisponible === 0 ? (
                            <div className="px-4 py-2 bg-red-50 border border-red-200 rounded-lg">
                              <span className="text-sm font-semibold text-red-600">
                                Agotado
                              </span>
                            </div>
                          ) : (
                            <div className="flex items-center space-x-3">
                              <button
                                onClick={() => decrementarCantidad(tipo.id)}
                                disabled={!cantidades[tipo.id] || cantidades[tipo.id] === 0}
                                className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 disabled:opacity-30 disabled:cursor-not-allowed flex items-center justify-center transition"
                              >
                                <MinusIcon className="h-4 w-4 text-gray-700" />
                              </button>
                              
                              <span className="text-lg font-bold text-gray-900 w-8 text-center">
                                {cantidades[tipo.id] || 0}
                              </span>
                              
                              <button
                                onClick={() => incrementarCantidad(tipo.id, tipo.cantidadDisponible)}
                                disabled={cantidades[tipo.id] >= tipo.cantidadDisponible || getTotalTickets() >= MAX_ENTRADAS_POR_COMPRA}
                                className="w-8 h-8 rounded-full bg-black hover:bg-gray-800 disabled:opacity-30 disabled:cursor-not-allowed flex items-center justify-center transition"
                              >
                                <PlusIcon className="h-4 w-4 text-white" />
                              </button>
                            </div>
                          )}
                        </div>
                      </div>
                    ))}

                    {/* Total y Botón de Compra */}
                    {getTotalTickets() > 0 && (
                      <div className="pt-4 border-t border-gray-200">
                        <div className="flex items-center justify-between mb-4">
                          <span className="text-sm text-gray-600">Total</span>
                          <span className="text-2xl font-bold text-gray-900">
                            S/ {calcularTotal().toFixed(2)}
                          </span>
                        </div>
                        
                        <button
                          onClick={handleComprar}
                          className="w-full bg-black text-white py-3 rounded font-bold hover:bg-gray-800 transition"
                        >
                          Continuar
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
