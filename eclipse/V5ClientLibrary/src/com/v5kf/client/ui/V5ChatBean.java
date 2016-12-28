package com.v5kf.client.ui;

import com.v5kf.client.lib.entity.V5Message;

public class V5ChatBean {

	private V5Message message;
	// [添加]语音相关
	private boolean voicePlaying;
	private boolean loaded;
	
	public V5ChatBean(V5Message msg) {
		this.message = msg;
		this.voicePlaying = false;
	}
	
	public V5Message getMessage() {
		return message;
	}
	
	public void setMessage(V5Message message) {
		this.message = message;
	}

	public boolean isVoicePlaying() {
		return voicePlaying;
	}

	public void setVoicePlaying(boolean voicePlaying) {
		this.voicePlaying = voicePlaying;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
}
