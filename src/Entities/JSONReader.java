package Entities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JSONReader {
    public static Map<String, String> readJson(String filePath) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line.trim());
            }
        }

        String jsonString = jsonBuilder.toString();

        if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            jsonString = jsonString.substring(1, jsonString.length() - 1);
        }

        String[] keyValuePairs = jsonString.split(",");
        Map<String, String> gameData = new HashMap<>();

        for (String pair : keyValuePairs) {

            String[] entry = pair.split(":");

            String key = entry[0].replace("\"", "").trim();
                
            String value = entry[1].trim();

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            gameData.put(key, value);
        }

        return gameData;
    }
}