package com.projet.mtr895.entities;

import lombok.Getter;
import java.util.HashSet;

@Getter
public class TestSuite {

    private final String dirName;
    private final String testSuiteName;
    private final HashSet<TestCase> testCases;

    public TestSuite(String dirName, String testSuiteName, HashSet<TestCase> testCases){
        this.dirName = dirName;
        this.testSuiteName = testSuiteName;
        this.testCases = testCases;
    }

}
