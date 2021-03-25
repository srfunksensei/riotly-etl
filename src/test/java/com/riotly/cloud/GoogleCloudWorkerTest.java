package com.riotly.cloud;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.PageImpl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

public class GoogleCloudWorkerTest {

    private static final String CREDENTIALS_TEST_FILE = "credentials_test.json";

    @Test
    public void authExplicit_nullJsonPath() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> GoogleCloudWorker.authExplicit(null, null));
    }

    @Test
    public void authExplicit_differentFileExtension() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> GoogleCloudWorker.authExplicit("test.txt", null));
    }

    @Test
    public void authExplicit() throws IOException {
        final Storage storage = GoogleCloudWorker.authExplicit(CREDENTIALS_TEST_FILE, null);
        Assertions.assertNotNull(storage, "Expected to create storage");
        Assertions.assertEquals(GoogleCloudWorker.DEFAULT_PROJECT_ID, storage.getOptions().getProjectId(), "Expected different project id");

        final ServiceAccountCredentials credentials = (ServiceAccountCredentials) storage.getOptions().getCredentials();
        Assertions.assertEquals("riotly-interview", credentials.getProjectId(), "Expected different project id");
        Assertions.assertEquals("riotly-interview@riotly-interview.iam.gserviceaccount.com", credentials.getClientEmail(), "Expected different email");
        Assertions.assertEquals("106528588169890060822", credentials.getClientId(), "Expected client id");
    }

    @Test
    public void collectJsonObjectNamesInBucketDirectory_nullDirectory() {
        final GoogleCloudWorker googleCloudWorker = new GoogleCloudWorker(null, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> googleCloudWorker.collectJsonObjectNamesInBucketDirectory(null));
    }

    @Test
    public void collectJsonObjectNamesInBucketDirectory_blankDirectory() {
        final GoogleCloudWorker googleCloudWorker = new GoogleCloudWorker(null, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> googleCloudWorker.collectJsonObjectNamesInBucketDirectory("   "));
    }

    @Test
    public void collectJsonObjectNamesInBucketDirectory_noBlobs() {
        final Storage storage = Mockito.mock(Storage.class);
        final Bucket bucket = Mockito.mock(Bucket.class);

        final Page<Blob> page = new PageImpl<>(null, "cursor", new ArrayList<>());
        Mockito.when(bucket.list()).thenReturn(page);

        final GoogleCloudWorker googleCloudWorker = new GoogleCloudWorker(storage, bucket);
        final Set<Blob> result = googleCloudWorker.collectJsonObjectNamesInBucketDirectory(GoogleCloudWorker.DATA_DIRECTORY_NAME);
        Assertions.assertNotNull(result, "Expected result");
        Assertions.assertEquals(0, result.size(), "Expected no results");
    }

    @Test
    public void download_nullBlob() {
        final GoogleCloudWorker googleCloudWorker = new GoogleCloudWorker(null, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> googleCloudWorker.download(null, null));
    }

    @Test
    public void upload_nullPath() {
        final GoogleCloudWorker googleCloudWorker = new GoogleCloudWorker(null, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> googleCloudWorker.upload(null, null));
    }

    @Test
    public void upload_nullBlob() {
        final GoogleCloudWorker googleCloudWorker = new GoogleCloudWorker(null, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> googleCloudWorker.upload(Paths.get("test"), null));
    }
}
