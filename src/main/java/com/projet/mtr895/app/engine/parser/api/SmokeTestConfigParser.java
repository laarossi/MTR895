package com.projet.mtr895.app.engine.parser.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projet.mtr895.app.engine.parser.ExecConfigurationParser;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmokeTestConfigParser implements ExecConfigurationParser {

    @Override
    public void setDefaults(K6ExecConfig k6ExecConfig) {
        k6ExecConfig.setK6JSONFilePath("k6/smoke-testing.js");
        k6ExecConfig.getK6Options().putIfAbsent("vus", "5");
        k6ExecConfig.getK6Options().putIfAbsent("iterations", "10");
    }

    @Override
    public void setConfig(TestCase testCase, K6ExecConfig k6ExecConfig) throws Exception {
        Map<String, Object> execDataMap = testCase.getExecConfigJSONMap();
        if(execDataMap == null || execDataMap.isEmpty())
            throw new Exception("Exec object cannot be empty");

        if(testCase.getRequest() == null)
            throw new Exception("Request cannot be empty");

        List<Object> thresholds = (List<Object>) execDataMap.getOrDefault("thresholds", new ArrayList<>());
        k6ExecConfig.getK6Options().put("vus", execDataMap.getOrDefault("vus", "5").toString());
        k6ExecConfig.getK6Options().put("iterations", execDataMap.getOrDefault("iterations", "10").toString());
        k6ExecConfig.getK6Options().put("max-redirects", execDataMap.getOrDefault("max-redirects", "10").toString());
        k6ExecConfig.getK6Options().put("insecure-skip-tls-verify", execDataMap.getOrDefault("insecure-skip-tls-verify", false));
        k6ExecConfig.getK6Options().put("include-system-env-vars", execDataMap.getOrDefault("include-system-env-vars", false));
        k6ExecConfig.getK6Options().put("no-connection-reuse", execDataMap.getOrDefault("no-connection-reuse", false));
        k6ExecConfig.getK6Options().put("no-vu-connection-reuse", execDataMap.getOrDefault("no-vu-connection-reuse", false));
        k6ExecConfig.getK6EnvironmentVariables().put("thresholds", new ObjectMapper().writeValueAsString(thresholds));
        k6ExecConfig.getK6EnvironmentVariables().put("request", new ObjectMapper().writeValueAsString(testCase.getRequest()));
        Map<String, Object> expectedResponse = (Map<String, Object>) execDataMap.get("response");
        if(expectedResponse != null && !expectedResponse.isEmpty())
            k6ExecConfig.getK6EnvironmentVariables().put("response", new ObjectMapper().writeValueAsString(expectedResponse));

        Map<String, Object> outputExecParams = (Map<String, Object>) execDataMap.get("output");
        if(outputExecParams == null || outputExecParams.isEmpty())
            return;

        k6ExecConfig.setSummary((Boolean) outputExecParams.getOrDefault("summary", true));
        k6ExecConfig.setOutputFile((String) outputExecParams.getOrDefault("file", null));
        k6ExecConfig.setOutputFormat((String) outputExecParams.getOrDefault("format", "json"));
    }

}
