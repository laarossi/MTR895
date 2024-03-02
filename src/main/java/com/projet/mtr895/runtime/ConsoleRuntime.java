package com.projet.mtr895.runtime;

import com.projet.mtr895.app.TestExecutor;

public class ConsoleRuntime implements RuntimeWrapper{
    @Override
    public void run(String... args) {
        TestExecutor.runTests("/home/youssef-laarossi/Documents/MTR895/test-data/api-testing");
    }
}
