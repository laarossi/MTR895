package com.projet.mtr895.app.engine.executor;

import com.projet.mtr895.app.entities.TestCase;

public interface Executor {

    default boolean run(TestCase testCase, String outputDir) throws Exception {
        boolean statusTestCase = executeTestCase(testCase, outputDir);
        generateReport(testCase);
        return statusTestCase;
    }

    default boolean run(TestCase testCase) throws Exception {
        boolean statusTestCase = executeTestCase(testCase);
        generateReport(testCase);
        return statusTestCase;
    }

    boolean executeTestCase(TestCase testCase, String outputDir) throws Exception;

    boolean executeTestCase(TestCase testCase) throws Exception;

    void generateReport(TestCase testCase) throws Exception;

}
