package com.projet.mtr895.app.engine.parser.k6;

import java.util.HashMap;
import java.util.Map;

public class SmokeTestConfigParser extends K6ExecConfigurationParser{

    @Override
    public Map<String, String> parseK6Options(Map<String, Object> execDataMap) {
        Map<String, String> optionsVariables = new HashMap<>();
        optionsVariables.put("vus", (String) execDataMap.get("vus"));
        optionsVariables.put("iterations", (String) execDataMap.get("iterations"));
        optionsVariables.put("maxDuration", (String) execDataMap.get("maxDuration"));
        optionsVariables.put("executor", (String) execDataMap.get("per-vu-iteration"));
        return optionsVariables;
    }
}
