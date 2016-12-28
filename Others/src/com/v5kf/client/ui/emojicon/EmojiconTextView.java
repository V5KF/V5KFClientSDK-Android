/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.v5kf.client.ui.emojicon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.v5kf.client.R;

public class EmojiconTextView extends TextView {
    private int mEmojiconSize;
    private OnURLClickListener mListener;
    

	public interface OnURLClickListener {
    	public void onClick(View v, String url);
    }
	
	public void setURLClickListener(OnURLClickListener l) {
		this.mListener = l;
	}

    public EmojiconTextView(Context context) {
        super(context);
        init(null);
    }

    public EmojiconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiconTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs == null) {
            mEmojiconSize = (int) getTextSize();
        } else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon); // R.styleable.Emojicon
            mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
            a.recycle();
        }
        setText(getText());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
    	if (!TextUtils.isEmpty(text)) {
//    		Pattern mentionsPattern = Pattern.compile("");
//    	    String mentionsScheme = String.format("[URL=\"%s\"]%s[/URL]", "http://baidu.com", "百度一下");
//    	    Linkify.addLinks(this, mentionsPattern, mentionsScheme) ;
    		
    		/* URL获取和事件判断 */
            SpannableString style = new SpannableString(text);
            URLSpan[] urls = style.getSpans(0, text.length(), URLSpan.class);
            SpannableStringBuilder sps = new SpannableStringBuilder(style);
            sps.clearSpans();
            for (URLSpan url : urls) {  
            	MyClickSpan myURLSpan = new MyClickSpan(url.getURL());  
            	sps.setSpan(myURLSpan, style.getSpanStart(url), style.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
            }            
            Pattern pattern = Pattern.compile("[http|https]+[://]+[0-9A-Za-z:/[-]_[&]#[?][=][.]]*", Pattern.CASE_INSENSITIVE);
            Matcher mch = pattern.matcher(text);
            int startPoint = 0;
            while (mch.find(startPoint)) {
            	int endPoint = mch.end();
            	String hit = mch.group();
            	ClickableSpan clickSpan = new MyClickSpan(hit);
            	sps.setSpan(clickSpan, endPoint - hit.length(), endPoint, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            	startPoint = endPoint;
            }
            
        	/* QQ face transform */
        	SpannableString spanStr = QFaceiconUtil.getQFaceText(getContext(), sps, mEmojiconSize);
            SpannableStringBuilder builder = new SpannableStringBuilder(spanStr);
            
            /* Emoji transform */
//            EmojiconHandler.addEmojis(getContext(), builder, mEmojiconSize, mEmojiconAlignment, mEmojiconTextSize, mTextStart, mTextLength, mUseSystemDefault);
            text = builder;
        }
        super.setText(text, type);
    }
    
    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;
        super.setText(getText());
	}

    class MyClickSpan extends ClickableSpan {
    	
    	String text;

    	public MyClickSpan(String str) {
    		super();
			this.text = str;
		}
    	
    	@Override
    	public void updateDrawState(TextPaint ds) {
    		ds.setColor(Color.parseColor("#00F0FF")); //FFCD23
    		ds.setUnderlineText(true);
    	}
    	
		@Override
		public void onClick(View widget) {
			Log.i("EmojiconTextView", "URL:" + text);
			if (null == mListener) {
				Intent intent = new Intent();   
		        intent.setAction("android.intent.action.VIEW");    
		        Uri content_url = Uri.parse(text);   
		        intent.setData(content_url);
		        getContext().startActivity(intent);
			} else {
				mListener.onClick(widget, text);
			}
		}
    }
}
