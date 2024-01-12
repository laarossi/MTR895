package com.projet.mtr895.app.engine.parser.k6;

import com.projet.mtr895.app.engine.parser.ExecConfigurationParser;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;

import java.util.Map;

public abstract class K6ExecConfigurationParser implements ExecConfigurationParser {

    @Override
    public void setDefaults(K6ExecConfig k6ExecConfig) {
        if(k6ExecConfig.getK6JSONFilePath() == null || k6ExecConfig.getK6JSONFilePath().isEmpty())
            k6ExecConfig.setK6JSONFilePath("k6/smoke-testing.js");

        k6ExecConfig.getK6Options().putIfAbsent("vus", "5");
        k6ExecConfig.getK6Options().putIfAbsent("executor", "per-vu-iteration");
        k6ExecConfig.getK6Options().putIfAbsent("iteration", "20");
        k6ExecConfig.getK6Options().putIfAbsent("duration", "5s");
    }

    @Override
    public K6ExecConfig parse(Map<String, Object> execDataMap){
        K6ExecConfig k6ExecConfig = new K6ExecConfig();
        k6ExecConfig.getK6Options().putAll(parseK6EnvironmentVariables(execDataMap));
        k6ExecConfig.getK6EnvironmentVariables().putAll(parseK6Options(execDataMap));
        setDefaults(k6ExecConfig);
        return k6ExecConfig;
    }

}
