import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { TicketIcon, CalendarIcon, MapPinIcon, ClockIcon, CheckCircleIcon, ArrowDownTrayIcon } from '@heroicons/react/24/outline';
import QRCode from 'qrcode';
import jsPDF from 'jspdf';
import { logger } from '../utils/logger';

export default function MisTickets() {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const ticketsPerPage = 5;

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
      logger.error('Error cargando tickets:', err);
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

  // Paginación
  const indexOfLastTicket = currentPage * ticketsPerPage;
  const indexOfFirstTicket = indexOfLastTicket - ticketsPerPage;
  const currentTickets = tickets.slice(indexOfFirstTicket, indexOfLastTicket);
  const totalPages = Math.ceil(tickets.length / ticketsPerPage);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const descargarTicketPDF = async (ticket) => {
    try {
      const pdf = new jsPDF({
        orientation: 'portrait',
        unit: 'mm',
        format: 'a4'
      });

      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();

      // Fondo con gradiente (simulado con rectángulos)
      pdf.setFillColor(20, 184, 166); // teal-500
      pdf.rect(0, 0, pageWidth, 50, 'F');
      
      // Header
      pdf.setTextColor(255, 255, 255);
      pdf.setFontSize(28);
      pdf.setFont('helvetica', 'bold');
      pdf.text('TICKET EXPRESS', pageWidth / 2, 20, { align: 'center' });
      
      pdf.setFontSize(14);
      pdf.setFont('helvetica', 'normal');
      pdf.text('Tu entrada al mejor entretenimiento', pageWidth / 2, 30, { align: 'center' });

      // Ticket ID Badge
      pdf.setFillColor(255, 255, 255);
      pdf.roundedRect(pageWidth / 2 - 40, 35, 80, 10, 2, 2, 'F');
      pdf.setTextColor(20, 184, 166);
      pdf.setFontSize(9);
      pdf.setFont('courier', 'bold');
      pdf.text(ticket.ticketId, pageWidth / 2, 41.5, { align: 'center' });

      // Línea divisoria
      pdf.setDrawColor(200, 200, 200);
      pdf.setLineWidth(0.5);
      pdf.line(20, 55, pageWidth - 20, 55);

      // Título del evento
      pdf.setTextColor(0, 0, 0);
      pdf.setFontSize(22);
      pdf.setFont('helvetica', 'bold');
      const eventoNombre = ticket.eventoNombre || 'Evento';
      const splitNombre = pdf.splitTextToSize(eventoNombre, pageWidth - 40);
      let currentY = 70;
      splitNombre.forEach((line, index) => {
        pdf.text(line, pageWidth / 2, currentY + (index * 8), { align: 'center' });
      });

      // Estado del ticket
      currentY = currentY + (splitNombre.length * 8) + 10;
      pdf.setFillColor(16, 185, 129); // green-500
      pdf.roundedRect(pageWidth / 2 - 25, currentY, 50, 8, 2, 2, 'F');
      pdf.setTextColor(255, 255, 255);
      pdf.setFontSize(10);
      pdf.setFont('helvetica', 'bold');
      pdf.text('✓ CONFIRMADO', pageWidth / 2, currentY + 5.5, { align: 'center' });

      // QR Code centrado
      currentY = currentY + 15;
      const qrSize = 80;
      const qrX = (pageWidth - qrSize) / 2;
      const qrY = currentY;
      
      pdf.setFillColor(255, 255, 255);
      pdf.roundedRect(qrX - 5, qrY - 5, qrSize + 10, qrSize + 10, 3, 3, 'F');
      pdf.setDrawColor(20, 184, 166);
      pdf.setLineWidth(2);
      pdf.roundedRect(qrX - 5, qrY - 5, qrSize + 10, qrSize + 10, 3, 3, 'S');
      
      pdf.addImage(ticket.qrDataUrl, 'PNG', qrX, qrY, qrSize, qrSize);
      
      pdf.setTextColor(100, 100, 100);
      pdf.setFontSize(9);
      pdf.setFont('helvetica', 'normal');
      pdf.text('Escanea este código en la entrada', pageWidth / 2, qrY + qrSize + 10, { align: 'center' });

      // Detalles del evento
      let yPos = qrY + qrSize + 20;
      pdf.setFillColor(249, 250, 251); // gray-50
      pdf.roundedRect(20, yPos, pageWidth - 40, 70, 3, 3, 'F');
      
      yPos += 10;
      pdf.setTextColor(0, 0, 0);
      pdf.setFontSize(12);
      pdf.setFont('helvetica', 'bold');
      pdf.text('DETALLES DEL EVENTO', 30, yPos);

      // Fecha
      yPos += 10;
      pdf.setFontSize(9);
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(100, 100, 100);
      pdf.text('Fecha:', 30, yPos);
      pdf.setFont('helvetica', 'normal');
      pdf.setTextColor(0, 0, 0);
      const fechaTexto = ticket.evento?.fechaEvento 
        ? new Date(ticket.evento.fechaEvento).toLocaleDateString('es-PE', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
          })
        : 'Por definir';
      const splitFecha = pdf.splitTextToSize(fechaTexto, pageWidth - 80);
      splitFecha.forEach((line, index) => {
        pdf.text(line, 50, yPos + (index * 5));
      });

      // Ubicación
      yPos += (splitFecha.length * 5) + 5;
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(100, 100, 100);
      pdf.text('Lugar:', 30, yPos);
      pdf.setFont('helvetica', 'normal');
      pdf.setTextColor(0, 0, 0);
      const ubicacionTexto = ticket.evento?.ubicacion || 'Por definir';
      const splitUbicacion = pdf.splitTextToSize(ubicacionTexto, pageWidth - 80);
      splitUbicacion.forEach((line, index) => {
        pdf.text(line, 50, yPos + (index * 5));
      });

      // Tipo de entrada
      yPos += (splitUbicacion.length * 5) + 5;
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(100, 100, 100);
      pdf.text('Tipo:', 30, yPos);
      pdf.setFont('helvetica', 'normal');
      pdf.setTextColor(0, 0, 0);
      pdf.text(ticket.tipoEntrada || 'General', 50, yPos);

      // Cantidad
      yPos += 8;
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(100, 100, 100);
      pdf.text('Cantidad:', 30, yPos);
      pdf.setFont('helvetica', 'normal');
      pdf.setTextColor(0, 0, 0);
      pdf.text(`${ticket.cantidad || 1} entrada(s)`, 50, yPos);

      // Precio total
      yPos += 13;
      pdf.setDrawColor(200, 200, 200);
      pdf.line(30, yPos - 3, pageWidth - 30, yPos - 3);
      
      pdf.setFontSize(11);
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(0, 0, 0);
      pdf.text('TOTAL PAGADO:', 30, yPos + 3);
      pdf.setFontSize(16);
      pdf.setTextColor(20, 184, 166);
      const precioTotal = (ticket.total || ticket.precioTotal || 0).toFixed(2);
      pdf.text(`S/ ${precioTotal}`, pageWidth - 30, yPos + 3, { align: 'right' });

      // Footer
      const footerY = Math.max(yPos + 20, pageHeight - 25);
      pdf.setFillColor(243, 244, 246);
      pdf.rect(0, footerY, pageWidth, 25, 'F');
      
      pdf.setTextColor(100, 100, 100);
      pdf.setFontSize(8);
      pdf.setFont('helvetica', 'normal');
      pdf.text('Ticket Express © 2025 - Todos los derechos reservados', pageWidth / 2, footerY + 6, { align: 'center' });
      pdf.text('Conserva este ticket para el ingreso al evento', pageWidth / 2, footerY + 11, { align: 'center' });
      
      pdf.setFont('helvetica', 'italic');
      pdf.text(`Fecha de compra: ${ticket.fechaCompra ? new Date(ticket.fechaCompra).toLocaleDateString('es-PE') : 'N/A'}`, pageWidth / 2, footerY + 16, { align: 'center' });

      // Descargar PDF
      const fileName = `ticket-${ticket.ticketId}-${eventoNombre.replace(/[^a-z0-9]/gi, '-').toLowerCase()}.pdf`;
      pdf.save(fileName);
    } catch (error) {
      console.error('Error generando PDF:', error);
      alert('Hubo un error al generar el PDF. Por favor, intenta nuevamente.');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-black mx-auto mb-4"></div>
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
          <h1 className="text-4xl font-black text-gray-900 mb-2">Mis Tickets</h1>
          <p className="text-gray-600 text-lg">Gestiona y visualiza todos tus tickets comprados</p>
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
            <h3 className="text-2xl font-black text-gray-900 mb-2">No tienes tickets</h3>
            <p className="text-gray-600 mb-6">Explora nuestros eventos y compra tu primer ticket</p>
            <button
              onClick={() => navigate('/')}
              className="bg-black text-white px-8 py-3 rounded-lg hover:bg-gray-800 transition font-bold shadow-lg"
            >
              Ver Eventos
            </button>
          </div>
        ) : (
          <>
            <div className="space-y-6">
              {currentTickets.map((ticket) => (
                <div
                  key={ticket.id}
                  className="bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition"
                >
                  <div className="md:flex">
                    {/* QR Code Section */}
                    <div className="md:w-1/3 bg-gradient-to-br from-teal-500 to-teal-600 p-8 flex flex-col items-center justify-center">
                      <div className="bg-white p-4 rounded-xl shadow-lg mb-4">
                        <img src={ticket.qrDataUrl} alt="QR Code" className="w-48 h-48" />
                      </div>
                      <div className="text-center">
                        <p className="text-white text-sm font-bold mb-1">Código de Ticket</p>
                        <p className="text-white text-xs font-mono bg-white/20 px-3 py-1.5 rounded">
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
                        <span className="bg-teal-100 text-teal-800 px-3 py-1 rounded-full text-sm font-bold">
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
                            <p className="text-2xl font-bold text-gray-900 mb-1">
                              S/ {(ticket.total || ticket.precioTotal || 0).toFixed(2)}
                            </p>
                          </div>
                          <button
                            onClick={() => descargarTicketPDF(ticket)}
                            className="flex items-center gap-2 bg-teal-600 text-white px-6 py-2.5 rounded-lg hover:bg-teal-700 transition font-bold shadow-md"
                          >
                            <ArrowDownTrayIcon className="h-5 w-5" />
                            Descargar PDF
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Paginación */}
            {totalPages > 1 && (
              <div className="mt-8 flex justify-center items-center gap-2">
                <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 1}
                  className={`px-4 py-2 rounded-lg font-medium transition ${
                    currentPage === 1
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                      : 'bg-white text-gray-700 hover:bg-gray-100 shadow-sm'
                  }`}
                >
                  ← Anterior
                </button>

                <div className="flex gap-1">
                  {[...Array(totalPages)].map((_, index) => {
                    const pageNumber = index + 1;
                    return (
                      <button
                        key={pageNumber}
                        onClick={() => handlePageChange(pageNumber)}
                        className={`px-4 py-2 rounded-lg font-medium transition ${
                          currentPage === pageNumber
                            ? 'bg-teal-600 text-white shadow-md'
                            : 'bg-white text-gray-700 hover:bg-gray-100 shadow-sm'
                        }`}
                      >
                        {pageNumber}
                      </button>
                    );
                  })}
                </div>

                <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages}
                  className={`px-4 py-2 rounded-lg font-medium transition ${
                    currentPage === totalPages
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                      : 'bg-white text-gray-700 hover:bg-gray-100 shadow-sm'
                  }`}
                >
                  Siguiente →
                </button>
              </div>
            )}
          </>
        )}

        {/* Modal de Detalle del Ticket */}
        {showModal && selectedTicket && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
              {/* Header del Modal */}
              <div className="bg-gradient-to-r from-teal-500 to-teal-600 p-6 rounded-t-2xl">
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-2xl font-bold text-white mb-2">
                      {selectedTicket.eventoNombre}
                    </h2>
                    <p className="text-teal-100 text-sm font-mono">
                      ID: {selectedTicket.ticketId}
                    </p>
                  </div>
                  <button
                    onClick={() => setShowModal(false)}
                    className="text-white hover:text-teal-100 transition"
                  >
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </div>

              {/* Contenido del Modal */}
              <div className="p-8">
                {/* QR Code Grande */}
                <div className="flex justify-center mb-8">
                  <div className="bg-white p-6 rounded-xl shadow-lg border-4 border-teal-500">
                    <img 
                      src={selectedTicket.qrDataUrl} 
                      alt="QR Code" 
                      className="w-64 h-64"
                    />
                  </div>
                </div>

                {/* Estado */}
                <div className="flex justify-center mb-6">
                  <span className="inline-flex items-center bg-green-100 text-green-800 px-6 py-2 rounded-full text-sm font-bold">
                    <CheckCircleIcon className="h-5 w-5 mr-2" />
                    Ticket Confirmado
                  </span>
                </div>

                {/* Información Detallada */}
                <div className="space-y-4">
                  <div className="bg-gray-50 rounded-lg p-4">
                    <div className="flex items-start">
                      <CalendarIcon className="h-6 w-6 text-teal-600 mr-3 mt-1" />
                      <div>
                        <p className="text-sm text-gray-600 font-medium">Fecha y Hora del Evento</p>
                        <p className="text-lg text-gray-900 font-semibold">
                          {selectedTicket.evento?.fechaEvento 
                            ? formatearFecha(selectedTicket.evento.fechaEvento)
                            : 'Por definir'}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-lg p-4">
                    <div className="flex items-start">
                      <MapPinIcon className="h-6 w-6 text-teal-600 mr-3 mt-1" />
                      <div>
                        <p className="text-sm text-gray-600 font-medium">Ubicación</p>
                        <p className="text-lg text-gray-900 font-semibold">
                          {selectedTicket.evento?.ubicacion || 'Por definir'}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-gray-50 rounded-lg p-4">
                      <p className="text-sm text-gray-600 font-medium mb-1">Tipo de Entrada</p>
                      <p className="text-lg text-gray-900 font-semibold">
                        {selectedTicket.tipoEntrada || 'General'}
                      </p>
                    </div>

                    <div className="bg-gray-50 rounded-lg p-4">
                      <p className="text-sm text-gray-600 font-medium mb-1">Cantidad</p>
                      <p className="text-lg text-gray-900 font-semibold">
                        {selectedTicket.cantidad || 1} entrada(s)
                      </p>
                    </div>
                  </div>

                  <div className="bg-teal-50 border-2 border-teal-200 rounded-lg p-4">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-700 font-medium">Total Pagado</span>
                      <span className="text-3xl font-bold text-teal-700">
                        S/ {(selectedTicket.total || selectedTicket.precioTotal || 0).toFixed(2)}
                      </span>
                    </div>
                  </div>

                  <div className="bg-gray-50 rounded-lg p-4">
                    <p className="text-sm text-gray-600 font-medium mb-1">Fecha de Compra</p>
                    <p className="text-gray-900">
                      {selectedTicket.fechaCompra 
                        ? new Date(selectedTicket.fechaCompra).toLocaleDateString('es-PE', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric'
                          })
                        : 'N/A'}
                    </p>
                  </div>
                </div>

                {/* Instrucciones */}
                <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <p className="text-sm text-blue-800 font-medium mb-2">
                    📱 Instrucciones de uso:
                  </p>
                  <ul className="text-sm text-blue-700 space-y-1 list-disc list-inside">
                    <li>Presenta este QR en la entrada del evento</li>
                    <li>Puedes mostrar el código desde tu celular o impreso</li>
                    <li>Llega con anticipación para evitar congestión</li>
                    <li>Conserva tu ticket hasta finalizar el evento</li>
                  </ul>
                </div>

                {/* Botones de Acción */}
                <div className="mt-8">
                  <button
                    onClick={() => descargarTicketPDF(selectedTicket)}
                    className="w-full flex items-center justify-center gap-2 bg-teal-600 text-white px-6 py-3 rounded-lg hover:bg-teal-700 transition font-bold shadow-lg"
                  >
                    <ArrowDownTrayIcon className="h-5 w-5" />
                    Descargar PDF
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
