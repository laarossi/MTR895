package com.projet.mtr895.app.engine;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.Mtr895Application;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.engine.executor.k6.K6Executor;
import com.projet.mtr895.app.engine.parser.ExecConfigurationParser;
import com.projet.mtr895.app.engine.parser.k6.SmokeTestConfigParser;
import com.projet.mtr895.app.entities.Request;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.api.APITestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.projet.mtr895.app.engine.TestLoader.getTest;

public class TestParser {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(Mtr895Application.class);

    public static Executor parseExecutor(TestCase testCase){
        if (testCase instanceof APITestCase) {
            return new K6Executor().parse(testCase);
        }
        return null;
    }

    public static Request parseRequest(Map<String, Object> requestJSONMap){
        if(requestJSONMap == null || requestJSONMap.isEmpty()) return null;
        Request request = new Request();
        request.setHost(requestJSONMap.get("host").toString());
        request.setMethod(requestJSONMap.get("method").toString());
        request.setAuthorization(requestJSONMap.get("authorization").toString());
        request.setContentType(requestJSONMap.get("contentType").toString());
        request.setContentLength((Integer) requestJSONMap.get("contentLength"));
        request.setCacheControl(requestJSONMap.get("cacheControl").toString());
        request.setUserAgent(requestJSONMap.get("userAgent").toString());
        request.setPayload(requestJSONMap.get("payload").toString());
        return request;
    }

    public static ExecConfig parseExecConfig(Map<String, Object> execDataMap) throws IOException {
        String[] type;
        try {
            type = getTest(execDataMap);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        if(type[0] == null || type[0].isEmpty() || type[1] == null || type[1].isEmpty()){
            System.out.println("Wrong data format");
            return null;
        }

        ExecConfigurationParser execConfigurationParser = getParserClass(type[0], type[1]);
        if(execConfigurationParser == null) throw new IOException("An error occurred with parsing ExecConfigurationMaster");
        return execConfigurationParser.parse(execDataMap);
    }

    private static ExecConfigurationParser getParserClass(String test, String testType){
        testType = testType.toLowerCase().trim();
        switch(testType) {
            case "smoke":
                return new SmokeTestConfigParser();
            case "load":
                break;
            default:
                break;
        }
        return null;
    }

}
