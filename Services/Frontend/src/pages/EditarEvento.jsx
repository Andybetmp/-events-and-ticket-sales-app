import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import {
  CalendarIcon,
  MapPinIcon,
  DocumentTextIcon,
  TagIcon,
  PlusIcon,
  TrashIcon,
  CheckCircleIcon,
  PhotoIcon,
  ArrowLeftIcon
} from '@heroicons/react/24/outline';

export default function EditarEvento() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
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
  const [currentImageUrl, setCurrentImageUrl] = useState(null);

  const [tiposEntrada, setTiposEntrada] = useState([]);
  const [evento, setEvento] = useState(null);

  const categorias = ['Musica', 'Deportes', 'Teatro', 'Concierto', 'Festival', 'Otro'];
  const estados = ['ACTIVO', 'CANCELADO', 'FINALIZADO'];

  useEffect(() => {
    cargarEvento();
  }, [id]);

  const cargarEvento = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await axios.get(`/api/eventos/${id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      const eventoData = response.data;
      setEvento(eventoData);

      // Convertir fecha ISO a formato datetime-local
      const fechaLocal = new Date(eventoData.fechaEvento);
      const offset = fechaLocal.getTimezoneOffset();
      fechaLocal.setMinutes(fechaLocal.getMinutes() - offset);
      const fechaString = fechaLocal.toISOString().slice(0, 16);

      setFormData({
        nombre: eventoData.nombre,
        descripcion: eventoData.descripcion || '',
        fechaEvento: fechaString,
        ubicacion: eventoData.ubicacion,
        categoria: eventoData.categoria || 'Musica',
        estado: eventoData.estado || 'ACTIVO'
      });

      setCurrentImageUrl(eventoData.imagenUrl);

      // Cargar tipos de entrada
      const tiposResponse = await axios.get(`/api/eventos/${id}/tipos-entrada`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      setTiposEntrada(tiposResponse.data || []);

    } catch (err) {
      console.error('Error cargando evento:', err);
      setError('No se pudo cargar el evento. Verifica que tengas permisos para editarlo.');
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
  };

  const handleTipoEntradaChange = (index, field, value) => {
    const nuevosTipos = [...tiposEntrada];
    nuevosTipos[index][field] = value;
    setTiposEntrada(nuevosTipos);
  };

  const agregarTipoEntrada = () => {
    setTiposEntrada([
      ...tiposEntrada,
      { nombre: '', descripcion: '', precio: 0, cantidadDisponible: 0, isNew: true }
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
    setCurrentImageUrl(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validaciones
    if (!formData.nombre || !formData.fechaEvento || !formData.ubicacion) {
      setError('Por favor completa todos los campos requeridos');
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

      // Subir nueva imagen si se seleccionó una
      let imagenUrl = currentImageUrl;
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
          setSaving(false);
          return;
        }
        setUploadingImage(false);
      }

      // Preparar datos de actualización
      const requestData = {
        nombre: formData.nombre,
        descripcion: formData.descripcion || 'Sin descripción',
        fechaEvento: new Date(formData.fechaEvento).toISOString(),
        ubicacion: formData.ubicacion,
        categoria: formData.categoria,
        imagenUrl: imagenUrl,
        estado: formData.estado
      };

      // Actualizar evento
      await axios.put(`/api/eventos/${id}`, requestData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      setSuccess(true);
      
      // Redirigir a Mis Eventos
      setTimeout(() => {
        navigate('/mis-eventos');
      }, 2000);

    } catch (err) {
      console.error('Error actualizando evento:', err);
      if (err.response?.status === 403) {
        setError('No tienes permisos para editar este evento');
      } else {
        setError(
          err.response?.data?.error || 
          err.response?.data?.message || 
          'No se pudo actualizar el evento. Por favor intenta nuevamente.'
        );
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-xl shadow-lg p-12 max-w-md text-center">
          <CheckCircleIcon className="h-20 w-20 text-green-500 mx-auto mb-6" />
          <h2 className="text-3xl font-bold text-gray-900 mb-4">¡Evento Actualizado!</h2>
          <p className="text-gray-600 mb-6">
            Los cambios se han guardado exitosamente. Redirigiendo...
          </p>
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => navigate('/mis-eventos')}
            className="flex items-center text-gray-600 hover:text-gray-900 mb-4 transition-colors"
          >
            <ArrowLeftIcon className="h-5 w-5 mr-2" />
            Volver a Mis Eventos
          </button>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Editar Evento</h1>
          <p className="text-gray-600">Modifica la información de tu evento</p>
        </div>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Advertencia si el evento no está activo */}
        {formData.estado !== 'ACTIVO' && (
          <div className="mb-6 bg-yellow-50 border border-yellow-200 text-yellow-800 px-4 py-3 rounded-lg">
            <p className="font-semibold">⚠️ Este evento está en estado: {formData.estado}</p>
            <p className="text-sm mt-1">Los eventos cancelados o finalizados no aceptan compras de entradas.</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Información Básica */}
          <div className="bg-white shadow-md rounded-xl p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">Información Básica</h2>
            
            <div className="space-y-6">
              {/* Nombre del Evento */}
              <div>
                <label htmlFor="nombre" className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre del Evento *
                </label>
                <div className="relative">
                  <input
                    type="text"
                    id="nombre"
                    name="nombre"
                    value={formData.nombre}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    placeholder="Ej: Concierto de Rock 2024"
                  />
                </div>
              </div>

              {/* Descripción */}
              <div>
                <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700 mb-2">
                  Descripción
                </label>
                <div className="relative">
                  <DocumentTextIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                  <textarea
                    id="descripcion"
                    name="descripcion"
                    value={formData.descripcion}
                    onChange={handleInputChange}
                    rows="4"
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    placeholder="Describe tu evento..."
                  />
                </div>
              </div>

              {/* Fecha y Ubicación */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label htmlFor="fechaEvento" className="block text-sm font-medium text-gray-700 mb-2">
                    Fecha y Hora *
                  </label>
                  <div className="relative">
                    <CalendarIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                    <input
                      type="datetime-local"
                      id="fechaEvento"
                      name="fechaEvento"
                      value={formData.fechaEvento}
                      onChange={handleInputChange}
                      required
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="ubicacion" className="block text-sm font-medium text-gray-700 mb-2">
                    Ubicación *
                  </label>
                  <div className="relative">
                    <MapPinIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                    <input
                      type="text"
                      id="ubicacion"
                      name="ubicacion"
                      value={formData.ubicacion}
                      onChange={handleInputChange}
                      required
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      placeholder="Ej: Estadio Nacional, Lima"
                    />
                  </div>
                </div>
              </div>

              {/* Categoría y Estado */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label htmlFor="categoria" className="block text-sm font-medium text-gray-700 mb-2">
                    Categoría *
                  </label>
                  <div className="relative">
                    <TagIcon className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                    <select
                      id="categoria"
                      name="categoria"
                      value={formData.categoria}
                      onChange={handleInputChange}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    >
                      {categorias.map(cat => (
                        <option key={cat} value={cat}>{cat}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div>
                  <label htmlFor="estado" className="block text-sm font-medium text-gray-700 mb-2">
                    Estado *
                  </label>
                  <select
                    id="estado"
                    name="estado"
                    value={formData.estado}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  >
                    {estados.map(est => (
                      <option key={est} value={est}>{est}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          </div>

          {/* Imagen del Evento */}
          <div className="bg-white shadow-md rounded-xl p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">Imagen del Evento</h2>
            
            <div className="space-y-4">
              {/* Preview de imagen actual o nueva */}
              {(imagePreview || currentImageUrl) && (
                <div className="relative">
                  <img
                    src={imagePreview || currentImageUrl}
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

              {/* Input de imagen */}
              <div className="flex items-center justify-center w-full">
                <label className="w-full flex flex-col items-center px-4 py-6 bg-white border-2 border-gray-300 border-dashed rounded-lg cursor-pointer hover:bg-gray-50">
                  <PhotoIcon className="h-12 w-12 text-gray-400" />
                  <span className="mt-2 text-sm text-gray-600">
                    {selectedImage || currentImageUrl ? 'Cambiar imagen' : 'Seleccionar imagen'}
                  </span>
                  <span className="mt-1 text-xs text-gray-500">PNG, JPG, GIF o WEBP hasta 10MB</span>
                  <input
                    type="file"
                    className="hidden"
                    accept="image/*"
                    onChange={handleImageSelect}
                  />
                </label>
              </div>
            </div>
          </div>

          {/* Tipos de Entrada - Solo lectura */}
          {tiposEntrada.length > 0 && (
            <div className="bg-white shadow-md rounded-xl p-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-4">Tipos de Entrada</h2>
              <p className="text-sm text-gray-600 mb-6">
                Los tipos de entrada se gestionan por separado. Aquí se muestran los tipos actuales.
              </p>
              
              <div className="space-y-4">
                {tiposEntrada.map((tipo, index) => (
                  <div key={index} className="border border-gray-200 rounded-lg p-4 bg-gray-50">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <span className="text-sm font-medium text-gray-700">Nombre:</span>
                        <p className="text-gray-900">{tipo.nombre}</p>
                      </div>
                      <div>
                        <span className="text-sm font-medium text-gray-700">Precio:</span>
                        <p className="text-gray-900">S/ {tipo.precio?.toFixed(2)}</p>
                      </div>
                      <div>
                        <span className="text-sm font-medium text-gray-700">Disponibles:</span>
                        <p className="text-gray-900">{tipo.cantidadDisponible}</p>
                      </div>
                      <div>
                        <span className="text-sm font-medium text-gray-700">Vendidas:</span>
                        <p className="text-gray-900">{tipo.cantidadVendida || 0}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Botones de Acción */}
          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => navigate('/mis-eventos')}
              className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving || uploadingImage}
              className="flex-1 px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {uploadingImage ? 'Subiendo imagen...' : saving ? 'Guardando...' : 'Guardar Cambios'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
