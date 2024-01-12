package com.projet.mtr895.app.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsoleUtils {

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

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

}
