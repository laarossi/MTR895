package com.projet.mtr895.app;

import ch.qos.logback.classic.Logger;
import com.jayway.jsonpath.JsonPath;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.TestSuite;
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
            Map<String, Object> execConfigDataMap = (Map<String, Object>) testCase.get("exec");
            LOG.info("Loading TestCase#" + i);
            TestCase tc =  new TestCase();
            tc.setId(i++);
            tc.setName((String) testCase.getOrDefault("name", "TestCase#" + tc.getId()));
            tc.setTestSuiteFile(testSuiteJSONFile.getAbsolutePath());
            try {
                tc.setRequest(TestParser.parseRequest((Map<String, Object>) testCase.get("request")));
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Skipping TestCase#" + tc.getId());
                continue;
            }
            tc.setExecConfigJSONMap(execConfigDataMap);
            try {
                if(!isTypeValid((String) execConfigDataMap.get("type")))
                    throw new Exception("Type parameter required in the exec configuration");
                tc.setType((String) execConfigDataMap.get("type"));
                testCases.add(tc);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Skipping TestCase#" + tc.getId());
            }
        }
        return testCases;
    }

    static boolean isTypeValid(String execType) throws Exception {
        if (execType == null) throw new Exception("Exec configuration : must contain a type parameter");
        Pattern pattern = Pattern.compile("(?<type>[a-z]+).(?<test>[a-z]+)");
        Matcher matcher = pattern.matcher(execType.toLowerCase());
        return matcher.matches();
    }




}
