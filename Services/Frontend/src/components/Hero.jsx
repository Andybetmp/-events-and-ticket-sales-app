import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/solid';
import axios from 'axios';

export default function Hero() {
  const [eventos, setEventos] = useState([]);
  const [currentSlide, setCurrentSlide] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarEventosDestacados();
  }, []);

  useEffect(() => {
    if (eventos.length > 0) {
      const interval = setInterval(() => {
        setCurrentSlide((prev) => (prev + 1) % Math.min(eventos.length, 5));
      }, 5000);
      return () => clearInterval(interval);
    }
  }, [eventos]);

  const cargarEventosDestacados = async () => {
    try {
      const response = await axios.get('/api/eventos');
      // Tomar los primeros 5 eventos como destacados
      setEventos(response.data.slice(0, 5));
    } catch (error) {
      console.error('Error cargando eventos destacados:', error);
    } finally {
      setLoading(false);
    }
  };

  const nextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % eventos.length);
  };

  const prevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + eventos.length) % eventos.length);
  };

  if (loading) {
    return (
      <div className="relative h-[500px] bg-gradient-to-r from-slate-900 to-slate-800 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-white"></div>
      </div>
    );
  }

  if (eventos.length === 0) {
    return (
      <div className="relative h-[500px] bg-gradient-to-r from-slate-900 to-slate-800 flex items-center justify-center">
        <div className="text-center text-white">
          <h2 className="text-4xl font-bold mb-4">No hay eventos destacados</h2>
          <p className="text-lg text-gray-300">Pronto habrá eventos increíbles aquí</p>
        </div>
      </div>
    );
  }

  const eventoActual = eventos[currentSlide];

  return (
    <div className="relative h-[500px] overflow-hidden group">
      {/* Imagen de fondo */}
      <div className="absolute inset-0">
        <img
          src={eventoActual.imagenUrl || 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=1920&h=1080&fit=crop'}
          alt={eventoActual.nombre}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-r from-black/80 via-black/60 to-transparent"></div>
      </div>

      {/* Contenido */}
      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-full flex items-center">
        <div className="max-w-2xl">
          <div className="inline-block px-4 py-2 bg-red-600 text-white text-sm font-bold mb-4 rounded">
            EVENTO DESTACADO
          </div>
          <h1 className="text-5xl md:text-6xl font-black text-white mb-4 leading-tight">
            {eventoActual.nombre}
          </h1>
          <p className="text-xl text-gray-200 mb-2">
            {eventoActual.categoria || 'Evento'}
          </p>
          <p className="text-lg text-gray-300 mb-6">
            {new Date(eventoActual.fecha).toLocaleDateString('es-ES', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            })}
          </p>
          <Link
            to={`/evento/${eventoActual.id}`}
            className="inline-block px-8 py-4 bg-white text-black font-bold text-lg rounded hover:bg-gray-100 transition-colors"
          >
            VER MÁS
          </Link>
        </div>
      </div>

      {/* Controles de navegación */}
      <button
        onClick={prevSlide}
        className="absolute left-4 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-3 rounded-full transition-all opacity-0 group-hover:opacity-100"
      >
        <ChevronLeftIcon className="w-6 h-6" />
      </button>
      <button
        onClick={nextSlide}
        className="absolute right-4 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-3 rounded-full transition-all opacity-0 group-hover:opacity-100"
      >
        <ChevronRightIcon className="w-6 h-6" />
      </button>

      {/* Indicadores */}
      <div className="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2">
        {eventos.map((_, index) => (
          <button
            key={index}
            onClick={() => setCurrentSlide(index)}
            className={`h-2 rounded-full transition-all ${
              index === currentSlide ? 'w-8 bg-white' : 'w-2 bg-white/50'
            }`}
          />
        ))}
      </div>
    </div>
  );
}
