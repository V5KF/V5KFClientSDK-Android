package com.v5kf.client.lib;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.callback.MessageSendCallback;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.client.ui.utils.MediaLoader;

public class MessageSendHelper {
	private static final String TAG = "MessageSendHelperMd2x";
	private Context mContext;
	
	class MessageSendRunnable implements Runnable {

		private V5Message mMessage;
		private MessageSendCallback mCallback;
		
		public MessageSendRunnable(V5Message message, MessageSendCallback callback) {
			this.mMessage = message;
			this.mCallback = callback;
		}
		
		@Override
		public void run() {
			sendMessage(mMessage, mCallback);
		}
	}
	
	public MessageSendHelper(Context context) {
		this.mContext = context;
	}
	
	public void sendMessageDelay(V5Message message, MessageSendCallback callback, long delayMillis) {
		Logger.d(TAG, "[sendMessageDelay] " + delayMillis);
		if (message.getRetryCount() < 3) {
			message.addRetryCount();
			new Handler().postDelayed(new MessageSendRunnable(message, callback), delayMillis);
		} else {
			message.setRetryCount(0);
			V5ClientAgent.getInstance().sendFailedHandle(callback, message, V5ExceptionStatus.ExceptionMessageSendFailed, "retry failed");
		}
	}
	
	public void sendMessage(V5Message message, MessageSendCallback callback) {
		switch (message.getMessage_type()) {
		case V5MessageDefine.MSG_TYPE_IMAGE:
			sendImageMessage((V5ImageMessage)message, callback);
			break;
			
		case V5MessageDefine.MSG_TYPE_VOICE:
			sendVoiceMessage((V5VoiceMessage)message, callback);
			break;
			
		case V5MessageDefine.MSG_TYPE_TEXT:
		default:
			sendMessageRequest(message, callback);
			break;
		}
	}
	
	public void sendImageMessage(V5ImageMessage imageMessage, MessageSendCallback callback) {
		imageMessage.setState(V5Message.STATE_SENDING);
		
		// 判断是否本地图片
		if (imageMessage.getFilePath() != null) {
			if (imageMessage.getPic_url() == null) { // 上传图片前
				// 图片格式验证
				String type = V5Util.getImageMimeType(imageMessage.getFilePath());
				imageMessage.setFormat(type);
				if (!V5Util.isValidImageMimeType(type)) {
					V5ClientAgent.getInstance().sendFailedHandle(callback, imageMessage, V5ExceptionStatus.ExceptionMessageSendFailed, "Unsupport image mimetype");
				} else {
					postMedia(imageMessage, imageMessage.getFilePath(), callback);
				}
			} else { // 上传图片后
				// 发送消息请求
				sendMessageRequest(imageMessage, callback);
			}
		} else if (imageMessage.getPic_url() != null) {
			// 发送消息请求
			sendMessageRequest(imageMessage, callback);
		} else {
			V5ClientAgent.getInstance().sendFailedHandle(callback, imageMessage, V5ExceptionStatus.ExceptionMessageSendFailed, "Empty image message");
		}
	}
	
	public void sendVoiceMessage(V5VoiceMessage voiceMessage, MessageSendCallback callback) {
		voiceMessage.setState(V5Message.STATE_SENDING);
		
		// 判断是否本地语音
		if (voiceMessage.getFilePath() != null) {
			if (!voiceMessage.isUpload() || voiceMessage.getUrl() == null) { // 上传语音前
				long fileSize = V5Util.getFileSize(voiceMessage.getFilePath());
				Logger.d(TAG, "File(" + fileSize + "):" + voiceMessage.getFilePath());
				if (fileSize > 0) {
					if (fileSize > 4*1024*1024) { // 文件大于4M直接发送失败
						V5ClientAgent.getInstance().sendFailedHandle(callback, voiceMessage, V5ExceptionStatus.ExceptionMessageSendFailed, "Voice size too large");
						return;
					} else {
						postMedia(voiceMessage, voiceMessage.getFilePath(), callback);
					}
				} else { // 文件未写完，低版本android出现此问题
					sendMessageDelay(voiceMessage, callback, 50);
				}
			} else { // 上传语音后
				// 发送消息请求
				Logger.i(TAG, "sendVoiceMessage -> sendMessage " + voiceMessage.getState());
				sendMessageRequest(voiceMessage, callback);
			}
		} else if (voiceMessage.getUrl() != null) {
			// 发送消息请求
			sendMessageRequest(voiceMessage, callback);
		} else {
			V5ClientAgent.getInstance().sendFailedHandle(callback, voiceMessage, V5ExceptionStatus.ExceptionMessageSendFailed, "Empty voice message");
		}
	}

	/**
	 * Message消息请求发送
	 * @param msg
	 * 
	 */
	protected void sendMessageRequest(V5Message msg, MessageSendCallback callback) {
		try {			
			String json = msg.toJson();
			V5ClientAgent.getInstance().sendMessage(json);
			
			// 根据连接状态判断是否发送成功
			if (V5ClientService.isConnected()) {
				V5ClientAgent.getInstance().sendSuccessHandle(callback, msg);
			} else {
				V5ClientAgent.getInstance().sendFailedHandle(callback, msg, V5ExceptionStatus.ExceptionMessageSendFailed, "connection closed");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			V5ClientAgent.getInstance().sendFailedHandle(callback, msg, V5ExceptionStatus.ExceptionUnknownError, e.getMessage());
		}
	}
	
	/**
	 * 上传媒体资源（一步完成）
	 * /$account_id/$visitor_id/[web|app|wechat|wxqy|yixin]/$filename
	 * @param message
	 * @param file
	 */
	private void postMedia(final V5Message message, String filePath, final MessageSendCallback handler) {
		final File file = new File(filePath);
		String url = String.format(V5ClientConfig.getUploadFormatURL(), file.getName());
		String auth = V5ClientConfig.getInstance(mContext).getAuthorization();
		Logger.d(TAG, "[postMedia] fileSize:" + file.length() + " url:" + url);
		HttpUtil.postFile(message, file, url, auth, new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				// 解析地址
				Logger.i(TAG, "[postMedia] success responseString:" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject data = new JSONObject(responseString);
						String url = data.optString("url");
						String media_id = data.optString("media_id");
						if (!TextUtils.isEmpty(url)) {
							if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_IMAGE) {
								V5ImageMessage imageMessage = (V5ImageMessage)message;
								imageMessage.setPic_url(url); // 设置图片pic_url
								if (!TextUtils.isEmpty(media_id)) {
									imageMessage.setMedia_id(media_id);
								}
								sendImageMessage(imageMessage, handler);
							} else {
								if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_VOICE) {
									V5VoiceMessage voiceMessage = (V5VoiceMessage)message;
									if (!TextUtils.isEmpty(media_id)) {
										voiceMessage.setMedia_id(media_id);
									}
									voiceMessage.setUrl(url);
									voiceMessage.setUpload(true);
									// 删除临时语音文件，重命名
									String cachePath = MediaLoader.copyPathToFileCche(getContext(), file, url);
									voiceMessage.setFilePath(cachePath);
									sendVoiceMessage(voiceMessage, handler);
								} else {
									// 其他文件类型
									V5ClientAgent.getInstance().sendFailedHandle(
											handler, 
											message, 
											V5ExceptionStatus.ExceptionMessageSendFailed, 
											"Media upload error: unsupport type");
								}
							}
							return;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				V5ClientAgent.getInstance().sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						"Media upload error: response error");
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postMedia] fileSize:" + file.length() + " failure(" + statusCode + 
						") responseString:" + responseString);
				V5ClientAgent.getInstance().sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						responseString);
			}
		});
	}
	
}
