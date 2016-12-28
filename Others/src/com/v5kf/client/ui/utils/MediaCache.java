package com.v5kf.client.ui.utils;


public class MediaCache {
	private String localPath;	// 本地路径
	private long duration;		// 毫秒
	
	public String getLocalPath() {
		return localPath;
	}
	
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	
}

