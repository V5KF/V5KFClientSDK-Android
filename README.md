# V5KFClientSDK-Android

> 此为V5KF智能客服Android客户端SDK快速接入文档，包含接入客服系统的基本配置和代码接口，更全面详细的文档可参考[这里](https://desk.v5kf.com/docs/sdk/android/index.html)

## 1 开发环境准备

1. V5KF客服系统账号
> 没有 V5KF 账号需要前往[官网](http://www.v5kf.com)注册账号。

2. 获得应用账号、站点编号
> 应用账号、站点编号作为 SDK 连接服务端的身份凭证，可登录V5KF管理后台在 "系统接入" -> "移动应用APP" 配置界面获取。

3. 填写对应平台的推送服务器地址(非必需)
> 为了使您的 APP 在集成本 SDK 后具有离线消息推送，建议填写您的推送服务器地址，同时也支持第三方推送平台，需要按照本文档规定填写您的 device_token 和绑定的用户 ID。

4. 下载 SDK
> 您可以到 V5KF [官网](http://www.v5kf.com)或者[V5KF Github](https://github.com/V5KF/V5KFClientSDK-iOS)*(建议)*页下载智能客服 SDK，包含了开发包和带 UI 界面的 Demo 示例工程。

5. 环境要求
> 在您集成智能客服 SDK 前环境要求如下:
	- Android SDK `Build-tools` 请升级到 21 及以上版本。
	- JAVA 编译版本 `JDK 1.7` 及以上版本。
	- 编译 Demo 需要 Android Support V7 22.1(含 AppCompatActivity) 及以上版本(需导入支持包 `android-support-v7-appcompat`)。
	- Android SDK 最低支持 Android API 9: Android 2.3(Gingerbread)。

## 2 SDK导入

导入 SDK 可以将 SDK 文件复制到您的项目中也可作为 library (eclipse) 或 module (Android Studio) 导入，建议采用导入 `Library` 方式，便于 SDK 维护和升级，从以下三点中选择合适您项目的方式导入。

### 2.1 导入eclipse

* 在`eclipse/`目录下将`V5ClientLibrary/`目录作为`library`项目导入eclipse：

> 右键eclipse项目列表 Import -> Existing Android Code Into Workspace -> 选择本地`V5ClientLibrary`所在目录 -> finish

* 在您的项目中添加`V5ClientLibrary`为`library`：

> 项目文件夹右键 Properties -> Android -> Library -> Add -> 选择`V5ClientLibrary`

* 编译环境：

> Android SDK需使用API 19以上版本

### 2.2 导入Android Studio

以下方式二选一：

* 1.配置gradle的dependencies添加远程依赖：

```
dependencies {
    compile 'com.v5kf.clientsdk:clientsdk:1.3.11'
}
```

* 2.将V5ClientLibrary目录作为`Module`导入：

> 在Android Studio选择 File -> New -> Import Module -> 选择本地`V5ClientLibrary`所在目录 -> Finish

### 2.3 以文件导入

1. 将 SDK 压缩包 `V5ClientLibrary` 中的 res 文件夹复制到您项目的对应 `res` 文件夹下;
2. 将 SDK 压缩包内的 `V5KF _1.x.x_rxxxx.jar` 复制到您的项目的 `libs` 文件夹下;

> 注:上述文件名称中的“x”表示 0~9 中某一数字，表示版本代号，下同。

## 3 配置AndroidManifest

可以参考 Demo 工程的 `AndroidManifest.xml` 文件来配置您的 AndroidManifest，无论是使用 SDK 的接口开发还是直接使用 Demo 工程的 UI 快速集成都需要对您的项目的 `AndroidManifest.xml` 文件进行下述配置，具体配置项目如下:

- **1. 添加必需的权限**

```xml
<!-- 网络访问权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- 获取网络状态权限 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<!-- 相机权限 -->
<uses-permission android:name="android.permission.CAMERA" />
<!-- 往 SDCard 写入数据权限 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
<!-- 录音权限 -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

- **2. 配置使用自定义的 Application**

在 Application 的 `onCreate` 中需要进行 SDK 的初始化，故需要自定义自己的
Application 类，并在 AndroidManifest.xml 中进行下面配置（若您的项目中已有自定义的Application基类，则可不必关心此项）:

```xml
<application
   android:allowBackup="true"
   android:name="com.your.package.您的自定义 Application 类" 
   android:icon="@drawable/ic_launcher" 
   android:label="@string/app_name" 
   android:theme="@style/AppTheme" >
	<!-- 其他内容 --> 
</application>
```

- **3. 添加必需的服务和 Activity声明**

```xml
<activity
	android:name="com.v5kf.client.ui.ClientChatActivity" 	android:configChanges="keyboardHidden|orientation|screenSize" 
	android:label="@string/v5_chat_title" 
	android:launchMode="singleTask" 
	android:windowSoftInputMode="adjustResize" >
</activity>
<service 
	android:name="com.v5kf.client.lib.V5ClientService" >
</service>
<activity android:name="com.v5kf.client.ui.WebViewActivity" >
</activity>
<activity
	android:theme="@style/v5_transparent_activity"
	android:name="com.v5kf.client.ui.ShowImageActivity" > 
</activity>
```

为兼容Android 7.0，1.2.10版本开始需要配置provider(application内)

```xml
<provider
    android:name="com.v5kf.client.lib.V5FileProvider"
    android:authorities="你的应用包名.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/v5_file_paths">
    </meta-data>
</provider>
```

注意：`V5ClientConfig.FILE_PROVIDER`的值需要设置成`android:authorities`的值：`V5ClientConfig.FILE_PROVIDER="你的应用包名.fileprovider"`（或其他不产生冲突的值），可在`V5ClientAgent.init`初始化同时进行设置。

## 4 SDK接口快速集成

### 4.1 初始化SDK

初始化需要在您自定义的 Application 中执行，示例如下:

```java
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub 
		super.onCreate();
		if (isMainProcess()) { // 判断为主进程，在主进程中初始化，多进程同时初始化可能导致不可预料的后果
			Logger.w("MyApplication", "onCreate isMainProcess V5ClientAgent.init");
			V5ClientConfig.FILE_PROVIDER = "你的应用包名.fileprovider"; // 设置fileprovider的authorities
			V5ClientAgent.init(this, "<站点编号>", "<APP ID>",  new V5InitCallback() {
				
				@Override
				public void onSuccess(String response) {
					// TODO Auto-generated method stub
					Logger.i("MyApplication", "V5ClientAgent.init(): " + response);
				}
				
				@Override
				public void onFailure(String response) {
					// TODO Auto-generated method stub
					Logger.e("MyApplication", "V5ClientAgent.init(): " + response);
				}
			});
		}
	}
	
	public boolean isMainProcess() {
		ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
		List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
		String mainProcessName = getPackageName();
		int myPid = android.os.Process.myPid();
		for (RunningAppProcessInfo info : processInfos) {
			if (info.pid == myPid && mainProcessName.equals(info.processName)) {
				return true;
			}
		}
		return false;
	}
}
```

其中 `<站点编号>` 和 `<APP ID>` 分别是从 V5 后台可以获取到的站点编号和AppID。
![SDK后台配置](./pictures/android_sdk_6.png)

### 4.2 用户信息和参数设置

使用 SDK 提供的 UI 集成，需要在启动会话界面之前进行用户信息和参数配置。配置项如下:

```java
V5ClientConfig config = V5ClientConfig.getInstance(MainActivity.this); 
// V5客服系统客户端配置
// config.setShowLog(true); // 显示日志，默认为true 

/*** 客户信息设置 ***/
// 如果更改了用户信息，需要在设置前调用shouldUpdateUserInfo
// config.shouldUpdateUserInfo();
// 【建议】设置用户昵称 
config.setNickname("android_sdk_test");
// 设置用户性别: 0-未知 1-男 2-女
config.setGender(1); 
// 【建议】设置用户头像URL
config.setAvatar("http://debugimg-10013434.image.myqcloud.com/fe1382d100019cfb572b1934af3d2c04/thumbnail"); 
/**
 *【建议】设置用户OpenId，以识别不同登录用户，不设置则默认由SDK生成，替代v1.2.0之前的uid,
 *  openId将透传到座席端(长度32字节以内，建议使用含字母数字和下划线的字符串，尽量不用特殊字符，若含特殊字符系统会进行URL encode处理，影响最终长度和座席端获得的结果)
 *	若您是旧版本SDK用户，只是想升级，为兼容旧版，避免客户信息改变可继续使用config.setUid，可不用openId
 */
config.setOpenId("android_sdk_test");
//config.setUid(uid); //【弃用】请使用setOpenId替代
// 设置用户VIP等级(0-5)
config.setVip(0);
// 使用消息推送时需设置device_token:集成第三方推送(腾讯信鸽、百度云推)或自定义推送地址时设置此参数以在离开会话界面时接收推送消息
//config.setDeviceToken(XGPushConfig.getToken(getApplicationContext())); 

// [1.3.0新增]设置V5系统内置的客户基本信息，区别于setUserInfo，这是V5系统内置字段
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

// 客户信息键值对，下面为示例（JSONObject）
JSONObject customContent = new JSONObject();
try {
	customContent.put("用户名", "V5KF");
	customContent.put("用户级别", "VIP");
	customContent.put("用户积分", "3000");
	customContent.put("浏览商品", "衬衣");
} catch (JSONException e) {
	e.printStackTrace();
}
// 设置客户信息（自定义字段名称与值，自定义JSONObjectjian键值对，开启会话前设置，替代之前通过`setUserWillSendMessageListener`在消息中携带信息的方式，此方式更加安全便捷）
config.setUserInfo(customContent);
```

当 `nickname`、`openId`、`avatar`、`device_token` 等配置项配置完，下次需要修改(如App内切换了登录账号，修改了客户昵称或头像时)，需要在修改信息前调用 **`V5ClientConfig.getInstance(context).shouldUpdateUserInfo()`**，这样才会向服务端更新这几个配置项。同样若想更新站点信息，需要在`onChatActivityConnect`中调用 **`V5ClientAgent.getInstance().updateSiteInfo()`**。客户信息、站点信息（包含机器人信息和转人工开场白等V5后台可设置的信息）的更新存在缓存策略，系统每隔7天更新，一般无需处理，需要即时更新时方才调用此处接口。

### 4.3 启动会话界面

通过简单地添加一个在线咨询按钮即可使用智能客服客户端功能，在按钮点击事件处理中加入:

```java
// 开启对话界面 
V5ClientAgent.getInstance().startV5ChatActivity(getApplicationContext());
```
