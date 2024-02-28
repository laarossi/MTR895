package com.projet.mtr895.app.engine.reporter;

import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.report.Report;

import java.io.*;

public class K6HTMLReporter implements Reporter{

    @Override
    public void report(File file) throws Exception {
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
    }

    public void initHTMLFile(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write(("<html><head><title>" + testCase.getName() + "</title>").getBytes());
        dataOutputStream.write("<link rel=\"stylesheet\" href=\"\"/>".getBytes());
    }
    public void addHeader(DataOutputStream dataOutputStream, String testCaseName, String testCaseType){}

}
