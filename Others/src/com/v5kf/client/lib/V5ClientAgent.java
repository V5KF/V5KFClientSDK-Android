package com.v5kf.client.lib;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.callback.MessageSendCallback;
import com.v5kf.client.lib.callback.OnGetMessagesCallback;
import com.v5kf.client.lib.callback.V5InitCallback;
import com.v5kf.client.lib.callback.V5MessageListener;
import com.v5kf.client.lib.entity.V5ControlMessage;
import com.v5kf.client.lib.entity.V5JSONMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.client.ui.ClientChatActivity;
import com.v5kf.client.ui.callback.OnChatActivityListener;
import com.v5kf.client.ui.callback.OnLocationMapClickListener;
import com.v5kf.client.ui.callback.OnURLClickListener;
import com.v5kf.client.ui.callback.UserWillSendMessageListener;
import com.v5kf.client.ui.keyboard.EmoticonsUtils;
import com.v5kf.client.ui.utils.ImageLoader;
import com.v5kf.client.ui.utils.MediaLoader;
import com.v5kf.client.ui.utils.URLCache;

public class V5ClientAgent {
	public static final String TAG = "ClientAgent";
	public static final long OPEN_QUES_MAX_ID = 9999999999L;
	
	/**
	 *  1.1.8_r0918 -> v1.1.8_r1115 -> 1.1.9优化hotques请求限制为一次
	 *  1.1.11 优化网络请求服务器地址,解决attr命名重复问题
	 *  1.2.0_r170412 增加openId替代uid，magic新消息接口，坐席开场白，结构优化
	 */
	public final static String VERSION = "1.2.0"; 
	/**
	 * v1.1.9_r1115
	 * v1.1.10_r170207
	 * v1.1.11_r170208
	 * v1.1.12_r170213
	 */
	public final static String VERSION_DESC = "v1.1.12_r170213"; // v1.1.8_r0918 -> v1.1.8_r1115 -> 1.1.9
	private static boolean isSDKInit = false;
	private int isForeground = 0;
	
	private Context mContext;
	private V5MessageListener mMessageListener;
	private OnGetMessagesCallback mGetMessagesCallback;
	private Handler mHandler;
	private DBHelper mDBHelper;
	private V5ConfigSP mConfigSP;
	private long mSessionStart = 0;
	protected boolean cacheLocalMsg;
//	public ClientOpenMode mOpenMode;
//	public String mOpenQuestion;
	protected long mMsgIdCount = 0;	// 开场问题Id[已失效]
	private boolean _authBlock = false;
	private boolean _getSiteInfo = false;
	private JSONArray _magic; 
	
	/* Activity监听器(UI库) */
	private OnURLClickListener mURLClickListener;
	private OnLocationMapClickListener mLocationMapClickListener;
	private OnChatActivityListener mChatActivityListener;
	private UserWillSendMessageListener mUserWillSendMessageListener;
	
	public enum ClientOpenMode {
		clientOpenModeDefault,	// 默认开场白方式（以设置的param参数为开场白，无消息记录显示默认开场白）
		clientOpenModeQuestion,	// 自定义开场白，设置开场问题获得对应开场白（此模式不可与优先人工客服同用，否则将失效）
		clientOpenModeNone,		// 无开场白方式，有则显示历史消息
		clientOpenModeAutoHuman; // 开场自动转人工客服
		
		public static ClientOpenMode getMode(int index) {
        	switch (index) {
        	case 0:
        		return clientOpenModeDefault;
        	case 1:
        		return clientOpenModeQuestion;
        	case 2:
        		return clientOpenModeNone;
        	case 3:
        		return clientOpenModeAutoHuman;
        	default:
        		return clientOpenModeDefault;
        	}
        }
		
		public static int getIndex(ClientOpenMode mode) {
			switch (mode) {
			case clientOpenModeDefault:
				return 0;
			case clientOpenModeQuestion:
				return 1;
			case clientOpenModeNone:
				return 2;
			case clientOpenModeAutoHuman:
				return 3;
			}
			return 0;
		}
	};
	
	public enum ClientServingStatus {
		clientServingStatusRobot, // 机器人服务
		clientServingStatusQueue, // 排队中（等待人工客服，当前为机器人服务）
		clientServingStatusWorker, // 人工服务
		clientServingStatusInTrust; // 人工交给机器人托管
		
        public static ClientServingStatus getStatus(int index) {
        	switch (index) {
        	case 0:
        		return clientServingStatusRobot;
        	case 1:
        		return clientServingStatusQueue;
        	case 2:
        		return clientServingStatusWorker;
        	case 3:
        		return clientServingStatusInTrust;
        	default:
        		return clientServingStatusRobot;
        	}
        }
	};
	
	public enum ClientLinkType {
		clientLinkTypeURL,		// 文本链接
		clientLinkTypeArticle	// 图文链接
//		clientLinkTypeNumber 	// 数字链接
	}
	
	private static class SingletonHolder {
		private static final V5ClientAgent singletonHolder = new V5ClientAgent();
	}
	
	private V5ClientAgent() {
//		Logger.d(TAG, "V5ClientAgent instance");
//		if (Looper.myLooper() != null  && V5ClientConfig.CALLBACK_ON_UI_THREAD) {
//			mHandler = new Handler(Looper.myLooper());
//			Logger.i(TAG, "The callbak method of MessageListener will run in the current UI thread");
//		} else {
//			Logger.i(TAG, "The callbak method of MessageListener will run in another thread");
//		}
	}
	
	public static V5ClientAgent getInstance() {
		return SingletonHolder.singletonHolder;
	}
	
	public static String getVersion() {
		return VERSION;
	}
	
	/**
	 * 在Application的onCreate中进行初始化，从AndroidManifest.xml读取配置信息
	 * @param conetxt
	 * @param callback
	 */
	public static void init(Context context, V5InitCallback callback) {
		if (context == null) {
			if (callback != null) {
				callback.onFailure("SDK auth failed: context null");
			}
			isSDKInit = false;
			return;
		}
		
		V5ClientConfig config = V5ClientConfig.getInstance(context);
		String siteId = config.getSiteId();
		String appID = config.getAppID();
		V5ConfigSP configSP = new V5ConfigSP(context);
		if (configSP.readSDKAuthFlag()) {
			if (configSP.readSiteId() != null && 
					!configSP.readSiteId().equals(siteId)) {
				configSP.removeSDKAuthFlag();
				configSP.removeUid();
				configSP.removeOpenId();
				String vid = configSP.readVisitorId();
				if (vid != null) {
					configSP.removeAuthorization(vid);
					configSP.removeVisitorId();
				}
				isSDKInit = false;
			} else {
				config.setSiteId(siteId);
				config.setAppID(appID);
				isSDKInit = true;
				if (callback != null) {
					callback.onSuccess("SDK auth success");
				}
				return;
			}
		}
		if (!isSDKInit) {
			configSP.saveSiteId(siteId);
			config.setAppID(appID);
			try {
				doSDKAuth(context, siteId, appID, callback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			if (callback != null) {
				callback.onSuccess("SDK auth success");
			}
		}
	}

	/**
	 * 在Application的onCreate中进行初始化，通过代码填写配置信息
	 * @param conetxt
	 * @param appkey
	 * @param siteId
	 * @param siteAccount
	 * @param callback
	 */
	public static void init(Context context, String siteId, String appID, V5InitCallback callback) {
		if (null == siteId || appID == null || context == null) {
			if (callback != null) {
				callback.onFailure("SDK auth failed: param null");
			}
			isSDKInit = false;
			return;
		}
		
		V5ConfigSP configSP = new V5ConfigSP(context);
		V5ClientConfig config = V5ClientConfig.getInstance(context);
		if (configSP.readSDKAuthFlag()) {
			if (configSP.readSiteId() != null && 
					!configSP.readSiteId().equals(siteId)) {
				configSP.removeSDKAuthFlag();
				configSP.removeUid();
				configSP.removeOpenId();
				String vid = configSP.readVisitorId();
				if (vid != null) {
					configSP.removeAuthorization(vid);
					configSP.removeVisitorId();
				}
				isSDKInit = false;
			} else {
				config.setSiteId(siteId);
				config.setAppID(appID);
				isSDKInit = true;
				if (callback != null) {
					callback.onSuccess("SDK auth success");
				}
				return;
			}
		}
		if (!isSDKInit) {
			config.setSiteId(siteId);
			config.setAppID(appID);
			try {
				doSDKAuth(context, siteId, appID, callback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			if (callback != null) {
				callback.onSuccess("SDK auth success");
			}
		}
	}

	/**
	 * SDK授权认证
	 * @param appkey
	 * @param siteId
	 * @param siteAccount
	 * @param appID
	 * @param callback
	 */
	private static void doSDKAuth(final Context context, String siteId, String appID,
			final V5InitCallback callback) throws JSONException {
		// 表情模块初始化
		EmoticonsUtils.initEmoticonsDB(context);
		if (V5ClientConfig.SKIP_INIT) {
			isSDKInit = true;
			if (callback != null) {
				callback.onSuccess("SDK auth success");
			}
		}
		
		if (null == siteId || null == appID) {
			if (!V5ClientConfig.SKIP_INIT) {
				isSDKInit = false;
			}
			if (callback != null) {
				callback.onFailure("SDK auth failed: param invalid");
			}
			return;
		}
		
		JSONObject json = new JSONObject();
		json.put("package", context.getApplicationInfo().packageName);
		try {
			json.put("app_name", getApplicationName(context));
		} catch (Exception e) {
			// pass
		}
		json.put("site_id", siteId);
		json.put("app_id", appID);
		json.put("platform", "android");
		Logger.d(TAG, "<Init request>: " + json.toString());
		HttpUtil.post(
				V5ClientConfig.getSDKInitURL(), 
				json.toString(), 
				new HttpResponseHandler(context) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				responseString = V5Util.decodeUnicode(responseString);
				Logger.d(TAG, "<Init response>: " + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int o_error = js.getInt("o_error");
					if (o_error == 0) { // 成功返回
						isSDKInit = true;
						V5ConfigSP configSP = new V5ConfigSP(context);
						configSP.saveSDKAuthFlag(true);
						int appPush = js.optInt("app_push");
						String appTitle = js.optString("app_title");
						String version = js.optString("version");
						String versionInfo = js.optString("version_info");

						configSP.saveAppPush(appPush);
						if (null != appTitle && !appTitle.isEmpty()) {
							configSP.saveNotificationTitle(appTitle);
						}
						if (VERSION.compareTo(version) < 0) {
							Logger.w(TAG, "V5 SDK info:" + versionInfo);
						}
						if (callback != null && !V5ClientConfig.SKIP_INIT) {
							callback.onSuccess("SDK auth success");
						}
					} else if (js.has("o_errmsg")) {
						String errmsg = js.getString("o_errmsg");
						Logger.e(TAG, "V5 SDK init failed(code:" + o_error + "):" + errmsg);
						if (!V5ClientConfig.SKIP_INIT) {
							isSDKInit = false;
						}
						if (callback != null && !V5ClientConfig.SKIP_INIT) {
							callback.onFailure("SDK auth failed: " + errmsg);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "V5 SDK init failed(code:" + statusCode + "):" + responseString);
				if (!V5ClientConfig.SKIP_INIT) {
					isSDKInit = false;
				}
				if (callback != null && !V5ClientConfig.SKIP_INIT) {
					callback.onFailure("SDK auth failed: " + responseString);
				}
			}
		});
	}

	private static String getApplicationName(Context context) { 
		PackageManager packageManager = null; 
		ApplicationInfo applicationInfo = null; 
		try { 
			packageManager = context.getPackageManager(); 
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0); 
		} catch (PackageManager.NameNotFoundException e) { 
			applicationInfo = null; 
		} 
		String applicationName = 
		(String) packageManager.getApplicationLabel(applicationInfo); 
		return applicationName; 
	}
	
	/**
	 * 是否连接
	 * @return
	 */
	public static boolean isConnected() {
		return V5ClientService.isConnected();
	}

	/**
	 * 上线。服务初始化
	 * @param context
	 */
	public void start(Context context, V5MessageListener listener) {
		if (null == context || null == listener) {
			Logger.e(TAG, "[V5ClientAgent->start] param null");
			return;
		}
		if (Looper.myLooper() != null && V5ClientConfig.CALLBACK_ON_UI_THREAD) {
			mHandler = new Handler(Looper.myLooper());
			Logger.i(TAG, "The callbak method of MessageListener will run in the current UI thread");
		} else {
			Logger.i(TAG, "The callbak method of MessageListener will run in another thread");
		}
		setMessageListener(listener);
		setContext(context);
		V5ClientConfig config = V5ClientConfig.getInstance(context);
		if (mConfigSP == null) {
			mConfigSP = new V5ConfigSP(context);
		}
		cacheLocalMsg = V5ClientConfig.UI_SUPPORT ? true : mConfigSP.readLocalDbFlag();
		if (mDBHelper == null && cacheLocalMsg) {
			mDBHelper = new DBHelper(context);
			try {
				mDBHelper.setTableName("v5_message_" + V5Util.hash(config.getV5VisitorId()));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				mDBHelper.setTableName("v5_message_" + config.getV5VisitorId());
			}
		}

		if (getConfig().getUserInfo() != null && getConfig().getUserInfo().length() > 0) {
			JSONArray customArray = new JSONArray();
            Iterator<String> it = getConfig().getUserInfo().keys();
            try {
	            while (it.hasNext()) {  
	                String key = (String) it.next();  
	                String value = getConfig().getUserInfo().getString(key);
	                JSONObject item = new JSONObject();
	                item.putOpt("key", key);
	                item.putOpt("val", value);
	                customArray.put(item);
	            }
				_magic = customArray;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (mConfigSP.readAuthorization(config.getV5VisitorId()) == null) {
//			Logger.v(TAG, "[start] initialization - should do auth");
			updateSiteInfo(); // 更新auth同时更新站点信息
			try {
				doAccountAuth();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
//			Logger.v(TAG, "[start] already auth - start client");
			onClientStart();
		}
		mSessionStart = 1;
	}

	/**
	 * 重连websocket
	 */
	public void reconnect() {
		if (null != mContext) {
			V5ClientService.reConnect(mContext);
		} else {
			Logger.e(TAG, "V5ClientAgent got null context! Please do V5ClientAgent.getInstance().start()");
		}
	}
	
	/**
	 * 检查websocket连接，未连接自动重连
	 */
	public void checkConnect() {
		if (!V5ClientAgent.isConnected() && null != mContext) {
			V5ClientService.reConnect(mContext);
		}
	}

	/**
	 * 使用UI接口调用该方法开启对话界面
	 * @param context
	 */
	public void startV5ChatActivity(Context context) {
		setContext(context);
		Intent chatIntent = new Intent(context, ClientChatActivity.class);
		chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(chatIntent);
	}

	/**
	 * 使用UI接口调用该方法开启对话界面
	 * @param context
	 */
	public void startV5ChatActivityWithBundle(Context context, Bundle bundle) {
		setContext(context);
		Intent chatIntent = new Intent(context, ClientChatActivity.class);
		chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		chatIntent.putExtras(bundle);
		context.startActivity(chatIntent);
	}
	
	/**
	 * 服务退出判断
	 * @return
	 */
	protected boolean isExit() {
		return mSessionStart == 0;
	}

	/**
	 * 账号认证
	 * @param context
	 * @throws JSONException
	 */
	protected void doAccountAuth() throws JSONException {
		if (_authBlock) {
			Logger.w(TAG, "Already in account auth...");
			return;
		}
		_authBlock = true;
		JSONObject json = new JSONObject();
		V5ClientConfig config = V5ClientConfig.getInstance(mContext);
		json.put("site", config.getSiteId());
		if (!TextUtils.isEmpty(config.getSiteAccount())) {
			json.put("account", config.getSiteAccount());
		}
		if (!TextUtils.isEmpty(config.getAppID())) {
			json.put("app_id", config.getAppID());
			json.put("account", config.getAppID());
		}
		json.put("visitor", config.getV5VisitorId()); // 获得用户唯一ID
		json.put("device", "android");
		String device_token = config.getDeviceToken();
		if (device_token != null) {
			json.put("dev_id", device_token);
		} else {
			Logger.v(TAG, "device_token not set!");
		}
		json.put("expires", 604800); // 一次Auth为7天有效期
		if (null != config.getNickname()) {
			json.put("nickname", config.getNickname());
		}
		if (0 != config.getGender()) {
			json.put("gender", config.getGender());
		}
		if (null != config.getAvatar()) {
			json.put("avatar", config.getAvatar());
		}
		if (0 != config.getVip()) {
			json.put("vip", config.getVip());
		}
		if (mDBHelper != null) { // 更新表名：v5_message_hash(visitor_id)
			try {
				mDBHelper.setTableName("v5_message_" + V5Util.hash(config.getV5VisitorId()));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				mDBHelper.setTableName("v5_message_" + config.getV5VisitorId());
			}
		}
		Logger.d(TAG, "Auth request:" + json.toString());
		HttpUtil.post(V5ClientConfig.getAccountAuthURL(), json.toString(), new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.d(TAG, "[auth] statusCode=" + statusCode + " responseString=" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject js = new JSONObject(responseString);
						if (js.has("authorization")) { // 成功返回authorization
							long expires = js.getLong("expires");
							long timestamp = js.getLong("timestamp");
							V5ClientConfig config = V5ClientConfig.getInstance(mContext);
							config.setExpires(expires);
							config.setTimestamp(timestamp);
							String auth = js.optString("authorization");
							if (auth != null && !auth.isEmpty()) {
								config.setAuthorization(auth);
							}
							onClientStart();
						} else if (js.has("o_error")) {
							int code = js.getInt("o_error");
							String desc = js.optString("o_errmsg");
							if (isSDKInit) {
								errorHandle(new V5KFException(V5ExceptionStatus.ExceptionAccountFailed, "[" + code + "]" + desc));
							} else {
								Logger.e(TAG, "start(): init SDK not success, please check the initialization");
								errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNotInitialized, "init not success"));
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						errorHandle(new V5KFException(V5ExceptionStatus.ExceptionAccountFailed, "JSONException:" + e.getMessage()));
					}
				} else {
					errorHandle(new V5KFException(V5ExceptionStatus.ExceptionConnectionError, "Connect error, Auth failed."));
				}
				_authBlock = false;
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.w(TAG, "doAuth->onFailure(" + statusCode + "): " + responseString);
				switch (statusCode) {
				case -10:
					errorHandle(new V5KFException(V5ExceptionStatus.ExceptionSocketTimeout, "Socket timeout, Auth failed."));
					break;
				case -14: // 无网络连接
					errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNoNetwork, "Unable to resolve host, Auth failed."));
					break;
				default:
					errorHandle(new V5KFException(V5ExceptionStatus.ExceptionAccountFailed, "Connect error, Auth failed."));
					break;
				}
				
				_authBlock = false;
			}
		});
	}
	
	/**
	 * 接口开启
	 * @param context
	 */
	private void onClientStart() {
		startClientService();
	}

	
//	/**
//	 * 获得会话初始标志
//	 * @return
//	 */
//	protected boolean isSessionStart() {
//		if (mContext == null) {
//			errorHandle(new V5KFException(V5KFException.ERR_CODE_NOT_START, "Client not start, Please start by V5ClientAgent.getInstance().start"));
//			return false;
//		}
//		if (mConfigSP == null) {
//			mConfigSP = new V5ConfigSP(mContext);
//		}
//		return mConfigSP.readSessionFlag();
//	}
//	
//	/**
//	 * 保存会话初始标志
//	 * @param flg
//	 */
//	protected void setSessionStart(boolean flg) {
//		if (mContext == null) {
//			errorHandle(new V5KFException(V5KFException.ERR_CODE_NOT_START, "Client not start, please start by V5ClientAgent.getInstance().start"));
//			return;
//		}
//		if (mConfigSP == null) {
//			mConfigSP = new V5ConfigSP(mContext);
//		}
//		mConfigSP.saveSessionFlag(flg);
//	}
	
	protected long getCurrentSessionStart() {
		return mSessionStart;
	}
	
	/**
	 * 获得开场消息
	 * @param mode
	 * @param param
	 */
	public void getOpeningMessage(ClientOpenMode mode, String param) {
		if (mode == ClientOpenMode.clientOpenModeQuestion
				&& param != null) {
			V5TextMessage openQuestion = V5MessageManager.getInstance().obtainTextMessage(param);
			mMsgIdCount++;
			openQuestion.setMsg_id(V5Util.getCurrentLongTime()/1000);
			sendOpeningQuestion(openQuestion);
		} else if (mode == ClientOpenMode.clientOpenModeDefault) {
			// 没有获得会话消息则获取开场白
			if (param != null && !param.isEmpty()) {
				sendPrologue(param);
			} else {
				if (_getSiteInfo || !TextUtils.isEmpty(getConfig().getRobotName())) {
					// 已获取站点信息直接发送开场白
					sendPrologue(null);
				} else {
					// 获取站点信息后再sendPrologue
					getSiteInfo(true);
				}
			}
		} else if (mode == ClientOpenMode.clientOpenModeAutoHuman) {
			// 自动转人工客服
			switchToArtificialService(null);
		}
	}
	
	public void updateSiteInfo() {
		getSiteInfo(false);
	}
	
	/**
	 * 获得站点信息
	 * @param context
	 */
	private void getSiteInfo(final boolean callback) {
		if (mContext == null) {
			Logger.e(TAG, "Client not start, please start by V5ClientAgent.getInstance().start");
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		HttpUtil.get(V5Util.getSiteInfoUrl(mContext), new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.i(TAG, "responseString:" + V5Util.decodeUnicode(responseString));
				try {
					JSONObject json = new JSONObject(V5Util.decodeUnicode(responseString));
					if (json.getString("state").equals("ok")) {
						_getSiteInfo = true;
						JSONObject robot = json.getJSONObject("robot");
						JSONObject info = json.getJSONObject("info");
						String robotIntro = robot.optString("intro");
						String workerIntro = info.optString("intro");
						getConfig().setWorkerIntro(workerIntro);
						getConfig().setRobotIntro(robotIntro);
						getConfig().setRobotName(robot.optString("name"));
						getConfig().setRobotPhoto(robot.optString("logo"));
						if (callback) {
							sendPrologue(null);
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (callback) {
					sendPrologue(null);
					return;
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "statusCode:" + statusCode + " responseString:" + responseString);
				if (callback) {
					sendPrologue(null);
					return;
				}
			}
		});
	}
	
	private void sendPrologue(String prologue) {
		String intro = null;
		if (!TextUtils.isEmpty(prologue)) {
			intro = prologue;
		} else {
			if (getConfig().getWorkerType() == 2 && 
					!TextUtils.isEmpty(getConfig().getWorkerIntro())) {
				intro = getConfig().getWorkerIntro();
				intro = intro.replaceAll("\\[@nickname\\]", getConfig().getWorkerName());
			} else if (!TextUtils.isEmpty(getConfig().getRobotIntro())) {
				intro = getConfig().getRobotIntro();
			}
		}
		if (TextUtils.isEmpty(intro)) {
			return;
		}
		V5TextMessage msg = V5MessageManager.getInstance().obtainTextMessage(intro);
		msg.setDirection(V5MessageDefine.MSG_DIR_FROM_ROBOT);
		msg.setSession_start(mSessionStart);
		msg.setMsg_id(V5Util.getCurrentLongTime()/1000);
//		if (null != mDBHelper && cacheLocalMsg) { // 保存开场白
//			mDBHelper.insert(msg);
//		}
		if (null != mHandler) { // 将开场白回调到message消息接口
			mHandler.post(new OnMessageRunnable(msg));
		} else if (getMessageListener() != null) {
			getMessageListener().onMessage(msg);
		}
	}
	
	/**
	 * 启动WS服务
	 */
	private void startClientService() {
		if (mContext == null) {
			Logger.e(TAG, "Client not start, please start by V5ClientAgent.getInstance().start");
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		Intent i = new Intent(mContext, V5ClientService.class);
		mContext.startService(i);
	}
	
	protected void onAppGoForeGround() {
		Logger.v(TAG, "[onAppGoForeGround]");
//		V5ClientConfig.NOTIFICATION_SHOW = false;
		if (mContext == null) {
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		// 清除本应用的通知
		V5ClientConfig.getNotificationManager(mContext).cancel(V5ClientConfig.getNotifyId(mContext));
		if (V5ClientService.isConnected()) {
			sendOnLineMessage();
			
			// [修改]点击通知 重新获取消息
			/* onStart -> 刷新最新会话数据 */
			this.updateMessages();
		} else if (mSessionStart > 1) {
			reconnect();
//			Intent i = new Intent(mContext, V5ClientService.class);
//			mContext.startService(i);
		}
	}
	
	protected void onAppGoBackground() {
		Logger.v(TAG, "[onAppGoBackground]");
//		V5ClientConfig.NOTIFICATION_SHOW = true;
		if (mConfigSP != null && mConfigSP.readAppPush() == 0) {
			return;
		}
		if (mContext == null) {
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		if (V5ClientService.isConnected()) {
			sendOffLineMessage();
		}
	}
	
	/**
	 * 调用服务的Activity的onStart方法中调用(用于通知发送判断)
	 */
	public void onStart() {
		isForeground++;
//		Logger.d(TAG, "<onStart> isForeground:" + isForeground);
		if (isForeground > 1) {
			return;
		} else if (isForeground < 1) {
			Logger.e(TAG, "V5CientAgent -> onStop() not match onStart()");
			return;
		}
		onAppGoForeGround();
	}

	/**
	 * 调用服务的Activity的onStop方法中调用(用于通知发送判断)
	 */
	public void onStop() {
		isForeground--;
//		Logger.d(TAG, "<onStop> isForeground:" + isForeground);
		if (isForeground > 0) {
			return;
		} else if (isForeground < 0) {
			Logger.e(TAG, "V5CientAgent -> onStop() not match onStart()");
			return;
		}
		onAppGoBackground();
	}
	
	public boolean isForeground() {
		return isForeground > 0;
	}
	
	/**
	 * 下线。退出服务，停止接收消息，通知服务端消息发到对方推送服务器
	 */
	public void onDestroy() {
		mSessionStart = 0;
//		V5ClientService.stop();
		if (mContext != null) {
			Logger.w(TAG, "[onDestroy] -> stopService");
			Intent i = new Intent(mContext, V5ClientService.class);
			mContext.stopService(i);
			
			Intent stopIntent = new Intent(V5ClientService.ACTION_STOP);
			// 通过广播发送给ws服务
			mContext.sendBroadcast(stopIntent);
		}

		mMessageListener = null;
		mContext = null;
	}
	
	/**
	 * 添加用户信息（可在对话过程中添加，但目前坐席端需要刷新才生效，所以不推荐）
	 * @param info
	 */
	public void addUserInfo(JSONObject info) {
		if (info == null || info.length() == 0) {
			return;
		}
		JSONArray customArray = new JSONArray();
        Iterator<String> it = info.keys();
        try {
            while (it.hasNext()) {  
                String key = (String) it.next();  
                String value = info.getString(key);
                JSONObject item = new JSONObject();
                item.putOpt("key", key);
                item.putOpt("val", value);
                customArray.put(item);
            }
            if (isConnected()) {
            	JSONObject json = new JSONObject();
    			try {
    				json.put("magic_arr", customArray);
    				sendMagicInfo(json);
    			} catch (JSONException e) {
    				e.printStackTrace();
    			}
            } else {
            	_magic = customArray;
            }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送magic信息
	 */
	protected void sendMagicInfo(JSONObject info) {
		try {
			JSONObject json = new JSONObject();
			json.put("o_type", "message");
			json.put("message_type", 26);
			json.put("code", 2);
			json.put("mjson", info);
			V5JSONMessage jsonMsg = new V5JSONMessage(json);
			sendMessage(jsonMsg.toJson());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送消息
	 * @param message
	 */
	public void sendMessage(V5Message message, MessageSendCallback handler) {
		if (mContext == null) {
			Logger.e(TAG, "Client not start, please start by V5ClientAgent.getInstance().start");
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		if (message.getMsg_id() == 0) {
			message.setMsg_id(V5Util.getCurrentLongTime());
		}
		message.setState(V5Message.STATE_SENDING);
		MessageSendHelper mMessageSendHelper = new MessageSendHelper(mContext);
		mMessageSendHelper.sendMessage(message, handler);
	}

	private void sendOpeningQuestion(V5Message openQues) {
		try {
			this.sendMessage(openQues.toJson());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendMessage(String json) {
		if (mContext == null) {
			Logger.e(TAG, "[sendMessage] mContext null");
			return;
		}
		
		//Logger.d(TAG, "sendMessage:" + json);
		Intent sendIntent = new Intent();
		sendIntent.putExtra("v5_message", json);
		sendIntent.setAction(V5ClientService.ACTION_SEND);
		// 通过广播发送给ws服务
		mContext.sendBroadcast(sendIntent);
	}
	
	/**
	 * 指定分组客服
	 * @param gid
	 * @param wid
	 */
	public void transferHumanService(int gid, int wid) {
		String argv = String.format(Locale.getDefault(), "%d %d", gid, wid);
		V5ControlMessage cmsg = V5MessageManager.getInstance().obtainControlMessage(1, 2, argv);
		sendMessage(cmsg, null);
	}

	/**
	 * 转人工客服
	 */
	public void switchToArtificialService(MessageSendCallback handler) {
		V5Message msg = V5MessageManager.getInstance().obtainControlMessage(1, 0, null);
		sendMessage(msg, handler);
	}
	
	/**
	 * 发上线消息
	 */
	protected void sendOnLineMessage() {
		V5Message msg = V5MessageManager.getInstance().obtainControlMessage(100, 0, null);
		try {
			sendMessage(msg.toJson());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mSessionStart = V5Util.getCurrentLongTime();
	}
	
	/**
	 * 发下线消息
	 */
	protected void sendOffLineMessage() {
		V5Message msg = V5MessageManager.getInstance().obtainControlMessage(101, 0, null);
		try {
			sendMessage(msg.toJson());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 按照指定位置请求指定消息数量历史消息
	 * @param context
	 * @param offset 请求起始位置
	 * @param size	最多返回消息数
	 * @param callback 返回消息回调
	 */
	public void getMessages(Context context, int offset, int size, OnGetMessagesCallback callback) {
		if (mDBHelper == null) {
			mDBHelper = new DBHelper(context);
		}
		new Thread(new GetLocalMessageRunnable(offset, size, callback)).start();
	}
	
	/**
	 * 更新会话消息，重新连接后调用，获得新的消息后会回调onConnect
	 */
	protected void updateMessages() {
		this.getOfflineMessages(0, 0, null);
		this.getStatus();
	}
	
	/**
	 * 获取会话状态 status = 0-机器人服务  1-排队等待 2-人工客服 3-机器人托管
	 * nickname, photo, gender 表示座席信息
	 */
	public void getStatus() {
		//Logger.d(TAG, "[getStatus]");
		try {
			JSONObject json = new JSONObject();
			json.put("o_type", "session");
			json.put("o_method", "get_status");
			V5JSONMessage jsonMsg = new V5JSONMessage(json);
			sendMessage(jsonMsg.toJson());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
 	
	/**
	 * 向服务器获取离线未读消息
	 * @param offset
	 * @param size
	 * @param callback
	 */
	public void getOfflineMessages(int offset, int size, OnGetMessagesCallback callback) {
		//Logger.d(TAG, "[updateMessages -> getCurrentMessages]");
		setGetMessagesCallback(callback);
		try {
			JSONObject json = new JSONObject();
			json.put("o_type", "session");
			json.put("o_method", "get_messages");
			json.put("size", size);
			json.put("offset", offset);
			V5JSONMessage jsonMsg = new V5JSONMessage(json);
			sendMessage(jsonMsg.toJson());
//			sendMessage(jsonMsg, new MessageSendCallback() {
//				
//				@Override
//				public void onSuccess(V5Message message) {
//					
//				}
//				
//				@Override
//				public void onFailure(V5Message message, int statusCode, String desc) {
//					errorHandle(new V5KFException(statusCode, desc));
//				}
//			});
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 清空本地历史消息记录
	 * @param context not null
	 */
	public void clearLocalHistoricalMessages(Context context) {
		if (mDBHelper == null) {
			mDBHelper = new DBHelper(context);
		}
		mDBHelper.delAll();
	}
	
	/**
	 * 清空客户认证缓存，清空图片、语音等媒体缓存
	 * @param context
	 */
	public static void clearCache(Context context) {
		V5ConfigSP csp = new V5ConfigSP(context);
		csp.clearCache();
		
		try {
			ImageLoader imgLoader = new ImageLoader(context, true, 0);
			imgLoader.clearCache();
			MediaLoader mediaLoader = new MediaLoader(context);
			mediaLoader.clearCache();
			URLCache urlCache = new URLCache();
			urlCache.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected Context getContext() {
		return mContext;
	}

	protected void setContext(Context context) {
		mContext = context;
	}
	
	protected Handler getHandler() {
		return mHandler;
	}
	
	public V5MessageListener getMessageListener() {
		return mMessageListener;
	}

	public void setMessageListener(V5MessageListener mListener) {
		mMessageListener = mListener;
	}
	
	/**
	 * 错误处理
	 * @param err_code
	 */
	protected void errorHandle(V5KFException ex) {
		if (null != mHandler) {
			mHandler.post(new OnErrorRunnable(ex));
		} else if (null != getMessageListener()) {
			getMessageListener().onError(ex);
		}
	}
	
//	/**
//	 * poll返回错误码处理
//	 * @param code
//	 */
//	protected void wsErrorHandler(int code, String desc) {
//		if (code == 0) { // 正常返回
//			return;
//		}
//		if (mContext == null) {
//			Logger.e(TAG, "V5ClientAgent got null context! Please do start again");
//			return;
//		}
//		
//		/* 非0为有错误 */
//		switch (code) {
//		case 50004:		// account disable
//		case 50005: 	// account failed
//		case 50010: {	// session closed
//			// URL失效、会话结束都需要重新认证获取会话URL
//			if (mConfigSP == null) {
//				mConfigSP = new V5ConfigSP(mContext);
//			}
//			mConfigSP.saveSessionStart(0);
//			try {
//				doAccountAuth();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			break;
//		}
//		case 50008:		// request timeout
//			// 超时则进行下一轮请求，无需处理
//			return;
//		}
//			
//		if (null != mHandler) {
//			mHandler.post(new OnErrorRunnable(new V5KFException(code, desc)));
//		} else if (getMessageListener() != null) {
//			getMessageListener().onError(new V5KFException(code, desc));
//		}
//	}
//
//	/**
//	 * 发送消息返回错误码处理
//	 * @param code
//	 * @param desc
//	 */
//	protected void sendEerrorHandler(V5Message message, int code, String desc) {
//		if (code == 0) { // 正常返回
//			return;
//		}
//		if (mContext == null) {
//			Logger.e(TAG, "V5ClientAgent got null context! Please do start again");
//			return;
//		}
//		
//		/* 非0为有错误 */
//		switch (code) {
//		case 50004: 	// account failed
//		case 50007:		// URL错误
//		case 50011:		// 无效会话ID
//		case 50010: {	// session closed
//			// URL失效、会话结束都需要重新认证获取会话URL
//			if (mConfigSP == null) {
//				mConfigSP = new V5ConfigSP(mContext);
//			}
//			mConfigSP.saveSessionStart(0);
//			try {
//				doAccountAuth();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			break;
//		}
//		case 50008:	// request timeout
//			break;
//		case 50005: // 错误的请求域
//			
//			break;
//		case 50006: // 内部错误
//		
//			break;
//		}
//		
//		// 无需回调到onError接口，已回调到MessageSendHandler
////		if (null != mHandler) {
////			mHandler.post(new OnErrorRunnable(new V5KFException(code, desc)));
////		} else if (getMessageListener() != null) {
////			getMessageListener().onError(new V5KFException(code, desc));
////		}
//	}
//
//	/**
//	 * 获取认证URL返回错误码处理
//	 * @param code
//	 * @param desc
//	 */
//	protected void authEerrorHandler(int code, String desc) {
//		if (code == 0) { // 正常返回
//			return;
//		}
//		
//		/* 非0为有错误 */
//		switch (code) {
//		case 50005: 	// account failed
//			break;
//		case 50004:			
//			break;
//		case 50010: // session closed			
//			break;
//		}
//		
//		if (null != mHandler) {
//			mHandler.post(new OnErrorRunnable(new V5KFException(code, desc)));
//		} else if (getMessageListener() != null) {
//			getMessageListener().onError(new V5KFException(code, desc));
//		}
//	}
	
	/**
	 * 消息发送成功处理
	 * @param handler
	 * @param code
	 * @param description
	 */
	protected void sendSuccessHandle(MessageSendCallback handler, V5Message message) {
		if (message != null) {
			message.setState(V5Message.STATE_ARRIVED);
			message.setSession_start(mSessionStart);
			// 保存消息
			if (null != mDBHelper && cacheLocalMsg) {
				mDBHelper.insert(message);
			}
			// 回调
			if (null != handler) {
				if (mHandler == null) {
					handler.onSuccess(message);
				} else {
					mHandler.post(new MessageSendSuccessRunnable(handler, message));
				}
			}
		}
	}
	
	/**
	 * 消息发送失败处理
	 * @param handler
	 * @param code
	 * @param desc
	 */
	protected void sendFailedHandle(MessageSendCallback handler, V5Message message, V5ExceptionStatus code, String desc) {
		if (message != null) {
			message.setState(V5Message.STATE_FAILURE);
		}
		if (null != handler) {
			if (mHandler == null) {
				handler.onFailure(message, code, desc);
			} else {
				mHandler.post(new MessageSendFailureRunnable(handler, message, code, desc));
			}
		}
	}
	
	protected static class OnMessageRunnable implements Runnable {
		
		private String msgString;
		private V5Message msgBean;
		
		public OnMessageRunnable(String msg) {
			msgString = msg;
		}
		
		public OnMessageRunnable(V5Message msg) {
			msgBean = msg;
		}

		@Override
		public void run() {
			if (V5ClientAgent.getInstance().getMessageListener() != null) {
				if (null != msgBean) {
					V5ClientAgent.getInstance().getMessageListener().onMessage(msgBean);
				}
				if (null != msgString) {
					V5ClientAgent.getInstance().getMessageListener().onMessage(msgString);
				}
			}
		}
	}
	
	class OnErrorRunnable implements Runnable {

		private V5KFException exception;
		
		public OnErrorRunnable(V5KFException ex) {
			exception = ex;
		}
		
		@Override
		public void run() {
			if (getMessageListener() != null) {
				getMessageListener().onError(exception);
			}
		}
	}
	
	class MessageSendSuccessRunnable implements Runnable {
		
		private MessageSendCallback sendHandler;
		private V5Message message;

		public MessageSendSuccessRunnable(MessageSendCallback handler, V5Message msg) {
			this.sendHandler = handler;
			this.message = msg;
		}
		
		@Override
		public void run() {
			if (null != sendHandler) {
				sendHandler.onSuccess(message);
			}
		}
	}

	class MessageSendFailureRunnable implements Runnable {
		
		private MessageSendCallback sendHandler;
		private V5Message message;
		private V5ExceptionStatus statusCode;
		private String description;
		
		public MessageSendFailureRunnable(
				MessageSendCallback handler, 
				V5Message msg,
				V5ExceptionStatus code,
				String desc) {
			this.sendHandler = handler;
			this.message = msg;
			this.statusCode = code;
			this.description = desc;
		}
		
		@Override
		public void run() {
			if (null != sendHandler) {
				sendHandler.onFailure(message, statusCode, description);
			}
		}
	}
	
	class GetLocalMessageRunnable implements Runnable {

		private int offset;
		private int size;
		private OnGetMessagesCallback callback;
		
		public GetLocalMessageRunnable(int offset, int size, OnGetMessagesCallback callback) {
			this.offset = offset;
			this.size = size;
			this.callback = callback;
		}
		
		@Override
		public void run() {
			final List<V5Message> msgList = new ArrayList<V5Message>();
			final boolean finish = mDBHelper.querySession(msgList, offset, size);
			size = msgList.size();
			if (callback != null) {
				if (mHandler != null) {
					mHandler.post(new Runnable() {							
						@Override
						public void run() {
							callback.complete(msgList, offset, size, finish);
						}
					});
				} else {
					callback.complete(msgList, offset, size, finish);
				}
			}
		}
	}

	protected static class GetOfflineMessageRunnable implements Runnable {
		
		private int offset;
		private int size;
		private boolean finish;
		private List<V5Message> msgs;
		
		public GetOfflineMessageRunnable(List<V5Message> msgs, int offset, int size, boolean finish) {
			this.offset = offset;
			this.size = size;
			this.finish = finish;
			this.msgs = msgs;
		}
		
		@Override
		public void run() {
			if (V5ClientAgent.getInstance().getGetMessagesCallback() != null) {
				V5ClientAgent.getInstance().getGetMessagesCallback().complete(msgs, offset, size, finish);
			}
		}
	}

	public V5ClientConfig getConfig() {
		return V5ClientConfig.getInstance(mContext);
	}
	
	public OnURLClickListener getURLClickListener() {
		return mURLClickListener;
	}

	public void setURLClickListener(OnURLClickListener mURLClickListener) {
		this.mURLClickListener = mURLClickListener;
	}

	public OnLocationMapClickListener getLocationMapClickListener() {
		return mLocationMapClickListener;
	}

	public void setLocationMapClickListener(OnLocationMapClickListener mLocationMapClickListener) {
		this.mLocationMapClickListener = mLocationMapClickListener;
	}

	public OnChatActivityListener getChatActivityListener() {
		return mChatActivityListener;
	}

	public void setChatActivityListener(OnChatActivityListener mChatActivityListener) {
		this.mChatActivityListener = mChatActivityListener;
	}

	public UserWillSendMessageListener getUserWillSendMessageListener() {
		return mUserWillSendMessageListener;
	}

	public void setUserWillSendMessageListener(
			UserWillSendMessageListener mUserWillSendMessageListener) {
		this.mUserWillSendMessageListener = mUserWillSendMessageListener;
	}

	protected OnGetMessagesCallback getGetMessagesCallback() {
		return mGetMessagesCallback;
	}

	protected void setGetMessagesCallback(OnGetMessagesCallback mGetMessagesCallback) {
		this.mGetMessagesCallback = mGetMessagesCallback;
	}
	
	/**
	 * 连接建立
	 */
	protected void onConnect() {
		sendOnLineMessage(); // 发上线消息
		if (V5ClientConfig.AUTO_WORKER_SERVICE) { // 自动转人工客服
			switchToArtificialService(null);
		}
		
		if (_magic != null) {
			JSONObject info = new JSONObject();
			try {
				info.put("magic_arr", _magic);
				sendMagicInfo(info);
				_magic = null;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (V5ClientConfig.UI_SUPPORT) {
			updateMessages();
		} else {
			// onConnect回调
			if (null != getHandler()) { 
				getHandler().post(new Runnable() {
					
					@Override
					public void run() {
						if (getMessageListener() != null) {
							getMessageListener().onConnect();
						}
					}
				});
			} else if (getMessageListener() != null) {
				getMessageListener().onConnect();
			}
		}
	}
	
	/**
	 * 接收消息
	 */
	protected void onMessage(String message) {
		try {
			JSONObject json = new JSONObject(message);
			if (json.optString("o_type").equals("message")) {
				V5Message messageBean = V5MessageManager.getInstance().receiveMessage(json);
				messageBean.setSession_start(mSessionStart);
				if (messageBean.getMsg_id() > 0 && messageBean.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
					// 开场问题的答案，在Activity里缓存
				} else if (null != mDBHelper && cacheLocalMsg) {
					mDBHelper.insert(messageBean);
				}
				if (null != getHandler()) { // 回调到Message消息接口
					getHandler().post(new OnMessageRunnable(messageBean));
				} else if (getMessageListener() != null) {
					getMessageListener().onMessage(messageBean);
				}
			} else if (json.optString("o_type").equals("session")) {
				if (json.optString("o_method").equals("get_status")) { // 对话状态信息：机器人或者客服信息
					int status = json.optInt("status");
					if (status == 2) {
						String nickname = json.optString("nickname");
						String photo = json.optString("photo");
						long wid = json.optLong("w_id");
						getConfig().setWorkerPhoto(photo);
						getConfig().setWorkerName(nickname);
						getConfig().setWorkerId(wid);
						getConfig().setWorkerType(status);
						// 保存wid->photo到本地
						V5ConfigSP configSP = new V5ConfigSP(getContext());
						configSP.savePhoto(wid, photo);
					}
					final ClientServingStatus ss = ClientServingStatus.getStatus(status);
					if (null != getHandler()) { // 回调到Message消息接口
						getHandler().post(new Runnable() {
							
							@Override
							public void run() {
								if (getMessageListener() != null) {
									getMessageListener().onServingStatusChange(ss);
								}
							}
						});
					} else if (getMessageListener() != null) {
						getMessageListener().onServingStatusChange(ss);
					}
				} else if (json.optString("o_method").equals("get_messages")) {
					if (V5ClientConfig.UI_SUPPORT) { // 需要界面显示查询并缓存离线消息
						List<V5Message> msgs = new ArrayList<V5Message>();
						JSONArray messages = json.optJSONArray("messages");
						if (null != messages && messages.length() > 0) {
							for (int i = 0; i < messages.length(); i++) {
								JSONObject item = messages.getJSONObject(i);
								V5Message msg = V5MessageManager.getInstance().receiveMessage(item);
								if (msg.getMsg_id() > 0 && msg.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
									// 排除开场问题
								} else {
									msgs.add(0, msg);
								}
								V5Message candidate = null;
								if (msg.getCandidate() != null && msg.getCandidate().size() > 0) {
									candidate = msg.getCandidate().get(0);
									if (candidate.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
										msgs.add(0, candidate);
									}
									msg.setCandidate(null);
								}
							}
						}
						
						if (mMsgIdCount == 0) {
							mMsgIdCount = msgs.size() + 1; //+V5Util.getCurrentLongTime()%100
						}
	
						if (getGetMessagesCallback() != null) {
							if (null != getHandler()) { // 回调到GetMessagesCallback接口
								getHandler().post(new GetOfflineMessageRunnable(msgs, json.optInt("offset"), json.optInt("size"), json.optBoolean("finish")));
							} else if (getGetMessagesCallback() != null) {
								getGetMessagesCallback().complete(msgs, json.optInt("offset"), json.optInt("size"), json.optBoolean("finish"));
							}
						} else {
							// 插入数据库
							if (msgs.size() > 0 && cacheLocalMsg) {
								// msgs倒序
								Collections.reverse(msgs);
								if (null != mDBHelper) {
									/**
									 * 注意插入数据库的顺序，决定了读取消息记录的顺序
									 */
									for (V5Message msg : msgs) {
										mDBHelper.insert(msg);
									}
								}
							}
							
							// onConnect回调
							if (null != getHandler()) { 
								getHandler().post(new Runnable() {
									
									@Override
									public void run() {
										if (getMessageListener() != null) {
											getMessageListener().onConnect();
										}
									}
								});
							} else if (getMessageListener() != null) {
								getMessageListener().onConnect();
							}
						}
					} else if (getGetMessagesCallback() != null) {
						List<V5Message> msgs = new ArrayList<V5Message>();
						JSONArray messages = json.optJSONArray("messages");
						if (null != messages && messages.length() > 0) {
							for (int i = 0; i < messages.length(); i++) {
								JSONObject item = messages.getJSONObject(i);
								V5Message msg = V5MessageManager.getInstance().receiveMessage(item);
								if (msg.getMsg_id() > 0 && msg.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
									// 排除开场问题
								} else {
									msgs.add(0, msg);
								}
								V5Message candidate = null;
								if (msg.getCandidate() != null && msg.getCandidate().size() > 0) {
									candidate = msg.getCandidate().get(0);
									if (candidate.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
										msgs.add(0, candidate);
									}
									msg.setCandidate(null);
								}
							}
						}
						if (null != getHandler()) { // 回调到GetMessagesCallback接口
							getHandler().post(new GetOfflineMessageRunnable(msgs, json.optInt("offset"), json.optInt("size"), json.optBoolean("finish")));
						} else if (getGetMessagesCallback() != null) {
							getGetMessagesCallback().complete(msgs, json.optInt("offset"), json.optInt("size"), json.optBoolean("finish"));
						}
					} else {
						if (null != getHandler()) { // 回调到字符串消息接口
							getHandler().post(new OnMessageRunnable(message));
						} else if (getMessageListener() != null) {
							getMessageListener().onMessage(message);
						}
					}
				} else {
					if (null != getHandler()) { // 回调到字符串消息接口
						getHandler().post(new OnMessageRunnable(message));
					} else if (getMessageListener() != null) {
						getMessageListener().onMessage(message);
					}
				}
			} else if (json.has("o_error")) {
				int code = json.getInt("o_error");
				if (code != 0) {
					String desc = json.optString("o_errmsg");
					errorHandle(new V5KFException(
							V5ExceptionStatus.ExceptionServerResponse, "[" + code + "]" + desc));//认证错误
				}
			} else {
				if (null != getHandler()) { // 回调到字符串消息接口
					getHandler().post(new OnMessageRunnable(message));
				} else if (getMessageListener() != null) {
					getMessageListener().onMessage(message);
				}
			}
		} catch (JSONException e) {
			//e.printStackTrace();
			Log.e(TAG, "", e);
			errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Unknown error, try reconnect"));
//			connectWebsocket(true);
		}
	}
}
