package com.projet.mtr895.app;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.Mtr895Application;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.engine.executor.api.K6TestExecutor;
import com.projet.mtr895.app.engine.executor.ui.SeleniumTestExecutor;
import com.projet.mtr895.app.engine.parser.ConfigParser;
import com.projet.mtr895.app.engine.parser.api.SmokeTestConfigParser;
import com.projet.mtr895.app.engine.parser.ui.SeleniumConfigParser;
import com.projet.mtr895.app.engine.reporter.K6HTMLReporter;
import com.projet.mtr895.app.engine.reporter.Reporter;
import com.projet.mtr895.app.entities.Request;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestParser {

    private static final Map<String, Class<? extends ConfigParser>> testConfigParsersMap = new HashMap<>();
    private static final Map<String, Class<? extends Executor>> testExecutorsMap = new HashMap<>();
    private static final Map<String, Class<? extends Reporter>> htmlReportGenerator = new HashMap<>();
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(Mtr895Application.class);

    static {
        testConfigParsersMap.put("api.smoke", SmokeTestConfigParser.class);
        testExecutorsMap.put("api.smoke", K6TestExecutor.class);
        htmlReportGenerator.put("api.smoke", K6HTMLReporter.class);
        testConfigParsersMap.put("ui.simple", SeleniumConfigParser.class);
        testExecutorsMap.put("ui.simple", SeleniumTestExecutor.class);
//        htmlReportGenerator.put("ui.simple", K6HTMLReporter.class);

    }

    public static Executor parseExecutor(TestCase testCase) throws Exception {
        Class<? extends Executor> executorClass = testExecutorsMap.get(testCase.getType());
        if(executorClass == null)
            throw new Exception("Executor for the corresponding test type : " + testCase.getType() + " has not been found");
        return executorClass.getDeclaredConstructor().newInstance();
    }

    public static Request parseRequest(Map<String, Object> requestJSONMap) throws Exception {
        if(requestJSONMap == null || requestJSONMap.isEmpty()){
            throw new Exception("Missing request data, please provide a request configuration");
        }

        String host = (String) requestJSONMap.getOrDefault("host", null),
                path = (String) requestJSONMap.getOrDefault("path", null),
                method = (String) requestJSONMap.getOrDefault("method", null);

        if (host == null || host.isEmpty())
            throw new Exception("Request [host] parameter must be provided");

        if (path == null || path.isEmpty())
            throw new Exception("Request [path] parameter must be provided");

        if (method == null || method.isEmpty())
            throw new Exception("Request [method] parameter must be provided");

        Request request = new Request();
        request.setHost(host);
        request.setMethod(method);
        request.setPath(path.equals("/") ? "" : path);
        HashMap<String, Object> headers;
        try{
            headers = (HashMap<String, Object>) requestJSONMap.get("headers");
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new Exception("An unexpected error while parsing request headers");
        }
        if(headers != null)
            for(Map.Entry<String, Object> entry : headers.entrySet())
                request.getHeaders().putIfAbsent(entry.getKey(), (String) entry.getValue());
        return request;
    }

    public static ExecConfig parseExecConfig(TestCase testCase) throws Exception {
        if(testCase == null)
            throw new NullPointerException("TestCase object is null");

        if(!TestLoader.isTypeValid(testCase.getType()))
            throw new Exception("Type parameter required in the exec configuration");

        Class<?> execConfigurationParserClass = testConfigParsersMap.get(testCase.getType().toLowerCase());
        if(execConfigurationParserClass == null)
            throw new IOException("ExecConfigurationParser not found for type [" + testCase.getType() + "]");

        ConfigParser configParser = (ConfigParser) execConfigurationParserClass.getDeclaredConstructor().newInstance();
        return configParser.parse(testCase);
    }

    public static Reporter parseReporter(TestCase testCase) throws Exception {
        return htmlReportGenerator.get(testCase.getType()).getDeclaredConstructor().newInstance();
    }


}
