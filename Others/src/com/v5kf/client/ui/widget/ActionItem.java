package com.v5kf.client.ui.widget;

import android.content.Context;

/**
 * 
 *	功能描述：弹窗内部子类项（绘制标题和图标）
 */
public class ActionItem {
	//定义图片对象
	public int mDrawableId;
	//定义文本对象
	public CharSequence mTitle;
	
	public ActionItem(int drawableId, CharSequence title){
		this.mDrawableId = drawableId;
		this.mTitle = title;
	}
	
	public ActionItem(Context context, int titleId, int drawableId){
		this.mTitle = context.getResources().getText(titleId);
		this.mDrawableId = drawableId;
	}
	
	public ActionItem(Context context, CharSequence title, int drawableId) {
		this.mTitle = title;
		this.mDrawableId = drawableId;
	}
}
