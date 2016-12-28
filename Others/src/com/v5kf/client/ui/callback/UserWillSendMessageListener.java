package com.v5kf.client.ui.callback;

import com.v5kf.client.lib.entity.V5Message;

/**
 * 需要传递自定义参数时建议使用
 * @author Chenhy
 *
 */
public interface UserWillSendMessageListener {

	/**
	 * Activity即将发送消息
	 * 返回值为输入值，可在函数内修改消息参数，比如添加自定义参数字段customer_content(键值对，透传到坐席端)
	 * @param message
	 * @return V5Message(不可为空)
	 */
	public V5Message onUserWillSendMessage(V5Message message);
}
