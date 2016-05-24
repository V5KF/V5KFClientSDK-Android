package com.v5kf.sdkdemo;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.callback.V5InitCallback;

import android.app.Application;

public class MyApplication extends Application {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		// 初始化V5 SDK
		V5ClientAgent.init(this, new V5InitCallback() {
			
			@Override
			public void onSuccess(String response) {
				// TODO Auto-generated method stub
				Logger.i("MyApplication", "V5ClientAgent.init() success: " + response);
			}
			
			@Override
			public void onFailure(String response) {
				// TODO Auto-generated method stub
				Logger.e("MyApplication", "V5ClientAgent.init() failure: " + response);
			}
		});
	}
}
