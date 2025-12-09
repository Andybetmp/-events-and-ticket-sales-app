import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';
import Hero from '../components/Hero';
import EventCard from '../components/EventCard';
import { FunnelIcon, Squares2X2Icon, ListBulletIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { logger } from '../utils/logger';

export default function Home() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [eventos, setEventos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filtroCategoria, setFiltroCategoria] = useState('Todos');
  const [vistaGrid, setVistaGrid] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const eventsPerPage = 9;
  const searchQuery = searchParams.get('search') || '';

  const categorias = [
    { id: 'Todos', nombre: 'Todos' },
    { id: 'Concierto', nombre: 'Conciertos' },
    { id: 'Deporte', nombre: 'Deportes' },
    { id: 'Teatro', nombre: 'Teatro' },
    { id: 'Entretenimiento', nombre: 'Entretenimiento' },
    { id: 'Otro', nombre: 'Otros' }
  ];

  useEffect(() => {
    cargarEventos();
  }, []);

  const cargarEventos = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/eventos');
      logger.log('Home - Eventos recibidos:', response.data);
      setEventos(response.data);
    } catch (error) {
      logger.error('Error cargando eventos:', error);
    } finally {
      setLoading(false);
    }
  };

  // Filtrar por categoría
  let eventosFiltrados = filtroCategoria === 'Todos'
    ? eventos
    : eventos.filter(e => {
        const categoriaEvento = e.categoria?.toLowerCase() || '';
        const categoriaFiltro = filtroCategoria.toLowerCase();
        return categoriaEvento === categoriaFiltro;
      });

  // Filtrar por búsqueda de texto
  if (searchQuery) {
    eventosFiltrados = eventosFiltrados.filter(e => {
      const query = searchQuery.toLowerCase();
      return (
        e.nombre?.toLowerCase().includes(query) ||
        e.descripcion?.toLowerCase().includes(query) ||
        e.ubicacion?.toLowerCase().includes(query) ||
        e.categoria?.toLowerCase().includes(query) ||
        e.organizador?.toLowerCase().includes(query)
      );
    });
  }

  const clearSearch = () => {
    setSearchParams({});
  };

  // Paginación
  const indexOfLastEvent = currentPage * eventsPerPage;
  const indexOfFirstEvent = indexOfLastEvent - eventsPerPage;
  const currentEvents = eventosFiltrados.slice(indexOfFirstEvent, indexOfLastEvent);
  const totalPages = Math.ceil(eventosFiltrados.length / eventsPerPage);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Reset página al cambiar filtros
  useEffect(() => {
    setCurrentPage(1);
  }, [filtroCategoria, searchQuery]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Hero />

      {/* Sección de Eventos */}
      <div id="eventos" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        
        {/* Header con título y controles */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-6">
          <div className="mb-4 md:mb-0">
            <h2 className="text-2xl font-bold text-gray-900">
              Próximos eventos
            </h2>
          </div>

          {/* Controles de vista */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => setVistaGrid(true)}
              className={`p-2 rounded transition-all ${
                vistaGrid 
                  ? 'bg-black text-white' 
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              <Squares2X2Icon className="w-5 h-5" />
            </button>
            <button
              onClick={() => setVistaGrid(false)}
              className={`p-2 rounded transition-all ${
                !vistaGrid 
                  ? 'bg-black text-white' 
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              <ListBulletIcon className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Filtros de categoría */}
        <div className="mb-8 border-b border-gray-200">
          <div className="flex gap-1 overflow-x-auto">
            {categorias.map(cat => (
              <button
                key={cat.id}
                onClick={() => setFiltroCategoria(cat.id)}
                className={`px-6 py-3 font-semibold transition-all whitespace-nowrap ${
                  filtroCategoria === cat.id
                    ? 'text-black border-b-2 border-black'
                    : 'text-gray-600 hover:text-gray-900 border-b-2 border-transparent hover:border-gray-300'
                }`}
              >
                {cat.nombre}
              </button>
            ))}
          </div>
        </div>

        {/* Grid de Eventos */}
        {loading ? (
          <div className="flex flex-col justify-center items-center py-20">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-black rounded-full animate-spin"></div>
            <p className="mt-6 text-gray-600 font-medium">Cargando eventos...</p>
          </div>
        ) : eventosFiltrados.length === 0 ? (
          <div className="text-center py-20 bg-white rounded shadow-sm">
            <h3 className="text-xl font-bold text-gray-900 mb-2">No se encontraron eventos</h3>
            <p className="text-gray-600 mb-6">
              {searchQuery 
                ? `No hay eventos que coincidan con "${searchQuery}".`
                : filtroCategoria === 'Todos' 
                  ? 'Parece que no hay eventos creados todavía.'
                  : `No hay eventos en la categoría "${categorias.find(c => c.id === filtroCategoria)?.nombre}".`
              }
            </p>
          </div>
        ) : (
          <>
            <div className={vistaGrid ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6' : 'space-y-4'}>
              {currentEvents.map(evento => (
                <EventCard key={evento.id} evento={evento} />
              ))}
            </div>

            {/* Paginación */}
            {totalPages > 1 && (
              <div className="mt-12 flex justify-center items-center gap-2">
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
                    // Mostrar primeras 2, últimas 2 y páginas cercanas a la actual
                    const showPage = 
                      pageNumber === 1 ||
                      pageNumber === totalPages ||
                      (pageNumber >= currentPage - 1 && pageNumber <= currentPage + 1);
                    
                    const showEllipsis = 
                      (pageNumber === 2 && currentPage > 3) ||
                      (pageNumber === totalPages - 1 && currentPage < totalPages - 2);

                    if (showEllipsis) {
                      return (
                        <span key={pageNumber} className="px-3 py-2 text-gray-400">
                          ...
                        </span>
                      );
                    }

                    if (!showPage) return null;

                    return (
                      <button
                        key={pageNumber}
                        onClick={() => handlePageChange(pageNumber)}
                        className={`px-4 py-2 rounded-lg font-medium transition ${
                          currentPage === pageNumber
                            ? 'bg-black text-white shadow-md'
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
      </div>
    </div>
  );
}
