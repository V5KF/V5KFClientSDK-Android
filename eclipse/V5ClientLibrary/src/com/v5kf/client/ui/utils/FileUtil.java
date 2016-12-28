package com.v5kf.client.ui.utils;

import java.io.File;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

public class FileUtil {

//	/**
//	 * Try to return the absolute file path from the given Uri
//	 *
//	 * @param context
//	 * @param uri
//	 * @return the file path or null
//	 */
//	public static String getRealFilePath( final Context context, final Uri uri ) {
//	    if ( null == uri ) return null;
//	    final String scheme = uri.getScheme();
//	    String data = null;
//	    if ( scheme == null )
//	        data = uri.getPath();
//	    else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
//	        data = uri.getPath();
//	    } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
//	        Cursor cursor = context.getContentResolver().query( uri, new String[] { ImageColumns.DATA }, null, null, null );
//	        if ( null != cursor ) {
//	            if ( cursor.moveToFirst() ) {
//	                int index = cursor.getColumnIndex( ImageColumns.DATA );
//	                if ( index > -1 ) {
//	                    data = cursor.getString( index );
//	                }
//	            }
//	            cursor.close();
//	        }
//	    }
//	    return data;
//	}
	
	@TargetApi(19) 
	public static String getRealFilePath(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {

	        // Return the remote address
	        if (isGooglePhotosUri(uri))
	            return uri.getLastPathSegment();

	        return getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	        String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
	
	/**
	 * 保存图片的路径
	 * @param context
	 * @return
	 */
	public static String getImageSavePath(Context context) {
		return Environment.getExternalStorageDirectory() + "/" + "Pictures" + "/v5kf";
	}

	/**
	 * 外部图片缓存路径
	 * @param context
	 * @return
	 */
	public static String getExternalImageCachePath(Context context) {
		return Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/imagecache";
	}
	
	/**
	 * 内部图片缓存路径[1.1.0新版本启用此路径]
	 * @param context
	 * @return
	 */
	public static String getImageCachePath(Context context) {
		return getPackageCachePath(context) + "/imagecache";
	}
	
//	/**
//	 * 内部图片缓存路径[1.0.4旧版]
//	 * @param context
//	 * @return
//	 */
//	public static String getImageCachePath(Context context) {
////		return getOldImageCachePath(context);
//		return getPackageCachePath(context) + "/imagecache";
//	}
	
	/**
	 * 语音等媒体文件缓存路径[1.1.0更新路径]
	 * @param context
	 * @return
	 */
	public static String getMediaCachePath(Context context) {
//		return getOldImageCachePath(context);
		return getPackageCachePath(context) + "/mediacache";
	}
	
	/**
	 * 缓存文件目录
	 * @param context
	 * @return
	 */
	public static String getPackageCachePath(Context context) {
		return context.getCacheDir().getAbsolutePath();
	}

	/**
	 * 存储卡上应用数据保存路径
	 * @param context
	 * @return
	 */
	public static String getPackagePath(Context context) {
		return Environment.getExternalStorageDirectory() + "/" + context.getPackageName();
	}

	/**
	 * Crash日志文件路径
	 * @param context
	 * @return
	 */
	public static String getCrashLogPath(Context context) {
		return Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/crash";
	}
	
	/**
	 * 获得图片保存名称
	 * @return
	 */
	public static String getImageName(String tag) {
		return "v5kf_" + tag + DateUtil.getCurrentLongTime() + ".jpg";
	}

	public static String getImageName() {
		return "v5kf" + DateUtil.getCurrentLongTime() + ".jpg";
	}
	
	/**
     * 删除路径，如是目录也全部删除
     * @param filePath
     */
    public static void deleteFile(String filePath) {
    	if (!TextUtils.isEmpty(filePath)) {     
            try {  
                File file = new File(filePath);     
                if (file.isDirectory()) { // 处理目录     
                    File files[] = file.listFiles();     
                    for (int i = 0; i < files.length; i++) {     
                    	deleteFile(files[i].getAbsolutePath());     
                    }
                    file.delete();
                } else { // 如果是文件，删除
                	file.delete();
                } 
            } catch (Exception e) {  
                e.printStackTrace();  
            }     
        }
    }
    
    public static boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);        
        if (!file.exists()) {
            if (file.mkdirs()) {                
                return true;
            } else {
                return false;

            }
        }
        return true;
    }
    
    public static boolean isFileExists(String strPath) {
    	File file = new File(strPath);        
    	return file.exists();
    }
}
