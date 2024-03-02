package com.projet.mtr895;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.TestExecutor;
import com.projet.mtr895.runtime.ConsoleRuntime;
import com.projet.mtr895.runtime.RuntimeWrapper;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;


public class Mtr895Application implements CommandLineRunner {

    private String executionMode = null;
    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(Mtr895Application.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(Mtr895Application.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws Exception {
        if(args.length != 0) parseArguments(args);
        LOG.info("EXECUTION MODE : " + this.executionMode);
        RuntimeWrapper runtimeWrapper = this.executionMode.equals("console") ? new ConsoleRuntime() : null;
        if(runtimeWrapper == null){
            LOG.error("Unable to initialize the execution wrapper, check the execution mode parameter");
            return;
        }
        runtimeWrapper.run(args);
    }

    private void parseArguments(String[] args) {
        this.executionMode = args[0].startsWith("--") ? args[0].substring(2) : "console";
    }


}

