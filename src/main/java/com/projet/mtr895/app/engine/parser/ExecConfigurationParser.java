package com.projet.mtr895.app.engine.parser;

import com.projet.mtr895.app.entities.exec.ExecConfig;

import java.util.Map;

public interface ExecConfigurationParser {
    ExecConfig parse(Map<String, Object> execDataMap);

    void setDefaults();

    default Map<String, String> parseK6Options(Map<String, Object> execDataMap){
        return null;
    }

    default Map<String, String> parseK6EnvironmentVariables(Map<String, Object> execDataMap){
        return null;
    }
}
