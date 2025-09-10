import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 100 }, // ramp-up
    { duration: '2m', target: 100 },  // carga sostenida
    { duration: '30s', target: 0 },   // ramp-down
  ],
};

export default function () {
  let res1 = http.get('http://localhost:8080/items/MLA120350', {
    headers: { 'X-Forwarded-For': '152.152.152.152' }
  });
  check(res1, {
    'items status is 200': (r) => r.status === 200,
  });

  let res2 = http.get('http://localhost:8080/categories/MLA120350', {
    headers: { 'X-Forwarded-For': '152.152.152.152' }
  });
  check(res2, {
    'categories status is 200': (r) => r.status === 200,
  });
}
