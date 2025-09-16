import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 100 }, // Ramp-up: sube hasta 100 VUs
    { duration: '5m', target: 100 },  // Carga sostenida: 100 VUs durante 1 minuto
    { duration: '30s', target: 0 },   // Ramp-down: baja a 0 VUs
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'], // menos del 1% de fallos
    http_req_duration: ['p(95)<500'], // 95% de las peticiones bajo 500ms
  },
};

export default function () {
  for (let i = 0; i < 2; i++) { // cada VU hace 2 peticiones por iteración
    let res = http.get('http://34.172.64.199/categories/MLA120350', {
      headers: { 'X-Forwarded-For': '127.0.0.1' }
    });

    check(res, {
      'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
    });

    sleep(0.5); // espera 500ms entre peticiones
  }
}
