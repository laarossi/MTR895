package com.projet.mtr895.app.engine.executor.ui;

import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.SeleniumExecConfig;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SeleniumTestExecutor implements Executor {

    @Override
    public InputStream run(TestCase testCase) throws Exception {
        SeleniumExecConfig execConfig = (SeleniumExecConfig) testCase.getExecConfig();
        WebDriver webDriver = switch (execConfig.getWebDriver()) {
            case "chrome" -> new ChromeDriver();
            case "edge" -> new EdgeDriver();
            case "firefox" -> new FirefoxDriver();
            default -> throw new IllegalStateException("Unexpected value for driver: " + execConfig.getWebDriver());
        };

        String url = testCase.getRequest().getHost() + '/' + testCase.getRequest().getPath();
        webDriver.get(url);
        int timeout = (int) execConfig.getOptions().getOrDefault("timeouts", 0);
        if (timeout > 0)
            webDriver.manage().timeouts().implicitlyWait(Duration.ofMillis(timeout));

        ChromeOptions chromeOptions = new ChromeOptions();
        String strategy = (String) execConfig.getOptions().getOrDefault("pageLoadStrategy","eager");
        PageLoadStrategy pageLoadStrategy = switch (strategy) {
            case "eager" -> PageLoadStrategy.EAGER;
            case "normal" -> PageLoadStrategy.NORMAL;
            case "none" -> PageLoadStrategy.NONE;
            default -> throw new IllegalStateException("Unexpected value for page load strategy : " + execConfig.getWebDriver());
        };

        chromeOptions.setPageLoadStrategy(pageLoadStrategy);
        chromeOptions.setAcceptInsecureCerts((Boolean) execConfig.getOptions().getOrDefault("acceptInsecureCerts", false));
        Map<?,?> proxy = (Map<String, String>) execConfig.getOptions().getOrDefault("proxy", new HashMap<>());
        if (!proxy.isEmpty()){
            String http = (String) proxy.getOrDefault("host", null),
                    port = (String) proxy.getOrDefault("port", null);
            if (http != null && port != null){
                Proxy p = new Proxy();
                p.setHttpProxy(http + ":" + port);
                chromeOptions.setProxy(p);
            }
        }
        webDriver.get(url);
        return Executor.super.run(testCase);
    }
}
