package com.projet.mtr895.app.engine.parser;

import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;

import java.util.Map;

public interface ExecConfigurationParser {

    void setDefaults(K6ExecConfig k6ExecConfig);
    void setConfig(TestCase testCase, K6ExecConfig k6ExecConfig) throws Exception;

    default ExecConfig parse(TestCase testCase) throws Exception {
        K6ExecConfig k6ExecConfig = new K6ExecConfig();
        setConfig(testCase, k6ExecConfig);
        setDefaults(k6ExecConfig);
        return k6ExecConfig;
    }

}
