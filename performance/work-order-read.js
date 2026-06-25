import http from 'k6/http'
import { check } from 'k6'
import { Rate } from 'k6/metrics'

const baseUrl = __ENV.BASE_URL || 'http://host.docker.internal:8080'
const authToken = __ENV.AUTH_TOKEN
const testPath = __ENV.TEST_PATH || '/api/work-orders/page?pageNum=1&pageSize=5&status=PENDING_REVIEW'
const businessFailures = new Rate('business_failures')

if (!authToken) {
  throw new Error('AUTH_TOKEN is required. Log in first and pass a valid Bearer token.')
}

export const options = {
  scenarios: {
    sustained_business_reads: {
      executor: 'constant-arrival-rate',
      rate: 17,
      timeUnit: '1s',
      duration: '1m',
      preAllocatedVUs: 20,
      maxVUs: 100
    }
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    business_failures: ['rate<0.01'],
    checks: ['rate>0.99']
  }
}

export default function () {
  const response = http.get(`${baseUrl}${testPath}`, {
    headers: { Authorization: `Bearer ${authToken}` },
    tags: { endpoint: 'work-order-page-query' }
  })

  let apiCode = 0
  try {
    apiCode = response.json('code')
  } catch (error) {
    apiCode = 0
  }

  const passed = check(response, {
    'HTTP status is 200': result => result.status === 200,
    'business code is 200': () => apiCode === 200
  })
  businessFailures.add(!passed)
}

