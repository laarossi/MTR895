package com.projet.mtr895.app.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;

@Getter
@Setter
public class TestSuite {

    private String dirName;
    private String testSuiteName;
    private HashSet<TestCase> testCases;

    public TestSuite(){

    }

    public TestSuite(String dirName, String testSuiteName, HashSet<TestCase> testCases){
        this.dirName = dirName;
        this.testSuiteName = testSuiteName;
        this.testCases = testCases;
    }

}
