package com.v5kf.client.lib.callback;

import com.v5kf.client.lib.V5ClientAgent.ClientServingStatus;
import com.v5kf.client.lib.V5KFException;
import com.v5kf.client.lib.entity.V5Message;

/**
 * 消息服务回调接口
 * @author V5KF_8
 *
 */
public interface V5MessageListener {
	public void onConnect();
    public void onMessage(String json); // 返回消息为json字符串(兼容后期接口类型扩展)
    public void onMessage(V5Message message); // 返回消息对象
	public void onError(V5KFException error); // 返回异常信息
	public void onServingStatusChange(ClientServingStatus status); // 客户服务状态改变
}
