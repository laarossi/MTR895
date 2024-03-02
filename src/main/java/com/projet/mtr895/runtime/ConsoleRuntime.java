package com.projet.mtr895.runtime;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.TestExecutor;
import com.projet.mtr895.app.TestLoader;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class ConsoleRuntime implements RuntimeWrapper{

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);
    @Override
    public void run(String... args) throws Exception {
        LOG.info("RUNNING APP IN CONSOLE");
        List<String> testingDirectories = Arrays.stream(args).skip(1).toList();
        TestExecutor.runTests(testingDirectories);
    }
}
