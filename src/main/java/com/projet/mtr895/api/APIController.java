package com.projet.mtr895.api;
import com.projet.mtr895.app.TestExecutor;
import com.projet.mtr895.app.entities.TestCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.HashMap;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("api-test")
public class APIController {


    @PostMapping
    public ResponseEntity<?> smokeTest(@RequestBody Map<String, Object> testCaseJSONMap) {
        Map<String, Object> response = new HashMap<>();
        TestCase testCase = null;
        try {
            testCase = TestExecutor.loadTest(testCaseJSONMap);
        } catch (Exception e) {
            response.put("status", false);
            response.put("message", e.getLocalizedMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (testCase == null){
            response.put("status", false);
            response.put("message", "failed to load test case");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            boolean executionStatus = TestExecutor.runTest(testCase);
            response.put("execution-status", executionStatus);
            response.put("data", testCase.getJsonExecutionResultsMap());
        } catch (Exception e) {
            response.put("status", false);
            response.put("message", e.getLocalizedMessage());
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}