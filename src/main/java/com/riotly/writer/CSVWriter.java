package com.riotly.writer;

import com.riotly.file.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CSVWriter {

	public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	public static final String ALL_FILES_MERGED_CSV_NAME = "all.csv";

	public static final String DEFAULT_CSV_SEPARATOR = ",";

	public static final String USER_COLUMN_NAME = "user";
	public static final String DATE_COLUMN_NAME = "date";

	public static final String CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_COLUMN_NAME = "dms_per_day";
	public static final String SEEN_STORIES_PER_DAY_CSV_COLUMN_NAME = "seen_stories_per_day";
	public static final String COMMENTS_PER_DAY_CSV_COLUMN_NAME = "comments_per_day";
	public static final String UNFOLLOW_PER_DAY_CSV_COLUMN_NAME = "unfollows_per_day";
	public static final String LIKES_PER_DAY_CSV_COLUMN_NAME = "likes_per_day";
	public static final String FOLLOWS_PER_DAY_CSV_COLUMN_NAME = "follows_per_day";
	public static final String FOLLOWS_BACK_PER_DAY_CSV_COLUMN_NAME = "follow_backs_per_day";
	public static final String FOLLOWING_PER_DAY_CSV_COLUMN_NAME = "following_count";
	public static final String FOLLOWERS_PER_DAY_CSV_COLUMN_NAME = "follower_count";
	public static final String BLOCKED_FOLLOWERS_PER_DAY_CSV_COLUMN_NAME = "blocked_followers_per_day";
	public static final String ENGAGEMENT_PER_DAY_CSV_COLUMN_NAME = "engagement_per_day";

	public static final List<String> COLUMNS_SORTED = Arrays.asList(ENGAGEMENT_PER_DAY_CSV_COLUMN_NAME,
			BLOCKED_FOLLOWERS_PER_DAY_CSV_COLUMN_NAME, FOLLOWERS_PER_DAY_CSV_COLUMN_NAME,
			FOLLOWING_PER_DAY_CSV_COLUMN_NAME, FOLLOWS_BACK_PER_DAY_CSV_COLUMN_NAME,
			FOLLOWS_PER_DAY_CSV_COLUMN_NAME, LIKES_PER_DAY_CSV_COLUMN_NAME,
			UNFOLLOW_PER_DAY_CSV_COLUMN_NAME, COMMENTS_PER_DAY_CSV_COLUMN_NAME,
			SEEN_STORIES_PER_DAY_CSV_COLUMN_NAME, CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_COLUMN_NAME);

	public static Path generateCSV(final String filePath, final Map<Long, Map<String, Long>> data) {
		return generateCSV(filePath, data, DEFAULT_CSV_SEPARATOR);
	}

	public static Path generateCSV(final String filePath, final Map<Long, Map<String, Long>> data,
			final String separator) {
		if (filePath == null || filePath.trim().isEmpty()) {
			System.err.println("File path not provided");
			return null;
		}

		if (!filePath.endsWith("json")) {
			System.out.println("File path provided is not json file");
			return null;
		}
		
		final String csvPath = filePath.replaceAll("json$", "csv");
		final Path path = Paths.get(csvPath);

		final String user = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.indexOf("."));
		final String content = generateCSVString(user, data, separator);
		try {
			FileHelper.writeToFile(content, csvPath);
		} catch (IOException e) {
			System.err.println("Could not write content fo file: " + csvPath);
		}

		return path;
	}

	private static String generateCSVString(final String user, final Map<Long, Map<String, Long>> data,
			final String separator) {
		if (data == null) {
			return "";
		}

		final String header = generateCSVHeader(separator);

		final StringBuilder builder = new StringBuilder();
		builder.append(header);
		
		for (final Long key : data.keySet()) {
			final Map<String, Long> columnsData = data.get(key);
			if (columnsData != null) {
				builder.append(user).append(separator);
				builder.append(FORMATTER.format(Date.from(Instant.ofEpochMilli(key)))).append(separator);

				COLUMNS_SORTED.forEach(column -> builder.append(columnsData.get(column)).append(separator));
				builder.deleteCharAt(builder.length() - 1);
				builder.append("\n");
			}
		}

		return builder.toString();
	}

	private static String generateCSVHeader(final String separator) {
		return USER_COLUMN_NAME + separator + DATE_COLUMN_NAME + separator
				+ COLUMNS_SORTED.stream().reduce((a, b) -> a.concat(separator).concat(b)).get() + "\n";
	}
}
