package com.riotly.writer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CSVWriterTest {

    @Test
    public void generateCSV_filePathNull() {
        final Path result = CSVWriter.generateCSV(null, null);
        Assertions.assertNull(result, "Expected null");
    }

    @Test
    public void generateCSV_filePathNotJson(@TempDir final Path tempDir) throws IOException {
        final Path namesFilePaht = tempDir.resolve("names.txt");
        Files.createFile(namesFilePaht);

        final Path result = CSVWriter.generateCSV(namesFilePaht.toAbsolutePath().toString(), null);
        Assertions.assertNull(result, "Expected null");

        final List<File> files = Files.list(tempDir)
                .map(Path::toFile)
                .collect(Collectors.toList());
        Assertions.assertEquals(1, files.size(), "Expected file not to be created");
    }

    @Test
    public void generateCSV_nullData(@TempDir final Path tempDir) throws IOException {
        final Path testJsonFilePath = tempDir.resolve("test.json");
        Files.createFile(testJsonFilePath);

        final Path result = CSVWriter.generateCSV(testJsonFilePath.toAbsolutePath().toString(), null);
        Assertions.assertNotNull(result, "Expected null");

        final List<File> files = Files.list(tempDir)
                .map(Path::toFile)
                .collect(Collectors.toList());
        Assertions.assertEquals(2, files.size(), "Expected file to be created");

        final Optional<File> csvOpt = files.stream().filter(file -> file.getAbsolutePath().endsWith("csv")).findAny();
        Assertions.assertTrue(csvOpt.isPresent(), "Expected to find csv file created");

        final File csvFile = csvOpt.get();
        final List<String> csvLines = Files.readAllLines(csvFile.toPath());
        Assertions.assertEquals(0, csvLines.size(), "Expected different number of lines");
    }

    @Test
    public void generateCSV(@TempDir final Path tempDir) throws IOException {
        final Path testJsonFilePath = tempDir.resolve("test.json");
        Files.createFile(testJsonFilePath);

        final Map<String, Long> data = new HashMap<>();
        CSVWriter.COLUMNS_SORTED.forEach(column -> data.put(column, 1L));

        final Map<Long, Map<String, Long>> userData  = new HashMap<Long, Map<String, Long>>() {{
            put(1L, data);
        }};
        final Path result = CSVWriter.generateCSV(testJsonFilePath.toAbsolutePath().toString(), userData);
        Assertions.assertNotNull(result, "Expected null");

        final List<File> files = Files.list(tempDir)
                .map(Path::toFile)
                .collect(Collectors.toList());
        Assertions.assertEquals(2, files.size(), "Expected file to be created");

        final Optional<File> csvOpt = files.stream().filter(file -> file.getAbsolutePath().endsWith("csv")).findAny();
        Assertions.assertTrue(csvOpt.isPresent(), "Expected to find csv file created");

        final File csvFile = csvOpt.get();
        final List<String> csvLines = Files.readAllLines(csvFile.toPath());
        Assertions.assertEquals(2, csvLines.size(), "Expected different number of lines");
    }
}
