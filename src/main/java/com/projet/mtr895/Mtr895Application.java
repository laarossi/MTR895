package com.projet.mtr895;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.engine.TestExecutor;
import com.projet.mtr895.engine.TestLoader;
import com.projet.mtr895.engine.executor.Executor;
import com.projet.mtr895.entities.TestCase;
import com.projet.mtr895.entities.TestSuite;
import com.projet.mtr895.entities.exec.ExecConfig;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.util.HashSet;


public class Mtr895Application implements CommandLineRunner {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(Mtr895Application.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(Mtr895Application.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws IOException {
        HashSet<TestSuite> testSuites = new TestLoader().loadTests("/home/youssef-laarossi/Documents/edunos/MTR895/test-data/api-testing");
        for (TestSuite testSuite : testSuites){
            for(TestCase testCase : testSuite.getTestCases()){
                Executor executor = TestExecutor.parseExecutor(testCase);
                ExecConfig execConfig =
                executor.run();
            }
        }
    }

}

