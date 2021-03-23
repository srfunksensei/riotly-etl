package com.riotly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.riotly.cloud.GoogleCloudWorker;
import com.riotly.file.FileHelper;
import com.riotly.parser.JSONFlattener;
import com.riotly.writer.CSVWriter;

public class App {
	public static void main(String[] args) throws IOException {
		final Path downloadToPath = FileHelper.createDirectory(GoogleCloudWorker.DATA_DIRECTORY_NAME);
		if (downloadToPath == null) {
			return;
		}

		try {
			final ClassLoader classLoader = App.class.getClassLoader();

			final Storage storage = GoogleCloudWorker.authExplicit(classLoader.getResource("credentials.json").getPath(), GoogleCloudWorker.DEFAULT_PROJECT_ID);
			final Bucket bucket = storage.get(GoogleCloudWorker.DEFAULT_BUCKET_NAME);

			final GoogleCloudWorker worker = new GoogleCloudWorker(storage, bucket);
			final Set<Blob> blobs = worker.collectJsonObjectNamesInBucketDirectory(GoogleCloudWorker.DATA_DIRECTORY_NAME);
			for (final Blob b : blobs) {
				final File file = new File(b.getName());
				
				 worker.download(b, file.toPath());
			}
			
			Stream.of(downloadToPath.toFile().listFiles()).parallel().forEach(f -> {
				Map<Long, Map<String, Long>> data = new JSONFlattener().parseJson(f);
				CSVWriter.generateCSV(f.getPath(), data);
			});
			
			final List<Path> srcFiles = Files.list(downloadToPath)
									.filter(s -> s.toString().endsWith(".csv"))
									.collect(Collectors.toList());
			final Path mergedSrc = FileHelper.merge(srcFiles, Optional.empty());
			srcFiles.add(mergedSrc);
			
			FileHelper.zipFiles(srcFiles, Optional.empty());
			
			if (worker.getOutputBlob().isPresent()) {
				worker.upload(Paths.get(FileHelper.ZIP_ARCHIVE_PATH), worker.getOutputBlob().get());
			}
			
		} catch (IOException e) {
			System.err.println(e.fillInStackTrace().toString());
		} finally {
			FileHelper.deleteFileOrFolder(downloadToPath);
		}
	}
}