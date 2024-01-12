package com.projet.mtr895.app.engine;

import ch.qos.logback.classic.Logger;
import com.jayway.jsonpath.JsonPath;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.TestSuite;
import com.projet.mtr895.app.entities.api.APITestCase;
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

    public static TestSuite loadTestSuite(String testSuiteJSONFile) throws Exception {
        File loadedTestSuiteFile = new File(testSuiteJSONFile);
        if(Files.isDirectory(loadedTestSuiteFile.toPath())){
          LOG.warn("The File provided is a directory.");
          return null;
        }
        LOG.info("TestSuite : " + loadedTestSuiteFile);
        return new TestSuite(loadedTestSuiteFile.getParent(), loadedTestSuiteFile.getName(), loadTestCases(loadedTestSuiteFile));
    }

    private static HashSet<TestCase> loadTestCases(File testSuiteJSONFile) {
        HashSet<TestCase> testCases = new HashSet<>();
        List<Map<String, Object>> testCasesJSONObjects;
        try {
            testCasesJSONObjects = JsonPath.read(Files.readString(testSuiteJSONFile.toPath()), "$");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int i = 1;
        for (Map<String, Object> testCase : testCasesJSONObjects) {
            String[] type;
            Map<String, Object> execConfigDataMap = (Map<String, Object>) testCase.get("exec");
            try {
                type = getTest(execConfigDataMap);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
            TestCase tc =  type[0].equals("api") ? apiTestCaseParse(testCase) : new TestCase();
            tc.setId(i++);
            tc.setExecConfigDataMap(execConfigDataMap);
            testCases.add(tc);
        }
        return testCases;
    }

    private static APITestCase apiTestCaseParse(Map<String, Object> testCase){
        APITestCase tc = new APITestCase();
        LOG.info("Loading TestCase#" + tc.getId());
        tc.setRequest(TestParser.parseRequest((Map<String, Object>) testCase.get("request")));
        Map<String, Object> execDataMap = (Map<String, Object>) testCase.get("exec");
        tc.setApiTestType(execDataMap.get("type").toString().toLowerCase());
        return tc;
    }

    static String[] getTest(Map<String, Object> execDataMap) throws Exception {
        String execType = (String) execDataMap.get("type");
        if (execType == null) throw new Exception("Exec configuration : must contain a type parameter");
        Pattern pattern = Pattern.compile("(?<type>[a-z]+).(?<test>[a-z]+)");
        Matcher matcher = pattern.matcher(execType.toLowerCase());
        if(!matcher.matches()) throw new Exception("Exec configuration : type parameter is not correct");
        return new String[]{matcher.group("type"), matcher.group("test")};
    }




}
