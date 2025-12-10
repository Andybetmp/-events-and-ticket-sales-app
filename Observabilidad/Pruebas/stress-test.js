import http from 'k6/http';
import { check, sleep, group } from 'k6';

// Configuración EXTREMA para Prueba de Estrés
export const options = {
  stages: [
    { duration: '1m', target: 50 },    // Fase 1: Calentamiento
    { duration: '2m', target: 200 },   // Fase 2: Carga media
    { duration: '2m', target: 500 },   // Fase 3: Carga alta
    { duration: '3m', target: 1000 },  // Fase 4: PUNTO DE QUIEBRE (Objetivo)
    { duration: '1m', target: 0 },     // Fase 5: Recuperación
  ],
  thresholds: {
    // Somos más tolerantes con la latencia en estrés (5s)
    http_req_duration: ['p(95)<5000'], 
    // Si más del 10% falla, el sistema ha colapsado
    http_req_failed: ['rate<0.10'],    
  },
};

const BASE_URL = 'http://localhost:8080';

function randomString(length) {
  return Math.random().toString(36).substring(2, length + 2);
}

export function setup() {
  // 1. Registrar usuario Admin
  const adminEmail = `admin_${randomString(5)}@stress.com`;
  const adminPass = 'admin123';
  
  const registerRes = http.post(`${BASE_URL}/api/orchestration/register`, JSON.stringify({
    nombre: "AdminStress", apellido: "Test", email: adminEmail, contrasena: adminPass
  }), { headers: { 'Content-Type': 'application/json' } });

  check(registerRes, { 'Setup: Admin registered': (r) => r.status === 200 });

  // 2. Login
  const loginRes = http.post(`${BASE_URL}/api/users/login`, JSON.stringify({
    email: adminEmail, contrasena: adminPass
  }), { headers: { 'Content-Type': 'application/json' } });

  const token = loginRes.json('token');

  // 3. Crear Evento Masivo
  const eventRes = http.post(`${BASE_URL}/api/orchestration/create-event`, JSON.stringify({
    nombre: `Stress Fest ${randomString(4)}`,
    descripcion: "Evento para prueba de estrés masiva",
    ubicacion: "Nube",
    fechaEvento: "2026-12-31T20:00:00",
    categoria: "Musica",
    tiposEntrada: [
      { nombre: "General", descripcion: "General", precio: 10.00, cantidad: 50000, orden: 1 }
    ]
  }), { 
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } 
  });

  check(eventRes, { 'Setup: Event created': (r) => r.status === 200 });
  
  return { tipoEntradaId: eventRes.json().tiposEntrada[0].id };
}

export default function (data) {
  const tipoEntradaId = data.tipoEntradaId;
  let authToken = '';
  // Usamos emails muy cortos para ahorrar memoria en el generador de carga
  let userEmail = `u${randomString(8)}@s.co`; 
  let userPass = '123456';

  group('1. Registro', function () {
    const res = http.post(`${BASE_URL}/api/orchestration/register`, JSON.stringify({
      nombre: "K6", apellido: "S", email: userEmail, contrasena: userPass
    }), { headers: { 'Content-Type': 'application/json' } });
    check(res, { 'Reg 200': (r) => r.status === 200 });
  });

  group('2. Login', function () {
    const res = http.post(`${BASE_URL}/api/users/login`, JSON.stringify({
      email: userEmail, contrasena: userPass
    }), { headers: { 'Content-Type': 'application/json' } });
    
    if (check(res, { 'Login 200': (r) => r.status === 200 })) {
      authToken = res.json('token');
    }
  });

  if (authToken) {
    group('3. Compra', function () {
      const res = http.post(`${BASE_URL}/api/orchestration/purchase-ticket`, JSON.stringify({
        tipoEntradaId: tipoEntradaId,
        cantidad: 1,
        paymentMethod: {
          cardNumber: "4532123456789012", cvv: "123", expiryDate: "12/28", cardHolder: "STRESS TEST"
        }
      }), { 
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${authToken}` } 
      });
      check(res, { 'Buy 200': (r) => r.status === 200 });
    });
  }

  sleep(1);
}