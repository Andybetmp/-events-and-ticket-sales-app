import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  UserCircleIcon,
  EnvelopeIcon,
  PhoneIcon,
  KeyIcon,
  CheckCircleIcon,
  XCircleIcon,
  ShieldCheckIcon,
  PencilSquareIcon
} from '@heroicons/react/24/outline';

export default function Perfil() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [activeTab, setActiveTab] = useState('info'); // 'info' o 'security'
  const [editMode, setEditMode] = useState(false);

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
      logger.error('Error cargando perfil:', err);
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
    
    if (activeTab === 'info') {
      // Validaciones para información personal
      if (!formData.nombre || !formData.apellido) {
        setError('El nombre y apellido son obligatorios');
        return;
      }

      if (formData.telefono && !/^[0-9]{9,15}$/.test(formData.telefono)) {
        setError('El teléfono debe contener entre 9 y 15 dígitos');
        return;
      }

      try {
        setSaving(true);
        setError('');
        
        const token = localStorage.getItem('token');
        if (!token) {
          navigate('/login');
          return;
        }

        const updateData = {
          nombre: formData.nombre,
          apellido: formData.apellido,
          telefono: formData.telefono || null
        };

        const response = await axios.put(`/api/users/${userData.id}`, updateData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        const currentUser = JSON.parse(localStorage.getItem('user'));
        const updatedUser = {
          ...currentUser,
          nombre: response.data.nombre,
          apellido: response.data.apellido,
          telefono: response.data.telefono
        };
        localStorage.setItem('user', JSON.stringify(updatedUser));

        setUserData(response.data);
        setSuccess('Información actualizada exitosamente');
        setEditMode(false);
        window.scrollTo({ top: 0, behavior: 'smooth' });

      } catch (err) {
        logger.error('Error actualizando perfil:', err);
        setError(
          err.response?.data?.mensaje || 
          err.response?.data?.error || 
          'No se pudo actualizar el perfil'
        );
      } finally {
        setSaving(false);
      }
    } else {
      // Validaciones para cambio de contraseña
      if (!formData.contrasenaActual) {
        setError('Debes ingresar tu contraseña actual');
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

      try {
        setSaving(true);
        setError('');
        
        const token = localStorage.getItem('token');
        if (!token) {
          navigate('/login');
          return;
        }

        const updateData = {
          contrasenaActual: formData.contrasenaActual,
          contrasena: formData.contrasena
        };

        await axios.put(`/api/users/${userData.id}`, updateData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        setSuccess('Contraseña actualizada exitosamente');
        setFormData(prev => ({
          ...prev,
          contrasenaActual: '',
          contrasena: '',
          confirmarContrasena: ''
        }));
        window.scrollTo({ top: 0, behavior: 'smooth' });

      } catch (err) {
        logger.error('Error actualizando contraseña:', err);
        setError(
          err.response?.data?.mensaje || 
          err.response?.data?.error || 
          'No se pudo actualizar la contraseña'
        );
      } finally {
        setSaving(false);
      }
    }
  };

  const handleCancelar = () => {
    if (activeTab === 'info') {
      setFormData({
        ...formData,
        nombre: userData.nombre || '',
        apellido: userData.apellido || '',
        telefono: userData.telefono || ''
      });
      setEditMode(false);
    } else {
      setFormData({
        ...formData,
        contrasenaActual: '',
        contrasena: '',
        confirmarContrasena: ''
      });
    }
    setError('');
    setSuccess('');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="w-16 h-16 border-4 border-gray-200 border-t-black rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center space-x-4 mb-4">
            <div className="w-20 h-20 bg-gradient-to-br from-teal-500 to-teal-600 rounded-full flex items-center justify-center text-white text-3xl font-bold">
              {userData?.nombre?.charAt(0)}{userData?.apellido?.charAt(0)}
            </div>
            <div>
              <h1 className="text-3xl font-black text-gray-900">Mi Perfil</h1>
              <p className="text-gray-600">{userData?.email}</p>
            </div>
          </div>
        </div>

        {/* Mensajes */}
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

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-md mb-6">
          <div className="border-b border-gray-200">
            <div className="flex">
              <button
                onClick={() => {
                  setActiveTab('info');
                  setError('');
                  setSuccess('');
                }}
                className={`flex-1 px-6 py-4 font-semibold transition-all flex items-center justify-center gap-2 ${
                  activeTab === 'info'
                    ? 'text-black border-b-2 border-black'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                <UserCircleIcon className="w-5 h-5" />
                Información Personal
              </button>
              <button
                onClick={() => {
                  setActiveTab('security');
                  setError('');
                  setSuccess('');
                  setEditMode(false);
                }}
                className={`flex-1 px-6 py-4 font-semibold transition-all flex items-center justify-center gap-2 ${
                  activeTab === 'security'
                    ? 'text-black border-b-2 border-black'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                <ShieldCheckIcon className="w-5 h-5" />
                Seguridad
              </button>
            </div>
          </div>

          <div className="p-8">
            {activeTab === 'info' ? (
              /* Tab de Información Personal */
              <form onSubmit={handleSubmit}>
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-bold text-gray-900">Datos Personales</h2>
                  {!editMode && (
                    <button
                      type="button"
                      onClick={() => setEditMode(true)}
                      className="flex items-center gap-2 text-teal-600 hover:text-teal-700 font-semibold"
                    >
                      <PencilSquareIcon className="w-5 h-5" />
                      Editar
                    </button>
                  )}
                </div>

                <div className="space-y-6">
                  {/* Email (solo lectura) */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Correo Electrónico
                    </label>
                    <div className="relative">
                      <EnvelopeIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                      <input
                        type="email"
                        value={userData?.email || ''}
                        disabled
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
                      />
                    </div>
                  </div>

                  {/* Nombre */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Nombre *
                    </label>
                    <div className="relative">
                      <UserCircleIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                      <input
                        type="text"
                        name="nombre"
                        value={formData.nombre}
                        onChange={handleInputChange}
                        disabled={!editMode}
                        required
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:border-teal-500 focus:ring-2 focus:ring-teal-100 disabled:bg-gray-50 disabled:text-gray-500"
                      />
                    </div>
                  </div>

                  {/* Apellido */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Apellido *
                    </label>
                    <div className="relative">
                      <UserCircleIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                      <input
                        type="text"
                        name="apellido"
                        value={formData.apellido}
                        onChange={handleInputChange}
                        disabled={!editMode}
                        required
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:border-teal-500 focus:ring-2 focus:ring-teal-100 disabled:bg-gray-50 disabled:text-gray-500"
                      />
                    </div>
                  </div>

                  {/* Teléfono */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Teléfono
                    </label>
                    <div className="relative">
                      <PhoneIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                      <input
                        type="tel"
                        name="telefono"
                        value={formData.telefono}
                        onChange={handleInputChange}
                        disabled={!editMode}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:border-teal-500 focus:ring-2 focus:ring-teal-100 disabled:bg-gray-50 disabled:text-gray-500"
                        placeholder="987654321"
                      />
                    </div>
                    <p className="mt-1 text-xs text-gray-500">9-15 dígitos (opcional)</p>
                  </div>
                </div>

                {editMode && (
                  <div className="flex gap-4 mt-8">
                    <button
                      type="button"
                      onClick={handleCancelar}
                      className="flex-1 px-6 py-3 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-bold"
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      disabled={saving}
                      className="flex-1 px-6 py-3 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors font-bold disabled:bg-gray-400"
                    >
                      {saving ? 'Guardando...' : 'Guardar Cambios'}
                    </button>
                  </div>
                )}
              </form>
            ) : (
              /* Tab de Seguridad */
              <form onSubmit={handleSubmit}>
                <h2 className="text-xl font-bold text-gray-900 mb-2">Cambiar Contraseña</h2>
                <p className="text-gray-600 text-sm mb-6">
                  Actualiza tu contraseña para mantener tu cuenta segura
                </p>

                <div className="space-y-6">
                  {/* Contraseña Actual */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Contraseña Actual *
                    </label>
                    <div className="relative">
                      <KeyIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                      <input
                        type="password"
                        name="contrasenaActual"
                        value={formData.contrasenaActual}
                        onChange={handleInputChange}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                        placeholder="Ingresa tu contraseña actual"
                        required
                      />
                    </div>
                  </div>

                  {/* Nueva Contraseña */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Nueva Contraseña *
                    </label>
                    <div className="relative">
                      <KeyIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                      <input
                        type="password"
                        name="contrasena"
                        value={formData.contrasena}
                        onChange={handleInputChange}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                        placeholder="Mínimo 8 caracteres"
                        required
                      />
                    </div>
                  </div>

                  {/* Confirmar Contraseña */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Confirmar Nueva Contraseña *
                    </label>
                    <div className="relative">
                      <KeyIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                      <input
                        type="password"
                        name="confirmarContrasena"
                        value={formData.confirmarContrasena}
                        onChange={handleInputChange}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:border-teal-500 focus:ring-2 focus:ring-teal-100"
                        placeholder="Repite la nueva contraseña"
                        required
                      />
                    </div>
                  </div>

                  <div className="bg-teal-50 border border-teal-200 rounded-lg p-4">
                    <h3 className="font-semibold text-teal-900 text-sm mb-2">Requisitos de contraseña:</h3>
                    <ul className="text-xs text-teal-800 space-y-1">
                      <li>• Mínimo 8 caracteres</li>
                      <li>• Debe incluir letras y números (recomendado)</li>
                    </ul>
                  </div>
                </div>

                <div className="flex gap-4 mt-8">
                  <button
                    type="button"
                    onClick={handleCancelar}
                    className="flex-1 px-6 py-3 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-bold"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={saving}
                    className="flex-1 px-6 py-3 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors font-bold disabled:bg-gray-400"
                  >
                    {saving ? 'Actualizando...' : 'Actualizar Contraseña'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
