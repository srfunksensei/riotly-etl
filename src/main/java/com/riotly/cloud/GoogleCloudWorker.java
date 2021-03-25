package com.riotly.cloud;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;

public class GoogleCloudWorker {

	public static final String GOOGLE_CLOUD_PLATFORM_URL = "https://www.googleapis.com/auth/cloud-platform";
	public static final String DEFAULT_PROJECT_ID = "riotly-interview";
	public static final String DEFAULT_BUCKET_NAME = "riotly-interview-test";

	public static final String DATA_DIRECTORY_NAME = "data";
	public static final String UPLOAD_TO_DIRECTORY_NAME = "output";

	public static final String JSON_EXTENSION = ".json";

	private final Storage storage;
	private final Bucket bucket;
	private String outputDirName;

	public GoogleCloudWorker(final Storage storage, final Bucket bucket) {
		this.storage = storage;
		this.bucket = bucket;
		this.outputDirName = UPLOAD_TO_DIRECTORY_NAME;
	}

	public GoogleCloudWorker(final Storage storage, final Bucket bucket, final String outputDirName) {
		this(storage, bucket);
		if (isNotBlank(outputDirName)) {
			this.outputDirName = outputDirName.trim();
		}
	}

	public void setOutputDirName(final String outputDirName) {
		if (isBlank(outputDirName)) {
			throw new IllegalArgumentException("Output directory name must be not blank");
		}
		this.outputDirName = outputDirName;
	}

	private static boolean isNotBlank(final String value) {
		return !isBlank(value);
	}

	private static boolean isBlank(final String value) {
		return value == null || value.trim().isEmpty();
	}

	public static Storage authExplicit(final String jsonPath, final String projectId) throws IOException {
		if (isBlank(jsonPath)) {
			throw new IllegalArgumentException("File path must be set");
		}
		if (!jsonPath.endsWith(JSON_EXTENSION)) {
			throw new IllegalArgumentException("File provided must be json file");
		}

		final GoogleCredentials credentials = GoogleCredentials.fromStream(GoogleCloudWorker.class.getClassLoader().getResourceAsStream(jsonPath))
				.createScoped(Lists.newArrayList(GOOGLE_CLOUD_PLATFORM_URL));

		final String projId = isNotBlank(projectId) ? projectId : DEFAULT_PROJECT_ID;

		return StorageOptions.newBuilder()
				.setCredentials(credentials)
				.setProjectId(projId)
				.build()
				.getService();
	}

	public Set<Blob> collectJsonObjectNamesInBucketDirectory(final String directory) {
		if (isBlank(directory)) {
			throw new IllegalArgumentException("Directory needs to be specified");
		}

		return StreamSupport.stream(bucket.list().iterateAll().spliterator(), false)
				.filter(o -> o.getName().startsWith(directory)
						&& o.getName().endsWith(JSON_EXTENSION))
				.collect(Collectors.toSet());
	}

	public Optional<Blob> getOutputBlob() {
		final String directoryPath = outputDirName + "/";
		return StreamSupport.stream(bucket.list().iterateAll().spliterator(), false)
				.filter(o -> o.getName().equals(directoryPath)).findFirst();
	}

	public void download(final Path downloadTo, final Blob blob) throws IOException {
		if (blob == null) {
			throw new IllegalArgumentException("Blob must be present");
		}

		PrintStream writeTo = System.out;
		if (downloadTo != null) {
			writeTo = new PrintStream(new FileOutputStream(downloadTo.toFile()));
		}

		if (blob.getSize() < 1_000_000) {
			// Blob is small read all its content in one request
			final byte[] content = blob.getContent();
			writeTo.write(content);
		} else {
			// When Blob size is big or unknown use the blob's channel reader.
			try (ReadChannel reader = blob.reader();
					WritableByteChannel channel = Channels.newChannel(writeTo)) {
				ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
				while (reader.read(bytes) > 0) {
					bytes.flip();
					channel.write(bytes);
					bytes.clear();
				}
			}
		}

		if (downloadTo == null) {
			writeTo.println();
		} else {
			writeTo.close();
		}
	}

	public void upload(final Path uploadFrom, final Blob outputBlob) throws IOException {
		if (uploadFrom == null) {
			throw new IllegalArgumentException("Upload path must be present");
		}
		if (outputBlob == null) {
			throw new IllegalArgumentException("Output blob must be present");
		}

		final BlobId blobId = BlobId.of(outputBlob.getBucket(), outputDirName + "/milan brankovic.zip");
		final BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/zip").build();
		
		if (Files.size(uploadFrom) > 1_000_000) {
			// When content is not available or large (1MB or more) it is recommended
			// to write it in chunks via the blob's channel writer.
			byte[] buffer = new byte[1024];
			try (WriteChannel writer = storage.writer(blobInfo);
				 InputStream input = Files.newInputStream(uploadFrom)) {
				int limit;
				while ((limit = input.read(buffer)) >= 0) {
					writer.write(ByteBuffer.wrap(buffer, 0, limit));
				}
			}
		} else {
			byte[] bytes = Files.readAllBytes(uploadFrom);
			// create the blob in one request.
			storage.create(blobInfo, bytes);
		}

		System.out.println("Blob was created");
	}
}
