package com.projet.mtr895.app.entities.exec;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class K6ExecConfig implements ExecConfig{

    private String k6JSONFilePath;
    private Map<String, String> k6Options = new HashMap<>();
    private Map<String, String> K6EnvironmentVariables = new HashMap<>();


}
