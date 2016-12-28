package com.v5kf.client.lib;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ControlMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5JSONMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MusicMessage;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;


public class V5MessageManager {
//	/**
//	 * 消息列表
//	 */
//	private List<V5Message> mMessageList = new ArrayList<V5Message>();

	private static class SingletonHolder {
		static V5MessageManager singletonHolder = new V5MessageManager();
	}
	
	public V5MessageManager() {
		
	}
	
	public static V5MessageManager getInstance() {
		return SingletonHolder.singletonHolder;
	}
	
//	protected void addV5Message(V5Message msg) {
//		mMessageList.add(msg);
//	}
//	
//	protected List<V5Message> getMessageList() {
//		return mMessageList;
//	}
//	
//	protected void clearMessageList() {
//		mMessageList.clear();
//	}
	
	/**
	 * 接收o_type为message的消息
	 * @param jsonMsg
	 * @return
	 * @throws JSONException
	 */
	public V5Message receiveMessage(JSONObject jsonMsg) throws JSONException {
		/* parse content according to message_type */
		int message_type = jsonMsg.getInt(V5MessageDefine.MESSAGE_TYPE);
		V5Message message = null;
		switch (message_type) {
		case V5MessageDefine.MSG_TYPE_TEXT:
			message = new V5TextMessage(jsonMsg);
			break;
			
		case V5MessageDefine.MSG_TYPE_LOCATION:
			message = new V5LocationMessage(jsonMsg);
			break;
			
		case V5MessageDefine.MSG_TYPE_CONTROL:
			message = new V5ControlMessage(jsonMsg);
			break;
			
		case V5MessageDefine.MSG_TYPE_ARTICLES:
			message = new V5ArticlesMessage(jsonMsg);
			break;
			
		case V5MessageDefine.MSG_TYPE_IMAGE:
			message = new V5ImageMessage(jsonMsg);
			break;
			
		case V5MessageDefine.MSG_TYPE_VOICE:
			message = new V5VoiceMessage(jsonMsg);
			break;
		
		case V5MessageDefine.MSG_TYPE_MUSIC:
			message = new V5MusicMessage(jsonMsg);
			break;
			
		default: // 不支持消息统一为V5JSONMessage
			message = new V5JSONMessage(jsonMsg);
			break;
		}
		
		return message;
	}
	
	/* 支持发送的消息格式目前有：文本、位置、图片、控制消息，今后将支持图文、语音等格式 */
	
	/**
	 * 获得文本消息对象，content不能为空
	 * @param content 不能为空
	 * @return
	 */
	public V5TextMessage obtainTextMessage(String content) {
		V5TextMessage message = new V5TextMessage(content);
		return message;
	}
	
	/**
	 * 获得位置消息对象，x,y不能为0
	 * @param x	不能为0
	 * @param y 不能为0
	 * @param scale 可为0
	 * @param label 可为null
	 * @return
	 */
	public V5LocationMessage obtainLocationMessage(double x, double y, double scale, String label) {
		V5LocationMessage message = new V5LocationMessage(x, y, scale, label);
		return message;
	}
	
	/**
	 * 获得控制消息对象，code不能为0
	 * @param code
	 * @param argc 可为0
	 * @param argv 可为null
	 * @return
	 */
	public V5ControlMessage obtainControlMessage(int code, int argc, String argv) {
		V5ControlMessage message = new V5ControlMessage(code, argc, argv);
		return message;
	}
	
	/**
	 * 发送网络图片
	 * @param pic_url
	 * @param media_id 微信媒体id 可为空
	 * @return
	 */
	public V5ImageMessage obtainImageMessage(String pic_url, String media_id) {
		V5ImageMessage message = new V5ImageMessage(pic_url, media_id);
		return message;
	}
	
	/**
	 * 发送本地图片，提供图片路径
	 * @param filePath
	 * @return
	 */
	public V5ImageMessage obtainImageMessage(String filePath) {
		V5ImageMessage message = new V5ImageMessage(filePath);
		return message;
	}
	
	/**
	 * 发送本地语音，提供语音路径
	 * @param filePath
	 * @return
	 */
	public static V5VoiceMessage obtainVoiceMessage(String filePath) {
		V5VoiceMessage message = new V5VoiceMessage(filePath);
		return message;
	}
	
	public V5JSONMessage obtainJSONMessage(JSONObject json) {
		V5JSONMessage message = null;
		try {
			message = new V5JSONMessage(json);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return message;
	}
	
}
