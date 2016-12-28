package com.v5kf.client.lib;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author chenhy
 * @version v1.0 2015-11-23 上午10:45:12
 * @description 客户端配置管理
 *
 */
public class V5ConfigSP {
	
//	private static final String PREFS_SSESSION_FLAG = "v5_new_session_flag";
//	private static final String PREFS_CHAT_URL = "v5_chat_url";
	private static final String PREFS_LOCAL_DB_FLAG = "v5_local_db_flag"; // 是否本地存储消息,默认开启
//	private static final String PREFS_SESSION_START_TIME = "v5_session_start_time";
	
	/* 连接认证信息 */
	private static final String PREFS_APPID = "v5_sdk_appid";
	private static final String PREFS_ACCOUNT = "v5_sdk_account";
	private static final String PREFS_SITE_ID = "v5_sdk_site_id";
	private static final String PREFS_TIMESTAMP = "v5_timestamp";
	private static final String PREFS_EXPIRES = "v5_expires";
	private static final String PREFS_VISITOR_ID = "v5_visitor_id";
	private static final String PREFS_UID = "v5_app_uid";
	private static final String PREFS_SDK_AUTH_FLAG = "v5_sdk_auth";// v5_sdk_initialization
//	private static final String PREFS_ACCOUNT_AUTH = "v5_authorization";
//	private static final String PREFS_WXYT_URL = "v5_wxyt_url";
	private static final String PREFS_DEV_TOKEN = "v5_device_token";
	private static final String PREFS_APP_PUSH = "v5_app_push";
	private static final String PREFS_APP_NOTIF_TITLE = "v5_app_notification_title";
		
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEdit;
	
	public V5ConfigSP(Context context) {
		this.mSharedPreferences = context.getSharedPreferences(V5ClientConfig.PREFS_FILE, Context.MODE_PRIVATE);
	}
	
//	protected void saveSignature(String v5VisitorId, String sign) {
//		mEdit = mSharedPreferences.edit();
//		mEdit.putString(v5VisitorId, sign);
//		mEdit.commit();
//	}
//	
//	public String readSignature(String v5VisitorId) {
//		String sign = mSharedPreferences.getString(v5VisitorId, null);
//		
//		// signature生存期判断
//		long expires = readExpires();
//		long timestamp = readTimestamp();
//		long current = V5Util.getCurrentLongTime() / 1000;
//		if ((timestamp + expires) < current - 3) {
//			return null;
//		} else {
//			return sign;
//		}
//	}
	
	public void saveString(String key, String value) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(key, value);
		mEdit.commit();
	}
	
	public String readString(String key) {
		return mSharedPreferences.getString(key, null);
	}
	
	public void savePhoto(long wid, String photo) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString("photo_" + wid, photo);
		mEdit.commit();
	}
	
	public String readPhoto(long wid) {
		return mSharedPreferences.getString("photo_" + wid, null);
	}

	public void saveVisitorId(String visitorId) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(PREFS_VISITOR_ID, visitorId);
		mEdit.commit();
	}
	
	public String readVisitorId() {
		return mSharedPreferences.getString(PREFS_VISITOR_ID, null);
	}
	
	/**
	 * 删除visitor ID缓存
	 */
	public void removeVisitorId() {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(PREFS_VISITOR_ID);
		mEdit.commit();
	}
	
	public void saveTimestamp(long timestamp) {
		mEdit = mSharedPreferences.edit();
		mEdit.putLong(PREFS_TIMESTAMP, timestamp);
		mEdit.commit();
	}
	
	public long readTimestamp() {
		return mSharedPreferences.getLong(PREFS_TIMESTAMP, 0);
	}

	public void saveSiteAccount(String account) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(PREFS_ACCOUNT, account);
		mEdit.commit();
	}
	
	public String readSiteAccount() {
		return mSharedPreferences.getString(PREFS_ACCOUNT, null);
	}

	public void saveAppID(String appid) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(PREFS_APPID, appid);
		mEdit.commit();
	}
	
	public String readAppID() {
		return mSharedPreferences.getString(PREFS_APPID, null);
	}
	
	public void saveSiteId(String siteId) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(PREFS_SITE_ID, siteId);
		mEdit.commit();
	}
	
	public String readSiteId() {
		return mSharedPreferences.getString(PREFS_SITE_ID, null);
	}

	public void saveExpires(long expires) {
		mEdit = mSharedPreferences.edit();
		mEdit.putLong(PREFS_EXPIRES, expires);
		mEdit.commit();
	}
	
	public long readExpires() {
		return mSharedPreferences.getLong(PREFS_EXPIRES, 0);
	}

//	public void saveSessionFlag(boolean flg) {
//		mEdit = mSharedPreferences.edit();
//		mEdit.putBoolean(PREFS_SSESSION_FLAG, flg);
//		mEdit.commit();
//	}
//	
//	public boolean readSessionFlag() {
//		return mSharedPreferences.getBoolean(PREFS_SSESSION_FLAG, true);
//	}

	public void saveAuthorization(String visitorId, String auth) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(visitorId, auth);
		mEdit.commit();
	}
	
	public String readAuthorization(String visitorId) {
		String auth = mSharedPreferences.getString(visitorId, null);
		
		// signature生存期判断
		long expires = readExpires();
		long timestamp = readTimestamp();
		long current = V5Util.getCurrentLongTime() / 1000;
		if ((timestamp + expires) < current - 3) {
			return null;
		} else {
			return auth;
		}
	}
	
	public void removeAuthorization(String visitorId) {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(visitorId);
		mEdit.commit(); 
	}

	public void saveUid(String uid) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(PREFS_UID, uid);
		mEdit.commit();
	}
	
	public String readUid() {
		return mSharedPreferences.getString(PREFS_UID, null);
	}
	
	public void removeUid() {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(PREFS_UID);
		mEdit.commit();
	}

	public void saveDeviceToken(String token) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(PREFS_DEV_TOKEN, token);
		mEdit.commit();
	}
	
	public String readDeviceToken() {
		return mSharedPreferences.getString(PREFS_DEV_TOKEN, null);
	}

	// [修改]取消保存wxyturl
//	public void saveWxyturl(String url) {
//		mEdit = mSharedPreferences.edit();
//		mEdit.putString(PREFS_WXYT_URL, url);
//		mEdit.commit();
//	}
//	
//	public String readWxyturl() {
//		return mSharedPreferences.getString(PREFS_WXYT_URL, null);
//	}

//	public void saveChatUrl(String url) {
//		mEdit = mSharedPreferences.edit();
//		mEdit.putString(PREFS_CHAT_URL, url);
//		mEdit.commit();
//	}
//	
//	public String readChatUrl() {
//		return mSharedPreferences.getString(PREFS_CHAT_URL, null);
//	}
//	
//	public void clearChatUrl() {
//		mEdit = mSharedPreferences.edit();
//		mEdit.remove(PREFS_CHAT_URL);
//		mEdit.commit();
//	}
	
	public void saveLocalDbFlag(boolean flg) {
		mEdit = mSharedPreferences.edit();
		mEdit.putBoolean(PREFS_LOCAL_DB_FLAG, flg);
		mEdit.commit();
	}
	
	public boolean readLocalDbFlag() {
		return mSharedPreferences.getBoolean(PREFS_LOCAL_DB_FLAG, true);
	}
	
//	public void saveSessionStart(long sessionStart) {
//		mEdit = mSharedPreferences.edit();
//		mEdit.putLong(PREFS_SESSION_START_TIME, sessionStart);
//		mEdit.commit();
//	}
//	
//	public long readSessionStart() {
//		long session = mSharedPreferences.getLong(PREFS_SESSION_START_TIME, 0);
//		if (0 == session) {
//			session = V5Util.getCurrentLongTime() / 1000;
//			saveSessionStart(session);
//		}
//		return session;
//	}

	public boolean readSDKAuthFlag() {
		return mSharedPreferences.getBoolean(PREFS_SDK_AUTH_FLAG, false);
	}
	
	public void saveSDKAuthFlag(boolean flg) {
		mEdit = mSharedPreferences.edit();
		mEdit.putBoolean(PREFS_SDK_AUTH_FLAG, flg);
		mEdit.commit();
	}
	
	public void removeSDKAuthFlag() {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(PREFS_SDK_AUTH_FLAG);
		mEdit.commit();
	}

	public void saveAppPush(int appPush) {
		mEdit = mSharedPreferences.edit();
		mEdit.putInt(PREFS_APP_PUSH, appPush);
		mEdit.commit();
	}
	
	public int readAppPush() {
		return mSharedPreferences.getInt(PREFS_APP_PUSH, 0);
	}
	
	public void saveNotificationTitle(String title) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(PREFS_APP_NOTIF_TITLE, title);
		mEdit.commit();
	}
	
	public String readNotificationTitle() {
		return mSharedPreferences.getString(PREFS_APP_NOTIF_TITLE, null);
	}
	
	public void clearCache() {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(PREFS_SDK_AUTH_FLAG);
		mEdit.remove(readVisitorId());
		mEdit.remove(PREFS_VISITOR_ID);
		mEdit.commit();
	}
}
