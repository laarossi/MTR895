package com.projet.mtr895.app.entities;

import com.projet.mtr895.app.entities.exec.ExecConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TestCase {
    private int id;
    private String name;
    private String testSuiteFile;
    private String type;
    private Request request;
    private ExecConfig execConfig;
    private Map<String, Object> execConfigJSONMap;
}
