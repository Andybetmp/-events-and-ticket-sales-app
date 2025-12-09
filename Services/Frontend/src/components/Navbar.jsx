import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { MagnifyingGlassIcon, XMarkIcon, TicketIcon, UserCircleIcon, ChevronDownIcon, ShieldCheckIcon } from '@heroicons/react/24/outline';
import { isAdmin } from '../utils/roleUtils';

export default function Navbar() {
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const token = localStorage.getItem('token');
  const userIsAdmin = isAdmin();

  // Ocultar barra de búsqueda en estas rutas
  const hideSearchPaths = ['/checkout', '/login', '/register', '/admin', '/forgot-password', '/reset-password'];
  const shouldShowSearch = !hideSearchPaths.some(path => location.pathname.startsWith(path));

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/?search=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleClearSearch = () => {
    setSearchQuery('');
    navigate('/'); // Redirigir a home sin parámetros de búsqueda
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/';
  };

  return (
    <nav className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2 text-black hover:text-gray-800">
            <span className="text-xl font-bold">Ticket Express</span>
          </Link>

          {/* Search Bar */}
          {shouldShowSearch && (
            <div className="hidden md:flex flex-1 max-w-md mx-8">
              <form onSubmit={handleSearch} className="relative w-full">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Buscar eventos..."
                  className="w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
                />
                <MagnifyingGlassIcon className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                {searchQuery && (
                  <button
                    type="button"
                    onClick={handleClearSearch}
                    className="absolute right-3 top-2.5 text-gray-400 hover:text-gray-600 transition"
                  >
                    <XMarkIcon className="h-5 w-5" />
                  </button>
                )}
              </form>
            </div>
          )}

          {/* Navigation Links */}
          <div className="flex items-center space-x-4">
            {token && user ? (
              <>
                {/* Admin Panel - Solo ADMIN */}
                {userIsAdmin && (
                  <Link
                    to="/admin"
                    className="flex items-center space-x-1 text-amber-600 hover:text-amber-700 font-medium transition"
                  >
                    <ShieldCheckIcon className="h-5 w-5" />
                    <span>Panel Admin</span>
                  </Link>
                )}
                
                {/* User Menu Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setShowUserMenu(!showUserMenu)}
                    className="flex items-center space-x-2 pl-4 border-l border-gray-300 hover:text-teal-600 transition"
                  >
                    <UserCircleIcon className="h-8 w-8 text-gray-600" />
                    <div className="hidden lg:flex items-center space-x-1">
                      <div className="text-left">
                        <p className="text-sm font-medium text-gray-900">{user.nombre} {user.apellido}</p>
                        {userIsAdmin && (
                          <p className="text-xs text-amber-600 font-semibold">ADMIN</p>
                        )}
                      </div>
                      <ChevronDownIcon className="h-4 w-4 text-gray-400" />
                    </div>
                  </button>

                  {/* Dropdown Menu */}
                  {showUserMenu && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-1 border border-gray-200">
                      {userIsAdmin && (
                        <>
                          <Link
                            to="/admin"
                            className="flex items-center space-x-2 px-4 py-2 text-sm text-amber-600 hover:bg-amber-50"
                            onClick={() => setShowUserMenu(false)}
                          >
                            <ShieldCheckIcon className="h-4 w-4" />
                            <span>Panel Admin</span>
                          </Link>
                          <hr className="my-1" />
                        </>
                      )}
                      <Link
                        to="/perfil"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setShowUserMenu(false)}
                      >
                        Mi Perfil
                      </Link>
                      <Link
                        to="/mis-tickets"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setShowUserMenu(false)}
                      >
                        Mis Tickets
                      </Link>
                      <Link
                        to="/mis-eventos"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setShowUserMenu(false)}
                      >
                        Mis Eventos
                      </Link>
                      <hr className="my-1" />
                      <button
                        onClick={handleLogout}
                        className="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100"
                      >
                        Cerrar Sesión
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-gray-700 hover:text-gray-900 font-medium transition"
                >
                  Iniciar Sesión
                </Link>
                <Link
                  to="/register"
                  className="bg-black text-white px-4 py-2 rounded-lg hover:bg-gray-800 transition font-bold"
                >
                  Registrarse
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
