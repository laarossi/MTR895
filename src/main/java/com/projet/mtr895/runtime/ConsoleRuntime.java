package com.projet.mtr895.runtime;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.TestExecutor;
import com.projet.mtr895.app.TestLoader;
import com.projet.mtr895.app.entities.TestCase;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConsoleRuntime implements RuntimeWrapper{

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);
    @Override
    public void run(String... args) throws Exception {
        LOG.info("RUNNING APP IN CONSOLE");
        if (args.length < 2 || !args[1].equals("--testDir")) {
            System.out.println("Usage: java ArgumentChecker --testDir <path1,path2,...> --outputDir <outputDirectory> --configDir <configDirectory>");
            System.exit(1);
        }

        String testDirs = args[2];
        String outputDir = null;
        String configDir = null;

        if (args.length >= 4 && args[3].equals("--outputDir")) {
            outputDir = args[4];
        }

        if (args.length >= 6 && args[5].equals("--configDir")) {
            configDir = args[6];
        }

        String[] testingDirectories = Arrays.stream(testDirs.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        if (testingDirectories.length == 0){
            System.out.println("No testing directories provided.");
        }

        System.out.println("Test directories: " + Arrays.toString(testingDirectories));
        if (outputDir != null) {
            System.out.println("Using " + outputDir + " as output directory");
        } else {
            System.out.println("Output directory not specified. Using default location (or handle as needed).");
        }
        Map<String, Boolean> testCases = TestExecutor.runTests(List.of(testingDirectories), outputDir, configDir);
        testCases.forEach((k, v) ->{
            LOG.info("TestCase : " + k + " | status : " + (v ? "Passed" : "Failed"));
        });
        if(testCases.containsValue(false)){
            System.exit(1);
        }else System.exit(0);
    }
}
