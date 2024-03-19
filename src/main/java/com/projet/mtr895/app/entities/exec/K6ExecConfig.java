package com.projet.mtr895.app.entities.exec;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class K6ExecConfig implements ExecConfig{
    private boolean summary;
    private String outputFormat;
    private String outputFile;
    private String k6JSONFilePath;
    private Map<String, Object> k6Options = new HashMap<>();
    private Map<String, String> K6EnvironmentVariables = new HashMap<>();
    private HTTPResponse expectedHTTPResponse;
    public static class HTTPResponse{
        public Map<String, String> headers = new HashMap<>();
        public String payload;
        public int status;
    }
}
