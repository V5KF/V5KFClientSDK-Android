package com.v5kf.client.ui.adapter;

import android.view.View;

public interface OnChatListClickListener {
	public void onMultiNewsClick(View v, int position, int viewType, int newsPos);
	public void onChatItemClick(View v, int position, int viewType);
	public void onChatItemLongClick(View v, int position, int viewType);
}