package com.v5kf.client.ui.utils;

import com.v5kf.client.R;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

public class VoiceErrorCode {
    public final static int SUCCESS = 0;
    public final static int E_NOSDCARD = 1001;
    public final static int E_STATE_RECODING = 1002;
    public final static int E_RECORD_NOT_PERMIT = 1003;
    public final static int E_UNKOWN = 1004;
     
     
    public static String getErrorInfo(Context vContext, int vType) throws NotFoundException
    {
        switch(vType)
        {
        case SUCCESS:
            return "success";
        case E_NOSDCARD:
            return vContext.getResources().getString(R.string.v5_error_no_sdcard);
        case E_STATE_RECODING:
            return vContext.getResources().getString(R.string.v5_error_state_record);  
        case E_UNKOWN:
        default:
            return vContext.getResources().getString(R.string.v5_error_unknown);           
             
        }
    }
 
}