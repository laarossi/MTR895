package com.projet.mtr895.app.entities.exec;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SeleniumExecConfig implements ExecConfig{

    private String webDriver;
    private Map<String, Object> options;
    private List<Map<String, Object>> checks;
    private List<SeleniumAction> seleniumAction;

    @Getter
    @Setter
    public static class SeleniumAction{
        private String event;
        private String element;
        private String selector;
        private float wait;
        private List<Map<String, Object>> afterChecks;
        private List<Map<String, Object>> beforeChecks;
    }

}
