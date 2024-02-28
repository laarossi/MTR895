package com.projet.mtr895.app;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.TestSuite;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class TestExecutor {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    public static HashSet<TestSuite> loadTests(String... testingDirectories) throws Exception {
        List<File> loadedTestSuitesFiles = Arrays.stream(testingDirectories).map(testingDirectory -> new File(testingDirectory).listFiles())
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .toList();
        HashSet<TestSuite> testSuites = new HashSet<>();
        for (File loadedTestSuiteFile : loadedTestSuitesFiles) {
            if (Files.isDirectory(loadedTestSuiteFile.toPath())) continue;
            LOG.info("TestSuite : " + loadedTestSuiteFile);
            TestSuite testSuite = TestLoader.loadTestSuite(loadedTestSuiteFile.getAbsolutePath());
            if(testSuite == null || testSuite.getTestCases() == null || testSuite.getTestCases().isEmpty()) continue;
            testSuite.getTestCases().
                    forEach(testCase -> {
                        try {
                            testCase.setExecConfig(TestParser.parseExecConfig(testCase));
                        } catch (Exception e) {
                            testCase.setExecConfig(null);
                            LOG.warn(e.getMessage());
                        }
                    });
            testSuites.add(testSuite);
        }
        return testSuites;
    }

    public static void runTests(String... testingDirectories) throws Exception {
        HashSet<TestSuite> testSuites = loadTests(testingDirectories);
        for (TestSuite testSuite : testSuites){
            LOG.info("Loading TestSuite[" + testSuite.getTestSuiteName() + "] .......");
            if(testSuite.getTestCases() == null || testSuite.getTestCases().isEmpty()){
                LOG.warn("TestSuite [" + testSuite.getTestSuiteName() + "] have 0 test cases");
                continue;
            }

            LOG.info("Executing TestSuite[" + testSuite.getTestSuiteName() + "] .......");
            for (TestCase testCase : testSuite.getTestCases()){
                LOG.info("Creating Executor for TestCase#" + testCase.getId());
                try {
                    Executor testCaseExecutor = TestParser.parseExecutor(testCase);
                    InputStream inputStream = testCaseExecutor.run(testCase);
                }catch (Exception e){
                    e.printStackTrace();
                    LOG.error("Skipping the execution of the TestCase#" + testCase.getId());
                }
            }
        }
    }

}
