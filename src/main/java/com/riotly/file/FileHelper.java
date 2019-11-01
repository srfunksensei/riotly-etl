package com.riotly.file;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import com.riotly.cloud.GoogleCloudWorker;

public class FileHelper {

	public static final String ZIP_ARCHIVE_PATH = GoogleCloudWorker.DATA_DIRECTORY_NAME + "/milan brankovic.zip";

	public static Path createDirectory(final String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			System.out.println("Creating directory with name: " + directoryName);
			directory.mkdir();
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

				System.out.println("Deleting directory with name: " + dir);
				Files.delete(dir);
				return CONTINUE;
			}
		});
	};

	public static void writeToFile(final String content, final String path) {
		try {
			FileUtils.write(new File(path), content);
		} catch (IOException e) {
			System.err.println("CSVWriter#writeToFile(content, fileName) IOException: " + e);
		}
	}
	
	public static void zipFiles(List<Path> srcFiles) throws IOException {
		FileOutputStream fos = new FileOutputStream(ZIP_ARCHIVE_PATH);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        
        for (Path srcFile : srcFiles) {
            File fileToZip = srcFile.toFile();
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
 
            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        
        zipOut.close();
        fos.close();
    }
}
