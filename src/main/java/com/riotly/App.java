package com.riotly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
	public static void main(String[] args) {
		try {
			final ClassLoader classLoader = App.class.getClassLoader();

			final Storage storage = GoogleCloudWorker.authExplicit(classLoader.getResource("credentials.json").getPath());
			final Bucket bucket = storage.get(GoogleCloudWorker.BUCKET_NAME);

			final Path downloadToPath = FileHelper.createDirectory(GoogleCloudWorker.DATA_DIRECTORY_NAME);

			final GoogleCloudWorker worker = new GoogleCloudWorker(storage, bucket);
			Set<Blob> blobs = worker.collectJsonObjectNamesInBucketDirectory(GoogleCloudWorker.DATA_DIRECTORY_NAME);
			for (Blob b : blobs) {
				final File file = new File(b.getName());
				
				 worker.download(b, file.toPath());
			}
			
			Stream.of(downloadToPath.toFile().listFiles()).parallel().forEach(f -> {
				Map<Long, Map<String, Integer>> data = new JSONFlattener().parseJson(f);
				new CSVWriter().generateCSV(f.getPath(), data);
			});
			
			List<Path> srcFiles = Files.list(downloadToPath)
									.filter(s -> s.toString().endsWith(".csv"))
									.collect(Collectors.toList());
			final Path mergedSrc = CSVWriter.merge(srcFiles);
			srcFiles.add(mergedSrc);
			
			FileHelper.zipFiles(srcFiles);
			
			if(worker.getOutputBlob().isPresent()) {
				worker.upload(Paths.get(FileHelper.ZIP_ARCHIVE_PATH), worker.getOutputBlob().get());
			}
			
			FileHelper.deleteFileOrFolder(downloadToPath);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}