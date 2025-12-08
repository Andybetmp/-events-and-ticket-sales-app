import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
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

  useEffect(() => {
    cargarEvento();
    cargarTiposEntrada();
  }, [id]);

  const cargarEvento = async () => {
    try {
      const response = await axios.get(`/api/eventos/${id}`);
      setEvento(response.data);
    } catch (err) {
      console.error('Error cargando evento:', err);
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
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600 mx-auto mb-4"></div>
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
            className="bg-primary-600 text-white px-6 py-2 rounded-lg hover:bg-primary-700 transition"
          >
            Volver a Eventos
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header del Evento */}
        <div className="bg-white rounded-xl shadow-md overflow-hidden mb-8">
          {/* Imagen del evento si existe */}
          {evento.imagenUrl && evento.imagenUrl.trim() !== '' && (
            <div className="h-96 overflow-hidden">
              <img 
                src={evento.imagenUrl} 
                alt={evento.nombre}
                className="w-full h-full object-cover"
                onError={(e) => {
                  console.error('Error cargando imagen en detalle:', evento.imagenUrl);
                  e.target.parentElement.style.display = 'none';
                }}
              />
            </div>
          )}
          
          <div className={`${evento.imagenUrl ? 'bg-white' : 'bg-gradient-to-r from-primary-600 to-purple-700'} px-8 py-12`}>
            <button
              onClick={() => navigate('/')}
              className={`${evento.imagenUrl ? 'text-primary-600' : 'text-white'} mb-4 hover:underline`}
            >
              ← Volver a eventos
            </button>
            <h1 className={`text-4xl font-bold ${evento.imagenUrl ? 'text-gray-900' : 'text-white'} mb-2`}>
              {evento.nombre}
            </h1>
            <div className="flex items-center space-x-2">
              <span className={`${evento.imagenUrl ? 'bg-primary-100 text-primary-700' : 'bg-white/20 text-white'} px-3 py-1 rounded-full text-sm font-medium`}>
                {evento.categoria || 'General'}
              </span>
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                evento.estado === 'ACTIVO' ? 'bg-green-500 text-white' :
                evento.estado === 'CANCELADO' ? 'bg-red-500 text-white' :
                'bg-gray-500 text-white'
              }`}>
                {evento.estado === 'ACTIVO' ? 'Disponible' : evento.estado}
              </span>
            </div>
          </div>

          <div className="px-8 py-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Detalles del Evento</h2>
                
                <div className="space-y-4">
                  <div className="flex items-start space-x-3">
                    <CalendarIcon className="h-6 w-6 text-primary-600 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-500">Fecha y Hora</p>
                      <p className="text-gray-900 font-medium">
                        {formatearFecha(evento.fechaEvento)}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start space-x-3">
                    <MapPinIcon className="h-6 w-6 text-primary-600 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-500">Ubicación</p>
                      <p className="text-gray-900 font-medium">{evento.ubicacion}</p>
                    </div>
                  </div>

                  <div className="flex items-start space-x-3">
                    <UserGroupIcon className="h-6 w-6 text-primary-600 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-500">Organizador</p>
                      <p className="text-gray-900 font-medium">{evento.organizador || 'No especificado'}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Descripción</h2>
                <p className="text-gray-700 leading-relaxed">
                  {evento.descripcion || 'Descripción no disponible'}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Tipos de Entrada */}
        <div className="bg-white rounded-xl shadow-md p-8 mb-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Selecciona tus Entradas</h2>
          
          {/* Mensaje de evento no disponible */}
          {evento.estado !== 'ACTIVO' && (
            <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <p className="text-sm text-yellow-700">
                    Este evento está <strong>{evento.estado}</strong> y no se pueden comprar entradas.
                  </p>
                </div>
              </div>
            </div>
          )}
          
          {tiposEntrada.length === 0 ? (
            <div className="text-center py-8">
              <TicketIcon className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-600">No hay tipos de entrada disponibles para este evento</p>
            </div>
          ) : (
            <div className="space-y-4">
              {tiposEntrada.map((tipo) => (
                <div
                  key={tipo.id}
                  className="border border-gray-200 rounded-lg p-6 hover:border-primary-300 transition"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3 mb-2">
                        <TagIcon className="h-5 w-5 text-primary-600" />
                        <h3 className="text-lg font-semibold text-gray-900">{tipo.nombre}</h3>
                      </div>
                      <p className="text-gray-600 text-sm mb-2">{tipo.descripcion || 'Entrada estándar'}</p>
                      <div className="flex items-center space-x-4 text-sm">
                        <span className="text-gray-600">
                          Disponibles: <span className="font-medium text-gray-900">{tipo.cantidadDisponible}</span>
                        </span>
                        <span className="text-2xl font-bold text-primary-600">
                          S/ {tipo.precio.toFixed(2)}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center space-x-4 ml-6">
                      <button
                        onClick={() => decrementarCantidad(tipo.id)}
                        disabled={evento.estado !== 'ACTIVO' || !cantidades[tipo.id] || cantidades[tipo.id] === 0}
                        className="p-2 rounded-lg bg-gray-100 hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition"
                      >
                        <MinusIcon className="h-5 w-5 text-gray-700" />
                      </button>
                      
                      <span className="text-xl font-bold text-gray-900 w-12 text-center">
                        {cantidades[tipo.id] || 0}
                      </span>
                      
                      <button
                        onClick={() => incrementarCantidad(tipo.id, tipo.cantidadDisponible)}
                        disabled={evento.estado !== 'ACTIVO' || cantidades[tipo.id] >= tipo.cantidadDisponible}
                        className="p-2 rounded-lg bg-primary-600 hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                      >
                        <PlusIcon className="h-5 w-5 text-white" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Resumen y Checkout */}
        {tiposEntrada.length > 0 && (
          <div className="bg-white rounded-xl shadow-md p-8 sticky bottom-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 mb-1">Total a pagar</p>
                <p className="text-3xl font-bold text-primary-600">
                  S/ {calcularTotal().toFixed(2)}
                </p>
                <p className="text-sm text-gray-500 mt-1">
                  {getTotalTickets()} ticket(s) seleccionado(s)
                </p>
              </div>
              
              <button
                onClick={handleComprar}
                disabled={getTotalTickets() === 0 || evento.estado !== 'ACTIVO'}
                className="bg-primary-600 text-white px-8 py-4 rounded-lg hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition font-semibold text-lg shadow-lg hover:shadow-xl"
              >
                {evento.estado !== 'ACTIVO' ? `Evento ${evento.estado}` : 'Continuar al Pago'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
