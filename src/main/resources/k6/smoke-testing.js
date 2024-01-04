import http from 'k6/http';
import { check } from 'k6';

const vus = __ENV.VUS || 5
const duration = __ENV.DURATION || 'GET'
const method = __ENV.METHOD || 'GET';
const payload = __ENV.PAYLOAD || '';
const contentType = __ENV.CONTENT_TYPE || 'application/json';
const additionalHeaders = __ENV.HEADERS ? JSON.parse(__ENV.HEADERS) : {};
const url = __ENV.HOST || 'GET';

export const options = {
    vus: vus,
    duration: duration,
};

export default function () {
    const params = {
        headers: {
            'Content-Type': contentType,
            ...additionalHeaders,
        },
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