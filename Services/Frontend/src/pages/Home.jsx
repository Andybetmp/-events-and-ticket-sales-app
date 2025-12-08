import { useState, useEffect } from 'react';
import axios from 'axios';
import Hero from '../components/Hero';
import EventCard from '../components/EventCard';

export default function Home() {
  const [eventos, setEventos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filtroCategoria, setFiltroCategoria] = useState('Todos');

  const categorias = ['Todos', 'Musica', 'Deportes', 'Teatro', 'Concierto', 'Festival', 'Otro'];

  useEffect(() => {
    cargarEventos();
  }, []);

  const cargarEventos = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/eventos');
      console.log('Home - Eventos recibidos:', response.data);
      setEventos(response.data);
    } catch (error) {
      console.error('Error cargando eventos:', error);
    } finally {
      setLoading(false);
    }
  };

  const eventosFiltrados = filtroCategoria === 'Todos'
    ? eventos
    : eventos.filter(e => e.categoria === filtroCategoria);

  return (
    <div className="min-h-screen bg-gray-50">
      <Hero />

      {/* Sección de Eventos */}
      <div id="eventos" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Filtros */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Eventos Disponibles</h2>
          <div className="flex flex-wrap gap-2">
            {categorias.map(cat => (
              <button
                key={cat}
                onClick={() => setFiltroCategoria(cat)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  filtroCategoria === cat
                    ? 'bg-primary-600 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-300'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        {/* Grid de Eventos */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600"></div>
          </div>
        ) : eventosFiltrados.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-xl text-gray-500">No hay eventos disponibles en esta categoría.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {eventosFiltrados.map(evento => (
              <EventCard key={evento.id} evento={evento} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
