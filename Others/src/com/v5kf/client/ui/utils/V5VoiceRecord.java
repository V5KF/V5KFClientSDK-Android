package com.v5kf.client.ui.utils;

import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5Util;

public class V5VoiceRecord {
     
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public final static int AUDIO_SAMPLE_RATE = 44100;  //44.1KHz,普遍使用的频率

	private static final String TAG = "V5VoiceRecord";
	
	private VoiceRecordListener mRecordListener;
	private MediaRecorder mMediaRecorder;
	private Context mContext;
	private boolean isRecording = false;
	private String mFilePath;
	
	public V5VoiceRecord(Context context, VoiceRecordListener listener) {
		this.mContext = context;
		this.mRecordListener = listener;
	}
	
	public int startListening() {
		Logger.d(TAG, "[startListening]");
		if (isRecording) {
			if (mRecordListener != null) {
            	mRecordListener.onErrorOfSpeech(VoiceErrorCode.E_STATE_RECODING, "Is recording, can not start");
            }
			return VoiceErrorCode.E_STATE_RECODING;
		}
		Logger.d(TAG, "[startListening] test - 1");
		mFilePath = FileUtil.getMediaCachePath(mContext) + "/" + V5Util.getCurrentLongTime() + ".amr";
		if (createMediaRecord() < 0) {
			if (mRecordListener != null) {
            	mRecordListener.onErrorOfSpeech(VoiceErrorCode.E_NOSDCARD, "File path not exist");
            }
			Logger.d(TAG, "[startListening] test - 2");
			return VoiceErrorCode.E_NOSDCARD;
		}
		try{
			Logger.d(TAG, "[startListening] test - 3");
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            // 让录制状态为true  
            isRecording = true;
            Logger.d(TAG, "[startListening] test - 4");
            if (mRecordListener != null) {
            	mRecordListener.onBeginOfSpeech();
            }
            Logger.d(TAG, "[startListening] test - 5");
            return VoiceErrorCode.SUCCESS;
        } catch(IOException ex){
            ex.printStackTrace();
            if (mRecordListener != null) {
            	mRecordListener.onErrorOfSpeech(VoiceErrorCode.E_UNKOWN, ex.toString());
            }
            return VoiceErrorCode.E_UNKOWN;
        }
	}

	public void stopListening() {
		Logger.d(TAG, "[stopListening]");
		if (!isRecording) {
			/* 清除输出文件 */
	        FileUtil.deleteFile(mFilePath);
	        if (mRecordListener != null) {
	        	mRecordListener.onErrorOfSpeech(VoiceErrorCode.E_RECORD_NOT_PERMIT, "未能开始录音...请检查录音权限");
	        }
	        return;
		}
		close();
		if (mRecordListener != null) {
        	mRecordListener.onResultOfSpeech(mFilePath);
        }
	}
	
	public void cancel(int state) {
		Logger.d(TAG, "[cancel] state=" + state);
		if (!isRecording) {
			Logger.w(TAG, "[cancel] not recording");
			return;
		}
		
		close();
		/* 清除输出文件 */
        FileUtil.deleteFile(mFilePath);
        if (mRecordListener != null) {
        	mRecordListener.onCancelOfSpeech(state);
        }
	}
	
	@TargetApi(10) @SuppressWarnings("deprecation")
	private int createMediaRecord(){
        /* ①Initial：实例化MediaRecorder对象 */
       mMediaRecorder = new MediaRecorder();
//       mMediaRecorder.reset();
       
       /* setAudioSource/setVedioSource*/
       mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //设置麦克风
        
       /* 设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
        * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
        */
       if (Build.VERSION.SDK_INT >= 10) {
    	   mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
       } else {
    	   mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
       }
       
        /* 设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
       mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         
		/* 设置输出文件的路径 */
		if (!FileUtil.isFolderExists(FileUtil.getMediaCachePath(mContext))) {
			return -1;
		}
		
		File file = new File(mFilePath);
		if (file.exists()) {  
		    file.delete();  
		}
		mMediaRecorder.setOutputFile(mFilePath);
		mMediaRecorder.setMaxDuration(60000);
		return 0;
	}
    
    
	private void close(){
		isRecording = false;
		if (mMediaRecorder != null) {  
			Logger.d(TAG, "[close] - start");
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						mMediaRecorder.stop();
						mMediaRecorder.reset();
						mMediaRecorder.release();  
						mMediaRecorder = null;
						Logger.d(TAG, "[close] - done");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
           }).start();
       }  
	}
	

	public interface VoiceRecordListener {
		public void onBeginOfSpeech();
		public void onCancelOfSpeech(int state);
		public void onResultOfSpeech(String path);
		public void onErrorOfSpeech(int errorCode, String desc);
		
//		public void onVolumeChanged(int volume);
	}
	
}
