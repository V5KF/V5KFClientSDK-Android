package com.v5kf.client.ui.callback;

import com.v5kf.client.lib.V5ClientAgent.ClientLinkType;

import android.content.Context;

/**
 * 自定义对话消息中链接点击事件，将替代默认方式
 * @author Chenhy
 *
 */
public interface OnURLClickListener {
	/**
	 * 返回值代表是否消费了此事件
	 * @param context
	 * @param type
	 * @param url
	 * @return
	 */
	public boolean onURLClick(Context context, ClientLinkType type, String url);
}
