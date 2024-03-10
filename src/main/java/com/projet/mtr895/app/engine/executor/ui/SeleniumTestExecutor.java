package com.projet.mtr895.app.engine.executor.ui;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.TestLoader;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.SeleniumExecConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeleniumTestExecutor implements Executor {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    @Override
    public InputStream run(TestCase testCase) throws Exception {
        SeleniumExecConfig execConfig = (SeleniumExecConfig) testCase.getExecConfig();
        ChromeOptions chromeOptions = parseWebDriverOptions(execConfig);
        WebDriver webDriver = createWebDriver(execConfig, chromeOptions, testCase);
        LOG.info("Performing checks....");
        performChecks(webDriver, execConfig.getChecks());
        performEvents(webDriver, execConfig.getSeleniumAction());
        webDriver.quit();
        return null;
    }

    private WebDriver createWebDriver(SeleniumExecConfig execConfig, ChromeOptions chromeOptions, TestCase testCase) {
        WebDriver webDriver = switch (execConfig.getWebDriver()) {
            case "chrome" -> new ChromeDriver(chromeOptions);
            default -> throw new IllegalStateException("Unexpected value for driver: " + execConfig.getWebDriver());
        };

        int timeout = (int) execConfig.getOptions().getOrDefault("timeouts", 0);
        if (timeout > 0)
            webDriver.manage().timeouts().implicitlyWait(Duration.ofMillis(timeout));

        String url = testCase.getRequest().getHost();
        webDriver.get(url);
        return webDriver;
    }


    private List<Boolean> performChecks(WebDriver webDriver, List<Map<String, Object>> checks) {
        List<Boolean> checkList = new ArrayList<>();
        for (Map<String, Object> check : checks) {
            boolean checkResult = true;
            String element = (String) check.getOrDefault("element", null),
                    selector = (String) check.getOrDefault("selector", "cssSelector"),
                    value = (String) check.getOrDefault("value", null),
                    className = (String) check.getOrDefault("className", null),
                    id = (String) check.getOrDefault("id", null),
                    logMessage = "";
            switch (element.toLowerCase()) {
                case "title":
                    checkResult = webDriver.getTitle().equals(value);
                    logMessage = "title, current: " + webDriver.getTitle() + ", expected: " + value;
                    break;

                case "pagesource":
                    checkResult = webDriver.getPageSource().equals(value);
                    logMessage = "pageSource";
                    break;

                case "currenturl":
                    checkResult = webDriver.getCurrentUrl().equals(value);
                    logMessage = "current URL, current: " + webDriver.getCurrentUrl() + ", expected: " + value;
                    break;

                default:
                    WebElement webElement;
                    if (selector.equals("xPath"))
                        webElement = webDriver.findElement(By.xpath(element));
                    else
                        webElement = webDriver.findElement(By.cssSelector(element));

                    if (webElement == null) {
                        LOG.error("Element " + element + " not found, checking of the element aborted");
                        break;
                    }
                    logMessage = "element[" + element + "]";
                    if (value != null) {
                        checkResult = webElement.getText().equals(value);
                        logMessage += ", current value : " + webElement.getText().replace("\n", "\\n ") + ", expected value: " + value.replace("\n", "\\n ");
                    }
                    if (className != null) {
                        checkResult = webElement.getAttribute("class").equals(className) && checkResult;
                        logMessage += ", current className : " + webElement.getAttribute("class") + ", expected className : " + className;
                    }
                    if (id != null) {
                        checkResult = webElement.getAttribute("id").equals(id) && checkResult;
                        logMessage += ", current id : " + webElement.getAttribute("id") + ", expected id : " + id;
                    }
                    break;
            }

            checkList.add(checkResult);
            String logInfo = checkResult ? "Checked successfully for " : "Unexpected value for ";
            LOG.info(logInfo + logMessage);
        }

        return checkList;
    }

    private void performEvents(WebDriver webDriver, List<SeleniumExecConfig.SeleniumAction> actions) {
        for (SeleniumExecConfig.SeleniumAction action : actions) {
            List<Map<String, Object>> beforeChecks = action.getBeforeChecks();
            LOG.info("Performing checks before executing the event....");
            if (performChecks(webDriver, beforeChecks).contains(false)) {
                LOG.error("Before checks failed for action " + action.getEvent() + " on element : " + action.getElement());
                LOG.error("Aborting the execution of the event");
                return;
            }

            WebElement webElement;
            if (action.getSelector().equals("xPath"))
                webElement = webDriver.findElement(By.xpath(action.getElement()));
            else
                webElement = webDriver.findElement(By.cssSelector(action.getElement()));

            if (webElement == null) {
                LOG.error("Element " + action.getElement() + " not found, aborting the execution of the event " + action.getEvent());
                return;
            }
            LOG.info("Performing event " + action.getEvent() + " on element " + action.getElement() + "....");
            switch (action.getEvent()) {
                case "click":
                    JavascriptExecutor executor = (JavascriptExecutor) webDriver;
                    executor.executeScript("arguments[0].scrollIntoView(true);", webElement);
                    new Actions(webDriver)
                            .click(webElement).perform();
                    break;

                case "double-click":
                    new Actions(webDriver)
                            .moveToElement(webElement)
                            .doubleClick(webElement).perform();
                    break;

                case "click-and-hold":
                    new Actions(webDriver)
                            .moveToElement(webElement)
                            .clickAndHold(webElement).perform();
                    break;

                case "move-to-element":
                    new Actions(webDriver).moveToElement(webElement).perform();
                    break;

            }

            LOG.info("Performing after checks....");
            List<Map<String, Object>> afterChecks = action.getAfterChecks();
            if (performChecks(webDriver, afterChecks).contains(false)) {
                LOG.error("After checks failed for action " + action.getEvent() + " on element : " + action.getElement());
            }
        }
    }

    private ChromeOptions parseWebDriverOptions(SeleniumExecConfig execConfig) {
        ChromeOptions chromeOptions = new ChromeOptions();
        String strategy = (String) execConfig.getOptions().getOrDefault("pageLoadStrategy", "eager");
        PageLoadStrategy pageLoadStrategy = switch (strategy) {
            case "eager" -> PageLoadStrategy.EAGER;
            case "normal" -> PageLoadStrategy.NORMAL;
            case "none" -> PageLoadStrategy.NONE;
            default ->
                    throw new IllegalStateException("Unexpected value for page load strategy : " + execConfig.getWebDriver());
        };

        chromeOptions.setPageLoadStrategy(pageLoadStrategy);
        chromeOptions.setAcceptInsecureCerts((Boolean) execConfig.getOptions().getOrDefault("acceptInsecureCerts", false));
        Map<?, ?> proxy = (Map<String, String>) execConfig.getOptions().getOrDefault("proxy", new HashMap<>());
        if (!proxy.isEmpty()) {
            String http = (String) proxy.getOrDefault("host", null),
                    port = proxy.getOrDefault("port", null).toString();
            if (http != null && port != null) {
                Proxy p = new Proxy();
                p.setHttpProxy(http + ":" + port);
                chromeOptions.setProxy(p);
            }
        }

        chromeOptions.addArguments("--headless=new");
        chromeOptions.addArguments("--window-size=1600,1000");
        return chromeOptions;
    }
}
