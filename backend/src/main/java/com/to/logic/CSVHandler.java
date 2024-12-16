package com.to.logic;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class CSVHandler {
    public static List<String> getKeywords() {
        ClassLoader classLoader = CSVHandler.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("versions_keywords.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = reader.readLine();
            return Arrays.asList(line.split(","));
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
