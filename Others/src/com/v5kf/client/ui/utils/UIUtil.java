package com.v5kf.client.ui.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5Util;

public class UIUtil {
	/* 权限申请返回码 */
	public static final int REQUEST_PERMISSION_CAMERA = 101; // 拍照权限
	public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 102; // 存储权限
	public static final int REQUEST_PERMISSION_RECORD_AUDIO = 103; // 录音权限
	public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 104; // GPS位置权限
	public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 105; // 网络位置权限
	public static final int REQUEST_PERMISSION_ALL = 100; // 全部权限
	
	public static final int MAX_PIC_H = 600;
	public static final int MAX_PIC_W = 480;
	
	/* 腾讯地图web service API */
	public static final String MAP_PIC_API_FORMAT = "http://apis.map.qq.com/ws/staticmap/v2/?center=%f,%f&zoom=15&size=300*200&maptype=roadmap&markers=size:small|color:0x207CC4|label:V|%f,%f&key=WWBBZ-5FQR4-QBWUP-DFRYQ-5Y4HH-2OBV6";
	public static final String MAP_WS_API_FORMAT = "http://apis.map.qq.com/ws/geocoder/v1/?location=%f,%f&key=WWBBZ-5FQR4-QBWUP-DFRYQ-5Y4HH-2OBV6&get_poi=1";
	
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
	
//	/**
//	 * 对于context.getResources().getIdentifier无法获取的数据,或者数组
//	 * 资源反射值
//	 * @paramcontext
//	 * @param name
//	 * @param type
//	 * @return
//	 */
//	private static Object getResourceId(Context context, String name, String type) {
//		String className = context.getPackageName() + ".R";
//		try {
//			Class<?> cls = Class.forName(className);
//			for (Class<?> childClass : cls.getClasses()) {
//				String simple = childClass.getSimpleName();
//				if (simple.equals(type)) {
//					for (Field field : childClass.getFields()) {
//						String fieldName = field.getName();
//						if (fieldName.equals(name)) {
//							return field.get(null);
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	/**
//	 *context.getResources().getIdentifier无法获取到styleable的数据
//	 * @param context
//	 * @param name
//	 * @return
//	 */
//	public static int getStyleable(Context context, String name) {
//		return ((Integer)getResourceId(context, name, "styleable")).intValue();
//	}
//
//	/**
//	 * 获取styleable的ID号数组
//	 * @paramcontext
//	 * @param name
//	 * @return
//	 */
//	public static int[] getStyleableArray(Context context,String name) {
//		return (int[])getResourceId(context, name, "styleable");
//	}
	
	
	/**
	 * 得到设备屏幕的宽度
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 得到设备屏幕的高度
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	
	/**
     * dp转 px.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int dp2px(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    /**
     * px转dp.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int px2dp(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) ((value * 160) / scale + 0.5f);
    }

	public static void closeSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
	}
	
	private static String getGeocoderUrl(double lat, double lng) {
		return String.format(Locale.CHINA, MAP_WS_API_FORMAT, lat, lng);
	}
	
	public static void getLocationTitle(final Context context, final double x, final double y, final TextView titleTv) {
		final String url = getGeocoderUrl(x, y);
		final URLCache urlCache = new URLCache();
		String value = urlCache.get(url);
		if (null != value) {
			if (titleTv != null) {
				titleTv.setText(value);
			}
			return;
		}
		new Thread(new Runnable() {			
			@Override
			public void run() {
				Logger.d("UIUtil", "[getLocationTitle] " + url);
				String responseString = HttpUtil.getHttpResp(url);
				if (null == responseString || responseString.isEmpty()) {
					Logger.d("UIUtil", "[getLocationTitle] SocketTimeOut");
					return;
				}
//				Logger.d("UIUtil", "[getLocationTitle] " + responseString);
				
				JSONObject json;
				try {
					json = new JSONObject(responseString);
					final String desc = json.getJSONObject("result").getString("address");
					urlCache.put(url, desc);
					Activity a = (Activity) context;
					a.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if (titleTv != null) {
								titleTv.setText(desc);
							}
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
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
     * 矫正拍照返回图片的角度
     * @param path
     */
    public static void correctBitmapAngle(String path) {
    	int degree = getBitmapDegree(path);
    	Logger.d("UIUtil", "[correctBitmapAngle] degree:" + degree);
    	if (degree == 0) {
    		return;
    	}
    	Bitmap bmp = rotateBitmapByDegree(BitmapFactory.decodeFile(path), degree);
    	if (null == bmp) {
    		return;
    	}
    	File file = new File(path);
        try {
        	if (!file.exists()) {
            	file.createNewFile();
            }
        	FileOutputStream fos = new FileOutputStream(file);
			if (null != fos) {
	        	bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	        	fos.flush();
	        	fos.close();
	        }
		} catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * 读取图片的旋转的角度
     *
     * @param path
     *            图片绝对路径
     * @return 图片的旋转角度
     */
    private static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    
    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm
     *            需要旋转的图片
     * @param degree
     *            旋转角度
     * @return 旋转后的图片
     */
    private static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
    	if (bm == null) {
    		Logger.w("v5kf-UIUtil", "rotateBitmap failed: src bitmap null");
    		return null;
    	}
        Bitmap returnBm = null;
      
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
    
//    /**
//     * 检查是否具有特点权限
//     * @param context
//     * @param permission
//     * @return
//     */
//    public static boolean hasPermission(Context context, String permission) {
//    	return context.checkPermission(permission, android.os.Process.myPid(), context.getApplicationInfo().uid) == PackageManager.PERMISSION_GRANTED;
//    }
    
    /**
     * 检查是否具有特定权限
     * @param context
     * @param permission
     * @return
     */
    public static boolean hasPermission(Activity context, String permission) {
    	int requestCode = 0;
    	switch (permission) {
    	case "android.permission.CAMERA":
    		requestCode = REQUEST_PERMISSION_CAMERA;
    		break;
    	case "android.permission.RECORD_AUDIO":
    		requestCode = REQUEST_PERMISSION_RECORD_AUDIO;
    		break;
    	case "android.permission.WRITE_EXTERNAL_STORAGE":
    		requestCode = REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
    		break;
    	case "android.permission.ACCESS_FINE_LOCATION":
    		requestCode = REQUEST_PERMISSION_ACCESS_FINE_LOCATION;
    	case "android.permission.ACCESS_COARSE_LOCATION":
    		requestCode = REQUEST_PERMISSION_ACCESS_COARSE_LOCATION;
    		break;
    	}
    	return checkAndRequestPermission(context, permission, requestCode);
//    	DevUtils.checkAndRequestPermission(this, "android.permission.CAMERA", Config.REQUEST_PERMISSION_CAMERA);
//		DevUtils.checkAndRequestPermission(this, "android.permission.RECORD_AUDIO", Config.REQUEST_PERMISSION_RECORD_AUDIO);
//		DevUtils.checkAndRequestPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE", Config.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
//		DevUtils.checkAndRequestPermission(this, "android.permission.ACCESS_FINE_LOCATION", Config.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
//		DevUtils.checkAndRequestPermission(this, "android.permission.ACCESS_COARSE_LOCATION", Config.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
//    	return context.checkPermission(permission, android.os.Process.myPid(), context.getApplicationInfo().uid) == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean checkAndRequestPermission(Activity context, String permission, int requestCode) {
    	int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, permission);
    	if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
    		ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode);
    		return false;
    	}
    	return true;
    }
    
    /** 
     * 得到自定义的progressDialog 
     * @param context 
     * @param msg 
     * @return 
     */  
    public static Dialog createLoadingDialog(Context context, String msg) {  
        LayoutInflater inflater = LayoutInflater.from(context);  
        View v = inflater.inflate(V5Util.getIdByName(context, "layout", "v5_loading_dialog"), null);// 得到加载view  
        LinearLayout layout = (LinearLayout) v.findViewById(V5Util.getIdByName(context, "id", "v5_dialog_view"));// 加载布局  
        TextView tipTextView = (TextView) v.findViewById(V5Util.getIdByName(context, "id", "id_tipTextView"));// 提示文字  
        if (msg != null) {
        	tipTextView.setText(msg);// 设置加载信息  
        }
  
        Dialog loadingDialog = new Dialog(context, V5Util.getIdByName(context, "style", "v5_loading_dialog"));// 创建自定义样式dialog  
  
        loadingDialog.setCancelable(true);// 不可以用“返回键”取消  
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(  
                LinearLayout.LayoutParams.MATCH_PARENT,  
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局  
        return loadingDialog;  
    }
}
