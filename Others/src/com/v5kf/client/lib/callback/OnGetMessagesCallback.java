package com.v5kf.client.lib.callback;

import java.util.List;

import com.v5kf.client.lib.entity.V5Message;

public interface OnGetMessagesCallback {
	public void complete(List<V5Message> msgs, int offset, int size, boolean finish); // 执行完成
}
