package com.projet.mtr895;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.engine.TestExecutor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;


public class Mtr895Application implements CommandLineRunner {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(Mtr895Application.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(Mtr895Application.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws Exception {
        TestExecutor.runTests("/home/youssef-laarossi/Documents/edunos/MTR895/test-data/api-testing");
    }

}

