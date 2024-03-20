package com.projet.mtr895.app.engine.reporter.api;

import ch.qos.logback.classic.Logger;
import com.jayway.jsonpath.JsonPath;
import com.projet.mtr895.app.TestLoader;
import com.projet.mtr895.app.engine.reporter.Reporter;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.report.Report;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class K6Reporter implements Reporter {

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

        Map<String, Object> executionResults = (Map<String, Object>) testCase.getJsonExecutionResultsMap().get("root_group");
        if (executionResults == null) {
            LOG.error("Empty metrics results json file");
            return;
        }


        initHTMLFile(dataOutputStream, testCase);
        openHTMLBody(dataOutputStream, testCase);
        includeTestCaseHeader(dataOutputStream, testCase);
        includeTestLogs(dataOutputStream, testCase);
        Map<String, Object> checks = null;
        if (testCase.getJsonExecutionResultsMap().get("root_group") != null) {
            checks = (Map<String, Object>) ((Map<?, ?>) testCase.getJsonExecutionResultsMap().get("root_group")).get("checks");
            if (checks != null && !checks.isEmpty())
                printChecks(checks, dataOutputStream);
        }

        if (testCase.getJsonExecutionResultsMap().get("metrics") != null) {
            Map<String, Object> metrics = (Map<String, Object>) testCase.getJsonExecutionResultsMap().get("metrics");
            List<String> httpMetricsB = metrics.keySet().stream().map(Object::toString)
                    .filter(m -> {
                        Map<String, Float> ms = (Map<String, Float>) metrics.get(m);
                        return ms.containsKey("min") && ms.containsKey("max") && ms.containsKey("avg");
                    })
                    .toList();
            List<String> httpMetricsT = metrics.keySet().stream().map(Object::toString)
                    .filter(m -> {
                        Map<String, Float> ms = (Map<String, Float>) metrics.get(m);
                        return !ms.containsKey("min") || !ms.containsKey("max") || !ms.containsKey("avg");
                    })
                    .toList();
            printHttpMetrics(dataOutputStream, httpMetricsB, metrics);
            printOtherMetrics(dataOutputStream, httpMetricsT, metrics);
            includeJS(dataOutputStream, metrics, httpMetricsB);
            includeJS(dataOutputStream, checks);
        }

        closeHTMLBody(dataOutputStream, testCase);
        closeHTMLFile(dataOutputStream, testCase);
    }

    private void printOtherMetrics(DataOutputStream dataOutputStream, List<String> httpMetrics, Map<String, Object> metrics) throws IOException {
        dataOutputStream.write("<h1 class='title'>Other Metrics</h1>".getBytes());
        for (String metric : httpMetrics) {
            Map<String, Object> metricObj = (Map<String, Object>) metrics.get(metric);
            if (metricObj == null || metricObj.isEmpty())
                continue;
            dataOutputStream.write(("<div class='metric'><h1>" + metric + "</h1><table>").getBytes());
            for (String key : metricObj.keySet()) {
                dataOutputStream.write(("<tr><td>" + key + "</td><td>" + metricObj.get(key) + "</td></tr>").getBytes());
            }
            dataOutputStream.write("</table></div>".getBytes());
        }
    }

    private void includeTestCaseHeader(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write(("" +
                "<div class='header'>" +
                "   <p>" + testCase.getName() + "</p>" +
                "   <a href=\"" + Path.of(testCase.getOutputDir()).toAbsolutePath() + "\" class='outputDir'>" + Path.of(testCase.getOutputDir()).toAbsolutePath() + "</a>" +
                "</div>").getBytes());
    }

    private void includeTestLogs(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        List<String> logs = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(Path.of(testCase.getOutputDir() + "/logs").toFile()));
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("time="))
                logs.add(line);
            line = reader.readLine();
        }
        if (logs.isEmpty()) {
            reader.close();
            return;
        }
        dataOutputStream.write("<h1 class='title'>Logs</h1>".getBytes());
        dataOutputStream.write("<div class='logs'>".getBytes());
        logs.forEach(log -> {
            try {
                dataOutputStream.write("<p>".getBytes());
                if (log.contains("level=error"))
                    dataOutputStream.write("<span class='error'>[ERROR]</span>".getBytes());
                else if (log.contains("level=info"))
                    dataOutputStream.write("<span class='info'>[INFO]</span>".getBytes());
                else if (log.contains("level=warn"))
                    dataOutputStream.write("<span class='warn'>[WARN]</span>".getBytes());
                dataOutputStream.write((log + "</p>").getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        dataOutputStream.write("</div>".getBytes());
        reader.close();
    }

    private void includeJS(DataOutputStream dataOutputStream, Map<String, Object> metrics, List<String> httpMetrics) throws IOException {
        dataOutputStream.write("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>".getBytes());
        httpMetrics.forEach(metric -> {
            Map<String, Float> metricsData = (Map<String, Float>) metrics.get(metric);
            metricsData.remove("thresholds");
            Object[] labelsSet = metricsData.keySet().toArray();
            StringBuilder label = new StringBuilder();
            for (int i = 0; i < labelsSet.length; i++) {
                if (i == 0) label.append("[");
                label.append("'").append(labelsSet[i]).append("'");
                if (i == labelsSet.length - 1) label.append("]");
                else label.append(",");
            }
            String values = metricsData.values().toString();
            try {
                dataOutputStream.write(("<script>\n" +
                        "\n" +
                        "  new Chart('" + metric + "', {\n" +
                        "    type: 'bar',\n" +
                        "    data: {\n" +
                        "      labels: " + label + ",\n" +
                        "      datasets: [{\n" +
                        "        label: '# responses in ms',\n" +
                        "        data: " + values + ",\n" +
                        "        borderWidth: 1\n" +
                        "      }]\n" +
                        "    },\n" +
                        " options : { responsive : false," +
                        "      scales: { \n" +
                        "        y: {\n" +
                        "          beginAtZero: true\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  });\n" +
                        "              var chartEl = document.getElementById(\"" + metric + "\");\n" +
                        "chartEl.height = 350" +
                        "</script>").getBytes());
                dataOutputStream.write(("<script>" +
                        "var vancesElements = document.getElementsByClassName(\"canvas\");\n" +
                        "\n" +
                        "// Loop through each element and set height and width to 200\n" +
                        "for (var i = 0; i < vancesElements.length; i++) {\n" +
                        "  vancesElements[i].style.height = \"200px\";\n" +
                        "  vancesElements[i].style.width = \"200px\";\n" +
                        "}" +
                        "</script>").getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void includeJS(DataOutputStream dataOutputStream, Map<String, Object> checks) throws IOException {
        checks.keySet().forEach(key -> {
            Map<String, Object> check = (Map<String, Object>) checks.get(key);
            try {
                dataOutputStream.write(("<script>\n" +
                        "\n" +
                        "  new Chart('" + key.replaceAll(" ", "_") + "', {\n" +
                        "    type: 'bar',\n" +
                        "    data: {\n" +
                        "      labels: ['fails','passes'],\n" +
                        "      datasets: [{\n" +
                        "        label: '# number of iterations/requests',\n" +
                        "        data: ["+ Integer.parseInt(check.get("fails").toString())+","+ Integer.parseInt(check.get("passes").toString()) +"],\n" +
                        "        borderWidth: 1\n" +
                        "      }]\n" +
                        "    },\n" +
                        " options : { responsive : false," +
                        "      scales: { \n" +
                        "        y: {\n" +
                        "          beginAtZero: true\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  });\n" +
                        "</script>").getBytes());
                dataOutputStream.write(("<script>" +
                        "var vancesElements = document.getElementsByClassName(\"canvas\");\n" +
                        "\n" +
                        "// Loop through each element and set height and width to 200\n" +
                        "for (var i = 0; i < vancesElements.length; i++) {\n" +
                        "  vancesElements[i].style.height = \"200px\";\n" +
                        "  vancesElements[i].style.width = \"200px\";\n" +
                        "}" +
                        "</script>").getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void printHttpMetrics(DataOutputStream dataOutputStream, List<String> httpMetrics, Map<String, Object> metrics) throws IOException {
        dataOutputStream.write("<br><h1 class=\"title\">Http Metrics :</h1>".getBytes());
        for (String metric : httpMetrics) {
            Map<String, Object> metricObj = (Map<String, Object>) metrics.get(metric);
            if (metricObj == null || metricObj.isEmpty())
                continue;
            dataOutputStream.write("<div class='metric'>".getBytes());
            dataOutputStream.write(("<h1>" + metric + "</h1>").getBytes());
            dataOutputStream.write(("<canvas id=\"" + metric + "\"></canvas>").getBytes());
            dataOutputStream.write("</div>".getBytes());
        }

        dataOutputStream.write("<br><br><h1 class=\"title\">Http Metrics Thresholds:</h1>".getBytes());
        for (String metric : httpMetrics) {
            Map<String, Object> metricObj = (Map<String, Object>) metrics.get(metric);
            if (metricObj == null || metricObj.isEmpty())
                continue;
            if (metricObj.containsKey("thresholds")) {
                dataOutputStream.write("<div class='metric'>".getBytes());
                dataOutputStream.write(("<h1>" + metric + "</h1>").getBytes());
                Map<String, Object> thresholds = (Map<String, Object>) metricObj.get("thresholds");
                dataOutputStream.write("<div class='thresholds'>".getBytes());
                for (String key : thresholds.keySet()) {
                    dataOutputStream.write(("<p>" + key + " : " + thresholds.get(key) + "</p>").getBytes());
                }
                dataOutputStream.write("</div>".getBytes());
                dataOutputStream.write("</div>".getBytes());
            }
        }

    }

    private void printChecks(Map<String, Object> checks, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write("<h1 class=\"title\">Check Results</h1>".getBytes());
        checks.keySet().forEach(key -> {
            try {
                Map<String, Object> mapCheckResult = (Map<String, Object>) checks.get(key);
                dataOutputStream.write("<div class='metric'>".getBytes());
                dataOutputStream.write(("<h1>" +  mapCheckResult.get("name")  + "</h1>").getBytes());
                dataOutputStream.write(("<canvas id=\"" +  mapCheckResult.get("name").toString().replaceAll(" ", "_") + "\"></canvas>").getBytes());
                dataOutputStream.write("</div>".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void initHTMLFile(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write(("<html>" +
                "<head>" +
                "<title>" + testCase.getName() + "</title>" +
                "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/charts.css/dist/charts.min.css\">").getBytes());
        includeCSS(dataOutputStream);
        dataOutputStream.write("</head>".getBytes());
    }

    private void includeCSS(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(("<style>#column-example-4 {\n" +
                "  width: 100%;\n" +
                "  max-width: 300px;\n" +
                "  margin-bottom: 300px;\n" +
                "}\n" +
                "#column-example-4 .column {\n" +
                "  --aspect-ratio: 4 / 3;\n" +
                "}#column-example-4 {\n" +
                "  width: 100%;\n" +
                "  max-width: 31%;\n" +
                "  height: 300px;\n" +
                "  display: inline-block;\n" +
                "  margin: 0px 1%;" +
                " }\n" +
                " body{\n" +
                "     margin: 20px 200px;\n" +
                "     background: #eee;\n" +
                " }\n" +
                "canvas{width:100%;height:600px;}.metric{display:inline-block;margin-right:1%;margin-bottom:0px;width:45%;" +
                "box-shadow: rgba(50, 50, 93, 0.25) 0px 6px 12px -2px, rgba(0, 0, 0, 0.3) 0px 3px 7px -3px;" +
                "padding:0px 20px;" +
                "margin-bottom:30px;" +
                "}\n" +
                ".title{border:0px; width:100%; padding:10px 20px; color:#4a4a4a; background:#e1e1e1; margin-bottom:20px;}" +
                "h1{font-size: 20px; font-family: sans-serif; width: 100%; padding-bottom: 3px; border-bottom: 1px solid #929292;}" +
                ".header{" +
                "width:50%;" +
                "margin:10px auto;" +
                "text-align:center;}" +
                ".logs {" +
                "height: 250px;\n" +
                "  overflow: scroll;\n" +
                "  background: black;\n" +
                "  color: #fff;\n" +
                "  padding: 0px 20px;" +
                "font-family: monospace;" +
                " margin-bottom : 20px;" +
                "}" +
                ".logs span{" +
                "float:left;" +
                "font-weight:bold;" +
                "margin-right:5px;" +
                "}" +
                ".error{color:red;}" +
                ".info{color:yellow;}" +
                ".warn{color:pink;}" +
                "table{margin-bottom: 50px;\n" +
                "  width: 100%;}" +
                "</style>").getBytes());
    }

    private void closeHTMLFile(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write("</html>".getBytes());
    }

    private void openHTMLBody(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write("<body>".getBytes());
    }

    private void closeHTMLBody(DataOutputStream dataOutputStream, TestCase testCase) throws IOException {
        dataOutputStream.write("</body>".getBytes());
    }

}
