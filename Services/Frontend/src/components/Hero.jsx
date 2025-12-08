import { Link } from 'react-router-dom';

export default function Hero() {
  return (
    <div className="relative bg-gradient-to-r from-primary-600 to-purple-700 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 lg:py-32">
        <div className="text-center">
          <h1 className="text-4xl md:text-6xl font-extrabold mb-6">
            🎵 Encuentra tu próximo evento
          </h1>
          <p className="text-xl md:text-2xl mb-8 text-purple-100 max-w-3xl mx-auto">
            Descubre los mejores conciertos, festivales y shows. Compra tus entradas de forma segura y rápida.
          </p>
          <a
            href="#eventos"
            className="inline-block bg-white text-primary-600 px-8 py-4 rounded-lg text-lg font-semibold hover:bg-gray-100 transition transform hover:scale-105 shadow-lg"
          >
            Ver Eventos ↓
          </a>
        </div>
      </div>
      <div className="absolute bottom-0 left-0 right-0">
        <svg
          viewBox="0 0 1440 120"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="w-full h-12"
        >
          <path
            d="M0 120L60 110C120 100 240 80 360 70C480 60 600 60 720 65C840 70 960 80 1080 80C1200 80 1320 70 1380 65L1440 60V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0Z"
            fill="white"
          />
        </svg>
      </div>
    </div>
  );
}
