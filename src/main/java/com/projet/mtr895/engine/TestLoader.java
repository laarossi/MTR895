package com.projet.mtr895.engine;

import ch.qos.logback.classic.Logger;
import com.jayway.jsonpath.JsonPath;
import com.projet.mtr895.engine.loader.k6.SmokeTestLoader;
import com.projet.mtr895.engine.parser.k6.K6Parser;
import com.projet.mtr895.engine.parser.k6.SmokeTestParser;
import com.projet.mtr895.entities.Request;
import com.projet.mtr895.entities.TestCase;
import com.projet.mtr895.entities.TestSuite;
import com.projet.mtr895.entities.api.APITestCase;
import com.projet.mtr895.entities.exec.K6ExecConfig;
import lombok.Getter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Getter
public class TestLoader {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);
    private final HashSet<TestSuite> testSuites = new HashSet<>();

    public HashSet<TestSuite> loadTests(String... testingDirectories) throws IOException {
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

    public TestSuite loadTest(String testSuiteJSONFile) throws IOException {
        File loadedTestSuiteFile = new File(testSuiteJSONFile);
        if(Files.isDirectory(loadedTestSuiteFile.toPath())){
          LOG.warn("The File provided is a directory.");
          return null;
        }
        LOG.info("TestSuite : " + loadedTestSuiteFile);
        return new TestSuite(loadedTestSuiteFile.getParent(), loadedTestSuiteFile.getName(), loadTestCases(loadedTestSuiteFile));
    }

    private HashSet<TestCase> loadTestCases(File testSuiteJSONFile) throws IOException {
        HashSet<TestCase> testCases = new HashSet<>();
        List<Map<String, Object>> testCasesJSONObjects = JsonPath.read(Files.readString(testSuiteJSONFile.toPath()), "$");
        int i = 1;
        for (Map<String, Object> testCase : testCasesJSONObjects) {
            TestCase tc = testCase.get("type").equals("api") ? apiTestCaseParse(i++, testCase) : new TestCase();
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
        K6ExecConfig k6ExecConfig = parseK6ExecConfig(execDataMap, SmokeTestParser.class);
        tc.setK6ExecConfig(k6ExecConfig);
        return tc;
    }

    private K6ExecConfig parseK6ExecConfig(Map<String, Object> execDataMap, Class<?> loaderClass){
        K6Parser parser = null;
        if (loaderClass == SmokeTestParser.class) parser = new SmokeTestParser();
        return parser == null ? null : parser.parse(execDataMap);
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
