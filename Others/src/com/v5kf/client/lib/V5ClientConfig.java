package com.v5kf.client.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.v5kf.client.lib.entity.V5Message;

public class V5ClientConfig {
	/*
	 * Log level: 1 - 5
	 */
	public static final int LOG_LV_ERROR = 1;
	public static final int LOG_LV_WARN = 2;
	public static final int LOG_LV_INFO = 3;
	public static final int LOG_LV_DEBUG = 4;
	public static final int LOG_LV_VERBOS = 5;
	
	// UI库以下两个参数必须为true
	public static final boolean CALLBACK_ON_UI_THREAD = true;
	// 是否需要UI显示(updateMessage)，无UI则不需要getMessages和getStatus，不自动查询离线消息
	public static final boolean UI_SUPPORT = true;
	
	private static int LOG_LEVEL = LOG_LV_INFO; // 日志显示级别
	private static boolean LOG_SHOW = true;	// 是否显示日志
	public static final boolean USE_THUMBNAIL = true; // 使用缩略图
	public static boolean AUTO_RETRY_ONERROR = true; // 连接断开是否自动重试(否则弹出对话框点击重试)
	
	public static int SOCKET_TIMEOUT = 20000; // 超时时间10s
	public static int UPLOAD_TIMEOUT = 30000; // 上传超时时间30s
	public static final String IMAGE_TYPE_SUPPORTED = "png|PNG|jpg|JPG|jpeg|JPEG|bmp|BMP|gif|GIF";
	
	protected static boolean AUTO_WORKER_SERVICE = false;
//	protected static final int NOTIFICATION_ID = 23;
	protected static final String ACTION_NOTIFICATION = "com.v5kf.android.intent.notification";
	protected static final String ACTION_NEW_MESSAGE = "com.v5kf.android.intent.action_message";
	
	public static boolean SKIP_INIT = false; // 跳过初始化
	public static boolean DEBUG = false; // 是否debug模式(连接debug服务端)
	public static boolean USE_HTTPS = true; // 默认使用https访问
	
	public static final boolean MAGIC = false; // 连接magic服务地址
	
	/**
	 * 客户端连接地址格式 -> [修改]使用静态函数方式获取
	 */
//	public static final String SDK_INIT_URL = (USE_HTTPS ? "https" : "http") + "://www.v5kf.com/public/appsdk/init";
//	public static final String WS_URL_FMT = "ws://chat.v5kf.com/sitews?token=%s&site=%s&o_id=%s";
//	protected static final String WS_URL_FMT = (USE_HTTPS ? "wss" : "ws") + "://chat.v5kf.com/" + DEBUG + "/appws/v2?auth=%s"; // ws地址
//			"site=%s&account=%s&visitor=%s&device=android&timestamp=%d&nonce=%d&expires=%d&signature=%s&
//	protected static final String CHAT_HOST = "http://chat.v5kf.com";
//	public static final String AUTH_URL = "http://chat.v5kf.com/public/webauth/v9"; // web认证v9
//	protected static final String AUTH_URL = (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + DEBUG + "/appauth/v2"; // app认证v2
//	protected static final String ORIGIN = "http://chat.v5kf.com";
//	protected static final String PIC_AUTH_URL = (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + DEBUG + "/wxyt/app?auth=";
//	protected static final String SDK_MEDIA_AUTH_URL = "https://chat.v5kf.com/" + DEBUG + "/wxyt/app?type=voice&suffix=amr&auth=";
	
	/**
	 * 获取站点配置信息地址格式 -> [修改]使用静态函数方式获取
	 */
//	protected static final String SITEINFO_URL_FMT = (USE_HTTPS ? "https" : "http") + "://www.v5kf.com/public/api_dkf/get_chat_siteinfo?sid=%s";
	
	// 本地化配置文件名称
	protected static final String PREFS_FILE = "v5kf_client";
	
	/* 用户信息 */
	// site、account
	/**
	 * @deprecated 使用openId替代
	 */
	private String uid; // 多用户账号APP必须
	private String openId; // 多用户账号APP必须
	private String nickname;
	private String avatar;
	private int vip; // 0-5
	private int gender;
	
	/* 用户magic信息 */
	private JSONObject userInfo; // 自定义magic参数，键值对数组形式[{"key":"1","val":"a"},{"key":"2","val":"b"}]
	
	/* 坐席信息 */
	private long workerId;
	private String workerName;
	private String workerPhoto;
	private int workerType; // 人工or机器人or机器人托管
	private String workerIntro;
	
	/* 机器人信息 */
	private String robotName;
	private String robotPhoto;
	private String robotIntro;
	
	/* SDK基础配置信息(AndroidManifest.xml)，需要缓存本地 */
	private String siteId;
	private String siteAccount;
	private String appID;
	
	/* 需要缓存本地，下次可直接连接，需要绑定uid，切换用户后需要重新认证 */
	private long timestamp;
//	private long nonce;//[修改]去掉nonce
	private long expires;
	private String authorization; // 此参数用于ws连接以及下面参数的签名
	private String v5VisitorId; /* 一个账号对应一个uid->v5VisitorId */
	/* 推送服务 */
	private String deviceToken; /* 一个App对应一个deviceToken */
	/* 通知 */
	private String notificationTitle;
	/* 是否发送心跳包  */
	private boolean heartBeatEnable = true;
	private int heartBeatTime = 30000; // 单位ms
	
	private Context mContext;
	private static V5ClientConfig mClientConfig = null;
	
	private V5ClientConfig(Context context) {
//		Logger.v("V5ClientConfig", "V5ClientConfig instance");
		this.mContext = context;		
	}
	
	/**
	 * 获得V5ClientConfig单例对象
	 * @param context
	 * @return
	 */
	public static V5ClientConfig getInstance(Context context) {
		if(mClientConfig == null) {
			synchronized (V5ClientConfig.class) {   // 保证了同一时间只能只能有一个对象访问此同步块        
				if(mClientConfig == null) {
					mClientConfig = new V5ClientConfig(context);
				}
			}
		}
		return mClientConfig;
	}
	
	/**
	 * 销毁V5ClientConfig单例对象
	 */
	public static synchronized void destroyInstance() {
		if (mClientConfig != null) 
			mClientConfig = null;
	}
	
	/**
	 * 是否允许本地消息缓存
	 * @param enable
	 */
	public void setLocalMessageCacheEnable(boolean enable) {
		V5ConfigSP csp = new V5ConfigSP(mContext);
		csp.saveLocalDbFlag(enable);
	}
	
	public boolean getLocalMessageCacheEnable() {
		V5ConfigSP csp = new V5ConfigSP(mContext);
		return csp.readLocalDbFlag();
	}
	
	/**
	 * 设置默认转人工客服
	 * @param isAutoWorker 是否默认转人工客服，默认为机器人接待
	 */
	public void setDefaultServiceByWorker(boolean isAutoWorker) {
		V5ClientConfig.AUTO_WORKER_SERVICE = isAutoWorker;
	}
	
	/**
	 * 获取站点号
	 * @param context
	 * @return
	 */
	public String getSiteId() {
		if (siteId != null) {
			return siteId;
		}
		
		ApplicationInfo appInfo = null;
		try {
			appInfo = mContext.getPackageManager()
			        .getApplicationInfo(mContext.getPackageName(),
			        PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (appInfo != null) {
			String msg = String.valueOf(appInfo.metaData.getInt("V5_SITE"));
			return msg;
		} else {
			return null;
		}
	}

	protected void setSiteId(String siteId) {
		this.siteId = siteId;
		V5ConfigSP configSP = new V5ConfigSP(mContext);
		configSP.saveSiteId(siteId);
	}

	/**
	 * 获取站点号
	 * @param context
	 * @return
	 */
	public String getSiteAccount() {
		if (siteAccount != null) {
			return siteAccount;
		}
		
		ApplicationInfo appInfo = null;
		try {
			appInfo = mContext.getPackageManager()
					.getApplicationInfo(mContext.getPackageName(),
							PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (appInfo != null) {
			String msg = String.valueOf(appInfo.metaData.getString("V5_ACCOUNT"));
			return msg;
		} else {
			return null;
		}
	}
	
	protected void setSiteAccount(String siteAccount) {
		this.siteAccount = siteAccount;
		V5ConfigSP configSP = new V5ConfigSP(mContext);
		configSP.saveSiteAccount(siteAccount);
	}

	protected void setAppID(String appID) {
		this.appID = appID;
		V5ConfigSP configSP = new V5ConfigSP(mContext);
		configSP.saveAppID(appID);
	}

	/**
	 * 获取APP ID
	 * @param context
	 * @return
	 */
	public String getAppID() {
		if (appID != null) {
			return appID;
		}
		
		ApplicationInfo appInfo = null;
		try {
			appInfo = mContext.getPackageManager()
					.getApplicationInfo(mContext.getPackageName(),
							PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (appInfo != null) {
			String msg = String.valueOf(appInfo.metaData.getString("V5_APPID"));
			return msg;
		} else {
			return null;
		}
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}
	
	
	public static int getLogLevel() {
		if (LOG_SHOW) {
			return LOG_LEVEL;
		} else {
			return 0;
		}
	}

	public void setLogLevel(int lOG_LEVEL) {
		LOG_LEVEL = lOG_LEVEL;
	}
	
	public void setShowLog(boolean show) {
		LOG_SHOW = show;
	}
	
	public boolean getShowLog() {
		return LOG_SHOW;
	}

	protected String getNotificationAction() {
		return ACTION_NOTIFICATION + getSiteId();
	}

//	public String getSignature() {
//		if (this.signature != null) {
//			return signature;
//		} else {
//			V5ConfigSP config = new V5ConfigSP(mContext);
//			return config.readSignature(getV5VisitorId());
//		}
//	}
//
//	public void setSignature(String signature) {
//		this.signature = signature;
//		V5ConfigSP config = new V5ConfigSP(mContext);
//		config.saveSignature(getV5VisitorId(), signature);
//	}

	public long getExpires() {
		if (expires != 0) {
			return expires;
		} else {
			V5ConfigSP config = new V5ConfigSP(mContext);
			return config.readExpires();
		}
	}

	protected void setExpires(long expires) {
		this.expires = expires;
		V5ConfigSP config = new V5ConfigSP(mContext);
		config.saveExpires(expires);
	}

//	public long getNonce() {
//		if (nonce != 0) {
//			return nonce;
//		} else {
//			V5ConfigSP config = new V5ConfigSP(mContext);
//			return config.readNonce();
//		}
//	}
//
//	public void setNonce(long nonce) {
//		this.nonce = nonce;
//		V5ConfigSP config = new V5ConfigSP(mContext);
//		config.saveNonce(nonce);
//	}

	public long getTimestamp() {
		if (timestamp != 0) {
			return timestamp;
		} else {
			V5ConfigSP config = new V5ConfigSP(mContext);
			return config.readTimestamp();
		}
	}

	protected void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		V5ConfigSP config = new V5ConfigSP(mContext);
		config.saveTimestamp(timestamp);
	}

	public String getAuthorization() {
		if (authorization != null) {
			return authorization;
		} else {
			V5ConfigSP config = new V5ConfigSP(mContext);
			return config.readAuthorization(getV5VisitorId());
		}
	}

	protected void setAuthorization(String authorization) {
		this.authorization = authorization;
		V5ConfigSP config = new V5ConfigSP(mContext);
		config.saveAuthorization(getV5VisitorId(), authorization);
	}

	public String getDeviceToken() {
		if (deviceToken == null) {
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			deviceToken = configSP.readDeviceToken();
//			if (deviceToken == null) {
//				try {
//					deviceToken = V5Util.hash(getAppkey());
//				} catch (NoSuchAlgorithmException e) {
//					e.printStackTrace();
//				}
//			}
		}
		Logger.v("V5ClientConfig", "getDeviceToken=" + deviceToken);
		return deviceToken;
	}

	/**
	 * 必须。消息推送应用识别，不同的设备不同的应用该值需不同
	 * @param deviceToken
	 */
	public void setDeviceToken(String deviceToken) {
		Logger.v("V5ClientConfig", "setDeviceToken:" + deviceToken);
		if (TextUtils.isEmpty(deviceToken)) {
			Logger.e("V5ClientConfig", "DeviceToken is null or empty!");
		} else {
			this.deviceToken = deviceToken;
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			configSP.saveDeviceToken(deviceToken);
		}
	}
	
	public String getNotificationTitle() {
		if (notificationTitle == null) {
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			notificationTitle = configSP.readNotificationTitle();
		}
		return notificationTitle;
	}

	public void setNotificationTitle(String title) {
		if (TextUtils.isEmpty(title)) {
			return;
		} else {
			this.notificationTitle = title;
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			configSP.saveNotificationTitle(title);
		}
	}

	/**
	 * @deprecated 使用openId替代
	 * @return
	 */
	public String getUid() {
		if (null == uid) {
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			uid = configSP.readUid();
		}
		return uid;
	}

	/**
	 * @deprecated 使用openId替代
	 * 必须。APP用户ID（支持用户账号切换的APP必须为不同用户账号设置该值）
	 * @param uid
	 */
	public void setUid(String uid) {
		if (null == uid) {
			Logger.e("V5ClientConfig", "Uid is null!");
			return;
		}
		this.uid = uid;
		V5ConfigSP configSP = new V5ConfigSP(mContext);
		String localUid = configSP.readUid();
		if (localUid != null && !localUid.equals(uid)) {
			// 重新设置uid与之前不同时需要清除之前的visitor缓存
			configSP.removeAuthorization(getV5VisitorId());
			configSP.removeVisitorId();
			v5VisitorId = null;
			authorization = null;
		}
		configSP.saveUid(uid);
	}
	
	public String getOpenId() {
		if (null == openId) {
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			openId = configSP.readOpenId();
		}
		return openId;
	}

	public void setOpenId(String openId) {
		if (null == openId) {
			Logger.e("V5ClientConfig", "Uid is null!");
			return;
		}
		this.openId = openId;
		V5ConfigSP configSP = new V5ConfigSP(mContext);
		String localOid = configSP.readOpenId();
		if (localOid != null && !localOid.equals(openId)) {
			// 重新设置openId与之前不同时需要清除之前的visitor缓存
			configSP.removeAuthorization(getV5VisitorId());
			configSP.removeVisitorId();
			v5VisitorId = null;
			authorization = null;
		}
		configSP.saveOpenId(openId);
		Logger.v("V5ClientConfig", "setOpenId:" + openId);
	}

	/**
	 * 64字节visitor ID(V5系统用户识别ID)
	 * @return
	 */
	public String getV5VisitorId() {
		if (v5VisitorId != null) {
			return v5VisitorId;
		}
		
		V5ConfigSP config = new V5ConfigSP(mContext);
		v5VisitorId = config.readVisitorId();
		if (null != v5VisitorId) {
			return v5VisitorId;
		}
		String account = getAppID();
		if (TextUtils.isEmpty(account)) {
			account = getSiteAccount();
		}
		if (!TextUtils.isEmpty(openId)) {
			try {
				v5VisitorId = URLEncoder.encode(openId, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else if (!TextUtils.isEmpty(uid)) {
			try {
				v5VisitorId = V5Util.hash(uid + account + getSiteId());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} else {
			try {
				DeviceUuidFactory uuidFactory = new DeviceUuidFactory(mContext);
				String duid = uuidFactory.getUuidString() + account + getSiteId();
				if (TextUtils.isEmpty(duid)) {
					duid = V5Util.getRandomString(48) + account + getSiteId();
				}
				v5VisitorId = V5Util.hash(duid);
//				Logger.v("V5ClientConfig", "VisitorId:" + v5VisitorId + " uid:" + duid);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		config.saveVisitorId(v5VisitorId);
			
		return v5VisitorId;
	}

	/**
	 * 获得通知id，即为站点编号值
	 * @param context
	 * @return
	 */
	protected static int getNotifyId(Context context) {
		int notifyId = 0;
		try {
			notifyId = Integer.parseInt(V5ClientConfig.getInstance(context).getSiteId());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return notifyId;
	}
	
	/**
	 * 更新用户信息前调用（setOpenId前）
	 */
	public void shouldUpdateUserInfo() {
		// 更新用户信息时调用，清除之前的visitor缓存
		V5ConfigSP configSP = new V5ConfigSP(mContext);
		configSP.removeAuthorization(getV5VisitorId());
		configSP.removeVisitorId();
		v5VisitorId = null;
		authorization = null;
	}
	
	protected static Notification getNotification(Context context, V5Message message) {
		// 此Builder为android.support.v4.app.NotificationCompat.Builder中的，下同。
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        
		// 系统收到通知时，通知栏上面滚动显示的文字。
		mBuilder.setTicker(message.getDefaultContent(context));
		V5ClientConfig config = V5ClientConfig.getInstance(context);
		Intent intent = null;
		intent = new Intent(config.getNotificationAction());
		Bundle bundle = new Bundle();
		bundle.putSerializable("v5_message", message);
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		// 点击通知之后需要跳转的页面
        PendingIntent pIntent = PendingIntent.getActivity(
        		context, 
        		0, 
        		intent, 
        		PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);
        
        // 通知标题
        String notifTitle = config.getNotificationTitle();
        if (null == notifTitle) {
        	notifTitle = context.getString(V5Util.getIdByName(context, "string", "v5_def_title"));
        }
        mBuilder.setContentTitle(notifTitle);
        // 通知内容
        mBuilder.setContentText(message.getDefaultContent(context));
 
        // 显示在通知栏上的小图标
        mBuilder.setSmallIcon(context.getApplicationInfo().icon);
        
        // 设置大图标，即通知条上左侧的图片（如果只设置了小图标，则此处会显示小图标）
        mBuilder.setLargeIcon(
    			BitmapFactory.decodeResource(context.getResources(),
    					context.getApplicationInfo().icon));
 
        // 设置为可清除模式
        mBuilder.setOngoing(false);
        
        // 点击自动消失 
        mBuilder.setAutoCancel(true);
                
        // 设置铃声、震动、LED灯提醒-默认 
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        
        Notification notification = mBuilder.build();
//        Logger.v("V5ClientConfig", "notifyMessage send <<<");
        return notification;
	}
	
	protected static NotificationManager getNotificationManager(Context context) {
		return (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}
	
	protected static String getSDKInitURL() {
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/public/appsdk/init";
	}
	
	protected static String getWSFormstURL() {
		if (MAGIC) {
			return (USE_HTTPS ? "wss" : "ws") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/magicws?auth=%s"; // ws地址
		} else {
			return (USE_HTTPS ? "wss" : "ws") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/appws/v2?auth=%s"; // ws地址
		}
	}
	
	protected static String getAccountAuthURL() {
		if (MAGIC) {
			return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/magicauth/v2"; // magic认证v2
		} else {
			return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/appauth/v2"; // app认证v2
		}
	}
	
//	/* 媒体文件上传统一接口 */
//	public static final String APP_MEDIA_POST_FMT = (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/upload/%s/%s/%s/%s"; // /$account_id/$visitor_id/[web|app|wechat|wxqy|yixin]/$filename
//	/* 媒体文件下载统一接口 */
//	public static final String APP_RESOURCE_V5_FMT = (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/resource/%s/%s"; // site_id/message_id

	/**
	 * 上传地址
	 * @return
	 */
	protected static String getUploadFormatURL() {
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/upload/%s"; // /$account_id=0/$visitor_id=0/[web|app|wechat|wxqy|yixin]/$filename
	}
	
	/**
	 * 媒体资源获取
	 * @return
	 */
	protected static String getResourceFormatURL() { //APP_RESOURCE_V5_FMT
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/resource/%s/%s";
	}
	
	/**
	 * 缩略图
	 * @return
	 */
	protected static String getPictureThumbnailFormatURL() { //APP_PIC_V5_THUMBNAIL_FMT
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/resource/%s/%s/thumbnail"; // 图片质量0-100
	}
	
	protected static String getSiteinfoFormatURL() {
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/public/api_dkf/get_chat_siteinfo?sid=%s";
	}

	protected static String getHotQuesFormatURL() {
		//http://chat.v5kf.com/public/api_dkf/get_hot_ques?sid=
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/public/api_dkf/get_hot_ques?sid=%s";
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	protected static String getPictureAuthURL() {
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/app?auth=";
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	protected static String getMediaAuthURL() { //SDK_MEDIA_AUTH_URL
		return (USE_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/app?type=voice&suffix=amr&auth=";
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	public String getWorkerPhoto() {
		return workerPhoto;
	}

	public void setWorkerPhoto(String workerPhoto) {
		this.workerPhoto = workerPhoto;
	}

	public int getWorkerType() {
		return workerType;
	}

	public void setWorkerType(int workerType) {
		this.workerType = workerType;
	}

	public long getWorkerId() {
		return workerId;
	}

	public void setWorkerId(long workerId) {
		this.workerId = workerId;
	}

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public String getRobotName() {
		if (!TextUtils.isEmpty(this.robotName)) {
			return this.robotName;
		} else {
			V5ConfigSP config = new V5ConfigSP(mContext);
			this.robotName = config.readString("v5_robot_name");
		}
		return robotName;
	}

	public void setRobotName(String name) {
		if (TextUtils.isEmpty(name)) {
			return;
		} else {
			this.robotName = name;
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			configSP.saveString("v5_robot_name", name);
		}
	}

	public String getRobotPhoto() {
		if (!TextUtils.isEmpty(this.robotPhoto)) {
			return this.robotPhoto;
		} else {
			V5ConfigSP config = new V5ConfigSP(mContext);
			this.robotPhoto = config.readString("v5_robot_photo");
		}
		return robotPhoto;
	}

	public void setRobotPhoto(String photo) {
		if (TextUtils.isEmpty(photo)) {
			return;
		} else {
			this.robotPhoto = photo;
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			configSP.saveString("v5_robot_photo", photo);
		}
	}

	public String getRobotIntro() {
		if (!TextUtils.isEmpty(this.robotIntro)) {
			return this.robotIntro;
		} else {
			V5ConfigSP config = new V5ConfigSP(mContext);
			this.robotIntro = config.readString("v5_robot_intro");
		}
		return robotIntro;
	}

	public void setRobotIntro(String intro) {
		if (TextUtils.isEmpty(intro)) {
			return;
		} else {
			this.robotIntro = intro;
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			configSP.saveString("v5_robot_intro", intro);
		}
	}
	
	public String getWorkerIntro() {
		if (!TextUtils.isEmpty(this.workerIntro)) {
			return this.workerIntro;
		} else {
			V5ConfigSP config = new V5ConfigSP(mContext);
			this.workerIntro = config.readString("v5_worker_intro");
		}
		return workerIntro;
	}

	public void setWorkerIntro(String _intro) {
		if (TextUtils.isEmpty(_intro)) {
			return;
		} else {
			this.workerIntro = _intro;
			V5ConfigSP configSP = new V5ConfigSP(mContext);
			configSP.saveString("v5_worker_intro", _intro);
		}
	}

	public boolean isHeartBeatEnable() {
		return heartBeatEnable;
	}

	public void setHeartBeatEnable(boolean heartBeatEnable) {
		this.heartBeatEnable = heartBeatEnable;
	}

	public int getHeartBeatTime() {
		return heartBeatTime;
	}

	public void setHeartBeatTime(int heartBeatTime) {
		this.heartBeatTime = heartBeatTime;
	}

	public JSONObject getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(JSONObject userInfo) {
		this.userInfo = userInfo;
	}

//	public void setWorkerPhoto(String photo, String nickname) {
//		if (photo == null || nickname == null || 
//				nickname.isEmpty() || photo.isEmpty()) {
//			return;
//		}
//		V5ConfigSP config = new V5ConfigSP(mContext);
//		config.saveString(photo, nickname);
//	}
//	
//	public String getWorkerPhoto(String nickname) {
//		if (nickname == null || nickname.isEmpty()) {
//			return null;
//		}
//		V5ConfigSP config = new V5ConfigSP(mContext);
//		return config.readString(nickname);
//	}
 }
