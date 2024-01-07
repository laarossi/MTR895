import http from 'k6/http';
import { check } from 'k6';

const envVars = JSON.parse(__ENV.ENV_VARS || '{}');
// Extract values from the configuration
const vus = envVars.vus || 5;
const duration = envVars.duration || '5s';
const method = envVars.method || 'GET';
const payload = envVars.payload || '';
const contentType = envVars.contentType || 'application/json';
const headers = envVars.headers || {};

export const options = {
    vus: vus,
    duration: duration,
};

export default function () {
    additionalHeaders.push(contentType)
    const params = {
        headers: additionalHeaders
    };
    let response;
    if (method.toUpperCase() === 'GET') {
        response = http.get(url, params);
    } else if (method.toUpperCase() === 'POST') {
        response = http.post(url, payload, params);
    } else if (method.toUpperCase() === 'PUT') {
        response = http.put(url, payload, params);
    } else if (method.toUpperCase() === 'DELETE') {
        response = http.del(url, payload, params);
    } else {
        console.error(`Unsupported HTTP method: ${method}`);
        return;
    }
    check(response, {
        'is status 200': (r) => r.status === 200,
    });
}