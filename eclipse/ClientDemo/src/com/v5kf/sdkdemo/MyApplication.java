package com.v5kf.sdkdemo;

import java.util.List;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientConfig;
import com.v5kf.client.lib.callback.V5InitCallback;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Process;

public class MyApplication extends Application {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Logger.w("MyApplication", "onCreate application[PID:" + Process.myPid() + "]");
		if (isMainProcess()) { // 在一个主进程中初始化即可，多进程执行可能导致不可预料的后果
			Logger.w("MyApplication", "onCreate isMainProcess V5ClientAgent.init");
			V5ClientConfig.FILE_PROVIDER = "com.v5kf.sdkdemo.fileprovider";
			V5ClientAgent.init(this, "10000", "27100800d9e0", new V5InitCallback() {
				
				@Override
				public void onSuccess(String response) {
					// TODO Auto-generated method stub
					Logger.i("MyApplication", "V5ClientAgent.init(): " + response);
				}
				
				@Override
				public void onFailure(String response) {
					// TODO Auto-generated method stub
					Logger.e("MyApplication", "V5ClientAgent.init(): " + response);
				}
			});
		}
	}
	
	public boolean isMainProcess() {
		ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		String mainProcessName = getPackageName();
		int myPid = android.os.Process.myPid();
		for (RunningAppProcessInfo info : processInfos) {
			if (info.pid == myPid && mainProcessName.equals(info.processName)) {
				return true;
			}
		}
		return false;
	}
}
