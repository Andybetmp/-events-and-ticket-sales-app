import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { TicketIcon, CalendarIcon, MapPinIcon, ClockIcon, CheckCircleIcon } from '@heroicons/react/24/outline';
import QRCode from 'qrcode';

export default function MisTickets() {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user') || 'null');

    if (!token || !user) {
      navigate('/login');
      return;
    }

    cargarTickets();
  }, [navigate]);

  const cargarTickets = async () => {
    try {
      setLoading(true);
      const user = JSON.parse(localStorage.getItem('user'));
      const token = localStorage.getItem('token');
      
      const response = await axios.get('/api/orchestration/my-tickets', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'X-User-ID': user.id
        }
      });
      
      if (Array.isArray(response.data)) {
        if (response.data.length === 0) {
          setTickets([]);
        } else {
          // Obtener información completa de los eventos
          const ticketsCompletos = await Promise.all(
            response.data.map(async (ticket) => {
              try {
                // Buscar el evento por nombre para obtener más detalles
                const eventosResponse = await axios.get('/api/eventos');
                const evento = eventosResponse.data.find(e => e.nombre === ticket.eventoNombre);
                
                // Generar QR code
                const qrDataUrl = await QRCode.toDataURL(ticket.ticketId);
                
                return { 
                  ...ticket, 
                  qrDataUrl,
                  evento: evento || null
                };
              } catch (err) {
                console.error('Error procesando ticket:', err);
                const qrDataUrl = await QRCode.toDataURL(ticket.ticketId);
                return { ...ticket, qrDataUrl, evento: null };
              }
            })
          );
          setTickets(ticketsCompletos);
        }
      } else {
        setError('Formato de respuesta inválido');
      }
    } catch (err) {
      console.error('Error cargando tickets:', err);
      setError('No se pudieron cargar tus tickets. Intenta nuevamente.');
    } finally {
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
          <p className="text-gray-600">Cargando tus tickets...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Mis Tickets</h1>
          <p className="text-gray-600">Gestiona y visualiza todos tus tickets comprados</p>
        </div>

        {/* Error */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-700">{error}</p>
          </div>
        )}

        {/* Tickets */}
        {tickets.length === 0 ? (
          <div className="bg-white rounded-xl shadow-md p-12 text-center">
            <TicketIcon className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No tienes tickets</h3>
            <p className="text-gray-600 mb-6">Explora nuestros eventos y compra tu primer ticket</p>
            <button
              onClick={() => navigate('/')}
              className="bg-primary-600 text-white px-6 py-3 rounded-lg hover:bg-primary-700 transition font-medium"
            >
              Ver Eventos
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {tickets.map((ticket) => (
              <div
                key={ticket.id}
                className="bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition"
              >
                <div className="md:flex">
                  {/* QR Code Section */}
                  <div className="md:w-1/3 bg-gradient-to-br from-primary-600 to-purple-700 p-8 flex flex-col items-center justify-center">
                    <div className="bg-white p-4 rounded-lg mb-4">
                      <img src={ticket.qrDataUrl} alt="QR Code" className="w-48 h-48" />
                    </div>
                    <div className="text-center">
                      <p className="text-white text-sm font-medium mb-1">Código de Ticket</p>
                      <p className="text-white text-xs font-mono bg-white/20 px-3 py-1 rounded">
                        {ticket.ticketId}
                      </p>
                    </div>
                  </div>

                  {/* Ticket Details */}
                  <div className="md:w-2/3 p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <h3 className="text-2xl font-bold text-gray-900 mb-1">
                          {ticket.eventoNombre || 'Evento'}
                        </h3>
                        <div className="flex items-center text-green-600 text-sm font-medium">
                          <CheckCircleIcon className="h-5 w-5 mr-1" />
                          Confirmado
                        </div>
                      </div>
                      <span className="bg-primary-100 text-primary-800 px-3 py-1 rounded-full text-sm font-semibold">
                        {ticket.tipoEntrada || 'General'}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                      <div className="flex items-start space-x-3">
                        <CalendarIcon className="h-5 w-5 text-gray-400 mt-0.5" />
                        <div>
                          <p className="text-sm text-gray-500">Fecha del Evento</p>
                          <p className="text-gray-900 font-medium">
                            {ticket.evento?.fechaEvento ? formatearFecha(ticket.evento.fechaEvento) : 'Por definir'}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-start space-x-3">
                        <MapPinIcon className="h-5 w-5 text-gray-400 mt-0.5" />
                        <div>
                          <p className="text-sm text-gray-500">Ubicación</p>
                          <p className="text-gray-900 font-medium">
                            {ticket.evento?.ubicacion || 'Por definir'}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-start space-x-3">
                        <ClockIcon className="h-5 w-5 text-gray-400 mt-0.5" />
                        <div>
                          <p className="text-sm text-gray-500">Fecha de Compra</p>
                          <p className="text-gray-900 font-medium">
                            {ticket.fechaCompra ? new Date(ticket.fechaCompra).toLocaleDateString('es-PE') : 'N/A'}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-start space-x-3">
                        <TicketIcon className="h-5 w-5 text-gray-400 mt-0.5" />
                        <div>
                          <p className="text-sm text-gray-500">Cantidad</p>
                          <p className="text-gray-900 font-medium">{ticket.cantidad || 1} ticket(s)</p>
                        </div>
                      </div>
                    </div>

                    <div className="border-t pt-4">
                      <div className="flex justify-between items-center">
                        <div>
                          <p className="text-sm text-gray-500">Precio Total</p>
                          <p className="text-2xl font-bold text-primary-600">
                            S/ {(ticket.total || ticket.precioTotal || 0).toFixed(2)}
                          </p>
                        </div>
                        <button
                          onClick={() => window.print()}
                          className="bg-gray-100 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-200 transition font-medium"
                        >
                          Imprimir Ticket
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
