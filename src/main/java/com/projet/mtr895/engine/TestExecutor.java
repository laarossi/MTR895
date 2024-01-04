package com.projet.mtr895.engine;

import com.projet.mtr895.entities.TestCase;
import com.projet.mtr895.entities.TestSuite;

public class TestExecutor {

    public static void run(TestSuite testSuite){
        for(TestCase testCase : testSuite.getTestCases())
            run(testCase);
    }

    public static void run(TestCase testCase){
        System.out.println("Executing TestCase#" + testCase.getId());
        
    }


}
