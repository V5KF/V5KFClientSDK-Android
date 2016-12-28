package com.v5kf.client.lib.entity;

import org.json.JSONException;
import org.json.JSONObject;

public class V5JSONMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8853786243636263817L;
	private JSONObject json;
	
	public V5JSONMessage() {
	}
	
	public V5JSONMessage(String json) throws JSONException {
		this.json = new JSONObject(json);
//		this.create_time = DateUtil.getCurrentLongTime() / 1000;
//		this.direction = QAODefine.MSG_DIR_TO_WORKER;
	}

	public V5JSONMessage(JSONObject jsonMsg) throws NumberFormatException, JSONException {
//		super(jsonMsg);
		this.json = jsonMsg;
	}

	@Override
	public String toJson() {
		if (json == null) {
			json = new JSONObject();
		}
		return json.toString();
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
}
