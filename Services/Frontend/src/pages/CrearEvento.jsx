import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  CalendarIcon,
  MapPinIcon,
  DocumentTextIcon,
  TagIcon,
  PlusIcon,
  TrashIcon,
  CheckCircleIcon,
  PhotoIcon
} from '@heroicons/react/24/outline';

export default function CrearEvento() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    fechaEvento: '',
    ubicacion: '',
    categoria: 'Musica',
    estado: 'ACTIVO'
  });

  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [uploadingImage, setUploadingImage] = useState(false);

  const [tiposEntrada, setTiposEntrada] = useState([
    { nombre: 'General', descripcion: '', precio: 0, cantidadDisponible: 0 }
  ]);

  const categorias = ['Musica', 'Deportes', 'Teatro', 'Concierto', 'Festival', 'Otro'];

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleTipoEntradaChange = (index, field, value) => {
    const nuevostipos = [...tiposEntrada];
    nuevostipos[index][field] = value;
    setTiposEntrada(nuevostipos);
  };

  const agregarTipoEntrada = () => {
    setTiposEntrada([
      ...tiposEntrada,
      { nombre: '', descripcion: '', precio: 0, cantidadDisponible: 0 }
    ]);
  };

  const eliminarTipoEntrada = (index) => {
    if (tiposEntrada.length > 1) {
      setTiposEntrada(tiposEntrada.filter((_, i) => i !== index));
    }
  };

  const handleImageSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        setError('La imagen no debe superar 10MB');
        return;
      }
      if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)) {
        setError('Formato de imagen no válido. Usa JPG, PNG, GIF o WEBP');
        return;
      }
      setSelectedImage(file);
      setImagePreview(URL.createObjectURL(file));
    }
  };

  const removeImage = () => {
    setSelectedImage(null);
    setImagePreview(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validaciones
    if (!formData.nombre || !formData.fechaEvento || !formData.ubicacion) {
      setError('Por favor completa todos los campos requeridos');
      return;
    }

    const tiposValidos = tiposEntrada.filter(tipo => 
      tipo.nombre && tipo.precio > 0 && tipo.cantidadDisponible > 0
    );

    if (tiposValidos.length === 0) {
      setError('Debes agregar al menos un tipo de entrada válido');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      const user = JSON.parse(localStorage.getItem('user'));

      // Subir imagen primero si existe
      let imagenUrl = null;
      if (selectedImage) {
        setUploadingImage(true);
        const formDataImage = new FormData();
        formDataImage.append('file', selectedImage);

        try {
          const uploadResponse = await axios.post('/api/images/upload', formDataImage, {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'multipart/form-data'
            }
          });
          imagenUrl = uploadResponse.data.fileUrl;
        } catch (uploadErr) {
          console.error('Error subiendo imagen:', uploadErr);
          setError('Error al subir la imagen');
          setUploadingImage(false);
          setLoading(false);
          return;
        }
        setUploadingImage(false);
      }

      // Preparar datos
      const requestData = {
        nombre: formData.nombre,
        descripcion: formData.descripcion || 'Sin descripción',
        fechaEvento: new Date(formData.fechaEvento).toISOString(),
        ubicacion: formData.ubicacion,
        categoria: formData.categoria,
        imagenUrl: imagenUrl,
        tiposEntrada: tiposValidos.map(tipo => ({
          nombre: tipo.nombre,
          descripcion: tipo.descripcion || `Entrada ${tipo.nombre}`,
          precio: parseFloat(tipo.precio),
          cantidad: parseInt(tipo.cantidadDisponible)
        }))
      };

      const response = await axios.post('/api/orchestration/create-event', requestData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      setSuccess(true);
      
      // Redirigir al detalle del evento creado
      setTimeout(() => {
        navigate(`/evento/${response.data.id}`);
      }, 2000);

    } catch (err) {
      console.error('Error creando evento:', err);
      console.error('Respuesta del servidor:', JSON.stringify(err.response?.data, null, 2));
      console.error('Status:', err.response?.status);
      setError(
        err.response?.data?.error || 
        err.response?.data?.message || 
        JSON.stringify(err.response?.data) ||
        'No se pudo crear el evento. Por favor intenta nuevamente.'
      );
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-xl shadow-lg p-12 max-w-md text-center">
          <CheckCircleIcon className="h-20 w-20 text-teal-500 mx-auto mb-6" />
          <h2 className="text-3xl font-black text-gray-900 mb-4">¡Evento Creado!</h2>
          <p className="text-gray-600 mb-6">
            Tu evento ha sido publicado exitosamente. Redirigiendo...
          </p>
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black mx-auto"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-4xl font-black text-gray-900 mb-2">Crear Evento</h1>
          <p className="text-gray-600 text-lg">Completa la información para publicar tu evento</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Información del Evento */}
          <div className="bg-white rounded-xl shadow-md p-6">
            <h2 className="text-xl font-black text-gray-900 mb-6 flex items-center">
              <DocumentTextIcon className="h-6 w-6 mr-2 text-teal-600" />
              Información del Evento
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre del Evento *
                </label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
                  placeholder="Ej: Concierto de Rock 2025"
                />
              </div>

              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Descripción
                </label>
                <textarea
                  name="descripcion"
                  value={formData.descripcion}
                  onChange={handleInputChange}
                  rows={4}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
                  placeholder="Describe tu evento..."
                />
              </div>

              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <PhotoIcon className="h-4 w-4 inline mr-1" />
                  Imagen del Evento
                </label>
                {!imagePreview ? (
                  <div className="mt-2 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:border-teal-500 transition-colors">
                    <div className="space-y-1 text-center">
                      <PhotoIcon className="mx-auto h-12 w-12 text-gray-400" />
                      <div className="flex text-sm text-gray-600">
                        <label
                          htmlFor="file-upload"
                          className="relative cursor-pointer bg-white rounded-md font-medium text-teal-600 hover:text-teal-700 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-teal-500"
                        >
                          <span>Subir imagen</span>
                          <input
                            id="file-upload"
                            name="file-upload"
                            type="file"
                            className="sr-only"
                            accept="image/jpeg,image/png,image/gif,image/webp"
                            onChange={handleImageSelect}
                          />
                        </label>
                        <p className="pl-1">o arrastra y suelta</p>
                      </div>
                      <p className="text-xs text-gray-500">PNG, JPG, GIF, WEBP hasta 10MB</p>
                    </div>
                  </div>
                ) : (
                  <div className="mt-2 relative">
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="w-full h-64 object-cover rounded-lg"
                    />
                    <button
                      type="button"
                      onClick={removeImage}
                      className="absolute top-2 right-2 bg-red-500 text-white rounded-full p-2 hover:bg-red-600 transition-colors"
                    >
                      <TrashIcon className="h-5 w-5" />
                    </button>
                  </div>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <CalendarIcon className="h-4 w-4 inline mr-1" />
                  Fecha y Hora *
                </label>
                <input
                  type="datetime-local"
                  name="fechaEvento"
                  value={formData.fechaEvento}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <MapPinIcon className="h-4 w-4 inline mr-1" />
                  Ubicación *
                </label>
                <input
                  type="text"
                  name="ubicacion"
                  value={formData.ubicacion}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
                  placeholder="Ej: Estadio Nacional, Lima"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <TagIcon className="h-4 w-4 inline mr-1" />
                  Categoría *
                </label>
                <select
                  name="categoria"
                  value={formData.categoria}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
                >
                  {categorias.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Estado
                </label>
                <select
                  name="estado"
                  value={formData.estado}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
                >
                  <option value="ACTIVO">Activo</option>
                  <option value="CANCELADO">Cancelado</option>
                  <option value="FINALIZADO">Finalizado</option>
                </select>
              </div>
            </div>
          </div>

          {/* Tipos de Entrada */}
          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-black text-gray-900 flex items-center">
                <TagIcon className="h-6 w-6 mr-2 text-teal-600" />
                Tipos de Entrada
              </h2>
              <button
                type="button"
                onClick={agregarTipoEntrada}
                className="flex items-center px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 transition text-sm font-bold"
              >
                <PlusIcon className="h-5 w-5 mr-1" />
                Agregar Tipo
              </button>
            </div>

            <div className="space-y-4">
              {tiposEntrada.map((tipo, index) => (
                <div key={index} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-3">
                    <h3 className="font-medium text-gray-900">Tipo de Entrada #{index + 1}</h3>
                    {tiposEntrada.length > 1 && (
                      <button
                        type="button"
                        onClick={() => eliminarTipoEntrada(index)}
                        className="text-red-600 hover:text-red-700 transition"
                      >
                        <TrashIcon className="h-5 w-5" />
                      </button>
                    )}
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Nombre *
                      </label>
                      <input
                        type="text"
                        value={tipo.nombre}
                        onChange={(e) => handleTipoEntradaChange(index, 'nombre', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500 text-sm"
                        placeholder="Ej: VIP, General, Preferencial"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Descripción
                      </label>
                      <input
                        type="text"
                        value={tipo.descripcion}
                        onChange={(e) => handleTipoEntradaChange(index, 'descripcion', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500 text-sm"
                        placeholder="Descripción opcional"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Precio (S/) *
                      </label>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        value={tipo.precio}
                        onChange={(e) => handleTipoEntradaChange(index, 'precio', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500 text-sm"
                        placeholder="0.00"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Cantidad de Entradas *
                      </label>
                      <input
                        type="number"
                        min="0"
                        value={tipo.cantidadDisponible}
                        onChange={(e) => handleTipoEntradaChange(index, 'cantidadDisponible', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-teal-500 focus:border-teal-500 text-sm"
                        placeholder="0"
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Error */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="text-red-700">{error}</p>
            </div>
          )}

          {/* Botones */}
          <div className="flex items-center justify-end space-x-4">
            <button
              type="button"
              onClick={() => navigate('/')}
              disabled={loading}
              className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 transition font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading || uploadingImage}
              className="px-8 py-3 bg-black text-white rounded-lg hover:bg-gray-800 disabled:bg-gray-400 disabled:cursor-not-allowed transition font-bold shadow-lg hover:shadow-xl"
            >
              {uploadingImage ? (
                <span className="flex items-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Subiendo imagen...
                </span>
              ) : loading ? (
                <span className="flex items-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Creando evento...
                </span>
              ) : (
                'Publicar Evento'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
