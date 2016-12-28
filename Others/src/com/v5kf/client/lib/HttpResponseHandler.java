package com.v5kf.client.lib;

import android.content.Context;

public abstract class HttpResponseHandler {
	private Context mContext;
	public HttpResponseHandler(Context context) {
		this.setContext(context);
	}
	public abstract void onSuccess(int statusCode, String responseString);
	public abstract void onFailure(int statusCode, String responseString);
	public Context getContext() {
		return mContext;
	}
	public void setContext(Context mContext) {
		this.mContext = mContext;
	}
}
