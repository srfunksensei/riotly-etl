package com.riotly.file;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHelperTest {

    public static final String TEST_DIR_NAME = "test";

    @AfterEach
    public void tearDown() throws IOException {
        final Path testDirPath = Paths.get(TEST_DIR_NAME);
        if (Files.exists(testDirPath)) {
            Files.delete(testDirPath);
        }
    }

    @Test
    public void createDirectory_nameNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FileHelper.createDirectory(null));
    }

    @Test
    public void createDirectory_nameEmptyString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FileHelper.createDirectory("    "));
    }

    @Test
    public void createDirectory() {
        final Path directoryPath = FileHelper.createDirectory(TEST_DIR_NAME);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(directoryPath, "Expected directory to be created"),
                () -> Assertions.assertTrue(Files.exists(directoryPath), "Expected directory to exist after creation"));
    }

    @Test
    public void createDirectory_directoryExist() {
        final Path directoryPath = FileHelper.createDirectory(TEST_DIR_NAME),
                dirPathExisting = FileHelper.createDirectory(TEST_DIR_NAME);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(directoryPath, "Expected directory to be present"),
                () -> Assertions.assertNotNull(dirPathExisting, "Expected directory to be present"),
                () -> Assertions.assertTrue(Files.exists(directoryPath), "Expected directory to exist"),
                () -> Assertions.assertTrue(Files.exists(dirPathExisting), "Expected directory to exist"),
                () -> Assertions.assertEquals(directoryPath, dirPathExisting, "Expected same directory path"));
    }

    @Test
    public void writeToFile_nullContent() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FileHelper.writeToFile(null, TEST_DIR_NAME));
    }

    @Test
    public void writeToFile_nullFilePath() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FileHelper.writeToFile("", null));
    }

    @Test
    public void writeToFile_emptyFilePath() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> FileHelper.writeToFile("", "    "));
    }

    @Test
    public void writeToFile(@TempDir final Path tempDir) throws IOException {
        final Path names = tempDir.resolve("names.txt");

        final String name = "name";

        FileHelper.writeToFile(name, names.toAbsolutePath().toString());

        Assertions.assertAll(
                () -> Assertions.assertTrue(Files.exists(names), "File should exist"),
                () -> Assertions.assertLinesMatch(Stream.of(name).collect(Collectors.toList()), Files.readAllLines(names)));
    }

    @Test
    public void deleteFileOrFolder_file() throws Exception {
        final Path testDirPath = Paths.get(TEST_DIR_NAME),
                testFile = testDirPath.resolve("file1.txt");

        FileHelper.deleteFileOrFolder(testFile);

        if (Files.exists(testFile)) {
            Assertions.fail("Expected to delete file in directory");
        }
    }

    @Test
    public void deleteFileOrFolder_dir() throws Exception {
        final Path testDirPath = Paths.get(TEST_DIR_NAME);
        FileHelper.deleteFileOrFolder(testDirPath);

        if (Files.exists(testDirPath)) {
            Assertions.fail("Expected to delete directory");
        }
    }

    @Test
    public void deleteFileOrFolder_dirRecursive() throws Exception {
        final Path testDirPath = Paths.get(TEST_DIR_NAME),
                testFile1 = testDirPath.resolve("file1.txt"),
                testFile2 = testDirPath.resolve("file2.doc");

        FileHelper.deleteFileOrFolder(testDirPath);

        if (Files.exists(testDirPath)) {
            Assertions.fail("Expected to delete directory");
        }

        if (Files.exists(testFile1) || Files.exists(testFile2)) {
            Assertions.fail("Expected to delete files in directory");
        }
    }

    @Test
    public void zipFiles() throws Exception {
        final Path directoryTest = FileHelper.createDirectory(TEST_DIR_NAME),
                outputDirectory = FileHelper.createDirectory("output"),
                zip = outputDirectory.resolve("output.zip");
        final File file1 = new File(directoryTest.toAbsolutePath().toString() + "/file1.txt"),
                file2 = new File(directoryTest.toAbsolutePath().toString() + "/file2.txt");

        Files.createFile(file1.toPath());
        Files.createFile(file2.toPath());

        FileHelper.zipFiles(Stream.of(file1.toPath(), file2.toPath()).collect(Collectors.toList()), new String[] { zip.toAbsolutePath().toString() });

        FileHelper.deleteFileOrFolder(directoryTest);
        FileHelper.deleteFileOrFolder(outputDirectory);

        if (Files.exists(directoryTest) || Files.exists(outputDirectory)) {
            Assertions.fail("Expected to delete directories");
        }

        if (Files.exists(file1.toPath()) || Files.exists(file2.toPath()) || Files.exists(zip)) {
            Assertions.fail("Expected to delete files in directory");
        }
    }
}
