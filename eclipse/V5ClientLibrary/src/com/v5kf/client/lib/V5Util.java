package com.v5kf.client.lib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.text.TextUtils;

import com.v5kf.client.lib.entity.V5ImageMessage;

public class V5Util {
	
//	/** 
//	 * 判断某个服务是否正在运行的方法 
//	 *  
//	 * @param context 
//	 * @param serviceName 
//	 *            是包名+服务的类名（例如：com.v5kf.client.lib.WsClientService） 
//	 * @return true代表正在运行，false代表服务没有正在运行 
//	 */  
//	public static boolean isServiceWork(Context context, String serviceName) {  
//	    boolean isWork = false;  
//	    ActivityManager myAM = (ActivityManager) context  
//	            .getSystemService(Context.ACTIVITY_SERVICE);  
//	    List<RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);  
//	    if (myList.size() <= 0) {
//	        return false;  
//	    }  
//	    for (int i = 0; i < myList.size(); i++) {  
//	        String mName = myList.get(i).service.getClassName().toString();  
//	        if (mName.equals(serviceName)) {  
//	            isWork = true;  
//	            break;  
//	        }  
//	    }  
//	    return isWork;  
//	}
	
	/**
     * 
     * unicode 解码
     * @param theString
     * @return
     */
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        
        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            value = (value << 4) + aChar - '0';
                            break;
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                            value = (value << 4) + 10 + aChar - 'a';
                            break;
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                            value = (value << 4) + 10 + aChar - 'A';
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
            	outBuffer.append(aChar);
        	}
        return outBuffer.toString();
    }
    
	/**
	 * SiteInfo请求地址
	 * @param context
	 * @return
	 */
	public static String getSiteInfoUrl(Context context) {
		V5ClientConfig config = V5ClientConfig.getInstance(context);;
		return String.format(V5ClientConfig.getSiteinfoFormatURL(), config.getSiteId()) ;
	}
	
//	/**
//	 * 检查网络是否连接
//	 * @param context
//	 * @return
//	 */
//	public static boolean isConnected(Context context) {
//		ConnectivityManager cm = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo info = cm.getActiveNetworkInfo();
//        if (info != null && info.isConnected()) {
//            return true;
//        }
//        return false;
//	}
	
//	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//	    int height = options.outHeight;
//	    int width = options.outWidth;
//	    int inSampleSize = 1;
//	    if ((height > reqHeight) || (width > reqWidth)) {
//	      int halfHeight = height / 2;
//	      int halfWidth = width / 2;
//
//	      while ((halfHeight / inSampleSize > reqHeight) && (halfWidth / inSampleSize > reqWidth)) {
//	        inSampleSize *= 2;
//	      }
//	    }
//	    return inSampleSize;
//	  }
//
//	  private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight)
//	  {
//	    Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
//	    if (src != dst) {
//	      src.recycle();
//	    }
//	    return dst;
//	  }
//
//	  public static Bitmap decodeSampledBitmapFromFd(String pathName)
//	  {
//	    BitmapFactory.Options options = new BitmapFactory.Options();
//	    options.inJustDecodeBounds = true;
//	    int reqWidth = 1024;
//	    int reqHeight = 1024;
//	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//	    options.inJustDecodeBounds = false;
//	    Bitmap src = BitmapFactory.decodeFile(pathName, options);
//	    return createScaleBitmap(src, reqWidth, reqHeight);
//	  }

	  /**
	   * Bitmap图片转jpeg字节流
	   * @param image
	   * @param maxSize 单位为KB 
	   * @return
	   */
	public static byte[] compressImageToByteArray(Bitmap image, int maxSize) {
		if (null == image) {
			return null;
		}
		Logger.d("Util", "compressImage before>>:" + image.getRowBytes() * image.getHeight());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int options = 95;
		image.compress(Bitmap.CompressFormat.JPEG, options, bos);  
		// Compress by loop
		while (bos.toByteArray().length / 1024 > maxSize) {  
			// Clean up os  
			bos.reset();  
			// interval 10  
			options -= 5;
			image.compress(Bitmap.CompressFormat.JPEG, options, bos);  
		}
		Logger.d("Util", "compressImage>>:" + bos.size());
		image.recycle();
		return bos.toByteArray();
	}

	public static long getFileSize(String path) {
		File f = new File(path);
		if (f.exists() && !f.isDirectory()) {
			return f.length();
		}
		return 0;
	}
	
	public static long getFileSize(File file) {
	    if ((file == null) || (!file.exists()))
	      return 0L;
	    if (!file.isDirectory())
	      return file.length();
	    List<File> dirs = new LinkedList<File>();
	    dirs.add(file);
	    long result = 0L;
	    while (!dirs.isEmpty()) {
	      File dir = (File)dirs.remove(0);
	      if (!dir.exists())
	        continue;
	      File[] listFiles = dir.listFiles();
	      if ((listFiles == null) || (listFiles.length == 0))
	        continue;
	      for (File child : listFiles) {
	        result += child.length();
	        if (child.isDirectory()) {
	          dirs.add(child);
	        }
	      }
	    }
	    return result;
	  }

	  /**
	   * 读取图片文件并对150Kb以上图片进行像素压缩，返回Bitmap对象
	   * @param imagePath
	   * @return
	   */
	  public static Bitmap getCompressBitmap(String imagePath)
	  {
	    long size_file;
	    try
	    {
	      size_file = getFileSize(new File(imagePath));
	    } catch (Exception e) {
	      size_file = 0L;
	    }
	    if (size_file == 0L) {
	      return null;
	    }
	    size_file /= 1000L;
	    Logger.d("Util", new StringBuilder().append("FileSize= ").append(size_file).toString());
	    int ample_size = 1;

	    if ((size_file <= 800L) && (size_file >= 400L))
	    {
	      ample_size = 2;
	    }
	    else if ((size_file > 801L) && (size_file < 1600L))
	    {
	      ample_size = 2;
	    }
	    else if ((size_file >= 1600L) && (size_file < 3200L))
	    {
	      ample_size = 4;
	    }
	    else if ((size_file >= 3200L) && (size_file <= 4800L))
	    {
	      ample_size = 4;
	    }
	    else if (size_file >= 4800L)
	    {
	      ample_size = 8;
	    } else {
//	    	BitmapFactory.Options newOpts = new BitmapFactory.Options();    
//	        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容  
//	        newOpts.inJustDecodeBounds = false;
//	    	Bitmap bitmap = BitmapFactory.decodeFile(imagePath, newOpts);
//	    	return bitmap;
	    }

	    BitmapFactory.Options bitoption = new BitmapFactory.Options();
	    bitoption.inJustDecodeBounds = true;
	    Bitmap bitmapPhoto = BitmapFactory.decodeFile(imagePath, bitoption);
	    bitoption.inJustDecodeBounds = false;
	    bitoption.inSampleSize = ample_size;
	    bitoption.inDither = false;    /*不进行图片抖动处理*/
	    bitoption.inPreferredConfig = Config.RGB_565;  // 一个像素占两字节

	    bitmapPhoto = BitmapFactory.decodeFile(imagePath, bitoption);
	    if (null == bitmapPhoto) {
	    	return null;
	    }
	    ExifInterface exif = null;
	    try {
	      exif = new ExifInterface(imagePath);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    int orientation = 0;
	    if (exif != null) {
	      orientation = exif.getAttributeInt("Orientation", 1);
	    }
	    Matrix matrix = new Matrix();
	    Bitmap bitmap;
	    if (orientation == 3) {
	      matrix.postRotate(180.0F);
	      bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
	    }
	    else
	    {
	      if (orientation == 6) {
	        matrix.postRotate(90.0F);
	        bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
	      }
	      else
	      {
	        if (orientation == 8) {
	          matrix.postRotate(270.0F);
	          bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
	        } else {
	          matrix.postRotate(0.0F);
	          bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
	        }
	      }
	    }
	    return bitmap;
	  }

//	  /**
//	   * Bitmap对象转输入流
//	   * @param bitmap
//	   * @return
//	   */
//	  public static InputStream bitmapToInputStream(Bitmap bitmap)
//	  {
//	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//	    InputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//	    return isBm;
//	  }
//
//	  /**
//	   * 压缩Bitmap图片
//	   * @param image
//	   * @return
//	   */
//	  public static Bitmap compressBitmap(Bitmap image)
//	  {
//	    while (true) {
//	      float width = image.getWidth();
//	      float height = image.getHeight();
//	      float max = width > height ? width : height;
//	      float minSize = 1024.0F / max;
//	      if ((width <= 1024.0F) && (height <= 1024.0F))
//	        break;
//	      Matrix matrix = new Matrix();
//
//	      matrix.postScale(minSize, minSize);
//	      image = Bitmap.createBitmap(image, 0, 0, (int)width, (int)height, matrix, true);
//	    }
//
//	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//	    image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//	    int options = 80;
//	    while (bos.toByteArray().length / 1024 > 150) {
//	      bos.reset();
//	      image.compress(Bitmap.CompressFormat.JPEG, options, bos);
//	      options -= 20;
//	    }
//	    ByteArrayInputStream isBm = new ByteArrayInputStream(bos.toByteArray());
//	    return BitmapFactory.decodeStream(isBm);
//	  }
//
//	  /**
//	   * 保存Bitmap图片到指定路径
//	   * @param ctx
//	   * @param filePath
//	   * @param mBitmap
//	   * @return
//	   */
//	  public static String saveBitmap(Context ctx, String filePath, Bitmap mBitmap) {
//	    File f = new File(new StringBuilder().append(getPicStorePath(ctx)).append("/").append(filePath).toString());
//	    try {
//	      f.createNewFile();
//
//	      FileOutputStream fOut = new FileOutputStream(f);
//	      mBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fOut);
//	      fOut.flush();
//	      fOut.close();
//	    } catch (IOException e) {
//	      e.printStackTrace();
//	      return null;
//	    }
//	    return f.getAbsolutePath();
//	  }
//	  
//	  /**
//	   * 获取图片存储地址
//	   * @param ctx
//	   * @return
//	   */
//	  public static String getPicStorePath(Context ctx) {
//	    File file = ctx.getExternalFilesDir(null);
//	    if (!file.exists()) {
//	      file.mkdir();
//	    }
//	    File imageStoreFile = new File(new StringBuilder().append(file.getAbsolutePath()).append("/v5kf").toString());
//	    if (!imageStoreFile.exists()) {
//	      imageStoreFile.mkdir();
//	    }
//	    return imageStoreFile.getAbsolutePath();
//	  }
//	  
//	  public static boolean copyFile(String oldPath, String newPath)
//	  {
//	    boolean isok = true;
//	    try {
//	      int byteread = 0;
//	      File oldfile = new File(oldPath);
//	      if (oldfile.exists()) {
//	        InputStream inStream = new FileInputStream(oldPath);
//	        FileOutputStream fs = new FileOutputStream(newPath);
//	        byte[] buffer = new byte[1024];
//	        while ((byteread = inStream.read(buffer)) != -1) {
//	          fs.write(buffer, 0, byteread);
//	        }
//	        fs.flush();
//	        fs.close();
//	        inStream.close();
//	      } else {
//	        isok = false;
//	      }
//	    } catch (Exception e) {
//	      isok = false;
//	    }
//	    return isok;
//	  }
	  
	/** 
     * Compress image by pixel, this will modify image width/height.  
     * Used to get thumbnail 
     *  
     * @param imgPath image path 
     * @param pixelW target pixel of width 
     * @param pixelH target pixel of height 
     * @return 
     */  
    public static Bitmap ratio(String imgPath, float pixelW, float pixelH) {
    	Logger.d("V5Util", "ratio--> w:" + pixelW + " h:" + pixelH);
        BitmapFactory.Options newOpts = new BitmapFactory.Options();    
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容  
        newOpts.inJustDecodeBounds = true;  
        newOpts.inPreferredConfig = Config.RGB_565;
        // Get bitmap info, but notice that bitmap is null now    
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);  
            
        newOpts.inJustDecodeBounds = false;    
        int w = newOpts.outWidth;    
        int h = newOpts.outHeight;    
        // 想要缩放的目标尺寸  
        float hh = pixelH; // 设置高度为240f时，可以明显看到图片缩小了  
        float ww = pixelW; // 设置宽度为120f，可以明显看到图片缩小了  
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可    
        int be = 1;//be=1表示不缩放    
        if (w > h && w > ww) { //如果宽度大的话根据宽度固定大小缩放    
            be = (int) (newOpts.outWidth / ww);    
        } else if (w < h && h > hh) { //如果高度高的话根据宽度固定大小缩放    
            be = (int) (newOpts.outHeight / hh);    
        }    
        if (be <= 0) be = 1;    
        newOpts.inSampleSize = be; //设置缩放比例  
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了  
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);  
        // 压缩好比例大小后再进行质量压缩  
//	        return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除  
        return bitmap;
    }
    

	/**
	 * 当前时间long millisecond
	 * @param getCurrentLongTime DateUtil 
	 * @return long
	 * @return
	 */
	public static long getCurrentLongTime() {
		return (new Date()).getTime();
	}
	
	/**
	 * 获取指定格式的当前日期字符串
	 * @param getCurrentTime DateUtil 
	 * @return String
	 * @param format
	 * @return
	 */
	public static String getCurrentTime(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		String currentTime = sdf.format(date);
		return currentTime;
	}

	/**
	 * 字符串转long格式日期TimeInMillis
	 * @param stringDateToLong DateUtil 
	 * @return long
	 * @param dateStr
	 * @return
	 */
	@SuppressLint("SimpleDateFormat") 
	public static long stringDateToLong(String dateStr) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	/**
	 * 当前时间字符串
	 * @return
	 */
	public static String getCurrentTime() {
		return getCurrentTime("yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 指定长度随机字符串
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {   
        StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");   
        StringBuffer sb = new StringBuffer();   
        Random random = new Random();   
        int range = buffer.length();   
        for (int i = 0; i < length; i ++) {   
            sb.append(buffer.charAt(random.nextInt(range)));   
        }   
        return sb.toString();   
    }
	
	/**
	 * 获得指定类型和名称的资源ID,如：getIdByName(context, "layout", "v5_chat_client_activity");
	 * @param context
	 * @param className
	 * @param resName
	 * @return
	 */
	public static int getIdByName(Context context, String defType, String resName) {
		String packageName = context.getPackageName();
		int indentify = context.getResources().getIdentifier(resName, defType, packageName);
		return indentify;
	}
	
	/**
	 * 16~32字节hash
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String hash(String data) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(data.getBytes());
		StringBuffer buf = new StringBuffer();
		byte[] bits = md.digest();
		for(int i = 0; i < bits.length; i++){
			int a = bits[i];
			
			// 32字节
			if(a<0) a+=256;
			if(a<16) buf.append("0");
			
			// 16~32字节随机
//			if(a<0) a+=256;
			
			// 16字节
//			a = a % 16;
//			if(a<0) a+=16;
			buf.append(Integer.toHexString(a));
		}
		return buf.toString();
	}
	
	/** 
     * 得到amr的时长 
     *  
     * @param file 
     * @return 
     * @throws IOException 
     */  
    public static long getAmrDuration(File file) throws IOException {  
        long duration = 0;  
//        int[] packedSize = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };  
//        RandomAccessFile randomAccessFile = null;  
//        try {  
//            randomAccessFile = new RandomAccessFile(file, "rw");  
//            long length = file.length();//文件的长度  
//            int pos = 6;//设置初始位置  
//            int frameCount = 0;//初始帧数  
//            int packedPos = -1;  
//            /////////////////////////////////////////////////////  
//            byte[] datas = new byte[1];//初始数据值  
//            while (pos <= length) {  
//                randomAccessFile.seek(pos);  
//                if (randomAccessFile.read(datas, 0, 1) != 1) {  
//                    duration = length > 0 ? ((length - 6) / 650) : 0;  
//                    break;  
//                }  
//                packedPos = (datas[0] >> 3) & 0x0F;  
//                pos += packedSize[packedPos] + 1;  
//                frameCount++;  
//            }  
//            /////////////////////////////////////////////////////  
//            duration += frameCount * 20;//帧数*20  
//        } finally {  
//            if (randomAccessFile != null) {  
//                randomAccessFile.close();  
//            }  
//        }  
        MediaPlayer player = new MediaPlayer();
        try {
        	player.setDataSource(file.getAbsolutePath());
        	player.prepare();
        	duration = player.getDuration();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        player.release();
        player = null;
        return duration;  
    }
    
    
    /**
	 * 优先加载本地图片路径，然后返回缩略图URL
	 * @param imageMessage
	 * @param siteId
	 * @return
	 */
	public static String getThumbnailUrlOfImage(V5ImageMessage imageMessage, String siteId) {
		String picUrl = imageMessage.getFilePath();
		if (!TextUtils.isEmpty(picUrl)) { // 优先判断本地图片，否则加载网络图片
			return picUrl;
		}

		picUrl = imageMessage.getPic_url();
		if (!TextUtils.isEmpty(picUrl)) {
			if (V5ClientConfig.USE_THUMBNAIL && picUrl.contains("image.myqcloud.com/")) { // 来自万象优图
				picUrl = picUrl + "/thumbnail";
			} else if (V5ClientConfig.USE_THUMBNAIL && 
					(picUrl.contains("mmbiz.qpic.cn/mmbiz/") || picUrl.contains("chat.v5kf.com/"))) {
				if (TextUtils.isEmpty(picUrl) && imageMessage.getMessage_id() != null && !imageMessage.getMessage_id().isEmpty()) {
					picUrl = String.format(V5ClientConfig.getPictureThumbnailFormatURL(), siteId, imageMessage.getMessage_id());
				} else {
					picUrl = picUrl + "/thumbnail";
				}
			}
		} else if (!TextUtils.isEmpty(imageMessage.getMedia_id())) {
			imageMessage.setPic_url(String.format(V5ClientConfig.getResourceFormatURL(), siteId, imageMessage.getMessage_id()));
			picUrl = String.format(V5ClientConfig.USE_THUMBNAIL ? V5ClientConfig.getPictureThumbnailFormatURL() : V5ClientConfig.getResourceFormatURL(), siteId, imageMessage.getMessage_id());
		}
		
		return picUrl;
	}
	
	public static String getImageMimeType(String file) {
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(file, options);
    	String type = options.outMimeType;
    	Logger.i("V5Util", "MimeType:" + type);
    	if (!TextUtils.isEmpty(type)) {
    		if (type.length() > 6) {
    			type = type.substring(6, type.length());
    		}
    		return type;
    	} else {
    		return null;
    	}
    }
    
    public static boolean isValidImageMimeType(String type) {
    	String types = V5ClientConfig.IMAGE_TYPE_SUPPORTED;
    	if (type != null && types.contains(type)) {
    		return true;
    	}
    	return false;
    }
}
