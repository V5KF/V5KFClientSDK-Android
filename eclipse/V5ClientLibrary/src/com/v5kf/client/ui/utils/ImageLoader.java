package com.v5kf.client.ui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.v5kf.client.R;
import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5Util;

public class ImageLoader {
	public static final int IMAGE_MIN_WH = 40; //dp
	public static final int IMAGE_MAX_WH = 220; //dp
	public static final int IMAGE_BASE_WH = 120; //dp
	
	/**
	 * 任务队列
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public ImageLoaderListener listener;
		public Context context;

		public PhotoToLoad(Context c, String u, ImageView i, ImageLoaderListener l) {
			context = c;
			url = u;
			imageView = i;
			listener = l;
		}
	}
	
	private class PhotosToLoad {
		public String url;
		public List<PhotoToLoad> photos;
		
		public PhotosToLoad(String url) {
			this.url = url;
		}
		
		public void addPhoto(PhotoToLoad pl) {
			if (photos == null) {
				photos = new ArrayList<PhotoToLoad>();
			}
			if (!hasPhoto(pl)) {
				photos.add(pl);
			}
		}
		
		public boolean hasUrl(String url, ImageView iv) {
			if (null == photos) {
				return false;
			}
			for (PhotoToLoad pl : photos) {
				if (pl.url.equals(url) && pl.imageView == iv) {
					return true;
				}
			}
			return false;
		}

		public boolean hasPhoto(PhotoToLoad photo) {
			if (null == photos) {
				return false;
			}
			for (PhotoToLoad pl : photos) {
				if (pl.url.equals(photo.url) && pl.imageView == photo.imageView) {
					return true;
				}
			}
			return false;
		}
	}
	
	private MemoryCache memoryCache;
	private FileCache fileCache;
//	private static Map<ImageView, String> imageViews = Collections
//			.synchronizedMap(new WeakHashMap<ImageView, String>());
	
	private static Map<String, PhotosToLoad> taskQueue = Collections
			.synchronizedMap(new WeakHashMap<String, PhotosToLoad>());
	
	private ExecutorService executorService;
	private boolean isSrc;	// src 还是 background
	
	private int mDefaultImg; // 加载中显示图片
	private int mFailureImag; // 加载失败显示图片
	
	private Context mContext;
	private ImageLoaderListener mListener;
	
//	public interface ImageLoaderListener {
//		public void onSuccess(String url, ImageView imageView);
//		public void onFailure(ImageLoader imageLoader, String url, ImageView imageView);
//	}
	public interface ImageLoaderListener {
		public void onSuccess(String url, ImageView imageView, Bitmap bmp);
		public void onFailure(ImageLoader imageLoader, String url, ImageView imageView);
	}

	/**
	 * @param context
	 *            上下文对象
	 * @param flag
	 *            true为source资源，false为background资源
	 */
	public ImageLoader(Context context, boolean srcFlag, int defaultImg, ImageLoaderListener listener) {
		if (mFailureImag == 0) {
			mFailureImag = R.drawable.v5_img_src_error;
		}
		memoryCache = new MemoryCache();
		fileCache = new FileCache(context, FileUtil.getImageCachePath(context));
		executorService = Executors.newFixedThreadPool(5);
		isSrc = srcFlag;
		this.mDefaultImg = defaultImg;
		this.mContext = context;
		this.mListener = listener;
	}
	
	public ImageLoader(Context context, boolean srcFlag, int defaultImg) {
		this(context, srcFlag, defaultImg, null);
	}

	public void DisplayImage(String url, ImageView imageView) {
		if (null == imageView) {
			return;
		}
		
		if (null == url || url.isEmpty()) {
			if (isSrc)
				imageView.setImageResource(mDefaultImg);
			else
				imageView.setBackgroundResource(mDefaultImg);
			return;
		}
		// 取消url编码
//		String u1 = url.substring(0, url.lastIndexOf("/") + 1);
//		String u2 = url.substring(url.lastIndexOf("/") + 1);
//		try {
//			u2 = URLEncoder.encode(u2, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		url = u1 + u2;
		
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			Logger.v("ImageLoader", "From MemoryCache:" + url);
			if (isSrc)
				imageView.setImageBitmap(bitmap);
			else
				imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			
			if (mListener != null) {
				mListener.onSuccess(url, imageView, bitmap);
			}
		} else {
			PhotosToLoad photos = taskQueue.get(url);
			if (photos != null) { /* 已有对应url请求 */
				if (!photos.hasUrl(url, imageView)) {
					photos.addPhoto(new PhotoToLoad(mContext, url, imageView, mListener));
				}
				return;
			} else {
				photos = new PhotosToLoad(url);
			}
			PhotoToLoad p = new PhotoToLoad(mContext, url, imageView, mListener);
			photos.addPhoto(p);
			queuePhoto(photos);
			
//			/* 判断已有url就等之前的加载完成 */
//			if (imageViews.values().contains(url)) { // 已有对应url无需再次请求
//				if (!imageViews.keySet().contains(imageView)) {				
//					imageViews.put(imageView, url); // 添加到任务map
//				}
//				return;
//			}
//			imageViews.put(imageView, url);
//			queuePhoto(url, imageView);
			
			if (isSrc)
				imageView.setImageResource(mDefaultImg);
			else
				imageView.setBackgroundResource(mDefaultImg);
		}
	}

//	private void queuePhoto(String url, ImageView imageView) {
//		PhotoToLoad p = new PhotoToLoad(mContext, url, imageView, mListener);
//		executorService.submit(new PhotosLoader(p));
//	}
	private void queuePhoto(PhotosToLoad photos) {
		executorService.submit(new PhotosLoader(photos));
	}

	public Bitmap getBitmap(String url) {
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			return bitmap;
		}
		try {
			File f = fileCache.getFile(url);
			// 从sd卡
			Bitmap b = onDecodeFile(f);
			if (b != null) {
				Logger.v("ImageLoader", "From FileCache:" + url);
				return b;
			} else { // 判断是否本地路径
				Bitmap localBmp = V5Util.ratio(
						url, 
						UIUtil.MAX_PIC_W * 2 / 3, 
						UIUtil.MAX_PIC_H * 2 / 3); // 压缩宽高
				if (localBmp != null) {
					Logger.v("ImageLoader", "From localFile:" + url);
					return localBmp;
				}
			}
			
			// 从网络
			Logger.v("ImageLoader", "ImageLoader-->download:" + url);
			HttpUtil.CopyStream(url, f);
			
			// 图片角度矫正
			UIUtil.correctBitmapAngle(f.getAbsolutePath());
			
			bitmap = onDecodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static Bitmap onDecodeFile(File f) {
		try {
			return BitmapFactory.decodeStream(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解码图像用来减少内存消耗
	 * 
	 * @param f
	 * @return
	 */
	public Bitmap decodeFile(File f) {
		try {
			// 解码图像大小
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			// 找到正确的刻度值，它应该是2的幂。
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	class PhotosLoader implements Runnable {
		PhotosToLoad photosToLoad;

		PhotosLoader(PhotosToLoad photos) {
			this.photosToLoad = photos;
		}

		@Override
		public void run() {
//			if (imageViewReused(photoToLoad))
//				return;		
			
			Bitmap bmp = getBitmap(photosToLoad.url);
			if (bmp == null) {
				Logger.e("ImageLoader", "getBitmap --> null");
			}
			memoryCache.put(photosToLoad.url, bmp);
			
			//Logger.d("ImageLoader", "memoryCache put:" + photoToLoad.url);
//			if (imageViewReused(photoToLoad))
//				return;
			
			for (PhotoToLoad pl : photosToLoad.photos) {
				BitmapDisplayer bd = new BitmapDisplayer(bmp, pl);
				if (pl.context instanceof Activity) {
					((Activity) pl.context).runOnUiThread(bd);
				} else {
					Activity a = (Activity) pl.imageView.getContext();
					a.runOnUiThread(bd);
				}
			}
			photosToLoad.photos.clear();
			taskQueue.remove(photosToLoad.url);
			
//			for (Entry<ImageView, String> entry : imageViews.entrySet()) { // 所有该url的imageview都进行加载
//				if (entry.getValue().equals(photoToLoad.url)) {
//					BitmapDisplayer bd = new BitmapDisplayer(bmp, entry.getKey(), entry.getValue());
//					if (mContext instanceof Activity) {
//						((Activity) mContext).runOnUiThread(bd);
//					} else {
//						Activity a = (Activity) entry.getKey().getContext();
//						a.runOnUiThread(bd);
//					}
//					imageViews.remove(entry.getKey());
//				}
//			}
//			Logger.i("ImageLoader", "imageViews.size = " + imageViews.size());
		}
	}

//	boolean imageViewReused(PhotoToLoad photoToLoad) {
//		String tag = imageViews.get(photoToLoad.imageView);
//		if (tag == null || !tag.equals(photoToLoad.url))
//			return true;
//		return false;
//	}
//
//	boolean imageViewReused(ImageView imageView, String url) {
//		String tag = imageViews.get(imageView);
//		if (tag == null || !tag.equals(url))
//			return true;
//		return false;
//	}

	/**
	 * 显示位图在UI线程
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	class BitmapDisplayer implements Runnable {
//		ImageView imageView;
//		String url;
		Bitmap bitmap;
		PhotoToLoad photoToLoad;
//
		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

//		public BitmapDisplayer(Bitmap b, ImageView iv, String url) {
//			bitmap = b;
//			this.imageView = iv;
//			this.url = url;
//		}

		public void run() {
			Logger.i("ImageLoader", "BitmapDisplayer -> run");
//			if (imageViewReused(this.imageView, this.url)) {
//				return;
//			}
			if (bitmap != null) {
				Logger.d("ImageLoader", "BitmapDisplayer-> imageView:" + photoToLoad.imageView + " url:" + photoToLoad.url);
				if (isSrc)
					photoToLoad.imageView.setImageBitmap(bitmap);
				else
					photoToLoad.imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
				
				if (photoToLoad.listener != null) {
					photoToLoad.listener.onSuccess(photoToLoad.url, photoToLoad.imageView, bitmap);
				}
			} else { // 获取图片失败
				if (isSrc)
					photoToLoad.imageView.setImageResource(mFailureImag);
				else
					photoToLoad.imageView.setBackgroundResource(mFailureImag);
				
				if (photoToLoad.listener != null) {
					photoToLoad.listener.onFailure(ImageLoader.this, photoToLoad.url, photoToLoad.imageView);
				}
			}
		}
	}

	/**
	 * 在适当的时机清理图片缓存
	 */
	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	/**
	 * 在适当的时机清理内存图片缓存
	 */
	public void clearMemoryCache() {
		memoryCache.clear();
	}
	
	/**
	 * 缓存Bitmap图片，id可为url
	 * @param bmp
	 * @param id
	 * @throws IOException
	 */
	public void saveImage(Bitmap bmp, String id) throws IOException {
		memoryCache.put(id, bmp);
		File f = fileCache.getFile(id);
		Bitmap b = onDecodeFile(f);
		if (b != null) {
			Logger.d("ImageLoader", "[saveImage] Already in FileCache:" + id);
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
		    out.flush();
		    out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static Bitmap getBitmap(Context context, String url) {
		MemoryCache memoryCache = new MemoryCache();
		Bitmap bitmapmm = memoryCache.get(url);
		if (bitmapmm != null) {
			return bitmapmm;
		}
		
		FileCache fileCache = new FileCache(context, FileUtil.getImageCachePath(context));
		try {
			File f = fileCache.getFile(url);
			// 从sd卡
			Bitmap b = onDecodeFile(f);
			if (b != null) {
				Logger.v("ImageLoader", "From FileCache:" + url);
				return b;
			} else { // 判断是否本地路径
				Bitmap localBmp = V5Util.ratio(
						url, 
						UIUtil.MAX_PIC_W * 2 / 3, 
						UIUtil.MAX_PIC_H * 2 / 3); // 压缩宽高
				if (localBmp != null) {
					Logger.v("ImageLoader", "From localFile:" + url);
					return localBmp;
				}
			}
			
			// 从网络
			Bitmap bitmap = null;
			Logger.v("ImageLoader", "ImageLoader-->download:" + url);
			HttpUtil.CopyStream(url, f);
			
			// 图片角度矫正
			UIUtil.correctBitmapAngle(f.getAbsolutePath());
			
			bitmap = onDecodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 限定最小最大宽高进行缩放，类似微信图片显示
	 * @param context
	 * @param w
	 * @param h
	 * @return
	 */
	public static V5Size getScaledSize(Context context, int w, int h) {
		// 想要缩放的目标尺寸
		float rW = w;
		float rH = h;
		float wh = UIUtil.dp2px(IMAGE_BASE_WH, context);
		float max = UIUtil.dp2px(IMAGE_MAX_WH, context);
		float min = UIUtil.dp2px(IMAGE_MIN_WH, context);
		Logger.d("ImageLoader", "[getScale] ratio min:" + wh + " max:" + wh);
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可    
		float scale = 1.0f;//scale=1表示不缩放    
		scale = (float) Math.sqrt((wh*wh)/(w*h));
		Logger.d("ImageLoader", "ratio [getScale] :" + scale);
		if (scale <= 0) scale = 1.0f;
				
		rW = (int)(w * scale);
		rH = (int)(h * scale);
		if (rW < rH) {
			if (rW > min && rH > max) {
				rW = rW * (max / rH);
				if (rW < min) {
					rW = min;
				}
				rH = (int)max;
			} else if (rW < min && rH < max) {
				rH = rH * (min / rW);
				if (rH > max) {
					rH = max;
				}
				rW = (int)min;
			} else if (rW < min && rH > max) {
				rW = (int)min;
				rH = (int)max;
			}
		} else if (rW > rH) {
			if (rW < max && rH < min) {
				rW = rW * (min / rH);
				if (rW > max) {
					rW = max;
				}
				rH = (int)min;
			} else if (rW > max && rH > min) {
				rH = rH * (max / rW);
				if (rH < min) {
					rH = min;
				}
				rW = (int)max;
			} else if (rW > max && rH < min) {
				rW = (int)max;
				rH = (int)min;
			}
		}
		// image scale type: centerCrop
		return new V5Size((int)rW, (int)rH);
	}
}
