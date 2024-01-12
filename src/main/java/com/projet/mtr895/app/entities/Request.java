package com.projet.mtr895.app.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class Request {

    private String url;
    private String host;
    private String method;
    private String contentType;
    private long contentLength;
    private String cacheControl;
    private String payload;
    private String userAgent;
    private String authorization;
    private HashMap<String, String> headers = new HashMap<>();

}
