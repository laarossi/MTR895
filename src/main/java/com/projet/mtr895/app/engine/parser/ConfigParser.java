package com.projet.mtr895.app.engine.parser;

import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;

public interface ConfigParser {

    void setDefaults(ExecConfig execConfig);
    ExecConfig setConfig(TestCase testCase) throws Exception;

    default ExecConfig parse(TestCase testCase) throws Exception {
        ExecConfig execConfig = setConfig(testCase);
        setDefaults(execConfig);
        return execConfig;
    }

}
