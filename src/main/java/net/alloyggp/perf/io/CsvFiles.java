package net.alloyggp.perf.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.alloyggp.perf.Csvable;

public class CsvFiles {
    public static void append(Csvable result, File outputCsvFile) {
        append(ImmutableList.of(result), outputCsvFile);
    }

    public static void append(List<? extends Csvable> allResults, File outputCsvFile) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputCsvFile, true))) {
            for (Csvable result : allResults) {
                List<String> values = result.getValuesForCsv();
                String delimiter = result.getDelimiter();
                validateCsv(values, delimiter);
                String line = Joiner.on(delimiter).join(values);
                out.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T extends Csvable> List<T> load(File csvFile, CsvLoadFunction<T> function) throws IOException {
        List<T> results = Lists.newArrayList();
        if (!csvFile.isFile()) {
            return results;
        }
        List<String> lines = Files.readAllLines(csvFile.toPath());
        for (String line : lines) {
            try {
                T value = function.load(line);
                results.add(value);
            } catch (Exception e) {
                //Ignore the line
                e.printStackTrace();
            }
        }
        return results;
    }

    public static interface CsvLoadFunction<T extends Csvable> {
        /**
         * Should throw an exception if the input is bad (e.g. the line is not actually a CSV).
         */
        T load(String inputLine) throws Exception;
    }

    private static void validateCsv(List<String> values, String delimiter) {
        for (String value : values) {
            if (value.contains(delimiter)) {
                throw new RuntimeException("Delimiter " + delimiter +
                        " found in CSV value: " + value + " among values: " + values);
            } else if (value.contains("\n") || value.contains("\r")) {
                throw new RuntimeException("Newline found in CSV value: " + value + " among values: " + values);
            }
        }
    }

    public static void rewrite(File csvFile, List<? extends Csvable> newResults) {
        if (!csvFile.isFile()) {
            return;
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(csvFile, false))) {
            for (Csvable result : newResults) {
                List<String> values = result.getValuesForCsv();
                String delimiter = result.getDelimiter();
                validateCsv(values, delimiter);
                String line = Joiner.on(delimiter).join(values);
                out.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
