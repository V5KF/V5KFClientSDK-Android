package com.v5kf.sdkdemo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientAgent.ClientLinkType;
import com.v5kf.client.lib.V5ClientAgent.ClientOpenMode;
import com.v5kf.client.lib.V5ClientAgent.ClientServingStatus;
import com.v5kf.client.lib.V5ClientConfig;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5ControlMessage;
import com.v5kf.client.ui.ClientChatActivity;
import com.v5kf.client.ui.callback.ChatActivityFuncIconClickListener;
import com.v5kf.client.ui.callback.OnChatActivityListener;
import com.v5kf.client.ui.callback.OnURLClickListener;
import com.v5kf.client.ui.callback.UserWillSendMessageListener;

public class MainActivity extends Activity implements OnChatActivityListener {

	private static final String TAG = "MainActivity";
	private Button mChatBtn;
//	private boolean flag_userBrowseSomething = true; // 浏览某商品标志
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getSupportActionBar().hide();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_test);
		initView();
		
		// 开启logcat输出，方便debug，发布时请关闭
		XGPushConfig.enableDebug(this, true);
		// 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(), XGIOperateCallback)带callback版本
		// 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
		// 具体可参考详细的开发指南
		// 传递的参数为ApplicationContext
		Context context = getApplicationContext();
		XGPushManager.registerPush(context, new XGIOperateCallback() {
			
			@Override
			public void onSuccess(Object arg0, int arg1) {
				Logger.e("MainActivity", "信鸽注册成功token：" + (String)arg0);
			}
			
			@Override
			public void onFail(Object arg0, int arg1, String arg2) {
				Logger.e("MainActivity", "信鸽注册失败");
			}
		});
		String app = V5ClientConfig.getInstance(this).getSiteId();
		app = (app == null)  ? "sdkDemo" : app;
		XGPushManager.setTag(context, app);
//		// 2.36（不包括）之前的版本需要调用以下2行代码
//		Intent service = new Intent(context, XGPushService.class);
//		context.startService(service);
		
		// 清除缓存，清除对话图片、语音消息的本地缓存
		//V5ClientAgent.clearCache(getApplicationContext());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);// 信鸽建议onNewIntent调用这句
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		XGPushManager.onActivityStarted(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		XGPushManager.onActivityStoped(this);
	}

	private void initView() {
		initTitle();
		mChatBtn = (Button) findViewById(R.id.btn_right);
		
		mChatBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String token = XGPushConfig.getToken(getApplicationContext());
		        if (token == null || token.isEmpty()) {
		        	Toast.makeText(
		        			getApplicationContext(),
		        			"XGPush register not finish, please wait for a while", 
		        			Toast.LENGTH_SHORT).show();
		        	return;
		        }
		        
				// V5客服系统客户端配置
		        V5ClientConfig config = V5ClientConfig.getInstance(MainActivity.this);
		        V5ClientConfig.USE_HTTPS = true; // 使用加密连接，默认true
		        V5ClientConfig.SOCKET_TIMEOUT = 20000; // 请求超时时间
		        config.setHeartBeatEnable(true); // 是否允许发送心跳包保活
		        config.setHeartBeatTime(30000); // 心跳包间隔时间ms
		        config.setShowLog(true); // 显示日志，默认为true
		        config.setLogLevel(V5ClientConfig.LOG_LV_DEBUG); // 显示日志级别，默认为全部显示
		        // 客户信息键值对（JSONObject）
		        JSONObject customContent = new JSONObject();
				try {
					customContent.put("用户名", "V5KF-Demo");
					customContent.put("用户级别", "VIP");
					customContent.put("用户积分", "0");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// 设置客户信息（自定义JSONObjectjian键值对，开启会话前设置）
				config.setUserInfo(customContent);
		        
		        // 取设备token作为openId
		        String openId = XGPushConfig.getToken(getApplicationContext());
		        config.setNickname("android_sdk_demo"); // 设置用户昵称
		        config.setGender(1); // 设置用户性别: 0-未知  1-男  2-女
		        // 设置用户头像URL
				config.setAvatar("http://debugimg-10013434.image.myqcloud.com/fe1382d100019cfb572b1934af3d2c04/thumbnail");
		        config.setVip(0); // 设置用户VIP等级（0-5）
		        // [1.3.0新增]设置V5系统内置的客户基本信息（有则设置，非必需），区别于setUserInfo，这是V5系统内置字段
		        JSONObject baseInfo = new JSONObject();
			    try {
				    baseInfo.put("country", "中国");
					baseInfo.put("province", "广东");
				    baseInfo.put("city", "深圳");
				    baseInfo.put("language", "zh-cn");
				    // nickname,gender,avatar,vip也可在此设置
				} catch (JSONException e) {
					e.printStackTrace();
				}
			    config.setBaseInfo(baseInfo);
			    
		        if (!TextUtils.isEmpty(openId) && !openId.equals("0")) {
		        	/**
			         *【建议】设置用户OpenId，以识别不同登录用户，不设置则默认由SDK生成，替代v1.2.0之前的uid,
			         *  openId将透传到座席端(建议使用含字母数字和下划线的字符串，尽量不用特殊字符，若含特殊字符系统会进行URL encode处理)
			         *	若您是旧版本SDK用户，只是想升级，为兼容旧版，避免客户信息改变可继续使用config.setUid，可不用openId
			         */
		        	config.setOpenId(openId);
		        }
		        //config.setUid(uid); //【弃用】请使用setOpenId替代
		        // 设置device_token：集成第三方推送(腾讯信鸽、百度云推)时设置此参数以在离开会话界面时接收推送消息
		        config.setDeviceToken(XGPushConfig.getToken(getApplicationContext())); // 【建议】设置deviceToken
		        
		        /* 开启会话界面 */
			    // 可用Bundle传递以下参数
			    Bundle bundle=new Bundle();
			    bundle.putInt("numOfMessagesOnRefresh", 10);	// 下拉刷新数量，默认为10
			    bundle.putInt("numOfMessagesOnOpen", 10);		// 开场显示历史消息数量，默认为10
			    bundle.putBoolean("enableVoice", true);			// 是否允许发送语音
			    bundle.putBoolean("showAvatar", true);			// 是否显示对话双方的头像
			    // 开场白模式，默认为固定开场白，可根据客服启动场景设置开场问题
			    bundle.putInt("clientOpenMode", ClientOpenMode.clientOpenModeDefault.ordinal());
			    //bundle.putString("clientOpenParam", "您好，请问有什么需要帮助的吗？");
			    
			    //Context context = getApplicationContext();
			    //Intent chatIntent = new Intent(context, ClientChatActivity.class);
				//chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//chatIntent.putExtras(bundle);
				//context.startActivity(chatIntent);
			    // 进入会话界面(可使用上面的方式或者调用下面方法)，携带bundle(不加bundle参数则全部使用默认配置)
			    V5ClientAgent.getInstance().startV5ChatActivityWithBundle(getApplicationContext(), bundle);
			    
				/* 添加聊天界面监听器(非必须，有相应需求则添加) */
			    // 界面生命周期监听[非必须]
			    V5ClientAgent.getInstance().setChatActivityListener(MainActivity.this);
			    // 消息发送监听[非必须]，可在此处向坐席透传来自APP客户的相关信息
			    V5ClientAgent.getInstance().setUserWillSendMessageListener(new UserWillSendMessageListener() {
					
					@Override
					public V5Message onUserWillSendMessage(V5Message message) {
						// TODO 可在此处添加消息参数(JSONObject键值对均为字符串)，采集信息透传到坐席端（v1.2.0版本开始不建议使用此方式，除非有实时更新需求的自定义信息）
		                // 【注意】v1.2.0以上版本建议使用V5ClientConfig的setUserInfo方法传递客户信息，可不必依附于消息
		                //if (flag_userBrowseSomething) {
		                //    JSONObject customContent = new JSONObject();
		                //    try {
		                //        customContent.put("用户级别", "VIP");
		                //        customContent.put("用户积分", "300");
		                //        customContent.put("来自应用", "ClientDemo");
		                //    } catch (JSONException e) {
		                //        e.printStackTrace();
		                //    }
		                //    message.setCustom_content(customContent);
		                //
		                //    flag_userBrowseSomething = false;
		                //}
						return message; // 注：必须将消息对象以返回值返回
					}
				});
			    
			    /**
			     * 点击链接监听
			     * onURLClick返回值：是否消费了此点击事件，返回true则SDK内不再处理此事件，否则默认跳转到指定网页
			     */
			    V5ClientAgent.getInstance().setURLClickListener(new OnURLClickListener() {

					@Override
					public boolean onURLClick(Context context,
							ClientLinkType type, String url) {
						// TODO Auto-generated method stub
						switch (type) {
						case clientLinkTypeArticle: // 点击图文
							
							break;
						case clientLinkTypeURL: // 点击URL链接
							
							break;
						}
						Logger.i(TAG, "onURLClick:" + url);
						return false; // 是否消费了此点击事件
					}
				});
			    
			    /**
			     * 点击对话输入框底部功能按钮
			     */
			    V5ClientAgent.getInstance().setChatActivityFuncIconClickListener(new ChatActivityFuncIconClickListener() {
					
			    	/**
			    	 * Activity点击底部功能按钮事件，icon参数值及含义如下：
				     * 		v5_icon_ques			//常见问题
				     * 		v5_icon_relative_ques	//相关问题
				     * 		v5_icon_photo			//图片
				     * 		v5_icon_camera			//拍照
				     * 		v5_icon_worker			//人工客服
			    	 * 返回值代表是否消费了此事件
			    	 * @param icon 点击的图标名称(对应SDK目录下res/values/v5_arrays中v5_chat_func_icon的值)
			    	 * @return boolean 是否消费事件(返回true则不响应默认点击效果，由此回调处理)
			    	 */
					@Override
					public boolean onChatActivityFuncIconClick(String icon) {
						// 在这里可以实现点击事件的自定义处理，如下示例点击“人工客服”转指定客服
						if (icon.equals("v5_icon_worker")) {
							// 转到指定客服,参数：(组id, 客服id),参数为0则不指定客服组或者客服
							V5ClientAgent.getInstance().transferHumanService(0, 0);
							// 返回true来拦截SDK内默认的实现
							return true;
						}
						return false;
					}
				});
			}
		});
	}

	private void initTitle() {
		findViewById(R.id.header_ib_imagebutton).setVisibility(View.GONE);
		TextView titleTv = (TextView) findViewById(R.id.header_htv_subtitle);
		titleTv.setText(R.string.v5_category);
	}

	/* 会话界面生命周期和连接状态的回调 */
	@Override
	public void onChatActivityCreate(ClientChatActivity activity) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "<onChatActivityCreate>");
	}

	@Override
	public void onChatActivityStart(ClientChatActivity activity) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "<onChatActivityStart>");
	}

	@Override
	public void onChatActivityStop(ClientChatActivity activity) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "<onChatActivityStop>");
	}

	@Override
	public void onChatActivityDestroy(ClientChatActivity activity) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "<onChatActivityDestroy>");
	}

	@Override
	public void onChatActivityConnect(ClientChatActivity activity) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "<onChatActivityConnect>");
		/*
		 * 连接建立后才可以调用消息接口发送消息，以下是发送消息示例
		 */
		// 转指定人工客服（调用时立即转），参数: 客服组id,客服id
		//V5ClientAgent.getInstance().transferHumanService(0, 132916);
		// 指定人工客服点击转人工按钮或者问题触发转人工时会转到指定人工，参数"0 132916"中两个数字先后对应需要转的客服组ID和客服ID
		//V5ClientAgent.getInstance().sendMessage(new V5ControlMessage(4, 2, "0 114052"), null);
					
		// 发送图文消息
//		V5ArticlesMessage articleMsg = new V5ArticlesMessage();
//		V5ArticleBean article = new V5ArticleBean(
//				"V5KF", 
//				"http://rs.v5kf.com/upload/10000/14568171024.png", 
//				"http://www.v5kf.com/public/weixin/page.html?site_id=10000&id=218833&uid=3657455033351629359", 
//				"V5KF是围绕核心技术“V5智能机器人”研发的高品质在线客服系统。可以运用到各种领域，目前的主要产品有：微信智能云平台、网页智能客服系统...");
//		ArrayList<V5ArticleBean> articlesList = new ArrayList<V5ArticleBean>();
//		articlesList.add(article);
//		articleMsg.setArticles(articlesList);
//		V5ClientAgent.getInstance().sendMessage(articleMsg, null);
	}

	@Override
	public void onChatActivityReceiveMessage(ClientChatActivity activity, V5Message message) {
		// TODO Auto-generated method stub
		Logger.d(TAG, "<onChatActivityReceiveMessage> " + message.getDefaultContent(this));
	}
	
	@Override
	public void onChatActivityServingStatusChange(ClientChatActivity activity,
			ClientServingStatus status) {
		// TODO Auto-generated method stub
		switch (status) {
		case clientServingStatusRobot:
		case clientServingStatusQueue:
			activity.setChatTitle("机器人服务中");
			break;
		case clientServingStatusWorker:
			activity.setChatTitle(V5ClientConfig.getInstance(getApplicationContext()).getWorkerName() + "为您服务");
			break;
		case clientServingStatusInTrust:
			activity.setChatTitle("机器人托管中");
			break;
		}
	}
}
