package com.v5kf.client.ui.utils;

import java.io.File;

import com.v5kf.client.lib.Logger;

import android.content.Context;

public class FileCache {

	private File cacheDir;

	public FileCache(Context context, String path) {
		// 找一个用来缓存图片的路径
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(path);
		else
			cacheDir = context.getCacheDir();
		
		if (!cacheDir.exists()) {
			Logger.d("FileCache", "dir not exists, mkdirs");
			cacheDir.mkdirs();
		}
	}

	public File getFile(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		return f;
	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}
}
