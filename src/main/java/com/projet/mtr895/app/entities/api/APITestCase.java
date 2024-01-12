package com.projet.mtr895.app.entities.api;

import com.projet.mtr895.app.entities.TestCase;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class APITestCase extends TestCase {
    private String apiTestType;
    private Map<String, Object> k6ExecutorConfigMap;
}
