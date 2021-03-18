package com.riotly.writer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.riotly.cloud.GoogleCloudWorker;
import com.riotly.file.FileHelper;

public class CSVWriter {

	public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	public static final String USER = "user";
	public static final String DATE = "date";

	public static final String CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_NAME = "dms_per_day";
	public static final String SEEN_STORIES_PER_DAY_CSV_NAME = "seen_stories_per_day";
	public static final String COMMENTS_PER_DAY_CSV_NAME = "comments_per_day";
	public static final String UNFOLLOW_PER_DAY_CSV_NAME = "unfollows_per_day";
	public static final String LIKES_PER_DAY_CSV_NAME = "likes_per_day";
	public static final String FOLLOWS_PER_DAY_CSV_NAME = "follows_per_day";
	public static final String FOLLOWS_BACK_PER_DAY_CSV_NAME = "follow_backs_per_day";
	public static final String FOLLOWING_PER_DAY_CSV_NAME = "following_count";
	public static final String FOLLOWERS_PER_DAY_CSV_NAME = "follower_count";
	public static final String BLOCKED_FOLLOWERS_PER_DAY_CSV_NAME = "blocked_followers_per_day";
	public static final String ENGAGEMENT_PER_DAY_CSV_NAME = "engagement_per_day";

	public static final List<String> COLUMNS_SORTED = Arrays.asList(ENGAGEMENT_PER_DAY_CSV_NAME,
			BLOCKED_FOLLOWERS_PER_DAY_CSV_NAME, FOLLOWERS_PER_DAY_CSV_NAME,
			FOLLOWING_PER_DAY_CSV_NAME, FOLLOWS_BACK_PER_DAY_CSV_NAME,
			FOLLOWS_PER_DAY_CSV_NAME, LIKES_PER_DAY_CSV_NAME,
			UNFOLLOW_PER_DAY_CSV_NAME, COMMENTS_PER_DAY_CSV_NAME,
			SEEN_STORIES_PER_DAY_CSV_NAME, CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_NAME);

	public static final String HEADER = CSVWriter.USER + "," + CSVWriter.DATE + ","
			+ CSVWriter.COLUMNS_SORTED.stream().reduce((a, b) -> a.concat(",").concat(b)).get();

	public void generateCSV(final String filePath, final Map<Long, Map<String, Long>> data,
			final String separator) {
		
		final String csvPath = filePath.replaceAll("json$", "csv");
		final String user = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.indexOf("."));
		
		final String content = generateCSVString(user, data, separator);
		try {
			FileHelper.writeToFile(content, csvPath);
		} catch (IOException e) {
			System.err.println("Could not write content fo file: " + csvPath);
		}
	}

	public void generateCSV(final String filePath, final Map<Long, Map<String, Long>> data) {
		generateCSV(filePath, data, ",");
	}

	private String generateCSVString(final String user, final Map<Long, Map<String, Long>> data,
			final String separator) {
		final String header = CSVWriter.USER + separator + CSVWriter.DATE + separator
				+ CSVWriter.COLUMNS_SORTED.stream().reduce((a, b) -> a.concat(separator).concat(b)).get() + "\n";

		final StringBuilder builder = new StringBuilder();
		builder.append(header);
		
		for (final Long key : data.keySet()) {
			builder.append(user).append(separator);
			builder.append(CSVWriter.FORMATTER.format(Date.from(Instant.ofEpochMilli(key)))).append(separator);
			
			final Map<String, Long> columnsData = data.get(key);
			for (final String column : CSVWriter.COLUMNS_SORTED) {
				builder.append(columnsData.get(column)).append(separator);
			}
			builder.deleteCharAt(builder.length() - 1);
			
			builder.append("\n");
		}

		return builder.toString();
	}
	
	public static Path merge(List<Path> paths) throws IOException {
	    final List<String> mergedLines = new ArrayList<> ();
	    for (Path p : paths){
	        List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
	        if (!lines.isEmpty()) {
	            if (mergedLines.isEmpty()) {
	                mergedLines.add(lines.get(0)); //add header only once
	            }
	            mergedLines.addAll(lines.subList(1, lines.size()));
	        }
	    }
	    
	    String mergedCsvName = "";
	    if(!mergedLines.isEmpty()) {
	    	mergedCsvName = GoogleCloudWorker.DATA_DIRECTORY_NAME + "/" + "all.csv";
	    	FileHelper.writeToFile(String.join("\n", mergedLines), mergedCsvName);
	    }
	    
	    return Paths.get(mergedCsvName);
	}
}
