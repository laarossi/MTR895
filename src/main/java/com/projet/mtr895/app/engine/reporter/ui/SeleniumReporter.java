package com.projet.mtr895.app.engine.reporter.ui;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.TestLoader;
import com.projet.mtr895.app.engine.reporter.Reporter;
import com.projet.mtr895.app.entities.TestCase;
import net.minidev.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeleniumReporter implements Reporter {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    @Override
    public void report(TestCase testCase) throws Exception {
        if (testCase == null) {
            LOG.error("Unexpected error the TestCase is null");
            return;
        }

        if (testCase.getJsonExecutionResultsMap() == null || testCase.getJsonExecutionResultsMap().isEmpty()) {
            LOG.error("Empty json results, check the execution of the TestCase");
            return;
        }

        File file = Files.createFile(Path.of(testCase.getOutputDir() + "/report.html")).toFile();
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
        initHTMLFile(dataOutputStream, testCase);
        dataOutputStream.write("<body>".getBytes());
        includeTestCaseHeader(dataOutputStream, testCase);
        Map<String, Object> jsonResult = testCase.getJsonExecutionResultsMap();
        List<Map<String, Object>> checks = (List<Map<String, Object>>) jsonResult.getOrDefault("checks", new HashMap<>());
        List<Map<String, Object>> events = (List<Map<String, Object>>) jsonResult.getOrDefault("events", new HashMap<>());
        if (!checks.isEmpty()) {
            printChecks(dataOutputStream, checks);
        }

        if (!events.isEmpty()) {
            printEvents(dataOutputStream, events);
        }

        dataOutputStream.write("</body></html>".getBytes());
        dataOutputStream.close();
    }

    private void printEvents(DataOutputStream dataOutputStream, List<Map<String, Object>> events) throws IOException {
        dataOutputStream.write("<h1 class=\"title\"> Events :</h1>".getBytes());
        for (Map<String, Object> event : events) {
            String element = (String) event.getOrDefault("element", null),
                    eventType = (String) event.getOrDefault("event", null),
                    eventExecutorStatus = event.getOrDefault("event-execution-status", false).toString(),
                    selector = (String) event.getOrDefault("selector", null);
            dataOutputStream.write("<div class='event'><br>".getBytes());
            dataOutputStream.write("<table>".getBytes());
            dataOutputStream.write("<tr>".getBytes());
            dataOutputStream.write(("<td class='data'>Event : </td><td>" + eventType + "</td>").getBytes());
            dataOutputStream.write("</tr>".getBytes());
            dataOutputStream.write("<tr>".getBytes());
            dataOutputStream.write(("<td class='data'>Element : </td><td>" + element + "</td>").getBytes());
            dataOutputStream.write("</tr>".getBytes());
            dataOutputStream.write("<tr>".getBytes());
            dataOutputStream.write(("<td class='data'>Selector : </td><td>" + selector + "</td>").getBytes());
            dataOutputStream.write("</tr>".getBytes());
            dataOutputStream.write("<tr>".getBytes());
            dataOutputStream.write(("<td class='data'>Status : </td><td>" + eventExecutorStatus + "</td>").getBytes());
            dataOutputStream.write("</tr>".getBytes());
            dataOutputStream.write("</table>".getBytes());
            List<Map<String, Object>> beforeChecks = (List<Map<String, Object>>) event.getOrDefault("before-checks", new HashMap<>());
            List<Map<String, Object>> afterChecks = (List<Map<String, Object>>) event.getOrDefault("after-checks", new HashMap<>());
            dataOutputStream.write("<h3 class=\"before-checks\"> Before Checks Results :</h1>".getBytes());
            printChecks(dataOutputStream, beforeChecks);
            dataOutputStream.write("<h3 class=\"before-checks\"> After Checks Results :</h1>".getBytes());
            printChecks(dataOutputStream, afterChecks);
            dataOutputStream.write("</div>".getBytes());
        }
    }

    private void printChecks(DataOutputStream dataOutputStream, List<Map<String, Object>> checks) throws IOException {
        dataOutputStream.write("<table class='content-table'>".getBytes());
        dataOutputStream.write(("<tr>" +
                "<th>Element</th>" +
                "<th>Selector</th>" +
                "<th>Value</th>" +
                "<th>Class Name</th>" +
                "<th>ID</th>" +
                "<th>Current Value</th>" +
                "<th>Status</th" +
                "</tr>").getBytes());
        for (Map<String, Object> check : checks) {
            String element = (String) check.getOrDefault("element", null),
                    selector = (String) check.getOrDefault("selector", "cssSelector"),
                    value = (String) check.getOrDefault("value", null),
                    className = (String) check.getOrDefault("className", null),
                    id = (String) check.getOrDefault("id", null),
                    currentValue = (String) check.getOrDefault("currentValue", null),
                    status = check.getOrDefault("status", false).toString();
            dataOutputStream.write(("<tr>" +
                    "<td>" + element + "</td>" +
                    "<td>" + selector + "</td>" +
                    "<td>" + value + "</td>" +
                    "<td>" + className + "</td>" +
                    "<td>" + id + "</td>" +
                    "<td>" + currentValue + "</td>" +
                    "<td>" + status + "</td>" +
                    "</tr>").getBytes());
        }
        dataOutputStream.write("</table>".getBytes());
    }

    public void initHTMLFile(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write(("<html>" +
                "<head>" +
                "<title>" + testCase.getName() + "</title>" +
                "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/charts.css/dist/charts.min.css\">").getBytes());
        includeCSS(dataOutputStream);
        dataOutputStream.write("</head>".getBytes());
    }

    private void includeTestCaseHeader(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write(("" +
                "<div class='header'>" +
                "   <p>" + testCase.getName() + "</p>" +
                "   <a href=\"" + Path.of(testCase.getOutputDir()) + "\" class='outputhir'>" + Path.of(testCase.getOutputDir()).toAbsolutePath() + "</a>" +
                "</div>").getBytes());
    }

    private void includeCSS(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write("<style>".getBytes());
        dataOutputStream.write((".title{border:0px; width:100%; padding:10px 20px; color:#4a4a4a; background:#e1e1e1; margin-bottom:20px;}" +
                "h1{font-size: 20px; font-family: sans-serif; width: 100%; padding-bottom: 3px; border-bottom: 1px solid #929292;}" +
                ".header{" +
                "width:50%;" +
                "margin:10px auto;" +
                "text-align:center;}" +
                ".event{display:inline-block;margin-right:1%;margin-bottom:0px;width:45%;" +
                "        box-shadow: rgba(50, 50, 93, 0.25) 0px 6px 12px -2px, rgba(0, 0, 0, 0.3) 0px 3px 7px -3px;" +
                "  padding:0px 20px;" +
                "  margin-bottom:30px;" +
                "                }" +
                ".event .title{width:90%;}" +
                ".event table:last-child{margin-bottom:20px;}" +
                "\n" +
                ".content-table {\n" +
                "  border-collapse: collapse;\n" +
                "  margin: 25px 0;\n" +
                "  font-size: 0.9em;\n" +
                "  min-width: 400px;\n" +
                "  border-radius: 5px 5px 0 0;\n" +
                "  overflow: hidden;\n" +
                "  box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);\n" +
                "}\n" +
                "\n" +
                ".content-table tr {\n" +
                "  background-color: #009879;\n" +
                "  color: #ffffff;\n" +
                "  text-align: left;\n" +
                "  font-weight: bold;\n" +
                "}\n" +
                "\n" +
                ".content-table th,\n" +
                ".content-table td {\n" +
                "  padding: 12px 15px;\n" +
                "}\n" +
                "\n" +
                ".content-table tbody tr {\n" +
                "  border-bottom: 1px solid #dddddd;\n" +
                "}\n" +
                "\n" +
                "\n" +
                ".content-table tbody tr:last-of-type {\n" +
                "  border-bottom: 2px solid #009879;\n" +
                "}\n" +
                "\n" +
                ".content-table tbody tr.active-row {\n" +
                "  font-weight: bold;\n" +
                "  color: #009879;\n" +
                "}\n" +
                ".content-table tbody td{color:black; background:white;}" +
                ".event table{width:100%;}" +
                ".event table td{padding:10px 20px;}" +
                ".event table td.data{background:#cecece; color:#4a4a4a;}" +
                "body{padding:20px 50px;}").getBytes());
        dataOutputStream.write("</style>".getBytes());
    }
}
