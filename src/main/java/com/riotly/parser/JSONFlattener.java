package com.riotly.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONFlattener {

	public static final String EGAGMENT_PER_DAY_JSON_NAME = "_engagementPerDay";
	public static final String EGAGMENT_PER_DAY_CSV_NAME = "engagement_per_day";
	public static final String BLOCKED_FOLLOWERS_PER_DAY_JSON_NAME = "_blockedFollowersPerDay";
	public static final String BLOCKED_FOLLOWERS_PER_DAY_CSV_NAME = "blocked_followers_per_day";
	public static final String FOLLOWERS_PER_DAY_JSON_NAME = "_followersPerDayEx";
	public static final String FOLLOWERS_PER_DAY_CSV_NAME = "follower_count";
	public static final String FOLLOWING_PER_DAY_JSON_NAME = "_followingsPerDayEx";
	public static final String FOLLOWING_PER_DAY_CSV_NAME = "following_count";
	public static final String FOLLOWS_BACK_PER_DAY_JSON_NAME = "_followsBackPerDay";
	public static final String FOLLOWS_BACK_PER_DAY_CSV_NAME = "follow_backs_per_day";
	public static final String FOLLOWS_PER_DAY_JSON_NAME = "_followsPerDayEx";
	public static final String FOLLOWS_PER_DAY_CSV_NAME = "follows_per_day";
	public static final String LIKES_PER_DAY_JSON_NAME = "_likesPerDayEx";
	public static final String LIKES_PER_DAY_CSV_NAME = "likes_per_day";
	public static final String UNFOLLOW_PER_DAY_JSON_NAME = "_unfollowDayEx";
	public static final String UNFOLLOW_PER_DAY_CSV_NAME = "unfollows_per_day";
	public static final String COMMENTS_PER_DAY_JSON_NAME = "_commentsPerDayEx";
	public static final String COMMENTS_PER_DAY_CSV_NAME = "comments_per_day";
	public static final String SEEN_STORIES_PER_DAY_JSON_NAME = "_seenStoriesPerDay";
	public static final String SEEN_STORIES_PER_DAY_CSV_NAME = "seen_stories_per_day";
	public static final String CONTACT_MEMBERS_FRIENDS_PER_DAY_JSON_NAME = "_contactMembersFriendsPerDayEx";
	public static final String CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_NAME = "dms_per_day";

	public static final String DATE_FIELD = "<CurrentDate>k__BackingField";
	public static final String COUNT_FIELD = "<NumberPerDay>k__BackingField";

	public static final Map<String, String> TRANSFORM_MAP_NAMES = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put(JSONFlattener.EGAGMENT_PER_DAY_JSON_NAME, JSONFlattener.EGAGMENT_PER_DAY_CSV_NAME);
			put(JSONFlattener.BLOCKED_FOLLOWERS_PER_DAY_JSON_NAME, JSONFlattener.BLOCKED_FOLLOWERS_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWERS_PER_DAY_JSON_NAME, JSONFlattener.FOLLOWERS_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWING_PER_DAY_JSON_NAME, JSONFlattener.FOLLOWING_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWS_BACK_PER_DAY_JSON_NAME, JSONFlattener.FOLLOWS_BACK_PER_DAY_CSV_NAME);
			put(JSONFlattener.FOLLOWS_PER_DAY_JSON_NAME, JSONFlattener.FOLLOWS_PER_DAY_CSV_NAME);
			put(JSONFlattener.LIKES_PER_DAY_JSON_NAME, JSONFlattener.LIKES_PER_DAY_CSV_NAME);
			put(JSONFlattener.UNFOLLOW_PER_DAY_JSON_NAME, JSONFlattener.UNFOLLOW_PER_DAY_CSV_NAME);
			put(JSONFlattener.COMMENTS_PER_DAY_JSON_NAME, JSONFlattener.COMMENTS_PER_DAY_CSV_NAME);
			put(JSONFlattener.SEEN_STORIES_PER_DAY_JSON_NAME, JSONFlattener.SEEN_STORIES_PER_DAY_CSV_NAME);
			put(JSONFlattener.CONTACT_MEMBERS_FRIENDS_PER_DAY_JSON_NAME,
					JSONFlattener.CONTACT_MEMBERS_FRIENDS_PER_DAY_CSV_NAME);
		}
	};

	public Map<Long, Map<String, Integer>> parseJson(final File file, String encoding) {
		Map<Long, Map<String, Integer>> result = new HashMap<>();

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
	
	public Map<Long, Map<String, Integer>> parseJson(final File file) {
		return parseJson(file, "UTF-8");
	}

	public Map<Long, Map<String, Integer>> parseJson(final String json) {
		final Map<Long, Map<String, Integer>> result = new HashMap<>();

		try {
			final JSONObject jsonObject = new JSONObject(json);
			
			for (final String key : TRANSFORM_MAP_NAMES.keySet()) {
				final JSONArray array = (JSONArray) jsonObject.get(key);
				for(int i = 0; i < array.length(); i++) {
					final JSONObject jobj = array.getJSONObject(i);
					
					final String substringTime = ((String)jobj.get(JSONFlattener.DATE_FIELD)).substring(6);
					final Long date = Long.parseLong(substringTime.substring(0, substringTime.indexOf("-")));
					if(!result.containsKey(date)) {
						result.put(date, JSONFlattener.initCounts());
					}
					
					Map<String, Integer> data = result.get(date);
					
					Integer count = (Integer) jobj.get(JSONFlattener.COUNT_FIELD);
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
	
	private static Map<String, Integer> initCounts(){
		final Map<String, Integer> map = new HashMap<>();
		
		for (String key : TRANSFORM_MAP_NAMES.keySet()) {
			map.put(TRANSFORM_MAP_NAMES.get(key), 0);
		}
		
		return map;
	}

}
