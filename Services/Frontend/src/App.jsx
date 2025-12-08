import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import MisTickets from './pages/MisTickets';
import EventoDetalle from './pages/EventoDetalle';
import Checkout from './pages/Checkout';
import CrearEvento from './pages/CrearEvento';
import MisEventos from './pages/MisEventos';
import EditarEvento from './pages/EditarEvento';
import Perfil from './pages/Perfil';
import AdminPanel from './pages/AdminPanel';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <Navbar />
        <main className="flex-grow">
          <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/evento/:id" element={<EventoDetalle />} />
          
          {/* Rutas Protegidas */}
          <Route path="/mis-tickets" element={
            <ProtectedRoute>
              <MisTickets />
            </ProtectedRoute>
          } />
          <Route path="/checkout" element={
            <ProtectedRoute>
              <Checkout />
            </ProtectedRoute>
          } />
          <Route path="/crear-evento" element={
            <ProtectedRoute>
              <CrearEvento />
            </ProtectedRoute>
          } />
          <Route path="/mis-eventos" element={
            <ProtectedRoute>
              <MisEventos />
            </ProtectedRoute>
          } />
          <Route path="/editar-evento/:id" element={
            <ProtectedRoute>
              <EditarEvento />
            </ProtectedRoute>
          } />
          <Route path="/perfil" element={
            <ProtectedRoute>
              <Perfil />
            </ProtectedRoute>
          } />
          <Route path="/admin" element={
            <ProtectedRoute>
              <Perfil />
            </ProtectedRoute>
          } />
          <Route path="/admin" element={
            <ProtectedRoute>
              <AdminPanel />
            </ProtectedRoute>
          } />
            {/* Otras rutas */}
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
