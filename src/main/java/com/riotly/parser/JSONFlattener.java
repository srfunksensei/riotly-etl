package com.riotly.parser;

import com.riotly.writer.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class JSONFlattener {

	public static final String ENGAGEMENT_PER_DAY_JSON_NAME = "_engagementPerDay";
	public static final String BLOCKED_FOLLOWERS_PER_DAY_JSON_NAME = "_blockedFollowersPerDay";
	public static final String FOLLOWERS_PER_DAY_JSON_NAME = "_followersPerDayEx";
	public static final String FOLLOWING_PER_DAY_JSON_NAME = "_followingsPerDayEx";
	public static final String FOLLOWS_BACK_PER_DAY_JSON_NAME = "_followsBackPerDay";
	public static final String FOLLOWS_PER_DAY_JSON_NAME = "_followsPerDayEx";
	public static final String LIKES_PER_DAY_JSON_NAME = "_likesPerDayEx";
	public static final String UNFOLLOW_PER_DAY_JSON_NAME = "_unfollowDayEx";
	public static final String COMMENTS_PER_DAY_JSON_NAME = "_commentsPerDayEx";
	public static final String SEEN_STORIES_PER_DAY_JSON_NAME = "_seenStoriesPerDay";
	public static final String CONTACT_MEMBERS_FRIENDS_PER_DAY_JSON_NAME = "_contactMembersFriendsPerDayEx";

	public static final String DATE_FIELD = "<CurrentDate>k__BackingField";
	public static final String COUNT_FIELD = "<NumberPerDay>k__BackingField";

	public static final Map<String, String> TRANSFORM_MAP_NAMES = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put(JSONFlattener.ENGAGEMENT_PER_DAY_JSON_NAME, CSVWriter.ENGAGEMENT_PER_DAY_CSV_NAME);
			put(JSONFlattener.BLOCKED_FOLLOWERS_PER_DAY_JSON_NAME, CSVWriter.BLOCKED_FOLLOWERS_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWERS_PER_DAY_JSON_NAME, CSVWriter.FOLLOWERS_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWING_PER_DAY_JSON_NAME, CSVWriter.FOLLOWING_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWS_BACK_PER_DAY_JSON_NAME, CSVWriter.FOLLOWS_BACK_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWS_PER_DAY_JSON_NAME, CSVWriter.FOLLOWS_PER_DAY_CSV_NAME);
			put(JSONFlattener.LIKES_PER_DAY_JSON_NAME, CSVWriter.LIKES_PER_DAY_CSV_NAME);
			put(JSONFlattener.UNFOLLOW_PER_DAY_JSON_NAME, CSVWriter.UNFOLLOW_PER_DAY_CSV_NAME);
			put(JSONFlattener.COMMENTS_PER_DAY_JSON_NAME, CSVWriter.COMMENTS_PER_DAY_CSV_NAME);
			put(JSONFlattener.SEEN_STORIES_PER_DAY_JSON_NAME, CSVWriter.SEEN_STORIES_PER_DAY_CSV_NAME);
			put(JSONFlattener.CONTACT_MEMBERS_FRIENDS_PER_DAY_JSON_NAME,
					CSVWriter.CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_NAME);
		}
	};

	public Map<Long, Map<String, Long>> parseJson(final File file) {
		return parseJson(file, Charset.defaultCharset().toString());
	}

	public Map<Long, Map<String, Long>> parseJson(final File file, String encoding) {
		Map<Long, Map<String, Long>> result = new HashMap<>();

		try {
			final String json = FileUtils.readFileToString(file, encoding);
			result = parseJson(json);
		} catch (IOException e) {
			System.err.println("JsonFlattener#parseJson(file, encoding) IOException: " + e);
		} catch (Exception ex) {
			System.err.println("JsonFlattener#parseJson(file, encoding) Exception: " + ex);
		}

		return result;
	}

	public Map<Long, Map<String, Long>> parseJson(final String json) {
		final Map<Long, Map<String, Long>> result = new HashMap<>();

		try {
			final JSONObject jsonObject = new JSONObject(json);
			
			for (final String key : TRANSFORM_MAP_NAMES.keySet()) {
				final JSONArray array = (JSONArray) jsonObject.get(key);
				for(int i = 0; i < array.length(); i++) {
					final JSONObject jObj = array.getJSONObject(i);
					
					final String substringTime = jObj.get(JSONFlattener.DATE_FIELD).toString().substring(6);
					final Long date = Long.parseLong(substringTime.substring(0, substringTime.indexOf("-")));
					if(!result.containsKey(date)) {
						result.put(date, JSONFlattener.initCounts());
					}
					
					Map<String, Long> data = result.get(date);
					
					Long count = Long.parseLong(jObj.get(JSONFlattener.COUNT_FIELD).toString());
					if(data.containsKey(TRANSFORM_MAP_NAMES.get(key))) {
						count += data.get(TRANSFORM_MAP_NAMES.get(key));
					}
					
					data.put(TRANSFORM_MAP_NAMES.get(key), count);
				}
			}
		} catch (JSONException je) {
			System.err.println("JsonFlattener#parseJson(json) JSONException: " + je);
		}

		return result;
	}
	
	private static Map<String, Long> initCounts(){
		final Map<String, Long> map = new HashMap<>();
		
		for (final String key : TRANSFORM_MAP_NAMES.keySet()) {
			map.put(TRANSFORM_MAP_NAMES.get(key), 0L);
		}
		
		return map;
	}

}
