# 🎫 SOA Ticketing - Frontend

Sistema de venta de entradas para eventos con arquitectura de microservicios orientada a servicios (SOA).

## 📋 Descripción

Frontend React moderno para la gestión completa de eventos y venta de tickets, integrado con una arquitectura de microservicios en el backend. Incluye autenticación JWT, gestión de perfiles, compra de tickets con timer, generación de PDFs con códigos QR y panel administrativo.

## 🚀 Tecnologías

- **React 19.2.0** - Framework de UI
- **Vite 6.0.5** - Build tool y dev server
- **React Router 7.1.1** - Navegación y rutas
- **Axios 1.7.9** - Cliente HTTP
- **Tailwind CSS 3.4.17** - Framework de estilos
- **Heroicons 2.2.0** - Iconos
- **jsPDF** - Generación de PDFs
- **QRCode 1.5.4** - Generación de códigos QR

## ✨ Funcionalidades Principales

### 🔐 Autenticación y Usuario
- ✅ Registro de usuarios con validaciones
- ✅ Login con JWT
- ✅ Recuperación de contraseña vía email
- ✅ Gestión de perfil (editar datos, cambiar contraseña)
- ✅ Rutas protegidas por autenticación y rol

### 🎭 Gestión de Eventos
- ✅ Catálogo de eventos con búsqueda y filtros por categoría
- ✅ Vista de detalles con tipos de entrada y precios
- ✅ Crear eventos (nombre, descripción, fecha, ubicación, imagen)
- ✅ Editar eventos propios
- ✅ Cancelar eventos
- ✅ Estados: ACTIVO, CANCELADO, FINALIZADO
- ✅ Badge "Agotado" cuando no hay entradas disponibles
- ✅ Vista en grid o lista

### 🎟️ Sistema de Compra
- ✅ Carrito con múltiples tipos de entrada
- ✅ Timer de 5 minutos para completar compra
- ✅ Límite de 5 entradas por compra
- ✅ Validación de stock en tiempo real
- ✅ Formulario de pago (mock)
- ✅ Proceso transaccional con patrón Saga

### 📄 Tickets
- ✅ Generación de códigos QR únicos por ticket
- ✅ Descarga de tickets en PDF profesional
- ✅ Visualización de tickets con QR visible
- ✅ Información completa del evento
- ✅ Estado de confirmación

### 📊 Panel de Administración
- ✅ Dashboard con estadísticas
- ✅ Gestión de eventos (solo ADMIN)
- ✅ Navegación rápida a secciones

### 🎨 Interfaz de Usuario
- ✅ Diseño responsive (mobile, tablet, desktop)
- ✅ Esquema de colores: Teal (#14B8A6) y Negro
- ✅ Animaciones y transiciones suaves
- ✅ Hero carousel en página principal
- ✅ Paginación en listados (9 eventos, 5 tickets/eventos por página)
- ✅ Estados de carga y mensajes de error

### 🛡️ Seguridad y Calidad
- ✅ Validación de formularios
- ✅ Manejo de errores centralizado
- ✅ Logging condicional (solo en desarrollo)
- ✅ Protección contra XSS y CSRF
- ✅ Headers de autenticación en todas las peticiones

## 📁 Estructura del Proyecto

```
Frontend/
├── src/
│   ├── components/
│   │   ├── EventCard.jsx          # Tarjeta de evento
│   │   ├── Footer.jsx              # Footer del sitio
│   │   ├── Hero.jsx                # Carrusel de eventos destacados
│   │   ├── Navbar.jsx              # Barra de navegación
│   │   └── ProtectedRoute.jsx     # HOC para rutas protegidas
│   ├── pages/
│   │   ├── AdminPanel.jsx          # Dashboard administrativo
│   │   ├── Checkout.jsx            # Proceso de compra
│   │   ├── CrearEvento.jsx         # Formulario crear evento
│   │   ├── EditarEvento.jsx        # Formulario editar evento
│   │   ├── EventoDetalle.jsx       # Vista detalle de evento
│   │   ├── ForgotPassword.jsx      # Recuperar contraseña
│   │   ├── Home.jsx                # Página principal
│   │   ├── Login.jsx               # Inicio de sesión
│   │   ├── MisEventos.jsx          # Eventos del organizador
│   │   ├── MisTickets.jsx          # Tickets del usuario
│   │   ├── Perfil.jsx              # Gestión de perfil
│   │   ├── Register.jsx            # Registro de usuario
│   │   └── ResetPassword.jsx       # Restablecer contraseña
│   ├── utils/
│   │   ├── logger.js               # Sistema de logging condicional
│   │   └── roleUtils.js            # Utilidades de roles
│   ├── App.jsx                     # Componente raíz con rutas
│   └── main.jsx                    # Entry point
├── public/                         # Recursos estáticos
├── index.html                      # HTML base
├── package.json                    # Dependencias
├── tailwind.config.js              # Configuración Tailwind
└── vite.config.js                  # Configuración Vite
```

## 🔧 Instalación y Configuración

### Requisitos Previos
- Node.js 18+ 
- npm o yarn
- Backend de microservicios corriendo (ver README principal)

### Pasos de Instalación

1. **Clonar el repositorio:**
```bash
git clone <repo-url>
cd SOA/Frontend
```

2. **Instalar dependencias:**
```bash
npm install
```

3. **Configurar variables de entorno:**
Crear archivo `.env` (opcional, usa proxy en vite.config.js):
```env
VITE_API_URL=http://localhost:8080
```

4. **Iniciar servidor de desarrollo:**
```bash
npm run dev
```

La aplicación estará disponible en `http://localhost:5173`

### Scripts Disponibles

```bash
npm run dev          # Inicia servidor de desarrollo
npm run build        # Build de producción
npm run preview      # Preview del build
npm run lint         # Ejecuta ESLint
```

## 🌐 Rutas de la Aplicación

### Públicas
- `/` - Home con listado de eventos
- `/login` - Inicio de sesión
- `/register` - Registro de usuario
- `/forgot-password` - Recuperar contraseña
- `/reset-password/:token` - Restablecer contraseña
- `/evento/:id` - Detalle de evento

### Protegidas (requieren autenticación)
- `/perfil` - Gestión de perfil
- `/mis-tickets` - Tickets comprados
- `/mis-eventos` - Eventos creados (organizadores)
- `/crear-evento` - Crear nuevo evento
- `/editar-evento/:id` - Editar evento
- `/checkout` - Proceso de compra

### Admin (requieren rol ADMIN)
- `/admin` - Panel administrativo

## 🔌 Integración con Backend

El frontend se comunica con los siguientes endpoints:

### Gateway (puerto 8080)
```
POST   /api/auth/login              # Login
POST   /api/auth/register           # Registro
POST   /api/usuarios/forgot-password # Recuperar contraseña
POST   /api/usuarios/reset-password  # Restablecer contraseña

GET    /api/eventos                 # Listar eventos
GET    /api/eventos/:id             # Detalle de evento
POST   /api/eventos                 # Crear evento
PUT    /api/eventos/:id             # Editar evento
DELETE /api/eventos/:id             # Cancelar evento

GET    /api/eventos/:id/tipos-entrada        # Tipos de entrada
POST   /api/eventos/:id/tipos-entrada        # Crear tipo entrada
PUT    /api/tipos-entrada/:id                # Editar tipo entrada
DELETE /api/tipos-entrada/:id                # Eliminar tipo entrada

GET    /api/usuarios/me             # Perfil del usuario
PUT    /api/usuarios/me             # Actualizar perfil
PUT    /api/usuarios/me/password    # Cambiar contraseña

POST   /api/orchestration/purchase-ticket    # Comprar ticket
GET    /api/orchestration/my-tickets         # Mis tickets
```

## 📱 Características Responsive

- **Mobile (< 768px):** Layout de columna única, menú hamburguesa
- **Tablet (768px - 1024px):** Grid de 2 columnas
- **Desktop (> 1024px):** Grid de 3 columnas, menú completo

## 🔒 Sistema de Autenticación

### Almacenamiento
- **JWT Token:** `localStorage.getItem('token')`
- **User Data:** `localStorage.getItem('user')` (JSON)

### Headers de Autenticación
```javascript
headers: {
  'Authorization': `Bearer ${token}`
}
```

### Roles
- `USER` - Usuario estándar (puede comprar tickets)
- `ORGANIZER` - Puede crear y gestionar eventos
- `ADMIN` - Acceso completo al sistema

## 🎫 Sistema de Tickets

### Generación de QR
Cada ticket genera un código QR único que contiene el `ticketId`:
```javascript
QRCode.toDataURL(ticket.ticketId, { width: 300, margin: 2 })
```

### Descarga de PDF
Los tickets se pueden descargar en formato PDF con:
- Código QR centrado
- Información del evento
- Detalles de compra
- Instrucciones de uso
- Diseño profesional con colores corporativos

## 🐛 Debugging

### Logger Condicional
El sistema usa un logger que solo muestra mensajes en desarrollo:

```javascript
import { logger } from '../utils/logger';

logger.log('Mensaje de debug');    // Solo en dev
logger.error('Error:', err);       // Solo en dev
```

En producción (`npm run build`), todos los logs están silenciados.

## 📈 Optimizaciones

- ✅ Code splitting por rutas
- ✅ Lazy loading de componentes
- ✅ Paginación para evitar renderizado masivo
- ✅ Debounce en búsquedas
- ✅ Caché de imágenes
- ✅ Minificación en build

## 🚧 Mejoras Futuras

- [ ] Upload de imágenes (actualmente solo URLs)
- [ ] Sistema de notificaciones toast
- [ ] Gráficos de estadísticas avanzadas
- [ ] Filtros por fecha, precio y ubicación
- [ ] Sistema de reseñas y calificaciones
- [ ] Validación de tickets con QR scanner
- [ ] Envío de tickets por email
- [ ] Wallet Pass (Apple/Google Pay)

## 📄 Licencia

Proyecto académico - Sistema de Venta de Entradas con Arquitectura SOA

---

**Desarrollado con React + Vite + Tailwind CSS**