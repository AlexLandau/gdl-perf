package net.alloyggp.perf.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class ResultFiles {

    public static Map<String, String> read(File resultFile) {
        Map<String, String> results = Maps.newHashMap();

        try {
            for (String line : Files.readAllLines(resultFile.toPath())) {
                String[] split = line.split("=", 2);
                if (split.length == 2) {
                    String key = split[0].trim();
                    String value = split[1].trim();
                    results.put(key, value);
                }
            }
        } catch (FileNotFoundException e) {
            //Treat this like an empty file
            return results;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    public static void write(Map<String, String> results, File outputFile) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputFile))) {
            for (Entry<String, String> entry : results.entrySet()) {
                out.write(entry.getKey() + " = " + entry.getValue());
                out.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
