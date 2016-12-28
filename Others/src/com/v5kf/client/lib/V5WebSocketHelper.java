package com.v5kf.client.lib;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


import com.v5kf.java.websocket.WebSocketImpl;
import com.v5kf.java.websocket.client.WebSocketClient;
import com.v5kf.java.websocket.drafts.Draft_17;
import com.v5kf.java.websocket.framing.FramedataImpl1;
import com.v5kf.java.websocket.framing.Framedata.Opcode;
import com.v5kf.java.websocket.handshake.ServerHandshake;

import android.util.Log;


public class V5WebSocketHelper {
	
	protected static final String TAG = "V5WebSocketHelper";
	private URI mUri;
	private WebsocketListener mListener;
	Map<String, String> mExtraHeaders;
	
	private static WebSocketClient mWSClient;
	private static boolean connected;

	public interface WebsocketListener {
        public void onConnect();
        public void onMessage(String message);
        public void onMessage(byte[] data);
        public void onDisconnect(int code, String reason);
        public void onError(Exception error);
    }
	
	public V5WebSocketHelper(URI uri, WebsocketListener listener, Map<String, String> extraHeaders) {
		mListener = listener;
		this.mExtraHeaders = extraHeaders;
		
		String query = "?" + uri.getQuery();
		/* 对auth进行urlencode */
        int indexQ = query.indexOf("?auth=");
        int indexA = query.indexOf("&auth=");
        if (indexQ != -1) {
        	String keyword = "?auth=";
            String auth = query.substring(indexQ + keyword.length());
            query = query.substring(0, indexQ);
            try {
				auth = java.net.URLEncoder.encode(auth, "utf-8");
			} catch (UnsupportedEncodingException e) {
				//e.printStackTrace();
				Log.e(TAG, "", e);
			}
            query = query + keyword + auth;
        } else if (indexA != -1) {
        	String keyword = "&auth=";
            String auth = query.substring(indexA + keyword.length());
            query = query.substring(0, indexA);
            try {
				auth = java.net.URLEncoder.encode(auth, "utf-8");
			} catch (UnsupportedEncodingException e) {
				//e.printStackTrace();
				Log.e(TAG, "", e);
			}
            query = query + keyword + auth;
        }
        
        String url = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + query;
        mUri = URI.create(url);
        
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Origin", "http://chat.v5kf.com");
		if (extraHeaders != null && extraHeaders.size() > 0) {
			headers.putAll(extraHeaders);
		}
		mWSClient = null;
		connected = false;
		mWSClient = new WebSocketClient(mUri, new Draft_17(), headers, V5ClientConfig.SOCKET_TIMEOUT) {
			
			@Override
			public void onOpen(ServerHandshake handshakedata) {
				// TODO Auto-generated method stub
				connected = true;
				if (mListener != null) {
					mListener.onConnect();
				}
			}
			
			@Override
			public void onMessage(String message) {
				// TODO Auto-generated method stub
				if (mListener != null) {
					mListener.onMessage(message);
				}
			}
			
			@Override
			public void onError(Exception ex) {
				// TODO Auto-generated method stub
				Logger.e(TAG, "[onError]: " + ex.getMessage());
				connected = false;
				if (mListener != null) {
					mListener.onError(ex);
					mListener = null;
				}
			}
			
			@Override
			public void onClose(int code, String reason, boolean remote) {
				// TODO Auto-generated method stub
				connected = false;
				if (mListener != null) {
					mListener.onDisconnect(code, reason);
					mListener = null;
				}
			}
		};
		WebSocketImpl.DEBUG = true;
	}
	
	public void connect() {
		if (connected) {
			Logger.w(TAG, "[connect] _block return");
			return;
		}
		if (mWSClient != null) {
			mWSClient.connect();
		} else {
			Logger.e(TAG, "[connect] websocket client null");
		}
	}

	public void connectBlocking() {
		if (connected) {
			Logger.w(TAG, "[connectBlocking] _block return");
			return;
		}
		if (mWSClient != null) {
			try {
				mWSClient.connectBlocking();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			Logger.e(TAG, "[connectBlocking] websocket client null");
		}
	}
	
	public void disconnect() {
		Logger.w(TAG, "[disconnect]");
		if (mWSClient != null) {
			mWSClient.closeConnection(1000, "Normal close");
		}
		mListener = null;
		connected = false;
	}

	public void disconnect(int code, String message) {
		Logger.w(TAG, "[disconnect:]");
		if (mWSClient != null) {
			mWSClient.closeConnection(code, message);
		}
		mListener = null;
		connected = false;
	}
	
	public boolean isConnected() {
		if (mWSClient != null && connected) {
			return mWSClient.isOpen();
		}
		return false;
	}
	
	public int getStatusCode() {
		if (mWSClient != null) {
			return mWSClient.getStatusCode();
		}
		return 0;
	}
	
	public void send(String text) {
		if (mWSClient != null) {
			mWSClient.send(text);
		} else {
			Logger.e(TAG, "[send] websocket client null");
		}
	}
	
	public void ping() {
		if (mWSClient != null) {
			FramedataImpl1 resp = new FramedataImpl1(Opcode.PING);
			resp.setFin(true);
			mWSClient.sendFrame(resp);
		} else {
			Logger.e(TAG, "[ping] websocket client null");
		}
	}
	
//	public void close(int code, String message) {
//		if (mWSClient != null) {
//			mWSClient.close(code, message);
//		} else {
//			Logger.e(TAG, "[close] websocket client null");
//		}
//		connected = false;
//	}
	
	public WebSocketClient getClient() {
		return mWSClient;
	}
}
