package com.projet.mtr895.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class TestCase {

    public enum TestType{
        WEB,
        API
    }
    private int id;
    private TestType type;
    private String host;
    private String authentication;
    private Map<String, String> headers = new HashMap<>();
    private String bodyType;
    private String bodyContent;

}
