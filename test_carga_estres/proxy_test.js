import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 100 }, // ramp-up
    { duration: '5m', target: 100 },  // carga sostenida
    { duration: '30s', target: 0 },   // ramp-down
  ],
};

export default function () {
  let res = http.get('http://localhost:8080/items/MLA120350', {
    headers: { 'X-Forwarded-For': '152.152.152.152' }
  });
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
