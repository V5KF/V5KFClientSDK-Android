package com.v5kf.client.ui.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5Util;
import com.v5kf.client.lib.entity.V5Message;

public class MediaLoader {
	private static final String TAG = "MediaLoader";
	private FileCache fileCache;
	private ExecutorService executorService;
	private static Map<String, MediaCache> mediaCaches = Collections
			.synchronizedMap(new WeakHashMap<String, MediaCache>());
	
//	private V5Message mMessage;
	private String mUrl;
	private int mTryTimes = 0;
	private MediaLoaderListener mListener;
	private Context mContext;
	private Object mObj;
	
	public interface MediaLoaderListener {
		public void onSuccess(V5Message msg, Object obj, MediaCache media);
		public void onFailure(MediaLoader mediaLoader, V5Message msg, Object obj);
	}

	/**
	 * @param context
	 *            上下文对象
	 * @param flag
	 *            true为source资源，false为background资源
	 */
	public MediaLoader(Context context, Object obj, MediaLoaderListener listener) {
		fileCache = new FileCache(context, FileUtil.getMediaCachePath(context));
		executorService = Executors.newFixedThreadPool(5);
		this.mListener = listener;
		this.mContext = context;
		this.mObj = obj;
	}

	public MediaLoader(Context context) {
		this(context, null, null);
		this.mContext = context;
	}

	/**
	 * 加载媒体(本地路径、网络路径)
	 * @param url
	 */
	public void loadMedia(String url, V5Message message, MediaLoaderListener listener) {
		mTryTimes++;
		if (listener != null) {
			this.mListener = listener;
		}
		this.mUrl = url;
//		this.mMessage = message;
		// 取消url编码
//		String u1 = url.substring(0, url.lastIndexOf("/") + 1);
//		String u2 = url.substring(url.lastIndexOf("/") + 1);
//		try {
//			u2 = URLEncoder.encode(u2, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		url = u1 + u2;
		MediaCache media = mediaCaches.get(url);
		if (media != null) {
			Logger.i(TAG, "From MemoryCache:" + url);
			if (mListener != null) {
				mListener.onSuccess(message, this.mObj, media);
			}
		} else {
			queueMedia(url, message);
		}
	}

	private void queueMedia(String url, V5Message message) {
		PhotoToLoad p = new PhotoToLoad(url, message);
		executorService.submit(new PhotosLoader(p));
	}

	private MediaCache getMediaData(String url) {
		if (null == url) {
			return null;
		}
		try {
			File f = fileCache.getFile(url); // amr文件
			
			// 从sd卡获取
			if (f.exists()) {
//				MediaPlayer mediaPlayer = new MediaPlayer();
//				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//				mediaPlayer.setDataSource(f.getAbsolutePath());
				long duration = V5Util.getAmrDuration(f);
				MediaCache media = new MediaCache();
				media.setLocalPath(f.getAbsolutePath());
				media.setDuration(duration);
				Logger.d(TAG, "From FileCache:" + url + " duration:" + duration);
				return media;
			} else { // 判断是否本地路径
				File path = new File(url);
				if (path.exists()) {
//					MediaPlayer mediaPlayer = new MediaPlayer();
//					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//					mediaPlayer.setDataSource(path.getAbsolutePath());
					long duration = V5Util.getAmrDuration(path);
					MediaCache media = new MediaCache();
					media.setLocalPath(path.getAbsolutePath());
					media.setDuration(duration);
					return media;
				}
			}
			
			// 从网络
			HttpUtil.CopyStream(url, f);
			
//			MediaPlayer mediaPlayer = new MediaPlayer();
//			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//			mediaPlayer.setDataSource(f.getAbsolutePath());
			long duration = V5Util.getAmrDuration(f);
			MediaCache media = new MediaCache();
			media.setLocalPath(f.getAbsolutePath());
			media.setDuration(duration);
			Logger.d(TAG, "MediaLoader-->download:" + url + " duration:" + duration);
			return media;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 任务队列
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	private class PhotoToLoad {
		public String url;
		public V5Message mMessage;

		public PhotoToLoad(String u, V5Message message) {
			url = u;
			mMessage = message;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			MediaCache media = getMediaData(photoToLoad.url);
			if (media != null) {
				mediaCaches.put(photoToLoad.url, media);
			}
			Logger.d(TAG, "memoryCache put:" + photoToLoad.url);
			
			MediaDisplayer md = new MediaDisplayer(media, photoToLoad);
			
			if (mContext instanceof Activity) {
				Activity a = (Activity) mContext;
				a.runOnUiThread(md);
			} else {
				if (media != null) {
					if (mListener != null) {
						mListener.onSuccess(photoToLoad.mMessage, mObj, media);
					}
				} else { // 获取失败
					FileUtil.deleteFile(fileCache.getFile(photoToLoad.url).getAbsolutePath());
					if (mListener != null) {
						mListener.onFailure(MediaLoader.this, photoToLoad.mMessage, mObj);
					}
				}
			}
		}
	}
	
	/**
	 * 显示位图在UI线程
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	class MediaDisplayer implements Runnable {
		MediaCache media;
		PhotoToLoad photoToLoad;

		public MediaDisplayer(MediaCache m, PhotoToLoad p) {
			media = m;
			photoToLoad = p;
		}

		public void run() {
			if (media != null) {
				if (mListener != null) {
					mListener.onSuccess(photoToLoad.mMessage, mObj, media);
				}
			} else { // 获取失败
				FileUtil.deleteFile(fileCache.getFile(photoToLoad.url).getAbsolutePath());
				if (mListener != null) {
					mListener.onFailure(MediaLoader.this, photoToLoad.mMessage, mObj);
				}
			}
		}
	}

	/**
	 * 在适当的时机清理图片缓存
	 */
	public void clearCache() {
		mediaCaches.clear();
		fileCache.clear();
	}

	/**
	 * 在适当的时机清理内存图片缓存
	 */
	public static void clearMemoryCache() {
		mediaCaches.clear();
	}
	
	/**
	 * 缓存Bitmap图片，id可为url
	 * @param bmp
	 * @param id
	 * @throws IOException
	 */
	public void saveMedia(MediaCache media, String id) throws IOException {
		mediaCaches.put(id, media);
	}

	public int getmTryTimes() {
		return mTryTimes;
	}

	public void setmTryTimes(int mTryTimes) {
		this.mTryTimes = mTryTimes;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}
	
	/**
	 * 指定路径的文件重命名
	 * @param context
	 * @param path 		源文件
	 * @param url		重命名的url
	 */
	public static String copyPathToFileCche(Context context, File src, String url) {
		FileCache cache = new FileCache(context, FileUtil.getMediaCachePath(context));
		File f = cache.getFile(url); // 缓存的amr文件
		src.renameTo(f);
		return f.getAbsolutePath();
	}
}
