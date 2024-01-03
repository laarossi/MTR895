package com.projet.mtr895.engine;

import com.jayway.jsonpath.JsonPath;
import com.projet.mtr895.entities.TestCase;
import com.projet.mtr895.entities.TestSuite;
import lombok.Getter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Getter
public class TestLoader {

    private final HashSet<TestSuite> testSuites = new HashSet<>();

    public HashSet<TestSuite> loadTests(List<String> testingDirectories) throws IOException {
        List<File> loadedTestSuitesFiles = testingDirectories.stream().map(testingDirectory -> new File(testingDirectory).listFiles())
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .toList();

        for (File loadedTestSuiteFile : loadedTestSuitesFiles) {
            if (Files.isDirectory(loadedTestSuiteFile.toPath())) continue;
            System.out.println("TestSuite : " + loadedTestSuiteFile);
            TestSuite testSuite = new TestSuite(loadedTestSuiteFile.getParent(), loadedTestSuiteFile.getName(), loadTestCases(loadedTestSuiteFile));
            this.testSuites.add(testSuite);
        }

        return this.testSuites;
    }

    public TestSuite loadTest(String testSuiteJSONFile) throws IOException {
        File loadedTestSuiteFile = new File(testSuiteJSONFile);
        if(Files.isDirectory(loadedTestSuiteFile.toPath())){
          System.out.println("The File provided is a directory.");
          return null;
        }
        System.out.println("TestSuite : " + loadedTestSuiteFile);
        return new TestSuite(loadedTestSuiteFile.getParent(), loadedTestSuiteFile.getName(), loadTestCases(loadedTestSuiteFile));
    }

    private HashSet<TestCase> loadTestCases(File testSuiteJSONFile) throws IOException {
        HashSet<TestCase> testCases = new HashSet<>();
        List<Map<String, Object>> testCasesJSONObjects = JsonPath.read(Files.readString(testSuiteJSONFile.toPath()), "$");
        int i = 1;
        for (Map<String, Object> testCase : testCasesJSONObjects) {
            // Init the testcase object
            TestCase tc = new TestCase();
            tc.setId(i++);
            System.out.println("Loading TestCase#" + tc.getId());
            // Essential configurations attributes
            tc.setType(testCase.get("type").equals("api") ? TestCase.TestType.API : TestCase.TestType.WEB);
            tc.setHost((String) testCase.get("host"));
            tc.setAuthentication((String) testCase.get("authentication"));
            // Load the testcase body for the request
            Map<String, Object> payload = (Map<String, Object>) testCase.get("payload");
            if (payload != null) {
                tc.setBodyType((String) payload.get("type"));
                tc.setBodyContent((String) payload.get("data"));
                try {
                    tc.setBodyContent(Files.readString(Path.of((String) payload.get("data"))));
                } catch (Exception ignored) {
                }
            }
            // Load the other attributes as headers
            Map<String, String> headersMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : testCase.entrySet()) {
                String key = entry.getKey().toLowerCase();
                if (key.startsWith("header-")) {
                    key = key.split("header-")[1];
                    String[] parts = key.split("-");
                    StringBuilder camelCaseBuilder = new StringBuilder();
                    for (String part : parts) {
                        camelCaseBuilder.append(Character.toUpperCase(part.charAt(0)))
                                .append(part.substring(1));
                    }
                    key = camelCaseBuilder.toString();
                    headersMap.put(key, entry.getValue().toString());
                }
            }
            tc.setHeaders(headersMap);
            testCases.add(tc);
        }
        return testCases;
    }

}
