package com.projet.mtr895;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.TestExecutor;
import com.projet.mtr895.runtime.ApiRuntime;
import com.projet.mtr895.runtime.ConsoleRuntime;
import com.projet.mtr895.runtime.RuntimeWrapper;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;


public class Mtr895Application {

    private static String executionMode = null;
    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(Mtr895Application.class);

    public static void main(String[] args) throws Exception {
        run(args);
    }

    public static void run(String... args) throws Exception {
        if(args.length != 0)
            parseArguments(args);
        LOG.info("EXECUTION MODE : " + executionMode);
        RuntimeWrapper runtimeWrapper = executionMode.equals("console") ? new ConsoleRuntime() : new ApiRuntime();
        runtimeWrapper.run(args);
    }

    private static void parseArguments(String[] args) {
        executionMode = args[0].startsWith("--") ? args[0].substring(2) : "console";
    }


}

