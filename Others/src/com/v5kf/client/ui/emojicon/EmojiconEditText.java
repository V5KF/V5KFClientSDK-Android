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

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import com.v5kf.client.R;
import com.v5kf.client.lib.Logger;

public class EmojiconEditText extends EditText {
    private int mEmojiconSize;

    public EmojiconEditText(Context context) {
        super(context);
        mEmojiconSize = (int) getTextSize();
    }

    public EmojiconEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiconEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
        mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
        a.recycle();
        setText(getText());
    }
    
    

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    	super.onTextChanged(text, start, lengthBefore, lengthAfter);
    	Logger.i("EmojiconEditText", "onTextChanged");
//    	if(isChanged){
//            return ;
//        }
//    	updateText(start, lengthBefore, lengthAfter);
    	if(onTextChangedInterface != null){
            onTextChangedInterface.onTextChanged(text);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(oldh > 0 && onSizeChangedListener != null){
            onSizeChangedListener.onSizeChanged();
        }
    }

    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;

        updateText(getText().length(), getText().length(), getText().length());
    }

    private void updateText(int start, int lengthBefore, int lengthAfter) {
    	if (!TextUtils.isEmpty(getText())) {
//    		isChanged = true;
    		CharSequence text = getText();
    		
    		boolean isQface = QFaceiconUtil.isQqFace(text);
    		SpannableStringBuilder builder = null;
    		if (isQface) {
	        	/* QQ face transform */
	        	SpannableString spanStr = QFaceiconUtil.getQFaceText(getContext(), text, mEmojiconSize);
	            builder = new SpannableStringBuilder(spanStr);
    		} else {
    			builder = new SpannableStringBuilder(text);
    		}
            
            /* Emoji transform */
//            EmojiconHandler.addEmojis(getContext(), builder, mEmojiconSize, mEmojiconAlignment, mEmojiconTextSize, 0, -1, mUseSystemDefault);
            text = builder;
            
            int oldPos = getSelectionStart();
            int oldLen = getText().length();
            super.setText(text, BufferType.EDITABLE);
            int newLen = getText().length();
            if (oldLen == newLen) {
            	setSelection(oldPos);
            }
//            isChanged = false;
        }
//    	isChanged = true;
//    	SpannableString spanStr = QFaceiconUtil.getQFaceText(getContext(), getText(), mEmojiconSize);
//        
//    	/* Emoji face transform */
//		EmojiconHandler.addEmojis(getContext(), spanStr, mEmojiconSize, mEmojiconAlignment, mEmojiconTextSize, mUseSystemDefault);
    }
    
    public void updateText() {
    	if (!TextUtils.isEmpty(getText())) {
//    		isChanged = true;
    		CharSequence text = getText();
    		
    		boolean isQface = QFaceiconUtil.isQqFace(text);
    		SpannableStringBuilder builder = null;
    		if (isQface) {
	        	/* QQ face transform */
	        	SpannableString spanStr = QFaceiconUtil.getQFaceText(getContext(), text, mEmojiconSize);
	            builder = new SpannableStringBuilder(spanStr);
    		} else {
    			builder = new SpannableStringBuilder(text);
    		}
            
            /* Emoji transform */
//            EmojiconHandler.addEmojis(getContext(), builder, mEmojiconSize, mEmojiconAlignment, mEmojiconTextSize, 0, -1, mUseSystemDefault);
            text = builder;
            
            int oldPos = getSelectionStart();
            int oldLen = getText().length();
            super.setText(text, BufferType.EDITABLE);
            int newLen = getText().length();
            if (oldLen == newLen) {
            	setSelection(oldPos);
            }
//            isChanged = false;
        }
//    	isChanged = true;
//    	SpannableString spanStr = QFaceiconUtil.getQFaceText(getContext(), getText(), mEmojiconSize);
//        
//    	/* Emoji face transform */
//		EmojiconHandler.addEmojis(getContext(), spanStr, mEmojiconSize, mEmojiconAlignment, mEmojiconTextSize, mUseSystemDefault);
    }

    public interface OnTextChangedInterface {
        void onTextChanged(CharSequence argo);
    }

    OnTextChangedInterface onTextChangedInterface;

    public void setOnTextChangedInterface(OnTextChangedInterface i) {
        onTextChangedInterface = i;
    }


    public interface OnSizeChangedListener {
        void onSizeChanged();
    }

    OnSizeChangedListener onSizeChangedListener;

    public void setOnSizeChangedListener(OnSizeChangedListener i) {
        onSizeChangedListener = i;
    }
}
