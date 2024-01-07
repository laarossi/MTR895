package com.projet.mtr895.engine;

import ch.qos.logback.classic.Logger;
import com.jayway.jsonpath.JsonPath;
import com.projet.mtr895.engine.parser.k6.K6ExecConfigurationParser;
import com.projet.mtr895.engine.parser.k6.SmokeTestConfigParser;
import com.projet.mtr895.entities.Request;
import com.projet.mtr895.entities.TestCase;
import com.projet.mtr895.entities.TestSuite;
import com.projet.mtr895.entities.api.APITestCase;
import com.projet.mtr895.entities.exec.ExecConfig;
import com.projet.mtr895.entities.exec.K6ExecConfig;
import lombok.Getter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class TestLoader {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);
    private final HashSet<TestSuite> testSuites = new HashSet<>();

    public HashSet<TestSuite> loadTests(String... testingDirectories) throws Exception {
        List<File> loadedTestSuitesFiles = Arrays.stream(testingDirectories).map(testingDirectory -> new File(testingDirectory).listFiles())
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .toList();
        for (File loadedTestSuiteFile : loadedTestSuitesFiles) {
            if (Files.isDirectory(loadedTestSuiteFile.toPath())) continue;
            LOG.info("TestSuite : " + loadedTestSuiteFile);
            TestSuite testSuite = new TestSuite(loadedTestSuiteFile.getParent(), loadedTestSuiteFile.getName(), loadTestCases(loadedTestSuiteFile));
            this.testSuites.add(testSuite);
        }
        return this.testSuites;
    }

    public TestSuite loadTest(String testSuiteJSONFile) throws Exception {
        File loadedTestSuiteFile = new File(testSuiteJSONFile);
        if(Files.isDirectory(loadedTestSuiteFile.toPath())){
          LOG.warn("The File provided is a directory.");
          return null;
        }
        LOG.info("TestSuite : " + loadedTestSuiteFile);
        return new TestSuite(loadedTestSuiteFile.getParent(), loadedTestSuiteFile.getName(), loadTestCases(loadedTestSuiteFile));
    }

    private HashSet<TestCase> loadTestCases(File testSuiteJSONFile) throws Exception {
        HashSet<TestCase> testCases = new HashSet<>();
        List<Map<String, Object>> testCasesJSONObjects = JsonPath.read(Files.readString(testSuiteJSONFile.toPath()), "$");
        int i = 1;
        for (Map<String, Object> testCase : testCasesJSONObjects) {
            ExecConfig execConfig = parseExecConfig((Map<String, Object>) testCase.get("exec"));
            TestCase tc = execConfig instanceof K6ExecConfig ? apiTestCaseParse(i++, testCase) : new TestCase();
            tc.setId(i++);
            tc.setExecConfig(execConfig);
            testCases.add(tc);
        }
        return testCases;
    }

    private APITestCase apiTestCaseParse(int id, Map<String, Object> testCase){
        APITestCase tc = new APITestCase();
        tc.setId(id);
        LOG.info("Loading TestCase#" + tc.getId());
        tc.setRequest(parseRequest((Map<String, Object>) testCase.get("request")));
        Map<String, Object> execDataMap = (Map<String, Object>) testCase.get("exec");
        tc.setApiTestType(execDataMap.get("type").toString().toLowerCase());

        return tc;
    }

    private ExecConfig parseExecConfig(Map<String, Object> execDataMap) throws Exception {
        String execType = (String) execDataMap.get("type");
        if (execType == null) throw new Exception("Exec configuration : must contain a type parameter");
        Pattern pattern = Pattern.compile("(?<type>a-z)+.(?<test>a-z)*");
        Matcher matcher = pattern.matcher(execType.toLowerCase());
        if(!matcher.matches()) throw new Exception("Exec configuration : type parameter is not correct");
        String execTest = matcher.group("type").toLowerCase(), execTestType = matcher.group("test").toLowerCase();
        if (execTest.equals("api")){
            if(execTestType.equals("smoke")) return new SmokeTestConfigParser().parse(execDataMap);
            else return null;
        }return null;
    }


    private Request parseRequest(Map<String, Object> requestJSONMap){
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

}
