package com.v5kf.client.ui.callback;

import com.v5kf.client.lib.V5ClientAgent.ClientServingStatus;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.ui.ClientChatActivity;

/**
 * 使用UI直接开发可监听此接口
 * （针对有较高自定义开发需求的接口，无特殊需求不建议使用，以免使用不当带来不可预知的问题）
 * @author Chenhy
 *
 */
public interface OnChatActivityListener {
	/* Activity的生命周期 */
	public void onChatActivityCreate(ClientChatActivity activity);
	public void onChatActivityStart(ClientChatActivity activity);
	public void onChatActivityStop(ClientChatActivity activity);
	public void onChatActivityDestroy(ClientChatActivity activity);
	
	/**
	 * Activity连接成功
	 */
	public void onChatActivityConnect(ClientChatActivity activity);
	
	/**
	 * Activity收到消息
	 * @param message
	 */
	public void onChatActivityReceiveMessage(ClientChatActivity activity, V5Message message);
	
	/**
	 * Activity客户服务状态改变
	 * @param activity
	 * @param status ClientServingStatus,当前服务状态，值定义如下
	 * 		clientServingStatusRobot, // 机器人服务
	 *  	clientServingStatusQueue, // 排队中（等待人工客服，当前为机器人服务）
	 *		clientServingStatusWorker, // 人工服务
	 *		clientServingStatusInTrust; // 人工交给机器人托管
	 */
	public void onChatActivityServingStatusChange(ClientChatActivity activity, ClientServingStatus status);
}
