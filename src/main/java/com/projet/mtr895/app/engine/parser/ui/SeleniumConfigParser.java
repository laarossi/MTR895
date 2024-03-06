package com.projet.mtr895.app.engine.parser.ui;

import com.projet.mtr895.app.engine.parser.ConfigParser;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;
import com.projet.mtr895.app.entities.exec.SeleniumExecConfig;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SeleniumConfigParser implements ConfigParser {
    @Override
    public void setDefaults(ExecConfig execConfig) {
        SeleniumExecConfig seleniumExecConfig = (SeleniumExecConfig) execConfig;
        if(seleniumExecConfig.getWebDriver() == null || seleniumExecConfig.getWebDriver().isEmpty())
            seleniumExecConfig.setWebDriver("chrome");
    }

    @Override
    public ExecConfig setConfig(TestCase testCase) throws Exception {
        SeleniumExecConfig execConfig = new SeleniumExecConfig();
        Map<String, Object> execDataMap = testCase.getExecConfigJSONMap();
        if(execDataMap == null || execDataMap.isEmpty()) {
            throw new Exception("Exec object cannot be empty");
        }

        if(testCase.getRequest() == null) {
            throw new Exception("Request cannot be empty");
        }

        if(execDataMap.containsKey("webDriver")){
            execConfig.setWebDriver((String) execDataMap.get("webDriver"));
        }

        Map<String, Object> options = (Map<String, Object>) execDataMap.getOrDefault("options", new HashMap<String, Object>());
        execConfig.setOptions(options);
        List<Map<String, Object>> events = (List<Map<String, Object>>) execDataMap.getOrDefault("events", new ArrayList<Map<String,Object>>());
        List<SeleniumExecConfig.SeleniumAction> seleniumActions = new ArrayList<>();
        for (Map<String, Object> action : events){
            SeleniumExecConfig.SeleniumAction seleniumAction = new SeleniumExecConfig.SeleniumAction();
            String elementSelector = (String) action.getOrDefault("element", null),
                    event = (String) action.getOrDefault("event", null);
            if (elementSelector == null){
                throw new Exception("Element selector is null");
            }
            seleniumAction.setElement(elementSelector);
            seleniumAction.setEvent(event);
            seleniumAction.setWait((Integer) action.getOrDefault("wait", 0));
            List<Map<String, String>> checkList = (List<Map<String, String>>) action.getOrDefault("then", new ArrayList<>());
            Map<String, String> checks = new HashMap<>();
            checkList.forEach(checks::putAll);
            seleniumAction.setExpectedElements(checks);
            seleniumActions.add(seleniumAction);
        }
        execConfig.setSeleniumAction(seleniumActions);
        execConfig.setChecks((List<Map<String, Object>>) execDataMap.getOrDefault("check", new HashMap<>()));
        return execConfig;
    }

}
