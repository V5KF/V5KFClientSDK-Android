package com.v5kf.client.lib;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.v5kf.client.lib.NetworkManager.NetworkListener;
import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.V5WebSocketHelper.WebsocketListener;

public class V5ClientService extends Service implements NetworkListener, WebsocketListener {

	public static final String TAG = "V5ClientService";
	private static final String ACTION_ALARM = "com.v5kf.client.alarm";
	private static final int HDL_CONNECT = 11;
	public static final String ACTION_SEND = "com.v5kf.client.send"; 
	public static final String ACTION_STOP = "com.v5kf.client.stop"; 

	private ServiceReceiver mMsgReceiver;
	private NetworkManager mNetReceiver;
	private ServiceHandler mHandler;
	private String mUrl;
	private static V5WebSocketHelper mClient;
	
	private int mRetryCount = 0;
	
	private boolean _connectBlock = false;
	
	protected static boolean isConnected() {
		if (mClient != null && mClient.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	protected static void reConnect(Context context) {
		Logger.i(TAG, "[reConnect]");
		if (mClient != null) {
			mClient.disconnect();
		}
		Intent i = new Intent(context, V5ClientService.class);
		context.startService(i);
	}

	protected static void stop() {
		if (mClient != null) {
			mClient.disconnect(1000, "Normal close");
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (Build.VERSION.SDK_INT < 18)
			startForeground(-1213, new Notification());
//		else 
//			startForeground(0, new Notification());
		initService();
	}
	
	private void initService() {
		mHandler = new ServiceHandler(this);
		
		mMsgReceiver = new ServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_ALARM);
		filter.addAction(ACTION_SEND);
		registerReceiver(mMsgReceiver, filter);
		//Logger.e(TAG, "onCreate -> registerReceiver");
		
		mNetReceiver = new NetworkManager();
		NetworkManager.init(getApplicationContext());
		registerReceiver(
				mNetReceiver, 
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		NetworkManager.addNetworkListener(this);
		initAlarm();
	}
	
	/**
	 * 定时任务初始化
	 */
	private void initAlarm() {
		Intent intent = new Intent(ACTION_ALARM);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 20 * 1000, V5ClientConfig.getInstance(getApplicationContext()).getHeartBeatTime(), pi);
		//Logger.v(TAG, "[Alarm - start] -> com.v5kf.client.alarm");
	}
	
	/**
	 * 取消定时任务
	 */
	private void cancelAlarm() {
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);  
        Intent intent = new Intent(ACTION_ALARM);  
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        if (pi != null){  
            am.cancel(pi);
        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.d(TAG, "[onStartCommand]");
		mRetryCount = 0;
		/* 开启service前需确保认证授权authorization不为空 */
		connectWebsocket(true);
		return super.onStartCommand(intent, flags, startId);
	}
	
	private synchronized void connectWebsocket() {
		connectWebsocket(false);
	}
	
	private synchronized void connectWebsocket(boolean forceNew) {
		if (V5ClientAgent.getInstance().isExit()) {
			Logger.v(TAG, "[connectWebsocket] isExit return");
			return;
		}
		if (mClient != null && mClient.isConnected()) {
			Logger.v(TAG, "[connectWebsocket] isConnected return");
			return;
		}
		if (_connectBlock) {
			Logger.v(TAG, "[connectWebsocket] _block return");
			return;
		}
		_connectBlock = true;
		
		Logger.v(TAG, "[connectWebsocket] auth:" +  V5ClientConfig.getInstance(this).getAuthorization());
		V5ClientConfig config = V5ClientConfig.getInstance(this);
		
		if (mClient != null) { // 已连接
			mClient.disconnect();
			mClient = null;
		}
//		if (mClient != null && mClient.isConnected()) { // 已连接
//			Logger.i(TAG, "[connectWebsocket] -> mClient != null && mClient.isConnected() = true");
//			if (forceNew) {
////				Logger.i(TAG, "disconnect and then forceNew client");
////				mClient.disconnect(4001, "Stop and new client");
//				mClient.disconnect();
//				mClient = null;
//				mUrl = String.format(Locale.CHINA, V5ClientConfig.getWSFormstURL(), config.getAuthorization()); 
//				mClient = new V5WebSocketHelper(URI.create(mUrl), this, null);
//				mClient.connect();
//				return;
//			} else {
//				_block = false;
//				return;
//			}
//		} else if (mClient != null) {
//			Logger.i(TAG, "[connectWebsocket] -> mClient != null && mClient.isConnected() = false");
//			if (forceNew) {
//				Logger.i(TAG, "forceNew client");
//				mClient.disconnect();
//				mClient = null;
//				mUrl = String.format(Locale.CHINA, V5ClientConfig.getWSFormstURL(), config.getAuthorization()); 
//				mClient = new V5WebSocketHelper(URI.create(mUrl), this, null);
//			}			
//			mClient.connect();
//			if (mUrl != null) {
//				Logger.d(TAG, "url:" + mUrl);
//			}
//		} else {
//			Logger.i(TAG, "[connectWebsocket] -> mClient == null");
			if (config.getAuthorization() == null) {
				// [修改]认证过期或者失效
				if (mRetryCount < 3) {
					try {
						V5ClientAgent.getInstance().doAccountAuth();
						mRetryCount++;
					} catch (JSONException e) {
						//e.printStackTrace();
						Log.e(TAG, "", e);
					}
				} else {
					mRetryCount = 0;
					V5ClientConfig.getInstance(this).shouldUpdateUserInfo();
					// [修改]通知界面是否重试
					V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionWSAuthFailed, "authorization failed"));
				}
				_connectBlock = false;
				return;
			}
			mUrl = String.format(Locale.CHINA, V5ClientConfig.getWSFormstURL(), config.getAuthorization()); 
			mClient = new V5WebSocketHelper(URI.create(mUrl), this, null);
			//Logger.d(TAG, "visitor_id:" + config.getV5VisitorId());
			mClient.connect();
			if (mUrl != null) {
				Logger.d(TAG, "mUrl:" + mUrl);
			}
//		}
	}
	
	private void keepService() {
		Logger.v(TAG, "[keepService] connect:" + isConnected() + " network:" + NetworkManager.isConnected(this));
		if (NetworkManager.isConnected(this) ) { // 判断是否连接
			if (isConnected()) {
				Logger.v(TAG, "[keepService] -> connected");
				mClient.ping();
			} else {
				Logger.v(TAG, "[keepService] -> not connect -> try connect");
				connectWebsocket();
			}
		} else {
			Logger.d(TAG, "[keepService] -> Network not connect");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.w(TAG, "V5ClientService -> onDestroy");
		if (mClient != null) {
			// 发送断连帧
			mClient.disconnect(1000, "Normal close");
			mClient = null;
		}
		//Logger.e(TAG, "onDestroy -> unregisterReceiver");
		unregisterReceiver(mMsgReceiver);
		unregisterReceiver(mNetReceiver);
		NetworkManager.removeNetworkListener(this);
		cancelAlarm();
		
//		if (!V5ClientAgent.getInstance().isExit()) {
//			Intent restartIntent = new Intent(getApplicationContext(), V5ClientService.class);
//			startService(restartIntent);
//		}
	}
	
	
	static class ServiceHandler extends Handler {
		
		WeakReference<V5ClientService> mService;
		
		public ServiceHandler(V5ClientService activity) {
			mService = new WeakReference<V5ClientService>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (null == mService.get()) {
				Logger.w(TAG, "ServiceHandler has bean GC");
				return;
			}
			switch (msg.what) {
			case HDL_CONNECT:
				mService.get().connectWebsocket();
				break;
			default:
				break;
			}
		}
	}
	
	class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			Logger.v(TAG, "<>onReceiver<>:" + intent.getAction());
			if (intent.getAction().equals(ACTION_ALARM)) {
				// 定时心跳包
				if (V5ClientConfig.getInstance(getApplicationContext()).isHeartBeatEnable()) {
					keepService();
				}
			} else if (intent.getAction().equals(ACTION_SEND)) {
				// 发送
				String msg = intent.getStringExtra("v5_message");
				if (null != msg) {
					sendMessage(msg);
				}
			} else if (intent.getAction().equals(ACTION_STOP)) {
				// 停止服务
				if (mClient != null) {
					mClient.disconnect();
				}
				stopSelf();
				Logger.d(TAG, "onReceiver:" + intent.getAction());
			}
		}
	}


	@Override
	public void onNetworkStatusChange(int netStatus, int oldStatus) {
		Logger.d(TAG, "[onNetworkStatusChange] -> " + netStatus);
		switch (netStatus) {
		case NetworkManager.NETWORK_MOBILE:
		case NetworkManager.NETWORK_WIFI:
			connectWebsocket();
			break;
			
		case NetworkManager.NETWORK_NONE:
			if (mClient != null) {
				mClient.disconnect();
			}
			V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNoNetwork, "no network"));
			break;
		}
	}

	public void sendMessage(String msg) {
		//Logger.d(TAG, ">>>sendMessage<<< :" + msg);
		if (isConnected()) {
			mClient.send(msg);
			Logger.i(TAG, ">>>sendMessage<<<:" + msg);
		} else {
			Logger.e(TAG, "[sendMessage] -> not connected");
			connectWebsocket();
			//V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNotConnected, "connection closed"));
		}
	}

	@Override
	public void onConnect() { // 连接成功后才可以开始调用消息接口
		Logger.i(TAG, ">>>onConnect<<< URL:" + mUrl);
		_connectBlock = false;
		
		mRetryCount = 0;
		
		V5ClientAgent.getInstance().onConnect();
	}

	@Override
	public void onMessage(String message) {
		Logger.d(TAG, ">>>onMessage<<<:" + message);
		V5ClientAgent.getInstance().onMessage(message);
	}

	@Override
	public void onMessage(byte[] data) {
		Logger.d(TAG, ">>>onMessage[byte]<<<" + data);
	}

	@Override
	public void onDisconnect(int code, String reason) {
		Logger.w(TAG, ">>>onDisconnect<<< [code:" + code + "]: " + reason);
		_connectBlock = false;
		// 断开重连判断
		if (V5ClientAgent.getInstance().isExit()) {
			// 不重连
			stopSelf();
			Logger.w(TAG, "[onDisconnect] stop service");
			return;
		} else {
			// 因网络原因重连
			if (NetworkManager.isConnected(this)) {
				switch (code) {
				case -1: // 正常关闭，不重连
					break;
				case 4001: // 手动强制重新连接
					mClient = null;
					connectWebsocket();
					break;
				case 4000: // 同一uid重复登录
					V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionConnectRepeat, "connection is cut off by same u_id"));
					break;
				case 1000: // 正常断开
				case 1005: //
				case 1006: // (websocket关闭前一连接abnormal)
					break;
				default:
//					connectWebsocket();
//					V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionConnectionError, "[" + code + "]" + reason));
					break;
				}
			} else {
				V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNoNetwork, "no network"));
			}
		}
	}

	@Override
	public void onError(Exception error) {
		_connectBlock = false;
		if (mClient == null) {
			Logger.e(TAG, "[onError] mClient == null");
			return;
		}
		if (error == null) {
			Logger.e(TAG, "[onError] error is null");
			return;
		}
		Logger.e(TAG, error.getClass() + ">>>onError<<<status code:" + mClient.getStatusCode() + " " + error.getMessage());
		if (isConnected()) {
			mClient.disconnect();
		}
		
		if (V5ClientAgent.getInstance().isExit()) {
			// 不重连
			stopSelf();
		} else {
			if (!NetworkManager.isConnected(this) || (error instanceof UnknownHostException)) {
				V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNoNetwork, "no network"));
			} else {
				if (mClient.getStatusCode() == 406 || mClient.getStatusCode() == 404) {
					Logger.d(TAG, "onError 40x retry:" + mRetryCount);
					if (mRetryCount < 3) {
						try {
							V5ClientAgent.getInstance().doAccountAuth();
							mRetryCount++;
						} catch (JSONException e) {
							//e.printStackTrace();
							Log.e(TAG, "", e);
						}
					} else {
						mRetryCount = 0;
						V5ClientConfig.getInstance(this).shouldUpdateUserInfo();
						V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionWSAuthFailed, "authorization failed"));
					}
				} else if ((error instanceof SocketTimeoutException) || (error.getMessage() != null &&
						(error.getMessage().toLowerCase(Locale.getDefault()).contains("timed out") ||
						error.getMessage().toLowerCase(Locale.getDefault()).contains("timeout") ||
						error.getMessage().toLowerCase(Locale.getDefault()).contains("time out")))) { // [超时]条件不充足
					if (mRetryCount < 3) {
						if (mClient != null) {
							mClient.disconnect();
						}
						mHandler.sendEmptyMessageDelayed(HDL_CONNECT, 50);
					} else {
						mRetryCount = 0;
						V5ClientAgent.getInstance().errorHandle(new V5KFException(
								V5ExceptionStatus.ExceptionSocketTimeout, "[" + mClient.getStatusCode() + "]" + error.getMessage()));
					}
				} else {
					//connectWebsocket(); //[修改]取消自动重连(死循环),在Activity中处理
					V5ClientAgent.getInstance().errorHandle(new V5KFException(
							V5ExceptionStatus.ExceptionConnectionError, "[" + mClient.getStatusCode() + "]" + error.getMessage()));
				}
			}
		}
	}
}
