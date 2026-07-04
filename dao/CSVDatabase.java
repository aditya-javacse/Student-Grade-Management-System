package dao;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVDatabase {
    private static final String DATA_DIR = "data/";

    static {
        // Ensure data directory exists
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Reads a CSV file and returns its rows as lists of fields.
     * The first line (header) is NOT skipped by default, DAOs should handle it.
     */
    public static List<List<String>> readTable(String filename) {
        List<List<String>> rows = new ArrayList<>();
        File file = new File(DATA_DIR + filename);
        if (!file.exists()) {
            return rows;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                // If a line is empty, skip
                if (line.trim().isEmpty()) continue;
                rows.add(parseCSVRow(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename + " - " + e.getMessage());
        }
        return rows;
    }

    /**
     * Writes a set of rows back to the CSV file. Overwrites the file.
     */
    public static void writeTable(String filename, List<String> header, List<List<String>> rows) {
        File file = new File(DATA_DIR + filename);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            // Write header
            if (header != null && !header.isEmpty()) {
                bw.write(toCSVRow(header));
                bw.newLine();
            }
            // Write rows
            for (List<String> row : rows) {
                bw.write(toCSVRow(row));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + filename + " - " + e.getMessage());
        }
    }

    /**
     * Appends a single row to a CSV file.
     */
    public static void appendRow(String filename, List<String> row) {
        File file = new File(DATA_DIR + filename);
        boolean needsHeader = !file.exists();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            bw.write(toCSVRow(row));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending to file: " + filename + " - " + e.getMessage());
        }
    }

    /**
     * Serializes a list of fields into a CSV-escaped row.
     */
    public static String toCSVRow(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (field == null) field = "";
            // Replace newlines with spaces to keep CSV single-line per row simple
            field = field.replace("\r", " ").replace("\n", " ");
            
            boolean needsQuotes = field.contains(",") || field.contains("\"") || field.trim().length() != field.length();
            String escaped = field.replace("\"", "\"\"");
            if (needsQuotes) {
                sb.append("\"").append(escaped).append("\"");
            } else {
                sb.append(escaped);
            }
            if (i < fields.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Parses a CSV row into a list of strings, handling escapes.
     */
    public static List<String> parseCSVRow(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;
        
        StringBuilder curField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote: "" -> "
                    curField.append('"');
                    i++; // skip next quote
                } else {
                    // Toggle quotes
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(curField.toString());
                curField.setLength(0);
            } else {
                curField.append(c);
            }
        }
        result.add(curField.toString());
        return result;
    }

    public static boolean fileExists(String filename) {
        return new File(DATA_DIR + filename).exists();
    }
}
