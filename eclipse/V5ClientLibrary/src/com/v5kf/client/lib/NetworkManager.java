package com.v5kf.client.lib;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class NetworkManager extends BroadcastReceiver {
	
	public static ArrayList<NetworkListener> mListeners = new ArrayList<NetworkListener>();

	public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_MOBILE = 2;
    
    private static int mOldNetworkState;
    
    public static void init(Context context) {
    	mOldNetworkState = getNetworkState(context);
    }
    
    public static abstract interface NetworkListener {
    	public abstract void onNetworkStatusChange(int netStatus, int oldStatus);
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.i("NetworkManager", "onReceive:" + getNetworkState(context));
		if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			int oldStatus = mOldNetworkState; // 原状态
            int status = getNetworkState(context); // 现状态
            if (oldStatus != status && mListeners.size() > 0) { // 通知接口完成加载
                mOldNetworkState = status;
                for (NetworkListener listener : mListeners) {
                	Logger.w("NetworkManager", "onNetworkStatusChange:" + status);
                	listener.onNetworkStatusChange(status, oldStatus);
                }
            }
        }
	}

	public static boolean addNetworkListener(NetworkListener listener) {
		return mListeners.add(listener);
	}
	
	public static boolean removeNetworkListener(NetworkListener listener) {
		if (mListeners.size() > 0) { // 通知接口完成加载
            return mListeners.remove(listener);
        }
		return false;
	}
	
	
	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        return false;
	}
	

	/**
	 * 获得网络连接状态
	 * @param context
	 * @return
	 */
    public static int getNetworkState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (netInfo != null) {
        	// Wifi
            State state = netInfo.getState();
            if (state == State.CONNECTED || state == State.CONNECTING) {
                return NETWORK_WIFI;
            }

            // 3G
            state = netInfo.getState();
            if (state == State.CONNECTED || state == State.CONNECTING) {
                return NETWORK_MOBILE;
            }
        }
        
        return NETWORK_NONE;
    }
    
//    public static String getNetworkStateString(Context context) {
//    	ConnectivityManager connManager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        // Wifi
//        State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//                .getState();
//        if (state == State.CONNECTED || state == State.CONNECTING) {
//            return "wifi";
//        }
//
//        // 3G
//        state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
//                .getState();
//        if (state == State.CONNECTED || state == State.CONNECTING) {
//            return "mobile";
//        }
//        
//        return "none";
//    }
}
