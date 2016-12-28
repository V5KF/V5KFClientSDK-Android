package com.v5kf.client.ui.callback;

import android.content.Context;

/**
 * 自定义对话消息中地图点击事件，将替代默认方式
 * @author Chenhy
 *
 */
public interface OnLocationMapClickListener {
	public void onLocationMapClick(Context context, double x, double y);
}
