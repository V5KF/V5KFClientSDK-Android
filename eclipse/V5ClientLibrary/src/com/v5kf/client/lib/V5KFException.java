package com.v5kf.client.lib;

public class V5KFException extends Exception {
	private static final long serialVersionUID = 6776338166781196694L;
	
	public enum V5ExceptionStatus {
		ExceptionNoError,			// 没有错误
		//ExceptionInitParamInvalid,	// 初始化参数无效
		//ExceptionInitFailed,		// 初始化中出现错误
		ExceptionNotInitialized,	// SDK未初始化或初始化失败
		ExceptionAccountFailed,		// 账号信息认证失败
		ExceptionNotConnected,		// 尚未建立连接
		ExceptionMessageSendFailed,	// 消息发送失败(MessageSendCallback参数)
		ExceptionImageUploadFailed, // 图片上传失败
		ExceptionNoNetwork,			// 没有网络
		ExceptionSocketTimeout,		// 响应超时
		ExceptionConnectionError,	// 网络请求错误
		ExceptionWSAuthFailed,		// ws的Authorization认证失败
		ExceptionConnectRepeat,		// 客户端出现重复连接
		ExceptionServerResponse,	// 错误类型为服务器返回
		ExceptionNoAudioPermission,	// 无录音权限
		ExceptionUnknownError;		// 未知错误
	}

	private V5ExceptionStatus status;
	private String description;
	
	public V5KFException(V5ExceptionStatus code, String desc) {
		this.setStatus(code);
		this.description = desc;
	}
	
	@Override
    public String toString() {
        String msg = getDescription();
        String name = getClass().getName();
        if (msg == null) {
            return name;
        }
        return name + "(" + getStatus() + "): " + msg;
    }

	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}

	public V5ExceptionStatus getStatus() {
		return status;
	}

	public void setStatus(V5ExceptionStatus status) {
		this.status = status;
	}
}
