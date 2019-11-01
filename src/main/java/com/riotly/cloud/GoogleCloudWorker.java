package com.riotly.cloud;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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

	public static final String PROJECT_ID = "riotly-interview";
	public static final String BUCKET_NAME = "riotly-interview-test";
	public static final String DATA_DIRECTORY_NAME = "data";

	public static final String UPLOAD_TO_DIRECTORY_NAME = "output";
	public static final String JSON_EXTENSTION = ".json";

	private Storage storage;
	private Bucket bucket;

	public GoogleCloudWorker(Storage storage, Bucket bucket) {
		this.storage = storage;
		this.bucket = bucket;
	}

	public Storage getStorage() {
		return this.storage;
	}

	public Bucket getBucket() {
		return this.bucket;
	}

	public static Storage authExplicit(String jsonPath) throws IOException {
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		return StorageOptions.newBuilder().setCredentials(credentials).setProjectId(GoogleCloudWorker.PROJECT_ID)
				.build().getService();
	}

	public Set<Blob> collectJsonObjectNamesInBucketDirectory(final String directory) {
		return StreamSupport.stream(bucket.list().iterateAll().spliterator(), false)
				.filter(o -> o.getName().startsWith(GoogleCloudWorker.DATA_DIRECTORY_NAME)
						&& o.getName().endsWith(GoogleCloudWorker.JSON_EXTENSTION))
				.collect(Collectors.toSet());
	}

	public Optional<Blob> getOutputBlob() {
		return StreamSupport.stream(bucket.list().iterateAll().spliterator(), false)
				.filter(o -> o.getName().equals(GoogleCloudWorker.UPLOAD_TO_DIRECTORY_NAME + "/")).findFirst();
	}

	public void download(Blob blob, Path downloadTo) throws IOException {
		if (blob == null) {
			System.err.println("No such object");
			return;
		}
		PrintStream writeTo = System.out;
		if (downloadTo != null) {
			writeTo = new PrintStream(new FileOutputStream(downloadTo.toFile()));
		}
		if (blob.getSize() < 1_000_000) {
			// Blob is small read all its content in one request
			byte[] content = blob.getContent();
			writeTo.write(content);
		} else {
			// When Blob size is big or unknown use the blob's channel reader.
			try (ReadChannel reader = blob.reader()) {
				WritableByteChannel channel = Channels.newChannel(writeTo);
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

	public void upload(Path uploadFrom, Blob outputBlob) throws IOException {
		BlobId blobId = BlobId.of(outputBlob.getBucket(), GoogleCloudWorker.UPLOAD_TO_DIRECTORY_NAME + "/milan brankovic.zip");
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/zip").build();
		
		if (Files.size(uploadFrom) > 1_000_000) {
			// When content is not available or large (1MB or more) it is recommended
			// to write it in chunks via the blob's channel writer.
			try (WriteChannel writer = storage.writer(blobInfo)) {
				byte[] buffer = new byte[1024];
				try (InputStream input = Files.newInputStream(uploadFrom)) {
					int limit;
					while ((limit = input.read(buffer)) >= 0) {
						try {
							writer.write(ByteBuffer.wrap(buffer, 0, limit));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
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
