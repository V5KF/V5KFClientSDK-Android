package com.v5kf.client.lib.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.V5Util;

public class V5MusicMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6045588653628213176L;
	private String title;
	private String music_url;
	private String hq_music_url;
	private String description;
	private String thumb_id;
	
	public V5MusicMessage() {
		// TODO Auto-generated constructor stub
	}
	
	public V5MusicMessage(String title, String music_url, String description) {
		this.title = title;
		this.music_url = music_url;
		this.description = description;
		this.message_type = V5MessageDefine.MSG_TYPE_TEXT;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5MusicMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json);
		this.title = json.optString(V5MessageDefine.MSG_TITLE);
		this.description = json.optString(V5MessageDefine.MSG_DESCRIPTION);
		this.music_url = json.optString(V5MessageDefine.MSG_MUSIC_URL);
		this.hq_music_url = json.optString(V5MessageDefine.MSG_HQ_MUSIC_URL);
		this.thumb_id = json.optString(V5MessageDefine.MSG_THUMB_ID);
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		json.put(V5MessageDefine.MSG_MUSIC_URL, this.music_url);
		json.put(V5MessageDefine.MSG_TITLE, this.title);
		json.put(V5MessageDefine.MSG_HQ_MUSIC_URL, this.hq_music_url);
		json.put(V5MessageDefine.MSG_DESCRIPTION, this.description);
		json.put(V5MessageDefine.MSG_THUMB_ID, this.thumb_id);
		return json.toString();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMusic_url() {
		return music_url;
	}

	public void setMusic_url(String music_url) {
		this.music_url = music_url;
	}

	public String getHq_music_url() {
		return hq_music_url;
	}

	public void setHq_music_url(String hq_music_url) {
		this.hq_music_url = hq_music_url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThumb_id() {
		return thumb_id;
	}

	public void setThumb_id(String thumb_id) {
		this.thumb_id = thumb_id;
	}


}
