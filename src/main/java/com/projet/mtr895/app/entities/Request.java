package com.projet.mtr895.app.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Request {

    private String host;
    private String method;
    private HashMap<String, String> headers = new HashMap<>();
    private Map<String, Map<String, Object>> cookies = new HashMap<>();
    private String payload;

}
