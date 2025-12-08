import { Navigate } from 'react-router-dom';

export default function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  
  if (!token) {
    // Redirigir a login si no hay token
    return <Navigate to="/login" replace />;
  }
  
  return children;
}
