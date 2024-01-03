import http from 'k6/http';
import { check } from 'k6';

export let options = {
    stages: [
        { duration: '1m', target: 10 }, // Ramp up to 10 virtual users over 1 minute
        { duration: '3m', target: 10 }, // Stay at 10 virtual users for 3 minutes
        { duration: '1m', target: 0 }, // Ramp down to 0 virtual users over 1 minute
    ],
};

export default function () {
    // Define command line options
    const method = __ENV.METHOD || 'GET';
    const payload = __ENV.PAYLOAD || '';
    const contentType = __ENV.CONTENT_TYPE || 'application/json';

    // Define additional headers if provided
    const additionalHeaders = __ENV.HEADERS ? JSON.parse(__ENV.HEADERS) : {};

    // Define the URL you want to test
    const url = 'https://example.com';

    // Define request options
    const params = {
        headers: {
            'Content-Type': contentType,
            ...additionalHeaders,
        },
    };

    // Perform the HTTP request based on the provided method
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

    // Check if the response is successful
    check(response, {
        'is status 200': (r) => r.status === 200,
    });
}