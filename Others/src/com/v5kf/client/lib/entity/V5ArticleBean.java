package com.v5kf.client.lib.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class V5ArticleBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5001671670590431331L;
	private String title;
	private String pic_url;
	private String url;
	private String description;
	
	public V5ArticleBean() {
		// TODO Auto-generated constructor stub
	}
	
	public V5ArticleBean(String title, String pic_url, String url, String description) {
		this.title = title;
		this.pic_url = pic_url;
		this.url = url;
		this.description = description;
	}
	
	public V5ArticleBean(JSONObject json) {
		title = json.optString(V5MessageDefine.MSG_TITLE);
		pic_url = json.optString(V5MessageDefine.MSG_PIC_URL);
		url = json.optString(V5MessageDefine.MSG_URL);
		description = json.optString(V5MessageDefine.MSG_DESCRIPTION);
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPic_url() {
		return pic_url;
	}
	
	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void toJson(JSONObject article) throws JSONException {
		article.put(V5MessageDefine.MSG_TITLE, this.title);
		article.put(V5MessageDefine.MSG_PIC_URL, this.pic_url);
		article.put(V5MessageDefine.MSG_URL, this.url);
		article.put(V5MessageDefine.MSG_DESCRIPTION, this.description);
	}
		
}
