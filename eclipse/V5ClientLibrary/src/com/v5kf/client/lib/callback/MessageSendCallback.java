package com.v5kf.client.lib.callback;

import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.entity.V5Message;


public interface MessageSendCallback {

	public void onSuccess(V5Message message);
	public void onFailure(V5Message message, V5ExceptionStatus statusCode, String desc);
}
