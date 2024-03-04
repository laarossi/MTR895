package com.projet.mtr895.app.engine.parser.ui;

import com.projet.mtr895.app.engine.parser.ConfigParser;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;
import com.projet.mtr895.app.entities.exec.SeleniumExecConfig;

public class SeleniumConfigParser implements ConfigParser {
    @Override
    public void setDefaults(ExecConfig execConfig) {
        SeleniumExecConfig seleniumExecConfig = (SeleniumExecConfig) execConfig;
        if(seleniumExecConfig.getWebDriver() == null || seleniumExecConfig.getWebDriver().isEmpty()) seleniumExecConfig.setWebDriver("chrome");
    }

    @Override
    public ExecConfig setConfig(TestCase testCase) throws Exception {
        return null;
    }

}
