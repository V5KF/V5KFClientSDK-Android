package com.v5kf.client.lib.entity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.V5Util;

public class V5ArticlesMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1680153002722203679L;
	private List<V5ArticleBean> articles;
	

	public V5ArticlesMessage() {
		this.articles = new ArrayList<V5ArticleBean>();
		this.message_type = V5MessageDefine.MSG_TYPE_ARTICLES;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5ArticlesMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json);
		if (null == articles) {
			articles = new ArrayList<V5ArticleBean>();
		}
		JSONArray atcArr = json.optJSONArray(V5MessageDefine.MSG_ARTICLES);
		if (atcArr != null) {
			for (int i = 0; i < atcArr.length(); i++) {
				V5ArticleBean article = new V5ArticleBean(atcArr.getJSONObject(i));
				articles.add(article);
			}
		}
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		JSONArray articlesArr = new JSONArray();
		if (null != articles) {
			for (int i = 0; i < articles.size(); i++) {
				JSONObject article = new JSONObject();
				articles.get(i).toJson(article);
				articlesArr.put(article);
			}
		}
		json.put(V5MessageDefine.MSG_ARTICLES, articlesArr);
		return json.toString();
	}

	public List<V5ArticleBean> getArticles() {
		return articles;
	}

	public void setArticles(List<V5ArticleBean> articles) {
		this.articles = articles;
	}

	
}
