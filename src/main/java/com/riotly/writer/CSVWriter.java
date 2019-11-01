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
import com.riotly.parser.JSONFlattener;

public class CSVWriter {

	public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	public static final String USER = "user";
	public static final String DATE = "date";

	public static final List<String> COLUMNS_SORTED = Arrays.asList(JSONFlattener.EGAGMENT_PER_DAY_CSV_NAME,
			JSONFlattener.BLOCKED_FOLLOWERS_PER_DAY_CSV_NAME, JSONFlattener.FOLLOWERS_PER_DAY_CSV_NAME,
			JSONFlattener.FOLLOWING_PER_DAY_CSV_NAME, JSONFlattener.FOLLOWS_BACK_PER_DAY_CSV_NAME,
			JSONFlattener.FOLLOWS_PER_DAY_CSV_NAME, JSONFlattener.LIKES_PER_DAY_CSV_NAME,
			JSONFlattener.UNFOLLOW_PER_DAY_CSV_NAME, JSONFlattener.COMMENTS_PER_DAY_CSV_NAME,
			JSONFlattener.SEEN_STORIES_PER_DAY_CSV_NAME, JSONFlattener.CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_NAME);
	public static final String HEADER = CSVWriter.USER + "," + CSVWriter.DATE + ","
			+ CSVWriter.COLUMNS_SORTED.stream().reduce((a, b) -> a.concat(",").concat(b)).get();

	public void generateCSV(final String filePath, final Map<Long, Map<String, Integer>> data,
			final String separator) {
		
		final String csvPath = filePath.replaceAll("json$", "csv");
		final String user = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.indexOf("."));
		
		final String content = generateCSVString(user, data, separator);
		FileHelper.writeToFile(content, csvPath);
	}

	public void generateCSV(final String filePath, final Map<Long, Map<String, Integer>> data) {
		generateCSV(filePath, data, ",");
	}

	private String generateCSVString(final String user, final Map<Long, Map<String, Integer>> data,
			final String separator) {
		final String header = CSVWriter.USER + separator + CSVWriter.DATE + separator
				+ CSVWriter.COLUMNS_SORTED.stream().reduce((a, b) -> a.concat(separator).concat(b)).get() + "\n";

		final StringBuilder builder = new StringBuilder();
		builder.append(header);
		
		for (final Long key : data.keySet()) {
			builder.append(user).append(separator);
			builder.append(CSVWriter.FORMATTER.format(Date.from(Instant.ofEpochMilli(key)))).append(separator);
			
			final Map<String, Integer> columnsData = data.get(key);
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
