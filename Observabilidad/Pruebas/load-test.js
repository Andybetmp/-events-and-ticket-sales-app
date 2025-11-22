import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { SharedArray } from 'k6/data';

// Configuración de la prueba de carga
export const options = {
  stages: [
    { duration: '30s', target: 10 }, // Subir a 10 usuarios en 30s
    { duration: '1m', target: 10 },  // Mantener 10 usuarios por 1 minuto
    { duration: '10s', target: 0 },  // Bajar a 0
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% de las peticiones deben ser < 2s
    http_req_failed: ['rate<0.01'],    // Menos del 1% de errores
  },
};

const BASE_URL = 'http://localhost:8080'; // URL del Gateway

// Generador de datos aleatorios simples
function randomString(length) {
  return Math.random().toString(36).substring(2, length + 2);
}

// Función Setup: Se ejecuta una vez al principio para crear el Evento
export function setup() {
  // 1. Registrar usuario Admin para crear evento
  const adminEmail = `admin_${randomString(5)}@test.com`;
  const adminPass = 'admin123';
  
  const registerRes = http.post(`${BASE_URL}/api/orchestration/register`, JSON.stringify({
    nombre: "Admin",
    apellido: "Test",
    email: adminEmail,
    contrasena: adminPass
  }), { headers: { 'Content-Type': 'application/json' } });

  check(registerRes, { 'Admin registered': (r) => r.status === 200 });

  // 2. Login Admin
  const loginRes = http.post(`${BASE_URL}/api/users/login`, JSON.stringify({
    email: adminEmail,
    contrasena: adminPass
  }), { headers: { 'Content-Type': 'application/json' } });

  const token = loginRes.json('token');

  // 3. Crear Evento
  const eventPayload = JSON.stringify({
    nombre: `K6 Rock Fest ${randomString(4)}`,
    descripcion: "Evento de prueba de carga",
    ubicacion: "Estadio Virtual",
    fechaEvento: "2026-12-31T20:00:00",
    categoria: "Musica",
    tiposEntrada: [
      { nombre: "General", descripcion: "General", precio: 50.00, cantidad: 10000, orden: 1 }
    ]
  });

  const eventRes = http.post(`${BASE_URL}/api/orchestration/create-event`, eventPayload, {
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  });

  check(eventRes, { 'Event created': (r) => r.status === 200 });
  
  const eventData = eventRes.json();
  // Retornamos el ID del tipo de entrada para que los VUs lo usen
  return { 
    tipoEntradaId: eventData.tiposEntrada[0].id 
  };
}

// Función Default: Se ejecuta por cada Usuario Virtual (VU)
export default function (data) {
  const tipoEntradaId = data.tipoEntradaId;
  let authToken = '';
  let userEmail = `user_${randomString(8)}@test.com`;
  let userPass = 'pass123456';

  group('1. Registro de Usuario', function () {
    const payload = JSON.stringify({
      nombre: "K6",
      apellido: "User",
      email: userEmail,
      contrasena: userPass
    });

    const res = http.post(`${BASE_URL}/api/orchestration/register`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    check(res, { 'Registro status 200': (r) => r.status === 200 });
  });

  group('2. Login', function () {
    const payload = JSON.stringify({
      email: userEmail,
      contrasena: userPass
    });

    const res = http.post(`${BASE_URL}/api/users/login`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });

    check(res, { 'Login status 200': (r) => r.status === 200 });
    authToken = res.json('token');
  });

  group('3. Comprar Ticket (SAGA)', function () {
    const payload = JSON.stringify({
      tipoEntradaId: tipoEntradaId,
      cantidad: 1, // Comprar 1 ticket
      paymentMethod: {
        cardNumber: "4532123456789012",
        cvv: "123",
        expiryDate: "12/28",
        cardHolder: "K6 TESTER"
      }
    });

    const res = http.post(`${BASE_URL}/api/orchestration/purchase-ticket`, payload, {
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
    });

    check(res, { 'Compra exitosa (200)': (r) => r.status === 200 });
  });

  group('4. Consultar Mis Tickets', function () {
    const res = http.get(`${BASE_URL}/api/orchestration/my-tickets`, {
      headers: { 'Authorization': `Bearer ${authToken}` },
    });

    check(res, { 'Consulta tickets (200)': (r) => r.status === 200 });
  });

  sleep(1); // Espera de 1 segundo entre iteraciones
}