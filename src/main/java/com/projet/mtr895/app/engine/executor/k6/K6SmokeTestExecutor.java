package com.projet.mtr895.app.engine.executor.k6;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.engine.TestLoader;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.api.APITestCase;
import com.projet.mtr895.app.entities.exec.ExecConfig;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;
import com.projet.mtr895.app.utils.ConsoleUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class K6SmokeTestExecutor extends K6Executor{

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    @Override
    public InputStream run(TestCase testCase) throws IOException {
        if(!ConsoleUtils.isK6Installed())
            throw new IOException("Grafana K6 is not installed in the system");

        if(!(testCase instanceof APITestCase))
            throw new IOException("TestCase is not compatible with the Executor");

        K6ExecConfig k6ExecConfig = (K6ExecConfig) testCase.getExecConfig();

        return null;
    }


}
