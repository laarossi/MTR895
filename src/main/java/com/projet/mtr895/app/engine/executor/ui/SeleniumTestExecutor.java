package com.projet.mtr895.app.engine.executor.ui;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projet.mtr895.app.TestLoader;
import com.projet.mtr895.app.TestParser;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.engine.reporter.Reporter;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.SeleniumExecConfig;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class SeleniumTestExecutor implements Executor {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    private boolean testCaseResult = true;

    private final JSONObject jsonResultObject = new JSONObject();
    @Override
    public boolean executeTestCase(TestCase testCase, String outputDirectory) throws IOException {
        SeleniumExecConfig execConfig = (SeleniumExecConfig) testCase.getExecConfig();
        jsonResultObject.put("name", testCase.getName());
        ChromeOptions chromeOptions = parseWebDriverOptions(execConfig);
        JSONArray tempArrayList = new JSONArray();
        if (performChecks(testCase, chromeOptions, execConfig, tempArrayList).contains(false)) testCaseResult = false;
        jsonResultObject.put("checks", tempArrayList);
        tempArrayList = new JSONArray();
        performEvents(testCase, chromeOptions, execConfig, tempArrayList);
        jsonResultObject.put("events", tempArrayList);
        String testCaseDir = testCase.getName()
                .toLowerCase()
                .replaceAll("\\s+", "_") + "_" + String.format("%04d", new Random().nextInt(10000));;
        initDirectories(testCaseDir, outputDirectory);
        testCase.setOutputDir(Path.of(outputDirectory, testCaseDir).toAbsolutePath().toString());
        File file = new File(Files.createFile(Path.of(testCase.getOutputDir(), "summary.json")).toUri());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(jsonResultObject));
        testCase.setJsonExecutionResultsMap(jsonResultObject);
        fileOutputStream.close();
        return testCaseResult;
    }

    @Override
    public boolean executeTestCase(TestCase testCase) {
        SeleniumExecConfig execConfig = (SeleniumExecConfig) testCase.getExecConfig();
        jsonResultObject.put("name", testCase.getName());
        ChromeOptions chromeOptions = parseWebDriverOptions(execConfig);
        JSONArray tempArrayList = new JSONArray();
        if (performChecks(testCase, chromeOptions, execConfig, tempArrayList).contains(false)) testCaseResult = false;
        jsonResultObject.put("checks", tempArrayList);
        tempArrayList = new JSONArray();
        performEvents(testCase, chromeOptions, execConfig, tempArrayList);
        jsonResultObject.put("events", tempArrayList);
        String testCaseDir = testCase.getName()
                .toLowerCase()
                .replaceAll("\\s+", "_") + "_" + String.format("%04d", new Random().nextInt(10000));
        testCase.setOutputDir(String.valueOf(Path.of(testCaseDir).toAbsolutePath()));
        File file = null;
        FileOutputStream fileOutputStream = null;
        try {
            initDirectories(testCaseDir, testCase.getOutputDir());
            file = new File(Files.createFile(Path.of(testCase.getOutputDir(), "summary.json")).toUri());
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(jsonResultObject));
        } catch (IOException e) {
            throw new RuntimeException("Unexpected Error, failed loading the test case");
        }
        testCase.setJsonExecutionResultsMap(jsonResultObject);
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected Error, failed loading the test case");
        }
        return testCaseResult;
    }

    private void initDirectories(String testCaseName, String outputDirectory) throws IOException {
        Path dir = Paths.get(outputDirectory, testCaseName);
        if (Files.exists(dir)) {
            try {
                Files.walk(dir)
                        .sorted((p1, p2) -> -p1.compareTo(p2))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Files.createDirectories(dir);
    }

    public void generateReport(TestCase testCase) throws Exception {
        if(testCase.getOutputDir() == null || testCase.getOutputDir().isEmpty()) return;
        Reporter reporter = TestParser.parseReporter(testCase);
        reporter.report(testCase);
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

    private List<Boolean> performChecks(TestCase testCase, ChromeOptions options, SeleniumExecConfig seleniumExecConfig, JSONArray jsonObject) {
        WebDriver webDriver = createWebDriver(seleniumExecConfig, options, testCase);
        List<Boolean> results = performChecks(webDriver, seleniumExecConfig.getChecks(), jsonObject);
        webDriver.quit();
        return results;
    }

    private List<Boolean> performChecks(WebDriver webDriver, List<Map<String, Object>> checks, JSONArray jsonObject) {
        List<Boolean> checkList = new ArrayList<>();
        for (Map<String, Object> check : checks) {
            JSONObject checkResult = new JSONObject();
            boolean isCheckedSuccessfully = true;
            String element = (String) check.getOrDefault("element", null),
                    selector = (String) check.getOrDefault("selector", "cssSelector"),
                    value = (String) check.getOrDefault("value", null),
                    className = (String) check.getOrDefault("className", null),
                    id = (String) check.getOrDefault("id", null),
                    logMessage = "";

            checkResult.put("element", element);
            checkResult.put("value", value);
            checkResult.put("selector", selector);
            checkResult.put("id", id);
            checkResult.put("className", className);
            switch (element.toLowerCase()) {
                case "title":
                    isCheckedSuccessfully = webDriver.getTitle().equals(value);
                    checkResult.put("currentValue", webDriver.getTitle());
                    logMessage = "title, current: " + webDriver.getTitle() + ", expected: " + value;
                    break;

                case "pagesource":
                    isCheckedSuccessfully = webDriver.getPageSource().equals(value);
                    logMessage = "pageSource";
                    break;

                case "currenturl":
                    isCheckedSuccessfully = webDriver.getCurrentUrl().equals(value);
                    checkResult.put("currentValue", webDriver.getCurrentUrl());
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
                        this.testCaseResult = false;
                        break;
                    }
                    logMessage = "element[" + element + "]";
                    if (value != null) {
                        isCheckedSuccessfully = webElement.getText().equals(value);
                        checkResult.put("currentValue", webElement.getText());
                        logMessage += ", current value : " + webElement.getText()
                                .replace("\n", "\\n ") + ", expected value: " + value.replace("\n", "\\n ");
                    }
                    if (className != null) {
                        isCheckedSuccessfully = webElement.getAttribute("class").equals(className) && isCheckedSuccessfully;
                        checkResult.put("currentValue", webElement.getAttribute("class"));
                        logMessage += ", current className : " + webElement.getAttribute("class") + ", expected className : " + className;
                    }
                    if (id != null) {
                        isCheckedSuccessfully = webElement.getAttribute("id").equals(id) && isCheckedSuccessfully;
                        checkResult.put("currentValue", webElement.getAttribute("id"));
                        logMessage += ", current id : " + webElement.getAttribute("id") + ", expected id : " + id;
                    }
                    break;
            }

            checkList.add(isCheckedSuccessfully);
            checkResult.put("status", isCheckedSuccessfully);
            jsonObject.add(checkResult);
            String logInfo = isCheckedSuccessfully ? "Checked successfully for " : "Unexpected value for ";
            LOG.info(logInfo + logMessage);
        }

        return checkList;
    }

    private void performEvents(TestCase testCase, ChromeOptions chromeOptions, SeleniumExecConfig execConfig, JSONArray jsonArray) {
        for (SeleniumExecConfig.SeleniumAction action : execConfig.getSeleniumAction()) {
            JSONObject jsonObject = new JSONObject();
            JSONArray tempArray = new JSONArray();
            WebDriver webDriver = createWebDriver(execConfig, chromeOptions, testCase);
            List<Map<String, Object>> beforeChecks = action.getBeforeChecks();
            LOG.info("Performing checks before executing the event....");
            if (performChecks(webDriver, beforeChecks, tempArray).contains(false)) {
                LOG.error("Before checks failed for action " + action.getEvent() + " on element : " + action.getElement());
                LOG.error("Aborting the execution of the event");
                webDriver.quit();
                jsonObject.put("before-checks", tempArray);
                jsonObject.put("event-execution-status", false);
                this.testCaseResult = false;
                break;
            }

            jsonObject.put("before-checks", tempArray);
            jsonObject.put("event", action.getEvent());
            jsonObject.put("element", action.getElement());
            jsonObject.put("selector", action.getSelector());
            WebElement webElement;
            if (action.getSelector().equals("xPath"))
                webElement = webDriver.findElement(By.xpath(action.getElement()));
            else
                webElement = webDriver.findElement(By.cssSelector(action.getElement()));

            if (webElement == null) {
                LOG.error("Element " + action.getElement() + " not found, aborting the execution of the event " + action.getEvent());
                jsonObject.put("event-execution-status", false);
                webDriver.quit();
                testCaseResult = false;
                break;
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

            jsonObject.put("event-execution-status", true);
            tempArray = new JSONArray();
            LOG.info("Performing after checks....");
            List<Map<String, Object>> afterChecks = action.getAfterChecks();
            if (performChecks(webDriver, afterChecks, tempArray).contains(false)) {
                LOG.error("After checks failed for action " + action.getEvent() + " on element : " + action.getElement());
                testCaseResult = false;
            }
            jsonObject.put("after-checks", tempArray);
            jsonArray.add(jsonObject);
            webDriver.quit();
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
