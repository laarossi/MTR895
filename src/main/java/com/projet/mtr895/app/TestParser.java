package com.projet.mtr895.app;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.Mtr895Application;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.engine.executor.api.K6TestExecutor;
import com.projet.mtr895.app.engine.executor.ui.SeleniumTestExecutor;
import com.projet.mtr895.app.engine.parser.ConfigParser;
import com.projet.mtr895.app.engine.parser.api.LoadTestConfigParser;
import com.projet.mtr895.app.engine.parser.api.SmokeTestConfigParser;
import com.projet.mtr895.app.engine.parser.ui.SeleniumConfigParser;
import com.projet.mtr895.app.engine.reporter.api.K6Reporter;
import com.projet.mtr895.app.engine.reporter.Reporter;
import com.projet.mtr895.app.engine.reporter.ui.SeleniumReporter;
import com.projet.mtr895.app.entities.Request;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestParser {

    private static final Map<String, Class<? extends ConfigParser>> testConfigParsersMap = new HashMap<>();
    private static final Map<String, Class<? extends Executor>> testExecutorsMap = new HashMap<>();
    private static final Map<String, Class<? extends Reporter>> htmlReportGenerator = new HashMap<>();
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(Mtr895Application.class);

    static {
        // parser configuration map
        testConfigParsersMap.put("api.smoke", SmokeTestConfigParser.class);
        testConfigParsersMap.put("api.load", LoadTestConfigParser.class);
        testConfigParsersMap.put("ui.simple", SeleniumConfigParser.class);

        // executor configuration map
        testExecutorsMap.put("api.smoke", K6TestExecutor.class);
        testExecutorsMap.put("api.load", K6TestExecutor.class);
        testExecutorsMap.put("ui.simple", SeleniumTestExecutor.class);

        // report generator map
        htmlReportGenerator.put("ui.simple", SeleniumReporter.class);
        htmlReportGenerator.put("api.smoke", K6Reporter.class);
        htmlReportGenerator.put("api.load", K6Reporter.class);
    }

    public static Executor parseExecutor(TestCase testCase) throws Exception {
        Class<? extends Executor> executorClass = testExecutorsMap.get(testCase.getType());
        if (executorClass == null)
            throw new Exception("Executor for the corresponding test type : " + testCase.getType() + " has not been found");
        return executorClass.getDeclaredConstructor().newInstance();
    }

    public static Request parseRequest(Map<String, Object> requestJSONMap) throws Exception {
        if (requestJSONMap == null || requestJSONMap.isEmpty()) {
            throw new Exception("Missing request data, please provide a request configuration");
        }

        String host = (String) requestJSONMap.getOrDefault("host", null),
                method = (String) requestJSONMap.getOrDefault("method", null),
                payload = (String) requestJSONMap.getOrDefault("payload", null);

        if (host == null || host.isEmpty())
            throw new Exception("Request [host] parameter must be provided");

        if (method == null || method.isEmpty())
            throw new Exception("Request [method] parameter must be provided");

        Request request = new Request();
        request.setHost(host);
        request.setMethod(method);
        request.setPayload(payload);
        HashMap<String, Object> headers = (HashMap<String, Object>) requestJSONMap.getOrDefault("headers", new HashMap<String, Object>());
        HashMap<String, Map<String, Object>> cookies = (HashMap<String, Map<String, Object>>) requestJSONMap.getOrDefault("cookies", new HashMap<>());

        if (headers != null)
            for (Map.Entry<String, Object> entry : headers.entrySet())
                request.getHeaders().putIfAbsent(entry.getKey(), (String) entry.getValue());

        if(cookies != null)
            for (Map.Entry<String,  Map<String, Object>> entry : cookies.entrySet())
                request.getCookies().putIfAbsent(entry.getKey(), entry.getValue());

        return request;
    }

    public static ExecConfig parseExecConfig(TestCase testCase) throws Exception {
        if (testCase == null)
            throw new NullPointerException("TestCase object is null");

        if (!TestLoader.isTypeValid(testCase.getType()))
            throw new Exception("Type parameter required in the exec configuration");

        Class<?> execConfigurationParserClass = testConfigParsersMap.get(testCase.getType().toLowerCase());
        if (execConfigurationParserClass == null)
            throw new IOException("ExecConfigurationParser not found for type [" + testCase.getType() + "]");

        ConfigParser configParser = (ConfigParser) execConfigurationParserClass.getDeclaredConstructor().newInstance();
        return configParser.parse(testCase);
    }

    public static Reporter parseReporter(TestCase testCase) throws Exception {
        return htmlReportGenerator.get(testCase.getType()).getDeclaredConstructor().newInstance();
    }


}
