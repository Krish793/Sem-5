import http from 'k6/http';
import { check, sleep } from 'k6';

// Diwali / Navratri Festival Peak Load Simulation for KalaSetu Marketplace
export const options = {
  stages: [
    { duration: '15s', target: 15 }, // Ramp-up: festive shoppers arriving
    { duration: '30s', target: 40 }, // Peak festival browse + search spike
    { duration: '15s', target: 0 },  // Cool-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests under 500ms
    http_req_failed: ['rate<0.01'],   // Less than 1% error rate
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const catalogRes = http.get(`${BASE_URL}/api/products`);
  check(catalogRes, {
    'catalog status is 200': (r) => r.status === 200,
    'catalog returns artisan crafts': (r) => r.body.includes('Bandhani'),
  });

  const searchRes = http.get(`${BASE_URL}/api/products/search?q=Patola`);
  check(searchRes, {
    'search status is 200': (r) => r.status === 200,
  });

  const healthRes = http.get(`${BASE_URL}/actuator/health`);
  check(healthRes, {
    'health is UP': (r) => r.status === 200 && r.body.includes('UP'),
  });

  sleep(1);
}
