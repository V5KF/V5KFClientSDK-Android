package com.v5kf.client.lib.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class V5TextMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6423596299568663146L;
	private String content;
	
	public V5TextMessage() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	public V5TextMessage(String _content) {
		this();
		this.content = _content;
		this.message_type = V5MessageDefine.MSG_TYPE_TEXT;
	}

	public V5TextMessage(JSONObject jsonMsg) throws NumberFormatException, JSONException {
		super(jsonMsg);
		this.content = jsonMsg.optString(V5MessageDefine.MSG_CONTENT);
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		json.put(V5MessageDefine.MSG_CONTENT, this.content);
		return json.toString();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
