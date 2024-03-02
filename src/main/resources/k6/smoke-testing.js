import http from 'k6/http';
import { check, fail } from 'k6';

const req_vars = JSON.parse(__ENV.request) || '{}';
const response = JSON.parse(__ENV.response) || '{}';
const host = req_vars.host;
const path = req_vars.path;
const method = req_vars.method;
const payload = req_vars.payload;
const headers = req_vars.headers || {};
const output = __ENV.output;

let statusError = false;
let payloadError = false;
let headersError = false;

export const options = {
    thresholds: {
        checks: ['rate>=1'],
    },
}

export default function () {
    const res = http.request(method, host + "/" + path, payload, { headers });
    console.log("response : " + JSON.stringify(res))
    check(res, {
        'check http status' : (r) => {
            if(r.status !== response.status){
                if(!statusError){
                    console.error(`Status Expected: ${response.status}, Actual: ${r.status}`);
                    statusError = true
                }
            }
            return r.status === response.status;
        },
        'check http payload' : (r) => {
            if(!r.body.includes(response.payload))
                if(!payloadError){
                    console.error("Not matching payload");
                    payloadError = true
                }
            return r.body.includes(response.payload);
        },
        'check http headers' : (r) => {
            for (const [expectedHeader, expectedValue] of Object.entries(response.headers)) {
                const actualValue = res.headers[expectedHeader];
                if (actualValue === undefined) {
                    if(!headersError){
                        console.error(`${expectedHeader} not found in the response headers`);
                        headersError = true
                    }
                    return false;
                } else if (actualValue !== expectedValue) {
                    if(!headersError){
                        console.error(`${expectedHeader} has incorrect value. Expected: ${expectedValue}, Actual: ${actualValue}`);
                        headersError = true
                    }
                    return false;
                }
            }
            return true;
        }
    });
}
