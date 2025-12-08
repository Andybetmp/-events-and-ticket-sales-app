// Utilidad para gestión de roles y permisos en el frontend

/**
 * Obtiene el rol del usuario actual desde localStorage
 * @returns {string|null} El rol del usuario ('ADMIN' o 'USUARIO') o null si no está autenticado
 */
export const getUserRole = () => {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  return user?.rol || null;
};

/**
 * Verifica si el usuario tiene un rol específico
 * @param {string} role - El rol a verificar ('ADMIN' o 'USUARIO')
 * @returns {boolean} true si el usuario tiene ese rol
 */
export const hasRole = (role) => {
  const userRole = getUserRole();
  return userRole === role;
};

/**
 * Verifica si el usuario es administrador
 * @returns {boolean} true si el usuario es ADMIN
 */
export const isAdmin = () => {
  return hasRole('ADMIN');
};

/**
 * Verifica si el usuario tiene al menos uno de los roles proporcionados
 * @param {string[]} roles - Array de roles a verificar
 * @returns {boolean} true si el usuario tiene alguno de los roles
 */
export const hasAnyRole = (roles) => {
  const userRole = getUserRole();
  return roles.includes(userRole);
};

/**
 * Obtiene información completa del usuario
 * @returns {object|null} Objeto con datos del usuario o null
 */
export const getCurrentUser = () => {
  return JSON.parse(localStorage.getItem('user') || 'null');
};
