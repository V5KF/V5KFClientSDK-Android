package com.v5kf.client.lib.callback;


/**
 * SDK初始化回调
 * @author V5KF_8
 *
 */
public interface V5InitCallback {
    public void onSuccess(String response);
    public void onFailure(String response);
}
