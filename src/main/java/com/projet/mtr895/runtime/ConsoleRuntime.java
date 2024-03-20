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
        if (testCases.isEmpty()){
            LOG.info("Executed successfully all test suites.");
            System.exit(0);
        }
        Path executionResults = Path.of(outputDir, "execution_results");
        if(Files.exists(executionResults)){
            Files.delete(executionResults);
        }
        File failFile = Files.createFile(executionResults).toFile();
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(failFile));
        testCases.forEach((k, v) ->{
            LOG.error("TestCase : " + k + " | status : " + (v ? "Passed" : "Failed"));
            try {
                dataOutputStream.write(("TestCase : " + k + " | status : " + (v ? "Passed" : "Failed\n")).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataOutputStream.close();
        System.exit(1);
    }
}
