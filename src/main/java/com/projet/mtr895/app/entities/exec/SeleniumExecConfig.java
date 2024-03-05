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
    private Map<String, Object> actions;
    private List<SeleniumAction> seleniumAction;

    @Getter
    @Setter
    public static class SeleniumAction{
        private String event;
        private String element;
        private float wait;
        private Map<String, String> expectedElements;
    }

}
