package com.v5kf.client.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.v5kf.client.R;
import com.v5kf.client.lib.DBHelper;
import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5Util;
import com.v5kf.client.lib.V5ClientAgent.ClientLinkType;
import com.v5kf.client.lib.V5ClientAgent.ClientOpenMode;
import com.v5kf.client.lib.V5ClientAgent.ClientServingStatus;
import com.v5kf.client.lib.V5ClientConfig;
import com.v5kf.client.lib.V5KFException;
import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.V5MessageManager;
import com.v5kf.client.lib.callback.MessageSendCallback;
import com.v5kf.client.lib.callback.OnGetMessagesCallback;
import com.v5kf.client.lib.callback.V5MessageListener;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.client.ui.adapter.ClientChatListAdapter;
import com.v5kf.client.ui.adapter.OnChatListClickListener;
import com.v5kf.client.ui.adapter.QuesListAdapter;
import com.v5kf.client.ui.adapter.QuesListAdapter.OnQuesClickListener;
import com.v5kf.client.ui.emojicon.EmojiconEditText;
import com.v5kf.client.ui.keyboard.AppBean;
import com.v5kf.client.ui.keyboard.AppFuncPageView;
import com.v5kf.client.ui.keyboard.AppsAdapter.FuncItemClickListener;
import com.v5kf.client.ui.keyboard.EmoticonBean;
import com.v5kf.client.ui.keyboard.EmoticonsIndicatorView;
import com.v5kf.client.ui.keyboard.EmoticonsKeyBoardBar;
import com.v5kf.client.ui.keyboard.EmoticonsUtils;
import com.v5kf.client.ui.keyboard.IView;
import com.v5kf.client.ui.keyboard.Utils;
import com.v5kf.client.ui.utils.FileUtil;
import com.v5kf.client.ui.utils.HttpUtil;
import com.v5kf.client.ui.utils.UIUtil;
import com.v5kf.client.ui.utils.V5VoiceRecord;
import com.v5kf.client.ui.utils.VoiceErrorCode;
import com.v5kf.client.ui.widget.AlertDialog;

public class ClientChatActivity extends Activity implements V5MessageListener, 
		OnChatListClickListener, OnQuesClickListener, OnRefreshListener, V5VoiceRecord.VoiceRecordListener {

    private static final String TAG = "ClientChatActivity";

    public static final int UI_LIST_ADD = 1;
    public static final int UI_LIST_UPDATE = 2;
    public static final int UI_LIST_UPDATE_NOSCROLL = 3;
    public static final int HDL_CHECK_CONNECT = 4;
    public static final int HDL_BOTTOM = 5;
    public static final int HDL_BOTTOM_SMOOTH = 6;
    public static final int UI_LIST_SCROLL = 7; // 滑到指定位置msg.arg1为位置
    public static final int HDL_VOICE_DISMISS = 101; 
    private static final int RECON_DELAY = 200; // ms
    
    private static final int REQUEST_CODE_PHOTO = 10;
    private static final int REQUEST_CODE_PHOTO_KITKAT = 11;
    private static final int REQUEST_CODE_CAMERA = 12;
    
    private static final int NUM_PER_PAGE = 10;
    
    private int isForeground = 0;
    
    private String mImageFileName;
    
    private String mCacheTitle;
    
    private EmoticonsKeyBoardBar mKeyBar;
    private LayoutInflater mInflater;
    
    // ActionBar
    private TextView mTitleTv; // 标题
    private Button mRightBtn; // 右按钮
    
    // List
    private ListView mChatListView;
    private ClientChatListAdapter mChatListAdapter;
    private List<V5ChatBean> mDatas;
    private SwipeRefreshLayout mRefreshLayout;
    
    // 常见问答
    private ListView mQuesList;
    private QuesListAdapter mQuesAdapter;
    private List<V5TextMessage> mQuesContents;
    private List<String> mHotQues;
    private List<String> mRelativeQues;
    private TextView mQuestionDesc;
    private TextView mQuestionEmpty;
    
    // 输入框
    private EmojiconEditText mInputEt;
    
    private Handler mHandler;
    
    // 历史会话序号
    private int mOffset = 0;
    //private boolean isCurrentFinish = false;
    private boolean isHistoricalFinish = false;
    // 连接标识
	private boolean isConnected = false;
	
	private Dialog mLoadingDialog;
	private AlertDialog mAlertDialog;
	
	// 语音有关view
 	private Button mBtnVoice;
 	private RelativeLayout layout_record;
 	private TextView tv_voice_second;
 	private TextView tv_voice_tips;
 	private TextView tv_voice_title;
 	private ImageView iv_record;
 	private MyCountDownTimer voice_timer;
 	private V5VoiceRecord mRecorder;

 	/* 客户自定义属性 */
	private int numOfMessagesOnRefresh = NUM_PER_PAGE;	// 下拉刷新加载历史消息数量
	private int numOfMessagesOnOpen = 0;				// 打开页面时加载历史消息数量
	private V5Message mOpenAnswer;						// 开场问题的答案，缓存以做保存数据库判断
	private ClientOpenMode mOpenMode = ClientOpenMode.clientOpenModeDefault;
	private String mOpenQuestion;
	private boolean enableVoice = true;
	private boolean showAvatar = true;
	
	private int reconnectFlag = 0; // 自动重连的标识
	private boolean scrollFlag = false;// 标记是否滑动
	private int lastVisibleItemPosition;// 标记上次滑动位置
	private boolean scrollUp = false; // 标记是否上滑
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "[onCreate]");
        //getSupportActionBar().hide(); // 隐藏ActionBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v5_activity_client_chat);
        
        mHandler = new BaseHandler(this);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        // 接收bundle数据
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
	        this.numOfMessagesOnRefresh = bundle.getInt("numOfMessagesOnRefresh", NUM_PER_PAGE);
	        this.numOfMessagesOnOpen = bundle.getInt("numOfMessagesOnOpen", 0);
	        this.mOpenMode = ClientOpenMode.values()[(bundle.getInt("clientOpenMode", ClientOpenMode.clientOpenModeDefault.ordinal()))];
	        this.mOpenQuestion = bundle.getString("clientOpenParam");
	        this.enableVoice = bundle.getBoolean("enableVoice", true); // 是否允许发送语音
	        this.showAvatar = bundle.getBoolean("showAvatar", true); // 是否显示对话双方的头像
        }
        
        findView();
        mKeyBar.setOrientation(getResources().getConfiguration().orientation);
        initView();
        
		// 开启V5消息服务
		V5ClientAgent.getInstance().start(this.getApplicationContext(), this);
		// 表情模块初始化
		EmoticonsUtils.initEmoticonsDB(this);
		
		//mHandler.sendEmptyMessageDelayed(HDL_CHECK_CONNECT, 30000);
		showLoadingProgress();
		
		if (null != V5ClientAgent.getInstance().getChatActivityListener()) {
    		V5ClientAgent.getInstance().getChatActivityListener().onChatActivityCreate(this);
    	}
    }
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
    	Logger.d(TAG, "[onNewIntent]");
//		// [修改]点击通知 重新获取消息
//		/* 点击通知进来刷新最新会话数据 */
//		if (mDatas != null) {
//			V5ClientAgent.getInstance().onNewIntent();
//		}
	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mKeyBar != null) {
			mKeyBar.hideAutoView();
		}
		Utils.closeSoftKeyboard(this);
		Logger.d(TAG, "[onConfigurationChanged] orientation:" + newConfig.orientation);
		// TODO
		mHandler.postDelayed(new Runnable() { // 异步加载
			
			@Override
			public void run() {
				reloadAllViews(newConfig.orientation);
				mKeyBar.hideAutoView();
			}
		}, 50);
	}

	private void reloadAllViews(int orientation) {
		// TODO
		setContentView(R.layout.v5_activity_client_chat);
	    // 重新布局，注意，这里删除了init()，否则又初始化了，状态就丢失
	    findView();
	    
		if (mKeyBar != null) {
			mKeyBar.setOrientation(orientation);
//			if (orientation == Configuration.ORIENTATION_LANDSCAPE) { 
//				// px
//				mKeyBar.setAutoViewHeight(UIUtil.getScreenHeight(getApplicationContext())/2 - 50);
//			} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//				// dp转px
//				mKeyBar.setAutoViewHeight(UIUtil.dp2px(Utils.getDefKeyboardHeight(getApplicationContext()), getApplicationContext()));
//			}
		}
		
	    initView();
	    if (!TextUtils.isEmpty(mCacheTitle)) {
    		mTitleTv.setText(mCacheTitle);
    	}
	}

	private void findView() {
		mTitleTv = (TextView) findViewById(R.id.header_htv_subtitle);
		mRightBtn = (Button) findViewById(R.id.header_right_btn);
		mChatListView = (ListView) findViewById(R.id.id_recycler_msgs);
		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_refresh);
		mKeyBar = (EmoticonsKeyBoardBar) findViewById(R.id.chat_activity_keybar);
		
		mInputEt = mKeyBar.getEt_chat();
		
		/* 语音有关 */
		mBtnVoice = mKeyBar.getBtn_voice(); // 长按语音输入按钮
        layout_record = (RelativeLayout) findViewById(R.id.id_mask_view);
		tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
		tv_voice_title = (TextView) findViewById(R.id.tv_voice_title);
		tv_voice_second = (TextView) findViewById(R.id.tv_voice_second);
		iv_record = (ImageView) findViewById(R.id.iv_record);
		
		/* 添加表情删除按钮 */
		View toolBtnView = mInflater.inflate(R.layout.v5_view_toolbtn_right_simple, null);
        toolBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mKeyBar.del();
            }
        });
        mKeyBar.addFixedView(toolBtnView, true); // 底部栏固定位置的删除按钮
	}

	private void initView() {
		mKeyBar.setVoiceVisibility(this.enableVoice); // [修改]false->true显示语音文字切换按钮
		mKeyBar.setBuilder(EmoticonsUtils.getBuilder(this, mKeyBar.getOrientation() == Configuration.ORIENTATION_LANDSCAPE)); // [修改]传入横竖屏参数
		
		mTitleTv.setText(R.string.v5_title_on_connection);
		
		/* 返回按钮 */
		findViewById(R.id.header_layout_leftview_container).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		/* 退出按钮 */
		mRightBtn.setVisibility(View.GONE);
//		mRightBtn.setBackgroundResource(R.drawable.v5_action_bar_quit);
//		mRightBtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				final WarningDialog dialog = new WarningDialog(ClientChatActivity.this);
//				dialog.setDialogMode(WarningDialog.MODE_TWO_BUTTON);
//				dialog.setContent(R.string.v5_on_exit_service);
//				dialog.setContentViewGravity(Gravity.CENTER);
//				dialog.setOnClickListener(new WarningDialogListener() {					
//					@Override
//					public void onClick(View view) {
//						if (view.getId() == R.id.btn_dialog_warning_right) {
//							V5ClientAgent.getInstance().onDestroy(); // 关闭消息服务
//							dialog.dismiss();
//							finish();
//						}
//					}
//				});
//				dialog.show();
//			}
//		});
				
		initChatListView();
		initAppfunc();	// 位置1
		initQuestionList(); // 位置2
		initKeyboardListener();
		initBtnVoice();
	}
	
	/* 初始化语音录入功能 */
	private void initBtnVoice() {
		mBtnVoice.setOnTouchListener(new VoiceTouchListen());
		mRecorder = new V5VoiceRecord(this, this);
	}

	private void initAppfunc() {
		View viewApps =  mInflater.inflate(R.layout.v5_view_apps, null);
		mKeyBar.add(viewApps); // 位置1-添加“+”号打开的功能界面(富媒体输入)
		
		/* 常见问题、位置、人工客服等功能界面 */
		AppFuncPageView pageApps = (AppFuncPageView)viewApps.findViewById(R.id.view_apv);
		pageApps.setOrientation(mKeyBar.getOrientation()); // [修改]横屏支持
		EmoticonsIndicatorView indicatorView = (EmoticonsIndicatorView)viewApps.findViewById(R.id.view_eiv);
		pageApps.setIndicatorView(indicatorView);
		
		ArrayList<AppBean> mAppBeanList = new ArrayList<AppBean>();
		String[] funcArray = getResources().getStringArray(R.array.v5_chat_func);
		String[] funcIconArray = getResources().getStringArray(R.array.v5_chat_func_icon);
		for (int i = 0; i < funcArray.length; i++) {
			if (funcIconArray[i].equals("v5_icon_location")) {
				continue;
			}
			AppBean bean = new AppBean();
			bean.setId(i);
			bean.setIcon(funcIconArray[i]);
			bean.setFuncName(funcArray[i]);
			mAppBeanList.add(bean);
		}
		pageApps.setAppBeanList(mAppBeanList);
		pageApps.setFuncItemClickListener(new FuncItemClickListener() {			
			@Override
			public void onFuncItemClick(View v, AppBean bean) {
				if (!isConnected) {
					showToast(R.string.v5_waiting_for_connection);
					return;
				}				
				switch (bean.getIcon()) {
				case "v5_icon_ques": // 常见问题
					getHotQuesAndShow();				
					break;
					
				case "v5_icon_relative_ques": // 相关问题					
					mQuestionDesc.setText(R.string.v5_relative_question);
					if (mRelativeQues == null) {
						mRelativeQues = new ArrayList<String>();
					}
					notifyQuestionDataSetChange(mRelativeQues);
					showQuestionList();
					break;
					
				case "v5_icon_photo": // 图片
					if (UIUtil.hasPermission(ClientChatActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
						openSystemPhoto();
					} else {
						showWarningDialog(R.string.v5_permission_photo_deny, null);
					}
					break;
				case "v5_icon_camera": // 拍照
					if (UIUtil.hasPermission(ClientChatActivity.this, "android.permission.CAMERA") && 
							UIUtil.hasPermission(ClientChatActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
						cameraPhoto();
					} else {
						showWarningDialog(R.string.v5_permission_camera_deny, null);
					}
					break;
					
				case "v5_icon_worker": // 人工客服
					V5ClientAgent.getInstance().switchToArtificialService(new MessageSendCallback() {
						
						@Override
						public void onSuccess(V5Message message) {
							// [修改]不显示[转人工客服]消息
//							mDatas.add(message);
//							mOffset++;
//							mHandler.obtainMessage(UI_LIST_UPDATE).sendToTarget();
						}
						
						@Override
						public void onFailure(V5Message message, V5KFException.V5ExceptionStatus statusCode, String desc) {
							switch (statusCode) {
							case ExceptionNoError:
								//showToast("转人工客服消息发送成功");
								break;
							
							default:
								//showToast("转人工客服消息发送失败");
								break;
							}
						}
					});
//					showToast(R.string.v5_artificial_service);
					break;					
				}
			}
		});
	}
	
	private void initQuestionList() {
		View viewQues =  mInflater.inflate(R.layout.v5_view_robot_candidate, null);
		mQuestionDesc = (TextView) viewQues.findViewById(R.id.id_candidate_desc);
		mQuestionEmpty = (TextView) viewQues.findViewById(R.id.id_candidate_empty);
		mQuestionDesc.setVisibility(View.VISIBLE);
        mKeyBar.add(viewQues); // 位置2-添加常见问答、相关问题显示界面
        
        /* 机器人候选答案列表初始化 */
        mQuesList = (ListView) viewQues.findViewById(R.id.id_candidate_list);
        /* 适配器初始化 */
        mQuesContents = new ArrayList<V5TextMessage>();
        mQuesAdapter = new QuesListAdapter(this, mQuesContents, this);
        mQuesList.setAdapter(mQuesAdapter);
	}

	private void initKeyboardListener() {		
		/* 表情点击 */
        mKeyBar.getEmoticonsPageView().addIViewListener(new IView() {
            @Override
            public void onItemClick(EmoticonBean bean) {
            	if (bean.getEventType() == EmoticonBean.FACE_TYPE_NOMAL) { // 表情图标
            		mInputEt.updateText();
            	} else if (bean.getEventType() == EmoticonBean.FACE_TYPE_USERDEF) { // 自定义图片表情
            		/* 
            		 * 自定义表情图片:发送图片
            		 * 图片发送功能完成后，后期可增加将图片添加为表情功能
            		 */
            	}
            }

            @Override
            public void onItemDisplay(EmoticonBean bean) {
            	
            }

            @Override
            public void onPageChangeTo(int position) {
            	
            }
        });

        /* KeyboardBar输入栏按钮监听器 */
        mKeyBar.setOnKeyBoardBarViewListener(new EmoticonsKeyBoardBar.KeyBoardBarViewListener() {
            @Override
            public void OnKeyBoardStateChange(int state, final int height) {
            	Logger.i(TAG, "OnKeyBoardStateChange -- 键盘变化height:" + height);
                mChatListView.post(new Runnable() {
                    @Override
                    public void run() {
                    	if (height > 0) { // 软键盘弹出
                    		mKeyBar.setManualOpen(false);
                    		scrollToBottom(false);
                    	} else if (height == 0) { // 软键盘收起
                    		//isManualOpen()判断是否手动点击展开KeyBar，非手动关闭时不自动关闭
                    		if (mKeyBar.isKeyBoardFootShow() && !mKeyBar.isManualOpen()) {
                				mKeyBar.hideAutoView();
                			}
                    		mKeyBar.setManualOpen(false);
                    		scrollToBottom(false);
                    	} else if (height == -1) { // 输入扩展框大小变化
                    		// 弹出
                    		scrollToBottom(false);
                    	} else if (height < 0) {
                    		//
                    	}
                    }
                });
            }

            @Override
            public void OnSendBtnClick(String msg) { // 发送按钮
            	if (!isConnected) {
					showToast(R.string.v5_waiting_for_connection);
					return;
				}
            	onSendClick(msg);    			
    			mKeyBar.clearEditText();
    			mQuesAdapter.clearSelect(); // 清空已选问题
            }

			@Override
			public void OnVideoBtnClick() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnMultimediaBtnClick() {
				// TODO Auto-generated method stub
				
			}
        });
	}
	
	private void initChatListView() {
		if (null == mDatas) {
			mDatas = new ArrayList<V5ChatBean>();
		}
		if (null == mChatListAdapter) {
			mChatListAdapter = new ClientChatListAdapter(this, mDatas, this, this.showAvatar);
		}
		mChatListView.setAdapter(mChatListAdapter);
        
        // 列表空白点击
        mChatListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			    case MotionEvent.ACTION_DOWN:
			    	// 隐藏键盘
			    	mKeyBar.hideAutoView();
			    	UIUtil.closeSoftKeyboard(ClientChatActivity.this);
			        break;
			    case MotionEvent.ACTION_UP:
			        v.performClick();
			        break;
			    default:
			        break;
			    }
				return false;
			}
		});
        
        // 下拉刷新
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(
        		R.color.v5_green, 
        		R.color.v5_red, 
        		R.color.v5_blue,
        		R.color.v5_yellow);
        
        mChatListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					scrollFlag = true;
				} else {
					scrollFlag = false;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if (scrollFlag) {
					if (firstVisibleItem > lastVisibleItemPosition) {
						scrollUp = true;
					}
					if (firstVisibleItem < lastVisibleItemPosition) {
					}
					if (firstVisibleItem == lastVisibleItemPosition) {
						return;
					}
					lastVisibleItemPosition = firstVisibleItem;
				}
			}
		});
	}
	
	private void showLoadingProgress() {
		if (null == mLoadingDialog) {
			mLoadingDialog = UIUtil.createLoadingDialog(this, null);
		}
		if (mLoadingDialog != null) {
			mLoadingDialog.show();
		}
	}
	
	private void dismissLoadingProgress() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
	}
	
	public void setChatTitle(String title) {
		mCacheTitle = title;
		mTitleTv.setText(title);
	}
	
	protected void showWarningDialog(int contentResId, View.OnClickListener listener) {
		mAlertDialog = 
				new AlertDialog(this).builder()
					.setTitle(R.string.v5_tips)
					.setMsg(contentResId)
					.setCancelable(false)
					.setNegativeButton(R.string.v5_btn_confirm, listener);
		
		mAlertDialog.show();
	}

	protected void showWarningDialog(int contentResId, int rightBtnRes, View.OnClickListener rightBtnListener) {
		mAlertDialog = 
				new AlertDialog(this).builder()
				.setTitle(R.string.v5_tips)
				.setMsg(contentResId)
				.setCancelable(false)
				.setPositiveButton(rightBtnRes, rightBtnListener)
				.setNegativeButton(0, null);
		
		mAlertDialog.show();
	}
	
	public void dismissWarningDialog() {
		if(mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
	}
	
	public boolean isDialogShow() {
		if(mAlertDialog != null && mAlertDialog.isShowing()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 加载历史消息
	 */
	private void loadMessages() {
		Logger.d(TAG, "[loadMessages]");
		if (null == mDatas) {
			mDatas = new ArrayList<V5ChatBean>();
		}
		mDatas.clear();
		getHistoricalMessages(this.numOfMessagesOnOpen);
	}
	
	/**
	 * 发送按钮点击
	 * @param v
	 */
	protected void onSendClick(String msg) {
		if (TextUtils.isEmpty(mInputEt.getText())) {
			showToast(R.string.v5_msg_input_empty);
			return;
		}
		V5TextMessage message = V5MessageManager.getInstance().obtainTextMessage(msg);
		sendV5Message(message);
	}
	
	/**
	 * 获取常见问答(额外接口)
	 */
	protected void getHotQuesAndShow() {
		if (mHotQues != null) {
			notifyQuestionDataSetChange(mHotQues);
			mQuestionDesc.setText(R.string.v5_hot_question);
			showQuestionList();
			return;
		}
		// 请求常见问答
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final String responseString = HttpUtil.getHttpResp(V5Util.getHotQuesUrl(getApplicationContext()));
				if (responseString != null) {
					mHandler.post(new Runnable() { // 在UI线程执行
						
						@Override
						public void run() {
							try {
								JSONObject resp = new JSONObject(UIUtil.decodeUnicode(responseString));
								Logger.d(TAG, "[HotReqsHttpClient] " + UIUtil.decodeUnicode(responseString));
								if (resp.get("state").equals("ok") && resp.getInt("total") > 0) {
									JSONArray arr = resp.getJSONArray("items");
									if (null == mHotQues) {
										mHotQues = new ArrayList<>();
									}
									for (int i = 0; i < arr.length(); i++) {
										mHotQues.add(arr.getString(i));
									}
									notifyQuestionDataSetChange(mHotQues);
									mQuestionDesc.setText(R.string.v5_hot_question);
									showQuestionList();
								} else {
									showToast(R.string.v5_toast_hot_reqs_empty);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});					
				}
			}
		}).start();
	}
	
	private void showQuestionList() {
//		mKeyBar.setManualOpen(true);
//		Utils.closeSoftKeyboard(ClientChatActivity.this);
//		mKeyBar.showAutoView();
		mKeyBar.show(EmoticonsKeyBoardBar.FUNC_CHILLDVIEW_LIST);
	}

	private void showToast(int resId) {
		Toast.makeText(getApplicationContext(), resId,
				Toast.LENGTH_SHORT).show();
	}

	private void showToast(String text) {
		Toast.makeText(getApplicationContext(), text,
				Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Logger.d(TAG, "[onResume]");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Logger.d(TAG, "[onStart] isForeground：" + isForeground);
		if (isForeground == 0) {
			V5ClientAgent.getInstance().onStart();
			isForeground++;
			if (!V5ClientAgent.isConnected()) {
				mTitleTv.setText(R.string.v5_title_on_connection);
				//showLoadingProgress();
			}
		}
		if (null != V5ClientAgent.getInstance().getChatActivityListener()) {
    		V5ClientAgent.getInstance().getChatActivityListener().onChatActivityStart(this);
    	}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Logger.d(TAG, "[onStop] isForeground：" + isForeground);
		if (isForeground == 1) {
			V5ClientAgent.getInstance().onStop();
			isForeground--;
		}
		if (null != V5ClientAgent.getInstance().getChatActivityListener()) {
    		V5ClientAgent.getInstance().getChatActivityListener().onChatActivityStop(this);
    	}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.d(TAG, "[onDestroy]");
		V5ClientAgent.getInstance().onDestroy();
		
		// TODO 退出页面时关闭语音播放
    	mChatListAdapter.stopVoicePlaying();
    	
    	if (null != V5ClientAgent.getInstance().getChatActivityListener()) {
    		V5ClientAgent.getInstance().getChatActivityListener().onChatActivityDestroy(this);
    	}
	}
	
	/**
	 * 向会话消息列表尾添加消息
	 * @param message
	 */
	private int addMessage(V5Message message) {
		if (message.getMsg_id() > 0 && message.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
			// 开场消息在保存数据库时++
		} else {
			mOffset++;
		}
		mDatas.add(new V5ChatBean(message));
		mHandler.obtainMessage(UI_LIST_ADD).sendToTarget();
		return mDatas.indexOf(message);
	}
	
	private void scrollToBottom(boolean smooth) {
//		Logger.d(TAG, "scrollToBottom:" + smooth);
		int position = mDatas.size() -  1;
		if (position >= 0) {
			if (smooth) {
				mChatListView.smoothScrollToPosition(position);
			} else {
				mChatListView.setSelection(position);
			}
		}
	}
	
	private void notifyChatDataSetChange() {
//		Logger.i(TAG, "[notifyChatDataSetChange]");
		mChatListAdapter.notifyDataSetChanged();
	}
	private void notifyQuestionDataSetChange(List<String> faqs) {
//		Logger.i(TAG, "[notifyQuestionDataSetChange] faqs:" + faqs);
		if (faqs == null) {
			return;
		}
		mQuesContents.clear(); // 刷新列表
		for (int i = 0; i < faqs.size(); i++) {
			V5TextMessage msg = V5MessageManager.getInstance().obtainTextMessage(faqs.get(i));
			mQuesContents.add(msg);
		}
		mQuesAdapter.notifyDataSetChanged();
		if (mQuesContents.size() > 0) {
        	mQuestionEmpty.setVisibility(View.GONE);
        	mQuesList.setVisibility(View.VISIBLE);
        } else {
        	mQuestionEmpty.setVisibility(View.VISIBLE);
        	mQuesList.setVisibility(View.GONE);
        }
	}
	
	/* 仅供adapter调用，每次调用延迟200ms更新 */
	public void sendEmptyMessage(int what) {
//		Logger.d(TAG, "[sendEmptyMessage]");
		if (!scrollUp) {
			mHandler.removeMessages(what);
			mHandler.sendEmptyMessageDelayed(what, 200);
		}
	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case UI_LIST_ADD: {
			notifyChatDataSetChange();
			mHandler.sendEmptyMessageDelayed(HDL_BOTTOM_SMOOTH, 100);  // 等100ms图片加载
//			scrollToBottom(true);
			break;
		}
		
		case UI_LIST_UPDATE: {
			notifyChatDataSetChange();
			scrollToBottom(false);
			//mHandler.sendEmptyMessageDelayed(HDL_BOTTOM, 100 * getImagesOfDatas());  // 等图片加载每张图100ms
			if (mRefreshLayout.isRefreshing()) {
				mRefreshLayout.setRefreshing(false);
			}
			// progress dismiss
			dismissLoadingProgress();
			break;
		}
		
		case UI_LIST_UPDATE_NOSCROLL: {
			if (mRefreshLayout.isRefreshing()) {
				mRefreshLayout.setRefreshing(false);
			}
			notifyChatDataSetChange();
			// progress dismiss
			dismissLoadingProgress();
			break;
		}
		
		case HDL_CHECK_CONNECT: {
//			if (!V5ClientAgent.isConnected() && isForeground) {
//				final WarningDialog dialog = new WarningDialog(ClientChatActivity.this);
//				dialog.setDialogMode(WarningDialog.MODE_ONE_BUTTON);
//				dialog.setContent(R.string.v5_connect_error);
//				dialog.setContentViewGravity(Gravity.CENTER);
//				dialog.setOnClickListener(new WarningDialogListener() {					
//					@Override
//					public void onClick(View view) {
//						dialog.dismiss();
//						finish();
//					}
//				});
//				dialog.show();
//			}
			reconnectFlag++;
			V5ClientAgent.getInstance().reconnect();
			break;
		}
		
		case HDL_VOICE_DISMISS: {
			// 隐藏录音layout[延迟隐藏]
			layout_record.setVisibility(View.GONE);
			break;
		}
		
		case HDL_BOTTOM:
			scrollToBottom(false);
			break;

		case HDL_BOTTOM_SMOOTH:
			scrollToBottom(true);
			break;
			
		case UI_LIST_SCROLL:
			if (msg.arg1 >= 0 && msg.arg1 < mDatas.size()) {
				mChatListView.setSelection(msg.arg1 - 1);
			}
			break;
		}
	}

    public static class BaseHandler extends Handler {
		
		WeakReference<ClientChatActivity> mActivity;
		
		public BaseHandler(ClientChatActivity activity) {
			mActivity = new WeakReference<ClientChatActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (null != mActivity.get()) {
				mActivity.get().handleMessage(msg);
			}
		}
	}
    
    @Override
    public void onConnect() {
    	Logger.i(TAG, "[onConnect]");
    	reconnectFlag = 0; // 重置flag
    	
//    	// [修改]每次连接获取数据
//    	isConnected = true;
//    	mOffset = 0;
//    	mTitleTv.setText(R.string.v5_chat_title);
//		loadMessages(); // 获取会话消息
    	if (!TextUtils.isEmpty(mCacheTitle)) {
    		mTitleTv.setText(mCacheTitle);
    	} else {
    		mTitleTv.setText(R.string.v5_chat_title);
    	}
    	if (mAlertDialog != null && mAlertDialog.isShowing()) {
    		mAlertDialog.dismiss();
    	}
    	if (!isConnected) { // 仅首次连接成功执行下列操作
    		if (null != V5ClientAgent.getInstance().getChatActivityListener()) {
        		V5ClientAgent.getInstance().getChatActivityListener().onChatActivityConnect(this);
        	}
    		
        	mOffset = 0;
    		loadMessages(); // 获取会话消息
        	isConnected = true;
    	} else {
    		mDatas.clear();
    		int openNum = mOffset;
    		mOffset = 0;
    		getHistoricalMessages(openNum);
    	}
    }

	@Override
	public void onMessage(String message) {
		Logger.d(TAG, "onMessage:" + message);
		JSONObject json;
		int code = 0;
		try {
			json = new JSONObject(message);
			if (json.has("o_error")) {
				code = json.getInt("o_error");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (code != 0) {
			switch (code) {
			case 50010:
				// 不提示会话结束，自动重新请求并发出消息
//				V5TextMessage messageBean = V5MessageManager.getInstance().obtainTextMessage("【已退出人工客服】");
//				
//				addMessage(messageBean);
				break;
			}
		}
	}

	@Override
	public void onMessage(V5Message message) {
		if (null == message) {
			Logger.e(TAG, "Null message object");
			return;
		}
		try {
			Logger.d(TAG, "onMessage<MessageBean>:" + message.toJson());
		} catch (Exception e) { // JSONException
			e.printStackTrace();
		}
		
		if (null != V5ClientAgent.getInstance().getChatActivityListener() && message.getDirection() != 8) {
    		V5ClientAgent.getInstance().getChatActivityListener().onChatActivityReceiveMessage(this, message);
    	}
		switch (message.getDirection()) {
		case 0: // 普通消息-来自人工
		case 2: // 普通消息-来自机器人
			if (!message.getDefaultContent(this).isEmpty()) {
				addMessage(message);
			}
		break;
		
		case 8: // 相关问题
			if (message.getCandidate() != null) {
				mQuesContents.clear(); // 刷新列表
				if (mRelativeQues == null) {
					mRelativeQues = new ArrayList<>();
				} else {
					mRelativeQues.clear();
				}
				for (V5Message msgContent : (List<V5Message>)message.getCandidate()) {
					if (msgContent.getMessage_type() == V5MessageDefine.MSG_TYPE_TEXT) {
						msgContent.setDirection(V5MessageDefine.MSG_DIR_TO_WORKER);
						mRelativeQues.add(((V5TextMessage)msgContent).getDefaultContent(ClientChatActivity.this));
					}
				}
				mQuestionDesc.setText(R.string.v5_relative_question);

				if (mRelativeQues == null) {
					mRelativeQues = new ArrayList<String>();
				}
				notifyQuestionDataSetChange(mRelativeQues);
				showQuestionList();
			}
			break;
			
		case 9: // 评价问卷
			addMessage(message);
			break;
		}
		if (message.getMsg_id() > 0 && message.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
			this.mOpenAnswer = message;
		}
	}
	
	@Override
	public void onServingStatusChange(ClientServingStatus status) {
		if (null != V5ClientAgent.getInstance().getChatActivityListener()) {
    		V5ClientAgent.getInstance().getChatActivityListener().onChatActivityServingStatusChange(this, status);
    	}
	}

	@Override
	public void onError(V5KFException error) {
		Logger.e(TAG, "onError " + error.toString() + " V5ClientAgent.isConnected():" + V5ClientAgent.isConnected() + " foreGround:" +V5ClientAgent.getInstance().isForeground());
		dismissLoadingProgress();
		//this.isConnected = V5ClientAgent.isConnected();
		if (!V5ClientAgent.isConnected()) {
			switch (error.getStatus()) {
			case ExceptionNoNetwork: // 无网络连接，需要检查网络
				mTitleTv.setText(R.string.v5_title_connect_closed);
				if (V5ClientAgent.getInstance().isForeground()) {
//					showToast(R.string.v5_connect_no_network);
					if (isDialogShow()) {
						break;
					}
					showWarningDialog(R.string.v5_connect_no_network,
							R.string.v5_btn_retry, 
							new View.OnClickListener() {
								
								@Override
								public void onClick(View v) {
									if (!V5ClientAgent.isConnected()) {
										V5ClientAgent.getInstance().reconnect();
										showLoadingProgress();
									}
								}
							});
				}
				break;
			case ExceptionConnectionError: // 连接异常出错，会自动重连
				if (V5ClientAgent.getInstance().isForeground()) {
					if (V5ClientConfig.AUTO_RETRY_ONERROR && reconnectFlag < 3) { // 自动重试3次
						mHandler.sendEmptyMessageDelayed(HDL_CHECK_CONNECT, RECON_DELAY);
					} else {
						mTitleTv.setText(R.string.v5_title_connect_closed);
	//					showToast(R.string.v5_connect_error);
						if (isDialogShow()) {
							break;
						}
						showWarningDialog(R.string.v5_connect_error,
								R.string.v5_btn_retry, 
								new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										if (!V5ClientAgent.isConnected()) {
											V5ClientAgent.getInstance().reconnect();
											showLoadingProgress();
										}
									}
								});
					}
				}
				break;
			case ExceptionNotConnected: // 未连接（发送消息时的反馈|网络断开时）
				if (V5ClientAgent.getInstance().isForeground()) {
					if (V5ClientConfig.AUTO_RETRY_ONERROR && reconnectFlag < 3) { // 自动重试3次
						mHandler.sendEmptyMessageDelayed(HDL_CHECK_CONNECT, RECON_DELAY);
					} else {
						mTitleTv.setText(R.string.v5_title_connect_closed);
	//					showToast(R.string.v5_connect_error);
						if (isDialogShow()) {
							break;
						}
						showWarningDialog(R.string.v5_connect_error,
								R.string.v5_btn_retry, 
								new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										if (!V5ClientAgent.isConnected()) {
											V5ClientAgent.getInstance().reconnect();
											showLoadingProgress();
										}
									}
								});
					}
				}
				break;
			case ExceptionNotInitialized:
				if (V5ClientConfig.getInstance(getApplicationContext()).getShowLog()) {
					showToast("SDK init failed");
				}
				break;
			case ExceptionWSAuthFailed: {
				if (V5ClientAgent.getInstance().isForeground()) {
					if (V5ClientConfig.AUTO_RETRY_ONERROR && reconnectFlag < 3) { // 自动重试3次
						mHandler.sendEmptyMessageDelayed(HDL_CHECK_CONNECT, RECON_DELAY);
					} else {
						mTitleTv.setText(R.string.v5_title_connect_closed);
						if (isDialogShow()) {
							break;
						}
						showWarningDialog(R.string.v5_connect_error,
								R.string.v5_btn_retry, 
								new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										if (!V5ClientAgent.isConnected()) {
											V5ClientAgent.getInstance().reconnect();
											showLoadingProgress();
										}
									}
								});
					}
				}
				break;
			}
			case ExceptionSocketTimeout: {
				if (V5ClientAgent.getInstance().isForeground()) {
					if (V5ClientConfig.AUTO_RETRY_ONERROR && reconnectFlag < 3) { // 自动重试3次
						mHandler.sendEmptyMessageDelayed(HDL_CHECK_CONNECT, RECON_DELAY);
					} else {
						mTitleTv.setText(R.string.v5_title_socket_timeout);
						if (isDialogShow()) {
							break;
						}
						showWarningDialog(R.string.v5_connect_timeout,
								R.string.v5_btn_retry, 
								new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										if (!V5ClientAgent.isConnected()) {
											V5ClientAgent.getInstance().reconnect();
											showLoadingProgress();
										}
									}
								});
					}
				}
				break;
			}
//			case ExceptionNoAudioPermission:
//				showWarningDialog(R.string.v5_error_record_not_permit, null);
//				break;
			default:
				break;
			}
		}
		mHandler.obtainMessage(UI_LIST_UPDATE_NOSCROLL).sendToTarget();
//		if (!NetworkManager.isConnected(this)) {
//			mHandler.obtainMessage(HDL_CHECK_CONNECT).sendToTarget();
//		}
	}

	@Override
	public void onChatItemClick(View v, int position, int viewType) {
		V5Message message = mDatas.get(position).getMessage();
		if (null == message) {
			Logger.e(TAG, "ViewHolder position:" + position + " has null V5Message");
			return;
		}
		
		if (v.getId() == R.id.id_news_layout) {
			if (ClientChatListAdapter.TYPE_SINGLE_NEWS == viewType) {
				V5ArticlesMessage msg = (V5ArticlesMessage) message;
				if (msg.getArticles().get(0) != null) {
					onSingleNewsClick(msg.getArticles().get(0).getUrl());
				}
			}
		} else if (v.getId() == R.id.id_left_location_layout
				|| v.getId() == R.id.id_right_location_layout) {
			if (ClientChatListAdapter.TYPE_IMG_L == viewType || 
					ClientChatListAdapter.TYPE_IMG_R == viewType) {
				V5ImageMessage msg = (V5ImageMessage) message;
				if (msg.getFilePath() != null && FileUtil.isFileExists(msg.getFilePath())) {
					gotoShowImageActivity(msg.getFilePath());
				} else if (msg.getPic_url() != null) {
					gotoShowImageActivity(msg.getPic_url());
				}
				
//				if (msg.getFilePath() != null) {
//					gotoShowImageActivity(msg.getFilePath());
//				} else if (msg.getPic_url() != null) {
//					gotoShowImageActivity(msg.getPic_url());
//				}  
			} else if (ClientChatListAdapter.TYPE_LOCATION_L == viewType || 
					ClientChatListAdapter.TYPE_LOCATION_R == viewType) {
				V5LocationMessage msg = (V5LocationMessage) message;
				if (V5ClientAgent.getInstance().getLocationMapClickListener() != null) {
					V5ClientAgent.getInstance().getLocationMapClickListener().onLocationMapClick(ClientChatActivity.this, msg.getX(), msg.getY());
				} else {
					gotoLocationMapActivity(msg.getX(), msg.getY());
				}
			}
		} else if (v.getId() == R.id.id_msg_fail_iv) { // 重发
			// 隐藏重发IV，显示ProgressBar
			V5ClientAgent.getInstance().checkConnect();
			V5ClientAgent.getInstance().sendMessage(message, new MessageSendCallback() {
				
				@Override
				public void onSuccess(V5Message message) {
					Logger.i(TAG, "V5Message Resend success");
					notifyChatDataSetChange();
				}
				
				@Override
				public void onFailure(V5Message message, V5KFException.V5ExceptionStatus statusCode, String desc) {
					Logger.e(TAG, "V5Message Resend failed");
					notifyChatDataSetChange();
				}
			});
		} else if (ClientChatListAdapter.TYPE_VOICE_L == viewType || 
				ClientChatListAdapter.TYPE_VOICE_R == viewType) {
			// 点击语音,已处理
		} else {
			Logger.d(TAG, "Click to close soft keyboard.");
			UIUtil.closeSoftKeyboard(this);
			if (mKeyBar.isKeyBoardFootShow()) {
				mKeyBar.hideAutoView();
			}
		}
	}

	@Override
	public void onChatItemLongClick(View v, int position, int viewType) {
		// 列表内控件长按事件响应
	}
	
	@Override
	public void onMultiNewsClick(View v, int position, int viewType, int newsPos) {
		// 隐藏键盘
    	mKeyBar.hideAutoView();
    	UIUtil.closeSoftKeyboard(ClientChatActivity.this);
    	
		if (position < mDatas.size()) {
			V5Message message = mDatas.get(position).getMessage();
			if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_ARTICLES) {
				V5ArticlesMessage articles = (V5ArticlesMessage) message;
				if (articles != null && articles.getArticles().size() > newsPos) {
					String url = articles.getArticles().get(newsPos).getUrl();
					boolean used = false;
					if (V5ClientAgent.getInstance().getURLClickListener() != null) {
						used = V5ClientAgent.getInstance().getURLClickListener().onURLClick(getApplicationContext(), ClientLinkType.clientLinkTypeArticle, url);
					}
					if (!used) {
						Intent intent = new Intent(this, WebViewActivity.class);
						intent.putExtra("url", url);
						startActivity(intent);
					}
				}
			}
		}
	}
	
	private void gotoLocationMapActivity(double x, double y) {
		//isForeground++;
		Intent intent = new Intent(this, ShowImageActivity.class);
		String url = String.format(Locale.CHINA, UIUtil.MAP_PIC_API_FORMAT, x, y, x, y);;
		intent.putExtra("pic_url", url);
		startActivity(intent);
	}

	private void gotoShowImageActivity(String pic_url) {
		//isForeground++;
		Intent intent = new Intent(this, ShowImageActivity.class);
		intent.putExtra("pic_url", pic_url);
		startActivity(intent);
	}

	private void onSingleNewsClick(String url) {
		// 隐藏键盘
    	mKeyBar.hideAutoView();
    	UIUtil.closeSoftKeyboard(ClientChatActivity.this);
    	
    	boolean used = false;
		if (V5ClientAgent.getInstance().getURLClickListener() != null) {
			used = V5ClientAgent.getInstance().getURLClickListener().onURLClick(getApplicationContext(), ClientLinkType.clientLinkTypeArticle, url);
		}
		if (!used) {
			Intent intent = new Intent(this, WebViewActivity.class);
			intent.putExtra("url", url);
			startActivity(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CAMERA || 
				requestCode == REQUEST_CODE_PHOTO_KITKAT ||
				requestCode == REQUEST_CODE_PHOTO) {
			Logger.d(TAG, "onStop isForeground onActivityResult");
			isForeground--;
			
			if (data != null) {
				// 图库获取拍好的图片
				if (data.getData() != null) { //防止没有返回结果 
					Uri uri = data.getData(); 
					if (uri != null) {
						String filePath = FileUtil.getRealFilePath(getApplicationContext(), uri);
						Logger.i(TAG, "Photo:" + filePath);
						// 图片格式验证
						String type = V5Util.getImageMimeType(filePath);
						if (!V5Util.isValidImageMimeType(type)) {
							showToast(String.format(getString(R.string.v5_unsupport_image_type_fmt), type));
							return;
						}
						// 图片角度矫正
						UIUtil.correctBitmapAngle(filePath);
						V5ImageMessage imageMessage = V5MessageManager.getInstance().obtainImageMessage(filePath);
						sendV5Message(imageMessage);
					}
				}
			} else if (resultCode == RESULT_OK) {
				// 拍照返回
				String filePath = FileUtil.getImageSavePath(this) + "/" + mImageFileName;
				Logger.i(TAG, "Camera:" + filePath);
				// 图片格式验证
				String type = V5Util.getImageMimeType(filePath);
				if (!V5Util.isValidImageMimeType(type)) {
					showToast(String.format(getString(R.string.v5_unsupport_image_type_fmt), type));
					return;
				}
				// 图片角度矫正
				UIUtil.correctBitmapAngle(filePath);
				V5ImageMessage imageMessage = V5MessageManager.getInstance().obtainImageMessage(filePath);
				sendV5Message(imageMessage);
			}
		}
	}

	/**
	 * 发送消息
	 * @param message
	 */
	private void sendV5Message(V5Message message) {
		addMessage(message);
		if (mOpenAnswer != null) {
			DBHelper dbh = new DBHelper(getApplicationContext());
			dbh.insert(mOpenAnswer, true);
			mOffset++;
			mOpenAnswer = null;
		}
		if (null != V5ClientAgent.getInstance().getUserWillSendMessageListener()) {
			V5Message returnMsg = V5ClientAgent.getInstance().getUserWillSendMessageListener().onUserWillSendMessage(message);
			if (returnMsg != null) {
				message = returnMsg;
			}
		}
		V5ClientAgent.getInstance().sendMessage(message, new MessageSendCallback() {
			
			@Override
			public void onSuccess(V5Message message) {
				Logger.d(TAG, "V5Message.getState:" + message.getState());
				notifyChatDataSetChange();
			}
			
			@Override
			public void onFailure(V5Message message, V5KFException.V5ExceptionStatus statusCode, String desc) {
				Logger.e(TAG, "V5Message.getState:" + message.getState() + " exception(" + statusCode + "):" + desc);
				notifyChatDataSetChange();
				if (statusCode == V5ExceptionStatus.ExceptionNoAudioPermission) {
					showWarningDialog(R.string.v5_error_record_not_permit, null);
				}
			}
		});
	}

	@Override
	public void onQuesItemClick(View v, int position, boolean isClicked) {
		V5Message msgContent = mQuesContents.get(position);
		if (null == msgContent) {
			Logger.e(TAG, "onQuesItemClick position:" + position + " has null V5Message");
			return;
		}
		if (isClicked) {
			mInputEt.setText(msgContent.getDefaultContent(this));
			mInputEt.setSelection(msgContent.getDefaultContent(this).length());
		} else {
			mInputEt.setText("");			
		}
	}

	@Override
	public void onRefresh() {
		if (!isConnected) {
			showToast(R.string.v5_waiting_for_connection);
			if (mRefreshLayout.isRefreshing()) {
				mRefreshLayout.setRefreshing(false);
			}
			return;
		}
		//if (isCurrentFinish) {
			if (isHistoricalFinish) {
				showToast(R.string.v5_no_more_messages);
				if (mRefreshLayout.isRefreshing()) {
					mRefreshLayout.setRefreshing(false);
				}
				return;
			}
			getHistoricalMessages(this.numOfMessagesOnRefresh);
		//} else {
		//	getCurrentMessages();
		//}		
	}
	
//	private void getCurrentMessages() {
//		mOffset = 0;
//		V5ClientAgent.getInstance().
//			getCurrentMessages(
//				0,
//				0,
//				new OnGetMessagesCallback() {
//		
//			@Override
//			public void complete(List<V5Message> msgs, int offset, int size, boolean finish) {
//				isCurrentFinish = finish;
//				if (mOffset == 0 && size == 0) {
//					isCurrentFinish = true;
//				}
//				if (msgs != null) {
//					for (V5Message msg : msgs) {
//						mDatas.add(0, msg);
//					}
//					mOffset += mDatas.size();
//				}
//				mHandler.sendEmptyMessage(UI_LIST_UPDATE);
//			}
//		});
//	}

	private void getHistoricalMessages(final int msgSize) {
		V5ClientAgent.getInstance().
			getMessages(
				this, 
				mOffset,
				msgSize,
				new OnGetMessagesCallback() {
		
			@Override
			public void complete(List<V5Message> msgs, int offset, int size, boolean finish) {
				isHistoricalFinish = finish;
				Logger.d(TAG, "[getHistoricalMessages] complete size=" + size);
				if (msgs != null) {
					for (V5Message msg : msgs) {
						mDatas.add(0, new V5ChatBean(msg));
					}
//					// [修改]排序
//					MessagesCompartor mc = new MessagesCompartor();
//					Collections.sort(mDatas, mc);
					mOffset += msgs.size();
				}
				
				if (mDatas.isEmpty() || mOpenMode != ClientOpenMode.clientOpenModeDefault) {
					V5ClientAgent.getInstance().getOpeningMessage(mOpenMode, mOpenQuestion);
				}
				if (offset == 0 && msgSize > 0) {
					mHandler.obtainMessage(UI_LIST_UPDATE).sendToTarget();
				} else {
					mHandler.obtainMessage(UI_LIST_UPDATE_NOSCROLL).sendToTarget();
					Message msg = new Message();
					msg.what = UI_LIST_SCROLL;
					msg.arg1 = msgs.size();
					mHandler.sendMessage(msg);
				}
			}
		});
	}

	/**
     * 打开系统相册
     */
    private void openSystemPhoto() {
    	isForeground++; // 防止触发V5ClientAgent.onStop()
    	
//        Intent intent = new Intent();
////		intent.setType("image/*");
//        intent.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                "image/*");
//        intent.setAction(Intent.ACTION_PICK);
//        startActivityForResult(intent, REQUEST_CODE_PHOTO);
        
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT  
        intent.addCategory(Intent.CATEGORY_OPENABLE);  
        intent.setType("image/*");
        if (android.os.Build.VERSION.SDK_INT >= 19) {                  
        	startActivityForResult(intent, REQUEST_CODE_PHOTO_KITKAT);    
        } else {                
        	startActivityForResult(intent, REQUEST_CODE_PHOTO);   
        }
    }
    
    /**
     * 调用相机拍照
     */
    private void cameraPhoto() {
    	isForeground++; // 防止触发V5ClientAgent.onStop()
    	
        String sdStatus = Environment.getExternalStorageState();
        /* 检测sdcard是否可用 */
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            showToast(R.string.v5_no_sdcard);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImageFileName = FileUtil.getImageName("capture");
        //必须确保文件夹路径存在，否则拍照后无法完成回调 
        File vFile = new File(FileUtil.getImageSavePath(this), mImageFileName);
        File vDirPath = vFile.getParentFile();
        if(!vDirPath.exists()) {
        	vDirPath.mkdirs();
        }
        Uri uri = Uri.fromFile(vFile);
        Logger.d(TAG, " Uri:" + FileUtil.getRealFilePath(this, uri));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
	}
    
    public int getNumOfMessagesOnRefresh() {
		return numOfMessagesOnRefresh;
	}

	public void setNumOfMessagesOnRefresh(int numOfMessagesOnRefresh) {
		this.numOfMessagesOnRefresh = numOfMessagesOnRefresh;
	}

	public int getNumOfMessagesOnOpen() {
		return numOfMessagesOnOpen;
	}

	public void setNumOfMessagesOnOpen(int numOfMessagesOnOpen) {
		this.numOfMessagesOnOpen = numOfMessagesOnOpen;
	}
	
	/**
	 * 消息排序比较器
	 * @author Chenhy	
	 * @email chenhy@v5kf.com
	 * @version v1.0 2016-3-7  
	 *
	 */
	static class MessagesCompartor implements Comparator<V5Message> {

		@Override
		public int compare(V5Message lhs, V5Message rhs) {
			long r = lhs.getCreate_time();
			long l = rhs.getCreate_time();
			if (l == r) {
				if (lhs.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
					return 1;
				} else if (rhs.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
					return -1;
				}
			} else if (l < r) {
				return 1;
			} else if (l > r) {
				return -1;
			}
			return 0;
		}
	}
	
	/**
	 * 长按说话
	 * @ClassName: VoiceTouchListen
	 * @author smile
	 * @date 2015-11-13 下午6:10:16
	 */
	class VoiceTouchListen implements View.OnTouchListener {
		
		private boolean isPressing;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.performClick();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Logger.i(TAG, "ACTION_DOWN");
				if (!UIUtil.hasPermission(ClientChatActivity.this, "android.permission.RECORD_AUDIO")) {
					showWarningDialog(R.string.v5_error_record_not_permit, null);
					return false;
				}
				v.setPressed(true);
				try {
					// 开始录音
					if (!isPressing) {
						isPressing = true;
						mRecorder.startListening();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					tv_voice_tips.setText(getString(R.string.v5_chat_voice_cancel_tips));
					//tv_voice_tips.setTextColor(Color.RED);
				} else {
					tv_voice_tips.setText(getString(R.string.v5_chat_voice_up_tips));
					//tv_voice_tips.setTextColor(Color.rgb(0x78, 0x78, 0x78));
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				Logger.i(TAG, "ACTION_UP");
				v.setPressed(false);
				try {
					if (isPressing) {
						isPressing = false;
						if (event.getY() < 0) { // 放弃录音
							mRecorder.cancel(-1);
							Logger.i(TAG, "放弃发送语音");
						} else { // 结束录音
							if (voice_timer != null && voice_timer.millisInCurrent < 1000) {
								
								Logger.i(TAG, "录音时间太短");
								mRecorder.cancel(-2);
							} else {
								mRecorder.stopListening();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			default:
				return false;
			}
		}
	}
	
	/** 
	 * 继承 CountDownTimer 
	 * 
	 * 重写 父类的方法 onTick() 、 onFinish() 
	 */ 
	class MyCountDownTimer extends CountDownTimer {
		
		protected long  millisInCurrent; // 语音用时
		protected long  millisTotal;	 // 最大总时长
	    /** 
	     * 
	     * @param millisInFuture 
	     *      表示以毫秒为单位 倒计时的总数 
	     * 
	     *      例如 millisInFuture=1000 表示1秒 
	     * 
	     * @param countDownInterval 
	     *      表示 间隔 多少微秒 调用一次 onTick 方法 
	     * 
	     *      例如: countDownInterval =1000 ; 表示每1000毫秒调用一次onTick() 
	     * 
	     */
		public MyCountDownTimer(long millisInFuture, long countDownInterval) { 
			super(millisInFuture, countDownInterval); 
			millisInCurrent = 0;
			millisTotal = millisInFuture;
		} 
	  
		@Override
		public void onFinish() { 
			Logger.i(TAG, "[onFinish]");
			mRecorder.stopListening();
		} 
	  
	    @Override
	    public void onTick(long millisUntilFinished) { // millisUntilFinished 剩余时长
	    	Logger.i(TAG, "[onTick] - " + millisUntilFinished);
	    	millisInCurrent = millisTotal - millisUntilFinished;
	    	tv_voice_second.setText(String.format("%.1f", millisUntilFinished/1000.0f));
	    	if (millisUntilFinished < 10000) {
	    		tv_voice_second.setTextColor(Color.RED); // 剩余时间小于10s时提示
	    	} else {
	    		tv_voice_second.setTextColor(0xff1ec3ff);
	    	}
	    } 
	}
	

	/* 语音 */
	// 更新录音UI状态
	private void showVoiceRecordingProgress() {
		// 显示录音layout
		layout_record.setVisibility(View.VISIBLE);
		tv_voice_tips.setVisibility(View.VISIBLE);
		tv_voice_title.setVisibility(View.VISIBLE);
		tv_voice_tips.setText(getString(R.string.v5_chat_voice_cancel_tips));
		// 录音动画
		Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.v5_round_loading);
		if (operatingAnim != null) {  
			iv_record.startAnimation(operatingAnim);  
		}
		// 开始倒计时
		tv_voice_second.setText("60.0");
		if (voice_timer == null) {
			voice_timer = new MyCountDownTimer(60000, 100);
		}
		voice_timer.start();
		Logger.d(TAG, "[showVoiceRecordingProgress] done");
	}
	
	/**
	 * 隐藏录音进度框
	 * @param state 0:成功  -1:取消 -2:太短
	 */
	private void dismissVoiceRecordingProgress(int state) {
		tv_voice_tips.setVisibility(View.GONE);
		tv_voice_title.setVisibility(View.GONE);
		
		// 停止倒计时
		if (voice_timer != null) {
			voice_timer.cancel();
		}
		Logger.i(TAG, "voice_timer stop");
		// 清除动画
		iv_record.clearAnimation();
		switch (state) {
		case 1:
			tv_voice_second.setText("结束");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 400);
			break;
		
		case 0:
			tv_voice_second.setText("成功");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 400);
			break;
		case -1:
			tv_voice_second.setText("取消");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 400);
			break;
		case -2:
			tv_voice_second.setText("太短");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 600);
			break;
		case -3:
			tv_voice_second.setText("出错");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 600);
			break;
		}
//		// 隐藏录音layout[延迟隐藏]
//		layout_record.setVisibility(View.GONE);
		Logger.d(TAG, "[dismissVoiceRecordingProgress] done");
	}
	
	@Override
	public void onBeginOfSpeech() {
		Logger.i(TAG, "[onBeginOfSpeech]");
		showVoiceRecordingProgress();
	}

	@Override
	public void onCancelOfSpeech(int state) {
		Logger.i(TAG, "[onCancelOfSpeech]");
		dismissVoiceRecordingProgress(state);
	}

	@Override
	public void onErrorOfSpeech(int errorCode, String desc) {
		Logger.e(TAG, "[onErrorOfSpeech] code("+errorCode+"):" + desc);
		if (layout_record.getVisibility() == View.VISIBLE) {
			dismissVoiceRecordingProgress(-3);
		}
		
		switch (errorCode) {
		case VoiceErrorCode.E_RECORD_NOT_PERMIT: // 可能未取得录音权限
			showWarningDialog(R.string.v5_error_record_not_permit, null);
			break;
		case VoiceErrorCode.E_NOSDCARD: // 存储路径错误
			showWarningDialog(R.string.v5_error_no_sdcard, null);
			break;
		}
	}

	@Override
	public void onResultOfSpeech(String path) {
		Logger.i(TAG, "[onResultOfSpeech] " + path + " fileSize:" + V5Util.getFileSize(new File(path)));
		dismissVoiceRecordingProgress(0);
		// 发送语音
		V5VoiceMessage voiceMessage = V5MessageManager.obtainVoiceMessage(path);
		sendV5Message(voiceMessage);
	}

}
