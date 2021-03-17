package com.riotly.file;

import com.riotly.cloud.GoogleCloudWorker;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class FileHelper {

	public static final String ZIP_ARCHIVE_PATH = GoogleCloudWorker.DATA_DIRECTORY_NAME + "/milan brankovic.zip";

	public static Path createDirectory(final String directoryName) {
		if (directoryName == null || directoryName.trim().isEmpty()) {
			throw new IllegalArgumentException("Directory name is required");
		}

		final File directory = new File(directoryName);
		if (!directory.exists()) {
			System.out.println("Creating directory with name: " + directoryName);
			final boolean isDirCreated = directory.mkdir();
			if (!isDirCreated) {
				System.out.println("Directory with name: " + directoryName + " not created successfully.");
				System.err.println("FileHelper#createDirectory(directoryName)");
				return null;
			}
		}

		return directory.toPath();
	}

	public static void deleteFileOrFolder(final Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(final Path file, final IOException e) {
				return handleException(e);
			}

			private FileVisitResult handleException(final IOException e) {
				System.err.println("FileHelper#deleteFileOrFolder(path)#handleException(e)" + e);
				return TERMINATE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
				if (e != null) {
					return handleException(e);
				}

				System.out.println("Deleting directory with name: " + dir.getFileName());
				Files.delete(dir);
				return CONTINUE;
			}
		});
	}

	public static void writeToFile(final String content, final String path) throws IOException {
		if (content == null) {
			throw new IllegalArgumentException("File content must be present");
		}

		if (path == null || path.trim().isEmpty()) {
			throw new IllegalArgumentException("File path must be present");
		}

		FileUtils.write(new File(path), content, Charset.defaultCharset());
	}
	
	public static void zipFiles(final List<Path> srcFiles, final String... filePath) throws IOException {
		if (srcFiles == null || srcFiles.isEmpty()) {
			return;
		}

		String zipArchivePath = ZIP_ARCHIVE_PATH;
		if (filePath != null && filePath.length > 0 && !filePath[0].trim().isEmpty()) {
			zipArchivePath = filePath[0];
		}

		try (final FileOutputStream fos = new FileOutputStream(zipArchivePath);
			 final ZipOutputStream zipOut = new ZipOutputStream(fos)) {

			for (final Path srcFile : srcFiles) {
				final File fileToZip = srcFile.toFile();
				try (final FileInputStream fis = new FileInputStream(fileToZip)) {
					final ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
					zipOut.putNextEntry(zipEntry);

					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
				}
			}
		}
	}
}
