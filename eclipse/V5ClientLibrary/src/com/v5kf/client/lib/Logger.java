package com.v5kf.client.lib;

import android.util.Log;

/**
 * 
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-1 下午7:32:52
 * @package com.v5kf.mcss.config of MCSS-Native
 * @file Logger.java 
 *
 */
public class Logger {
 
	public static void e(String tag, String msg){
		if(V5ClientConfig.getLogLevel() >= V5ClientConfig.LOG_LV_ERROR)
			Log.e(tag, "<v5kf>" + msg);
	}

	public static void w(String tag, String msg){
		if(V5ClientConfig.getLogLevel() >= V5ClientConfig.LOG_LV_WARN)
			Log.w(tag, "<v5kf>" + msg);
	}
	
	public static void i(String tag, String msg){
		if(V5ClientConfig.getLogLevel() >= V5ClientConfig.LOG_LV_INFO)
			Log.i(tag, "<v5kf>" + msg);
	}

	public static void d(String tag, String msg){
		if(V5ClientConfig.getLogLevel() >= V5ClientConfig.LOG_LV_DEBUG)
			Log.d(tag, "<v5kf>" + msg);
	}
	
	public static void v(String tag, String msg){
		if(V5ClientConfig.getLogLevel() >= V5ClientConfig.LOG_LV_VERBOS)
			Log.v(tag, "<v5kf>" + msg);
	}
}
