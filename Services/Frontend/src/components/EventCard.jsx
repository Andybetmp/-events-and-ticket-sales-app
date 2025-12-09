import { Link } from 'react-router-dom';
import { CalendarIcon, MapPinIcon, TicketIcon, HeartIcon } from '@heroicons/react/24/outline';
import { HeartIcon as HeartSolidIcon } from '@heroicons/react/24/solid';
import { useState } from 'react';

export default function EventCard({ evento }) {
  const [isFavorite, setIsFavorite] = useState(false);
  
  // Formatear fecha
  const fecha = new Date(evento.fechaEvento);
  const fechaFormateada = fecha.toLocaleDateString('es-ES', {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  }).toUpperCase();

  const mes = fecha.toLocaleDateString('es-ES', { month: 'short' }).toUpperCase();
  const dia = fecha.getDate();

  // Obtener el precio mínimo de las entradas
  const precioMinimo = evento.tiposEntrada && evento.tiposEntrada.length > 0
    ? Math.min(...evento.tiposEntrada.map(t => t.precio))
    : 0;

  // Colores por categoría
  const categoriaColors = {
    'Conciertos': 'bg-teal-100 text-teal-800',
    'Deportes': 'bg-green-100 text-green-800',
    'Teatro': 'bg-gray-100 text-gray-800',
    'Entretenimiento': 'bg-gray-100 text-gray-800',
    'Otro': 'bg-gray-100 text-gray-800'
  };

  return (
    <div className="event-card group">
      {/* Imagen del evento con overlay */}
      <Link to={`/evento/${evento.id}`} className="block relative overflow-hidden h-52">
        {evento.imagenUrl && evento.imagenUrl.trim() !== '' ? (
          <img 
            src={evento.imagenUrl} 
            alt={evento.nombre}
            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
            onError={(e) => {
              e.target.style.display = 'none';
              e.target.parentElement.innerHTML = `
                <div class="h-52 bg-gradient-to-br from-teal-500 via-teal-400 to-gray-900 flex items-center justify-center">
                  <svg class="w-20 h-20 text-white opacity-50" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M2 6a2 2 0 012-2h12a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6z"/>
                  </svg>
                </div>
              `;
            }}
          />
        ) : (
          <div className="h-52 bg-gradient-to-br from-teal-500 via-teal-400 to-gray-900 flex items-center justify-center">
            <TicketIcon className="w-20 h-20 text-white opacity-50" />
          </div>
        )}
        
        {/* Badge de categoría */}
        <div className="absolute top-3 left-3">
          <span className={`px-3 py-1 rounded text-xs font-semibold ${categoriaColors[evento.categoria] || categoriaColors['Otro']}`}>
            {evento.categoria}
          </span>
        </div>

        {/* Botón de favorito */}
        <button
          onClick={(e) => {
            e.preventDefault();
            setIsFavorite(!isFavorite);
          }}
          className="absolute top-3 right-3 p-2 bg-white/90 backdrop-blur-sm rounded-full hover:bg-white transition-all duration-200 shadow-md"
        >
          {isFavorite ? (
            <HeartSolidIcon className="w-5 h-5 text-red-500" />
          ) : (
            <HeartIcon className="w-5 h-5 text-gray-600" />
          )}
        </button>

        {/* Fecha badge */}
        <div className="absolute bottom-3 left-3 bg-white rounded shadow-md overflow-hidden">
          <div className="bg-red-600 text-white text-xs font-bold text-center py-1 px-3">
            {mes}
          </div>
          <div className="text-gray-900 text-xl font-bold text-center py-1 px-3">
            {dia}
          </div>
        </div>
      </Link>

      {/* Contenido */}
      <div className="p-5">
        <Link to={`/evento/${evento.id}`}>
          <h3 className="text-lg font-bold text-gray-900 mb-2 line-clamp-2 group-hover:text-teal-600 transition-colors duration-200 min-h-[3.5rem]">
            {evento.nombre}
          </h3>
        </Link>

        <div className="space-y-2 mb-4">
          <div className="flex items-center text-sm text-gray-600">
            <CalendarIcon className="h-4 w-4 mr-2 text-gray-900 flex-shrink-0" />
            <span className="font-medium">{fechaFormateada}</span>
          </div>

          <div className="flex items-center text-sm text-gray-600">
            <MapPinIcon className="h-4 w-4 mr-2 text-gray-900 flex-shrink-0" />
            <span className="line-clamp-1">{evento.ubicacion}</span>
          </div>
        </div>

        {/* Footer con precio y botón */}
        <div className="flex items-center justify-between pt-4 border-t border-gray-100">
          <div>
            <p className="text-xs text-gray-500 font-medium">Desde</p>
            <p className="text-base text-gray-900">
              S/ {precioMinimo.toFixed(2)}
            </p>
          </div>
          
          <Link 
            to={`/evento/${evento.id}`}
            className="px-5 py-2 bg-white text-black font-bold rounded hover:bg-gray-100 transition-colors text-sm border border-gray-300"
          >
            VER MÁS
          </Link>
        </div>
      </div>
    </div>
  );
}
