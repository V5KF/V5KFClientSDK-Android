package com.v5kf.client.lib.entity;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.v5kf.client.lib.V5Util;

public class V5VoiceMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3955469350716412848L;
	private String format;
	private String recognition;
	private String media_id;
	private String url;
	
	private String filePath; // 本地路径
	private long duration; 	// 时长(毫秒)
	private boolean upload; // 是否上传
	
	public V5VoiceMessage() {
		super();
		this.message_type = V5MessageDefine.MSG_TYPE_VOICE;
	}
	
	/**
	 * 上传本地语音文件(暂未支持)
	 * @param filePath
	 */
	public V5VoiceMessage(String filePath) {
		this.filePath = filePath;
		
		this.format = "amr";
		this.message_type = V5MessageDefine.MSG_TYPE_VOICE;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}
	
	public V5VoiceMessage(String url, String format, String recognition, String media_id) {
		this.url = url;
		this.format = format;
		this.recognition = recognition;
		this.media_id = media_id;
		this.message_type = V5MessageDefine.MSG_TYPE_VOICE;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5VoiceMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json);
		this.url = json.optString(V5MessageDefine.MSG_URL);
		this.format = json.optString(V5MessageDefine.MSG_FORMAT);
		this.media_id = json.optString(V5MessageDefine.MSG_MEDIA_ID);
		this.recognition = json.optString(V5MessageDefine.MSG_RECOGNITION);
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		if (!TextUtils.isEmpty(this.url)) {
			json.put(V5MessageDefine.MSG_URL, this.url);
		}
		json.put(V5MessageDefine.MSG_FORMAT, this.format);
		if (!TextUtils.isEmpty(this.recognition)) {
			json.put(V5MessageDefine.MSG_RECOGNITION, this.recognition);
		}
		if (!TextUtils.isEmpty(this.media_id)) {
			json.put(V5MessageDefine.MSG_MEDIA_ID, this.media_id);
		}
		return json.toString();
	}

	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getRecognition() {
		return recognition;
	}

	public void setRecognition(String recognition) {
		this.recognition = recognition;
	}

	public String getMedia_id() {
		return media_id;
	}

	public void setMedia_id(String media_id) {
		this.media_id = media_id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isUpload() {
		return upload;
	}

	public void setUpload(boolean upload) {
		this.upload = upload;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
}
