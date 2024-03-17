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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Getter
public class K6TestExecutor implements Executor {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    @Override
    public boolean executeTestCase(TestCase testCase, String outputDir) {
        if (!ConsoleUtils.isK6Installed()) {
            LOG.error("Grafana K6 tool not installed in the system");
            return false;
        }

        if (testCase == null){
            LOG.error("Aborting execution, TestCase is null. Please check the parsed JSON data in " + testCase.getName());
            return false;
        }

        if (testCase.getRequest() == null){
            LOG.error("Request Data cannot be null.  Please check the parsed JSON data in " + testCase.getName());
            return false;
        }

        if (testCase.getRequest().getHost() == null || testCase.getRequest().getHost().isEmpty()){
            LOG.error("Request Data missing host/path.  Please check the parsed JSON data in " + testCase.getName());
            return false;
        }

        K6ExecConfig k6ExecConfig = (K6ExecConfig) testCase.getExecConfig();
        String resultDir = testCase.getName()
                .toLowerCase()
                .replaceAll("\\s+", "_") + "_" + String.format("%04d", new Random().nextInt(10000));

        testCase.setOutputDir(String.valueOf(Path.of(outputDir, resultDir)));
        try {
            initDirectories(testCase);
            List<String> shellCommands = getShellCommands(k6ExecConfig, testCase);
            System.out.println(String.join(" ", shellCommands));
            return ConsoleUtils.run(String.join(" ", shellCommands), new File(testCase.getOutputDir() + "/logs"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void generateReport(TestCase testCase) {
        K6ExecConfig k6ExecConfig = (K6ExecConfig) testCase.getExecConfig();
        if (!k6ExecConfig.isSummary()) {
            LOG.info("Aborting the creation of the report as passed in the config");
            return;
        }
        String jsonString;
        Map<String, Object> summaryJsonContent;
        try {
            jsonString = Files.readString(Path.of(testCase.getOutputDir() + "/summary.json"));
            summaryJsonContent = JsonPath.read(jsonString, "$");
        } catch (Exception e) {
            LOG.error("Aborting the creation of the report, " + testCase.getOutputDir() + "/summary.json not found, check the execution of the K6 script");
            LOG.error(e.getMessage());
            return;
        }
        testCase.setJsonExecutionResultsMap(summaryJsonContent);
        try {
            Files.createFile(Path.of(testCase.getOutputDir() + "/report.html"));
        } catch (IOException e) {
            LOG.error("Aborting the creation of the report");
            LOG.error(e.getMessage());
            return;        }
        Reporter reporter = null;
        try {
            reporter = TestParser.parseReporter(testCase);
            reporter.report(testCase);
        } catch (Exception e) {
            LOG.error("Aborting the creation of the report");
            LOG.error(e.getMessage());
        }
    }

    private void initDirectories(TestCase testCase) throws IOException {
        Path dir = Path.of(testCase.getOutputDir());
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

    private List<String> getShellCommands(K6ExecConfig k6ExecConfig, TestCase testCase) throws IOException, URISyntaxException {
        List<String> shellCommands = new ArrayList<>(List.of("k6 run"));
        Path file = Files.createFile(Path.of(testCase.getOutputDir() + "/summary.json"));
        k6ExecConfig.getK6Options().put("summary-export", String.valueOf(file.toAbsolutePath()));
        for (Map.Entry<String, Object> optionsEntry : k6ExecConfig.getK6Options().entrySet()) {
            if (optionsEntry.getValue().toString().matches("true|false")) {
                if (optionsEntry.getValue().toString().equals("true"))
                    shellCommands.add("--" + optionsEntry.getKey());
                continue;
            }
            shellCommands.add("--" + optionsEntry.getKey() + " " + optionsEntry.getValue());
        }

        for (Map.Entry<String, String> optionsEntry : k6ExecConfig.getK6EnvironmentVariables().entrySet()) {
            shellCommands.add("-e " + optionsEntry.getKey() + "=" + optionsEntry.getValue());
        }

        boolean summaryDisplay = k6ExecConfig.isSummary();
        if (!summaryDisplay) shellCommands.add("--no-summary");
        URL resourceUrl = K6TestExecutor.class.getClassLoader().getResource(k6ExecConfig.getK6JSONFilePath());
        if (resourceUrl != null) {
            Path resourcePath;
            resourcePath = Paths.get(resourceUrl.toURI());
            shellCommands.add(resourcePath.toAbsolutePath().toString());

        } else {
            System.err.println("Resource not found");
        }
        return shellCommands;
    }

}
