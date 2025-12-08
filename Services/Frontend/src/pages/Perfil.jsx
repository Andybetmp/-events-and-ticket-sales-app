import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  UserCircleIcon,
  EnvelopeIcon,
  PhoneIcon,
  KeyIcon,
  CheckCircleIcon,
  XCircleIcon
} from '@heroicons/react/24/outline';

export default function Perfil() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showPasswordFields, setShowPasswordFields] = useState(false);

  const [userData, setUserData] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    telefono: '',
    contrasenaActual: '',
    contrasena: '',
    confirmarContrasena: ''
  });

  useEffect(() => {
    cargarPerfil();
  }, []);

  const cargarPerfil = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await axios.get('/api/users/me', {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      setUserData(response.data);
      setFormData({
        nombre: response.data.nombre || '',
        apellido: response.data.apellido || '',
        telefono: response.data.telefono || '',
        contrasenaActual: '',
        contrasena: '',
        confirmarContrasena: ''
      });
    } catch (err) {
      console.error('Error cargando perfil:', err);
      setError('No se pudo cargar el perfil del usuario');
      if (err.response?.status === 401) {
        navigate('/login');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validaciones
    if (!formData.nombre || !formData.apellido) {
      setError('El nombre y apellido son obligatorios');
      return;
    }

    if (formData.telefono && !/^[0-9]{9,15}$/.test(formData.telefono)) {
      setError('El teléfono debe contener entre 9 y 15 dígitos');
      return;
    }

    // Validar contraseña si se está cambiando
    if (formData.contrasena || formData.confirmarContrasena || formData.contrasenaActual) {
      if (!formData.contrasenaActual) {
        setError('Debes ingresar tu contraseña actual para cambiarla');
        return;
      }
      if (!formData.contrasena) {
        setError('Debes ingresar una nueva contraseña');
        return;
      }
      if (formData.contrasena.length < 8) {
        setError('La nueva contraseña debe tener al menos 8 caracteres');
        return;
      }
      if (formData.contrasena !== formData.confirmarContrasena) {
        setError('Las contraseñas no coinciden');
        return;
      }
    }

    try {
      setSaving(true);
      setError('');
      
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      // Preparar datos para actualizar (solo campos que cambiaron)
      const updateData = {
        nombre: formData.nombre,
        apellido: formData.apellido,
        telefono: formData.telefono || null
      };

      // Solo incluir contraseña si se está cambiando
      if (formData.contrasena && formData.contrasenaActual) {
        updateData.contrasenaActual = formData.contrasenaActual;
        updateData.contrasena = formData.contrasena;
      }

      const response = await axios.put(`/api/users/${userData.id}`, updateData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      // Actualizar datos del usuario en localStorage
      const currentUser = JSON.parse(localStorage.getItem('user'));
      const updatedUser = {
        ...currentUser,
        nombre: response.data.nombre,
        apellido: response.data.apellido,
        telefono: response.data.telefono
      };
      localStorage.setItem('user', JSON.stringify(updatedUser));

      setUserData(response.data);
      setSuccess('Perfil actualizado exitosamente');
      
      // Limpiar campos de contraseña
      setFormData(prev => ({
        ...prev,
        contrasenaActual: '',
        contrasena: '',
        confirmarContrasena: ''
      }));
      setShowPasswordFields(false);

      // Scroll al mensaje de éxito
      window.scrollTo({ top: 0, behavior: 'smooth' });

    } catch (err) {
      console.error('Error actualizando perfil:', err);
      setError(
        err.response?.data?.mensaje || 
        err.response?.data?.error || 
        'No se pudo actualizar el perfil. Por favor intenta nuevamente.'
      );
    } finally {
      setSaving(false);
    }
  };

  const handleCancelar = () => {
    setFormData({
      nombre: userData.nombre || '',
      apellido: userData.apellido || '',
      telefono: userData.telefono || '',
      contrasenaActual: '',
      contrasena: '',
      confirmarContrasena: ''
    });
    setShowPasswordFields(false);
    setError('');
    setSuccess('');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        {/* Header */}
        <div className="mb-8 text-center">
          <div className="flex justify-center mb-4">
            <UserCircleIcon className="h-24 w-24 text-primary-600" />
          </div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Mi Perfil</h1>
          <p className="text-gray-600">Actualiza tu información personal</p>
        </div>

        {/* Mensajes de error/éxito */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg flex items-center gap-3">
            <XCircleIcon className="h-5 w-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="mb-6 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg flex items-center gap-3">
            <CheckCircleIcon className="h-5 w-5 flex-shrink-0" />
            <span>{success}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Información Básica */}
          <div className="bg-white shadow-md rounded-xl p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">Información Básica</h2>
            
            <div className="space-y-6">
              {/* Email (solo lectura) */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                  Email (no modificable)
                </label>
                <div className="relative">
                  <EnvelopeIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                  <input
                    type="email"
                    id="email"
                    value={userData?.email || ''}
                    disabled
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
                  />
                </div>
                <p className="mt-1 text-sm text-gray-500">El email no se puede modificar</p>
              </div>

              {/* Rol (solo lectura) */}
              <div>
                <label htmlFor="rol" className="block text-sm font-medium text-gray-700 mb-2">
                  Rol
                </label>
                <div className="relative">
                  <input
                    type="text"
                    id="rol"
                    value={userData?.rol || 'USUARIO'}
                    disabled
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
                  />
                </div>
              </div>

              {/* Nombre */}
              <div>
                <label htmlFor="nombre" className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre *
                </label>
                <div className="relative">
                  <UserCircleIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                  <input
                    type="text"
                    id="nombre"
                    name="nombre"
                    value={formData.nombre}
                    onChange={handleInputChange}
                    required
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    placeholder="Ingresa tu nombre"
                  />
                </div>
              </div>

              {/* Apellido */}
              <div>
                <label htmlFor="apellido" className="block text-sm font-medium text-gray-700 mb-2">
                  Apellido *
                </label>
                <div className="relative">
                  <UserCircleIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                  <input
                    type="text"
                    id="apellido"
                    name="apellido"
                    value={formData.apellido}
                    onChange={handleInputChange}
                    required
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    placeholder="Ingresa tu apellido"
                  />
                </div>
              </div>

              {/* Teléfono */}
              <div>
                <label htmlFor="telefono" className="block text-sm font-medium text-gray-700 mb-2">
                  Teléfono
                </label>
                <div className="relative">
                  <PhoneIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                  <input
                    type="tel"
                    id="telefono"
                    name="telefono"
                    value={formData.telefono}
                    onChange={handleInputChange}
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    placeholder="987654321"
                  />
                </div>
                <p className="mt-1 text-sm text-gray-500">9-15 dígitos</p>
              </div>
            </div>
          </div>

          {/* Cambiar Contraseña */}
          <div className="bg-white shadow-md rounded-xl p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-bold text-gray-900">Cambiar Contraseña</h2>
              {!showPasswordFields && (
                <button
                  type="button"
                  onClick={() => setShowPasswordFields(true)}
                  className="text-primary-600 hover:text-primary-700 font-medium text-sm"
                >
                  Modificar contraseña
                </button>
              )}
            </div>

            {showPasswordFields ? (
              <div className="space-y-6">
                {/* Contraseña Actual */}
                <div>
                  <label htmlFor="contrasenaActual" className="block text-sm font-medium text-gray-700 mb-2">
                    Contraseña Actual *
                  </label>
                  <div className="relative">
                    <KeyIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                    <input
                      type="password"
                      id="contrasenaActual"
                      name="contrasenaActual"
                      value={formData.contrasenaActual}
                      onChange={handleInputChange}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      placeholder="Ingresa tu contraseña actual"
                    />
                  </div>
                  <p className="mt-1 text-sm text-gray-500">Necesaria para verificar tu identidad</p>
                </div>

                {/* Nueva Contraseña */}
                <div>
                  <label htmlFor="contrasena" className="block text-sm font-medium text-gray-700 mb-2">
                    Nueva Contraseña *
                  </label>
                  <div className="relative">
                    <KeyIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                    <input
                      type="password"
                      id="contrasena"
                      name="contrasena"
                      value={formData.contrasena}
                      onChange={handleInputChange}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      placeholder="Mínimo 8 caracteres"
                    />
                  </div>
                </div>

                {/* Confirmar Contraseña */}
                <div>
                  <label htmlFor="confirmarContrasena" className="block text-sm font-medium text-gray-700 mb-2">
                    Confirmar Nueva Contraseña *
                  </label>
                  <div className="relative">
                    <KeyIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                    <input
                      type="password"
                      id="confirmarContrasena"
                      name="confirmarContrasena"
                      value={formData.confirmarContrasena}
                      onChange={handleInputChange}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      placeholder="Repite la nueva contraseña"
                    />
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => {
                    setShowPasswordFields(false);
                    setFormData(prev => ({
                      ...prev,
                      contrasenaActual: '',
                      contrasena: '',
                      confirmarContrasena: ''
                    }));
                  }}
                  className="text-gray-600 hover:text-gray-800 text-sm font-medium"
                >
                  Cancelar cambio de contraseña
                </button>
              </div>
            ) : (
              <p className="text-gray-600 text-sm">
                Tu contraseña está protegida. Click en "Modificar contraseña" para cambiarla.
              </p>
            )}
          </div>

          {/* Botones de Acción */}
          <div className="flex gap-4">
            <button
              type="button"
              onClick={handleCancelar}
              className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex-1 px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {saving ? 'Guardando...' : 'Guardar Cambios'}
            </button>
          </div>
        </form>

        {/* Información adicional */}
        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h3 className="font-semibold text-blue-900 mb-2">ℹ️ Información importante</h3>
          <ul className="text-sm text-blue-800 space-y-1">
            <li>• El email no puede ser modificado por seguridad</li>
            <li>• Los cambios en el perfil se aplicarán inmediatamente</li>
            <li>• La contraseña debe tener al menos 8 caracteres</li>
            <li>• Todos los campos marcados con * son obligatorios</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
