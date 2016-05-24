package com.v5kf.sdk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientConfig;
import com.v5kf.client.lib.V5ClientAgent.ClientOpenMode;
import com.v5kf.client.lib.V5ClientAgent.ClientServingStatus;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.ui.ClientChatActivity;
import com.v5kf.client.ui.callback.OnChatActivityListener;
import com.v5kf.client.ui.callback.UserWillSendMessageListener;

public class MainActivity extends AppCompatActivity implements OnChatActivityListener {

    private static final String TAG = "MainActivity";
    private boolean flag_userBrowseSomething = true; // 浏览某商品标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startChatActivity();
            }
        });

        initV5Chat();
    }

    private void initV5Chat() {
        // V5客服系统客户端配置
        V5ClientConfig config = V5ClientConfig.getInstance(MainActivity.this);
        V5ClientConfig.USE_HTTPS = true; // 使用加密连接，默认true
        config.setShowLog(true); // 显示日志，默认为true
        config.setLogLevel(V5ClientConfig.LOG_LV_DEBUG); // 显示日志级别，默认为全部显示

        config.setNickname("android_sdk_test"); // 设置用户昵称
        config.setGender(1); // 设置用户性别: 0-未知  1-男  2-女
        // 设置用户头像URL
        config.setAvatar("http://debugimg-10013434.image.myqcloud.com/fe1382d100019cfb572b1934af3d2c04/thumbnail");
        //config.setUid(uid); // 【必须】设置用户ID，以识别不同登录用户
        // 设置device_token：集成第三方推送(腾讯信鸽、百度云推)时设置此参数以在离开会话界面时接收推送消息
        //config.setDeviceToken(XGPushConfig.getToken(getApplicationContext())); // 【建议】设置deviceToken
    }

    private void startChatActivity() {
        /* 开启会话界面 */
        // 可用Bundle传递以下参数
        Bundle bundle=new Bundle();
        bundle.putInt("numOfMessagesOnRefresh", 10);	// 下拉刷新数量，默认为10
        bundle.putInt("numOfMessagesOnOpen", 10);		// 开场显示历史消息数量，默认为0
        bundle.putBoolean("enableVoice", true);			// 是否允许发送语音
        bundle.putBoolean("showAvatar", true);			// 是否显示对话双方的头像
        // 开场白模式，默认为固定开场白，可根据客服启动场景设置开场问题
        bundle.putInt("clientOpenMode", ClientOpenMode.clientOpenModeDefault.ordinal());
        bundle.putString("clientOpenParam", "您好，请问有什么需要帮助的吗？");

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
                // TODO 可在此处添加消息参数(JSONObject键值对均为字符串)，采集信息透传到坐席端
                if (flag_userBrowseSomething) {
                    JSONObject customContent = new JSONObject();
                    try {
                        customContent.put("用户级别", "VIP");
                        customContent.put("用户积分", "300");
                        customContent.put("来自应用", "ClientDemo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    message.setCustom_content(customContent);

                    flag_userBrowseSomething = false;
                }
                return message; // 注：必须将消息对象以返回值返回
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
