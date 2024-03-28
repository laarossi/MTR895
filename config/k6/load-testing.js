import http from 'k6/http';
import {check, fail} from 'k6';
import {getCurrentStageIndex} from 'https://jslib.k6.io/k6-utils/1.3.0/index.js';


let req_vars = __ENV.request;
let response = '{}';
let host = null;
let method = null;
let payload = null;
let headers = null;
let cookies = null;
let stagesJSON = __ENV.stages || {};
let responsePayload = null;
let thresholds = '{}';
let statusError = false;
let payloadError = false;
let headersError = false;
let headersErrorNotFound = false;

if (__ENV.thresholds) {
    thresholds = JSON.parse(__ENV.thresholds) || '{}'
}

if (__ENV.request) {
    req_vars = JSON.parse(req_vars);
    host = req_vars.host;
    method = req_vars.method;
    payload = req_vars.payload;
    headers = req_vars.headers;
    if (req_vars.cookies)
        cookies = JSON.parse(cookies)
    else cookies = '{}'
}

if (__ENV.stages) {
    stagesJSON = JSON.parse(__ENV.stages);
}


if (__ENV.response) {
    response = JSON.parse(__ENV.response) || '{}'
}

if (response && response.payload){
    responsePayload = JSON.parse(open(response.payload));
}

thresholds.checks = ['rate>=1']

export const options = {
    thresholds: thresholds,
    stages: stagesJSON,
}


function compareObjects(o1, o2) {
    const normalizedObj1 = Object.fromEntries(Object.entries(o1).sort(([k1], [k2]) => k1.localeCompare(k2)));
    const normalizedObj2 = Object.fromEntries(Object.entries(o2).sort(([k1], [k2]) => k1.localeCompare(k2)));
    return JSON.stringify(normalizedObj1) === JSON.stringify(normalizedObj2);
}

export default function () {
    const res = http.request(method, host, payload, { headers : headers, cookies : cookies });
    check(res, {
        'check http status': (r) => {
            if (response === '{}' || !response.status) return true;
            if (r.status !== response.status) {
                if (!statusError) {
                    statusError = true;
                    console.error(`Status Expected: ${response.status}, Actual: ${r.status}`);
                }
            }
            return r.status === response.status;
        },
        'check http payload': (r) => {
            if (response === '{}' || !response.payload || responsePayload === '{}') return true;
            let rspJson = res.json();
            if (!compareObjects(rspJson, responsePayload))
                if (!payloadError) {
                    payloadError = true;
                    console.error("Not matching payload");
                }
            return compareObjects(rspJson, responsePayload);
        },
        'check http headers': (r) => {
            if (response === '{}' || !response.headers) return true;
            for (const [expectedHeader, expectedValue] of Object.entries(response.headers)) {
                const actualValue = res.headers[expectedHeader];
                if (actualValue === undefined) {
                    if (!headersErrorNotFound) {
                        headersErrorNotFound = true;
                        console.error(`${expectedHeader} not found in the response headers`);
                    }
                    return false;
                } else if (actualValue !== expectedValue) {
                    if (!headersError) {
                        headersError = true;
                        console.error(`${expectedHeader} has incorrect value. Expected: ${expectedValue}, Actual: ${actualValue}`);
                    }
                    return false;
                }
            }
            return true;
        },
        'check http cookies': (r) => {
            if (response === '{}' || !response.cookies) return true;
            for (const [expectedHeader, expectedValue] of Object.entries(cookies)) {
                const actualValue = res.headers[expectedHeader];
                if (actualValue === undefined) {
                    if (!headersErrorNotFound) {
                        headersErrorNotFound = true;
                        console.error(`${expectedHeader} not found in the response headers`);
                    }
                    return false;
                } else if (actualValue !== expectedValue) {
                    if (!headersError) {
                        headersError = true;
                        console.error(`${expectedHeader} has incorrect value. Expected: ${expectedValue}, Actual: ${actualValue}`);
                    }
                    return false;
                }
            }
            return true;
        }
    });
}


