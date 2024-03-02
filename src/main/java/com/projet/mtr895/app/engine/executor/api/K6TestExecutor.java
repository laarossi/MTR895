package com.projet.mtr895.app.engine.executor.api;

import ch.qos.logback.classic.Logger;
import com.jayway.jsonpath.JsonPath;
import com.projet.mtr895.app.TestLoader;
import com.projet.mtr895.app.TestParser;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.engine.reporter.Reporter;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.exec.K6ExecConfig;
import com.projet.mtr895.app.utils.ConsoleUtils;
import lombok.Getter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
public class K6TestExecutor implements Executor {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    @Override
    public InputStream run(TestCase testCase) throws Exception {
        if(!ConsoleUtils.isK6Installed())
            throw new IOException("Grafana K6 is not installed in the system");

        K6ExecConfig k6ExecConfig = (K6ExecConfig) testCase.getExecConfig();
        String resultDir = testCase.getName().toLowerCase().replaceAll("\\s+", "_") + "_" + String.format("%04d", new Random().nextInt(10000));;
        initDirectories(resultDir);
        testCase.setOutputDir(resultDir);
        List<String> shellCommands = getShellCommands(k6ExecConfig, resultDir);
        LOG.info(String.join(" ", shellCommands));
        Files.createFile(Path.of(resultDir + "/logs"));
        ConsoleUtils.run(String.join(" ", shellCommands), new File(resultDir + "/logs"));
        generateReport(testCase, resultDir);
        return null;
    }

    private void generateReport(TestCase testCase, String resultDir) throws Exception {
        Map<String, Object> summaryJsonContent = JsonPath.read(Files.readString(Path.of(resultDir + "/summary.json")), "$");
        testCase.setJsonExecutionResultsMap(summaryJsonContent);
        Files.createFile(Path.of(resultDir + "/report.html"));
        File htmlFile = new File(resultDir + "/report.html");
        Reporter reporter = TestParser.parseReporter(testCase);
        reporter.report(htmlFile, testCase);
    }

    private void initDirectories(String testCaseName) throws IOException {
        Path dir = Paths.get(testCaseName);
        if (Files.exists(dir)) {
            try {
                Files.walk(dir)
                        .sorted((p1, p2) -> -p1.compareTo(p2))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Files.createDirectories(dir);
    }

    private List<String> getShellCommands(K6ExecConfig k6ExecConfig, String resultDir) throws IOException {
        List<String> shellCommands = new ArrayList<>(List.of("k6 run"));
        Path file = Files.createFile(Path.of(resultDir + "/summary.json"));
        k6ExecConfig.getK6Options().put("summary-export", String.valueOf(file.toAbsolutePath()));
        for(Map.Entry<String, Object> optionsEntry : k6ExecConfig.getK6Options().entrySet()) {
            if(optionsEntry.getValue().toString().matches("true|false")){
                if (optionsEntry.getValue().toString().equals("true"))
                    shellCommands.add("--" + optionsEntry.getKey());
                continue;
            }
            shellCommands.add("--" + optionsEntry.getKey() + " " + optionsEntry.getValue());
        }

        for(Map.Entry<String, String> optionsEntry : k6ExecConfig.getK6EnvironmentVariables().entrySet()){
            shellCommands.add("-e " + optionsEntry.getKey() + "=" + optionsEntry.getValue());
        }

        boolean summaryDisplay = k6ExecConfig.isSummary();
        if(!summaryDisplay) shellCommands.add("--no-summary");
        URL resourceUrl = K6TestExecutor.class.getClassLoader().getResource(k6ExecConfig.getK6JSONFilePath());
        if (resourceUrl != null) {
            Path resourcePath;
            try {
                resourcePath = Paths.get(resourceUrl.toURI());
                shellCommands.add(resourcePath.toAbsolutePath().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Resource not found");
        }
        return shellCommands;
    }

}
