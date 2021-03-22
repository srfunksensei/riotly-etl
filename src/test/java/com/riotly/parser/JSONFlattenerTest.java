package com.riotly.parser;

import com.riotly.writer.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;

public class JSONFlattenerTest {

    private static final String TEST_JSON_FILE = "test_file.json";

    private final JSONFlattener underTest = new JSONFlattener();

    @Test
    public void parseJson() throws URISyntaxException {
        final File file = getFile(TEST_JSON_FILE);

        final Map<Long, Map<String, Long>> parsedResult = underTest.parseJson(file);
        assertResult(parsedResult);
    }

    @Test
    public void parseJson_withCharset() throws URISyntaxException {
        final File file = getFile(TEST_JSON_FILE);

        final Map<Long, Map<String, Long>> parsedResult = underTest.parseJson(file, Charset.defaultCharset().toString());
        assertResult(parsedResult);
    }

    @Test
    public void parseJson_flatJsonString() throws URISyntaxException, IOException {
        final File file = getFile(TEST_JSON_FILE);
        final String content = FileUtils.readFileToString(file, Charset.defaultCharset().toString());

        final Map<Long, Map<String, Long>> parsedResult = underTest.parseJson(content);
        assertResult(parsedResult);
    }

    private File getFile(final String fileName) throws URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        }

        return new File(resource.toURI());
    }

    private void assertResult(final Map<Long, Map<String, Long>> parsedResult) {
        Assertions.assertNotNull(parsedResult, "Expected file to be parsed");
        final Map<String, Long> result_1519200000000 = parsedResult.get(1519200000000L);
        Assertions.assertNotNull(result_1519200000000, "Expected to find result for key");
        Assertions.assertFalse(result_1519200000000.isEmpty(), "Expected to find result values for key");

        Assertions.assertEquals(new HashSet<>(CSVWriter.COLUMNS_SORTED), result_1519200000000.keySet(), "Expected different values for result column names");

        Assertions.assertEquals(183, result_1519200000000.get(CSVWriter.ENGAGEMENT_PER_DAY_CSV_COLUMN_NAME), "Expected different value");
    }
}
