import { Link } from 'react-router-dom';
import { CalendarIcon, MapPinIcon, TagIcon } from '@heroicons/react/24/outline';

export default function EventCard({ evento }) {
  // Debug: verificar imagenUrl
  console.log('EventCard - evento.imagenUrl:', evento.imagenUrl);
  
  // Formatear fecha
  const fecha = new Date(evento.fechaEvento);
  const fechaFormateada = fecha.toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  });

  // Obtener el precio mínimo de las entradas
  const precioMinimo = evento.tiposEntrada && evento.tiposEntrada.length > 0
    ? Math.min(...evento.tiposEntrada.map(t => t.precio))
    : 0;

  // Mapeo de categorías a emojis
  const categoriaEmojis = {
    'Musica': '🎵',
    'Deportes': '⚽',
    'Teatro': '🎭',
    'Concierto': '🎸',
    'Festival': '🎉',
    'Otro': '🎫'
  };

  return (
    <div className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-2xl transition-all duration-300 hover:-translate-y-1">
      {/* Imagen del evento */}
      {evento.imagenUrl && evento.imagenUrl.trim() !== '' ? (
        <div className="h-48 overflow-hidden">
          <img 
            src={evento.imagenUrl} 
            alt={evento.nombre}
            className="w-full h-full object-cover hover:scale-110 transition-transform duration-300"
            onError={(e) => {
              console.error('Error cargando imagen:', evento.imagenUrl);
              e.target.style.display = 'none';
              e.target.parentElement.innerHTML = `<div class="h-48 bg-gradient-to-br from-primary-400 to-purple-500 flex items-center justify-center"><span class="text-6xl">${categoriaEmojis[evento.categoria] || '🎫'}</span></div>`;
            }}
          />
        </div>
      ) : (
        <div className="h-48 bg-gradient-to-br from-primary-400 to-purple-500 flex items-center justify-center">
          <span className="text-6xl">{categoriaEmojis[evento.categoria] || '🎫'}</span>
        </div>
      )}

      {/* Contenido */}
      <div className="p-5">
        <h3 className="text-xl font-bold text-gray-900 mb-2 line-clamp-1">
          {evento.nombre}
        </h3>

        <div className="space-y-2 mb-4">
          <div className="flex items-center text-sm text-gray-600">
            <CalendarIcon className="h-4 w-4 mr-2 text-primary-600" />
            <span>{fechaFormateada}</span>
          </div>

          <div className="flex items-center text-sm text-gray-600">
            <MapPinIcon className="h-4 w-4 mr-2 text-primary-600" />
            <span className="line-clamp-1">{evento.ubicacion}</span>
          </div>

          <div className="flex items-center text-sm text-gray-600">
            <TagIcon className="h-4 w-4 mr-2 text-primary-600" />
            <span className="inline-block px-2 py-1 bg-primary-100 text-primary-700 rounded-full text-xs font-medium">
              {evento.categoria}
            </span>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between pt-4 border-t border-gray-200">
          <div>
            <p className="text-xs text-gray-500">Desde</p>
            <p className="text-2xl font-bold text-primary-600">
              S/{precioMinimo.toFixed(2)}
            </p>
          </div>

          <Link
            to={`/evento/${evento.id}`}
            className="bg-primary-600 text-white px-6 py-2 rounded-lg hover:bg-primary-700 transition font-medium text-sm"
          >
            Ver más →
          </Link>
        </div>
      </div>
    </div>
  );
}
