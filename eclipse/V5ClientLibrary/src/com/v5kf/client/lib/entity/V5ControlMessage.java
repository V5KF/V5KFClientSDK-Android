package com.v5kf.client.lib.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.V5Util;

public class V5ControlMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5392108456567971206L;
	private int argc;
	private String argv;
	private int code;
	
	public V5ControlMessage() {
		// TODO Auto-generated constructor stub
	}
	
	public V5ControlMessage(int code, int argc, String argv) {
		this.code = code;
		this.argc = argc;
		this.argv = argv;
		this.message_type = V5MessageDefine.MSG_TYPE_CONTROL;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5ControlMessage(int code) {
		this.code = code;
		this.message_type = V5MessageDefine.MSG_TYPE_CONTROL;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5ControlMessage(JSONObject jsonMsg) throws NumberFormatException, JSONException {
		super(jsonMsg);
		this.code = jsonMsg.optInt(V5MessageDefine.MSG_CODE);
		this.argc = jsonMsg.optInt(V5MessageDefine.MSG_ARGC);
		this.argv = jsonMsg.optString(V5MessageDefine.MSG_ARGV);
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		json.put(V5MessageDefine.MSG_CODE, this.code);
		if (argc != 0) {
			json.put(V5MessageDefine.MSG_ARGC, this.argc);
			json.put(V5MessageDefine.MSG_ARGV, this.argv);
		}
		return json.toString();
	}

	public int getArgc() {
		return argc;
	}

	public void setArgc(int argc) {
		this.argc = argc;
	}

	public String getArgv() {
		return argv;
	}

	public void setArgv(String argv) {
		this.argv = argv;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}


}
