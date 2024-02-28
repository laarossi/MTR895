package com.projet.mtr895.app.utils;

import ch.qos.logback.classic.Logger;
import com.projet.mtr895.app.TestLoader;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ConsoleUtils {

    private static final Logger LOG = (Logger) LoggerFactory
            .getLogger(TestLoader.class);

    public static boolean isK6Installed(){
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return isExecutableExists("k6.exe");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return isExecutableExists("k6");
        } else return false;
    }

    public static boolean isExecutableExists(String executableName) {
        try {
            Runtime.getRuntime().exec(executableName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void run(String sh) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(sh.split("\\s+"));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            LOG.info(line);
        }
        int exitCode = process.waitFor();
        LOG.warn("Exited with error code: " + exitCode);
    }

    public static void run(String sh, File file) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(sh.split("\\s+"));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
        String line;
        while ((line = reader.readLine()) != null) {
            LOG.info(line);
            dataOutputStream.write((line.concat("\n")).getBytes());
        }
        dataOutputStream.close();
        fileOutputStream.close();
        int exitCode = process.waitFor();
        LOG.warn("Exited with error code: " + exitCode);
    }


}
