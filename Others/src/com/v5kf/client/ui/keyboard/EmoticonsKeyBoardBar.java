package com.v5kf.client.ui.keyboard;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.v5kf.client.R;
import com.v5kf.client.lib.Logger;
import com.v5kf.client.ui.emojicon.EmojiconEditText;
import com.v5kf.client.ui.utils.UIUtil;

public class EmoticonsKeyBoardBar extends AutoHeightLayout implements IEmoticonsKeyboard, View.OnClickListener,EmoticonsToolBarView.OnToolBarItemClickListener {

	/**
	 * 0-显示表情
	 */
    public static int FUNC_CHILLDVIEW_EMOTICON = 0;
    
    /**
     * 1-显示多媒体内容
     */
    public static int FUNC_CHILLDVIEW_APPS = 1;
    
    /**
     * 2-显示自定义列表内容
     */
    public static int FUNC_CHILLDVIEW_LIST = 2;
    
    public int mChildViewPosition = -1;

    private EmoticonsPageView mEmoticonsPageView;
    private EmoticonsIndicatorView mEmoticonsIndicatorView;
    private EmoticonsToolBarView mEmoticonsToolBarView;

    /**
     * 支持符号表情的编辑框
     */
    private EmojiconEditText et_chat;
//    private EditText et_chat;
    
    /**
     * 包围输入框和表情按钮的RL
     */
    private RelativeLayout rl_input;
    
    /**
     * 整个输入框下方的LL
     */
    private LinearLayout ly_foot_func;
    
    /**
     * 表情按钮
     */
    private ImageView btn_face;
    
    /**
     * “+”按钮
     */
    private ImageView btn_multimedia;
    
    /**
     * 发送按钮
     */
    private Button btn_send;
    
    /**
     * 语音长按输入按钮
     */
    private Button btn_voice;
    
    /**
     * 语音文本切换按钮
     */
    private ImageView btn_voice_or_text;

    private boolean mIsMultimediaVisibility = true;

	private boolean manualOpen = false;
	
	public EmoticonsKeyBoardBar(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.v5_view_keyboardbar, this);
        initView();
	}

    public EmoticonsKeyBoardBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.v5_view_keyboardbar, this);
        initView();
    }
    
    public EmoticonsKeyBoardBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.v5_view_keyboardbar, this);
        initView();
    }

    private void initView() {
        mEmoticonsPageView = (EmoticonsPageView) findViewById(R.id.view_epv);
        mEmoticonsIndicatorView = (EmoticonsIndicatorView) findViewById(R.id.view_eiv);
        mEmoticonsToolBarView = (EmoticonsToolBarView) findViewById(R.id.view_etv);

        rl_input = (RelativeLayout) findViewById(R.id.rl_input);
        ly_foot_func = (LinearLayout) findViewById(R.id.ly_foot_func);
        btn_face = (ImageView) findViewById(R.id.btn_face);
        btn_voice_or_text = (ImageView) findViewById(R.id.btn_voice_or_text);
        btn_voice = (Button) findViewById(R.id.btn_voice);
        btn_multimedia = (ImageView) findViewById(R.id.btn_multimedia);
        btn_send = (Button) findViewById(R.id.btn_send);
        et_chat = (EmojiconEditText) findViewById(R.id.et_chat);
//        et_chat = (EditText) findViewById(R.id.et_chat);

        setAutoHeightLayoutView(ly_foot_func);
        btn_voice_or_text.setOnClickListener(this);
        btn_multimedia.setOnClickListener(this);
        btn_face.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_voice.setOnClickListener(this);

        mEmoticonsPageView.setOnIndicatorListener(new EmoticonsPageView.OnEmoticonsPageViewListener() {
            @Override
            public void emoticonsPageViewInitFinish(int count) {
                mEmoticonsIndicatorView.init(count);
            }

            @Override
            public void emoticonsPageViewCountChanged(int count) {
                mEmoticonsIndicatorView.setIndicatorCount(count);
            }

            @Override
            public void playTo(int position) {
                mEmoticonsIndicatorView.playTo(position);
            }

            @Override
            public void playBy(int oldPosition, int newPosition) {
                mEmoticonsIndicatorView.playBy(oldPosition, newPosition);
            }
        });

        mEmoticonsPageView.setIViewListener(new IView() {
            @Override
            public void onItemClick(EmoticonBean bean) {
                if (et_chat != null) {
                    et_chat.setFocusable(true);
                    et_chat.setFocusableInTouchMode(true);
                    et_chat.requestFocus();

                    // 删除
                    if (bean.getEventType() == EmoticonBean.FACE_TYPE_DEL) {
                        int action = KeyEvent.ACTION_DOWN;
                        int code = KeyEvent.KEYCODE_DEL;
                        KeyEvent event = new KeyEvent(action, code);
                        et_chat.onKeyDown(KeyEvent.KEYCODE_DEL, event);
                        return;
                    }
                    
                    // 用户自定义
                    else if (bean.getEventType() == EmoticonBean.FACE_TYPE_USERDEF) {
                        return;
                    }

                    // 其他默认表情
                    int index = et_chat.getSelectionStart();
                    Editable editable = et_chat.getEditableText();
                    if (index < 0) {
                        editable.append(bean.getContent());
                    } else {
                        editable.insert(index, bean.getContent());
                    }
                }
            }

            @Override
            public void onItemDisplay(EmoticonBean bean) { }

            @Override
            public void onPageChangeTo(int position) {
                mEmoticonsToolBarView.setToolBtnSelect(position);
            }
        });

        mEmoticonsToolBarView.setOnToolBarItemClickListener(new EmoticonsToolBarView.OnToolBarItemClickListener() {
            @Override
            public void onToolBarItemClick(int position) {
                mEmoticonsPageView.setPageSelect(position);
            }
        });

        et_chat.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!et_chat.isFocused()) {
                    et_chat.setFocusable(true);
                    et_chat.setFocusableInTouchMode(true);
                }
                return false;
            }
        });
        et_chat.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    setEditableState(true);
                } else {
                    setEditableState(false);
                }
            }
        });

        //////
        et_chat.setOnSizeChangedListener(new EmojiconEditText.OnSizeChangedListener() {//////
            @Override
            public void onSizeChanged() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if(mKeyBoardBarViewListener != null){
                            mKeyBoardBarViewListener.OnKeyBoardStateChange(mKeyboardState, -1);
                        }
                    }
                });
            }
        });

        //////
        et_chat.setOnTextChangedInterface(new EmojiconEditText.OnTextChangedInterface() {//////
            @Override
            public void onTextChanged(CharSequence arg0) {
                // -> 显示Add按钮
                if (TextUtils.isEmpty(arg0)) {
                    if(mIsMultimediaVisibility){
                        btn_multimedia.setVisibility(VISIBLE);
                        btn_send.setVisibility(GONE);
                    }
                }
                // -> 显示发送按钮
                else {
                    if(mIsMultimediaVisibility) {
                        btn_multimedia.setVisibility(GONE);
                        btn_send.setVisibility(VISIBLE);
                    }
                }
            }
        });

/*		// 改为普通EditText
        et_chat.addTextChangedListener(new TextWatcher() {//////
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				String str = s.toString();
                // -> 显示Add按钮
                if (TextUtils.isEmpty(str)) {
                    if(mIsMultimediaVisibility){
                        btn_multimedia.setVisibility(VISIBLE);
                        btn_send.setVisibility(GONE);
                    }
                }
                // -> 显示发送按钮
                else {
                    if(mIsMultimediaVisibility) {
                        btn_multimedia.setVisibility(GONE);
                        btn_send.setVisibility(VISIBLE);
                    }
                }
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});//////
*/
    }

    public void setEditableState(boolean b) {
        if (b) {
            et_chat.setFocusable(true);
            et_chat.setFocusableInTouchMode(true);
            et_chat.requestFocus();
            rl_input.setBackgroundResource(R.drawable.v5_edit_input_border_bg_active);
        } else {
            et_chat.setFocusable(false);
            et_chat.setFocusableInTouchMode(false);
            rl_input.setBackgroundResource(R.drawable.v5_edit_input_border_bg_normal);
        }
    }

    public EmoticonsToolBarView getEmoticonsToolBarView() {
        return mEmoticonsToolBarView;
    }

    public EmoticonsPageView getEmoticonsPageView() {
        return mEmoticonsPageView;
    }

    public EmojiconEditText getEt_chat() {//////
//	public EditText getEt_chat() {//////
        return et_chat;
    }
    
    public Button getBtn_voice() {
    	return btn_voice;
    }

    public void addToolView(int icon){
        if(mEmoticonsToolBarView != null && icon > 0){
            mEmoticonsToolBarView.addData(icon);
        }
    }

    public void addFixedView(View view , boolean isRight){
        if(mEmoticonsToolBarView != null){
            mEmoticonsToolBarView.addFixedView(view,isRight);
        }
    }

    public void clearEditText(){
        if(et_chat != null){
            et_chat.setText("");
        }
    }

    public void del(){
        if(et_chat != null){
            int action = KeyEvent.ACTION_DOWN;
            int code = KeyEvent.KEYCODE_DEL;
            KeyEvent event = new KeyEvent(action, code);
            et_chat.onKeyDown(KeyEvent.KEYCODE_DEL, event);
        }
    }

    public void setVoiceVisibility(boolean b){
        if(b){
            btn_voice_or_text.setVisibility(VISIBLE);
        }
        else{
            btn_voice_or_text.setVisibility(GONE);
        }
    }

    public void setMultimediaVisibility(boolean b){
        mIsMultimediaVisibility = b;
        if(b){
            btn_multimedia.setVisibility(VISIBLE);
            btn_send.setVisibility(GONE);
        }
        else{
            btn_send.setVisibility(VISIBLE);
            btn_multimedia.setVisibility(GONE);
        }
    }

    @Override
    public void setBuilder(EmoticonsKeyboardBuilder builder) {
        mEmoticonsPageView.setBuilder(builder);
        mEmoticonsToolBarView.setBuilder(builder);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	Logger.w(VIEW_LOG_TAG, "event:" + event.getKeyCode());//////
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            	Logger.w(VIEW_LOG_TAG, "KEYCODE_BACK ly_foot_func:" + (ly_foot_func != null ? ly_foot_func.isShown() : "null"));//////
                if (ly_foot_func != null && ly_foot_func.isShown()) {
                    hideAutoView();
                    btn_face.setImageResource(R.drawable.v5_icon_face_normal);
                    return true;
                } else {
                    return super.dispatchKeyEvent(event);
                }
        }
        return super.dispatchKeyEvent(event);
    }
    
    public boolean isKeyBoardFootShow() {
    	return ly_foot_func != null && ly_foot_func.isShown();
    }

    @Override
    public void showAutoView() {
    	if (!isKeyBoardFootShow()) {
    		super.showAutoView();
		}
    }
    
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_face) {
        	// 确保输入框显示
        	rl_input.setVisibility(VISIBLE);
            btn_voice.setVisibility(GONE);
            btn_voice_or_text.setImageResource(R.drawable.v5_btn_voice_or_text_bg);
            
        	setEditableState(true);
            switch (mKeyboardState){
                case KEYBOARD_STATE_NONE:
                case KEYBOARD_STATE_BOTH:
                    show(FUNC_CHILLDVIEW_EMOTICON);
//                    setManualOpen(true);
//                    Utils.closeSoftKeyboard(mContext);
//                	  showAutoView();
                    btn_face.setImageResource(R.drawable.v5_icon_face_pop);
                    break;
                case KEYBOARD_STATE_FUNC:
                    if(mChildViewPosition == FUNC_CHILLDVIEW_EMOTICON){
                        btn_face.setImageResource(R.drawable.v5_icon_face_normal);
                        setManualOpen(true);
                        Utils.openSoftKeyboard(et_chat);
                    }
                    else {
                        show(FUNC_CHILLDVIEW_EMOTICON);
                        btn_face.setImageResource(R.drawable.v5_icon_face_pop);
                    }
                    break;
            }
        }
        else if (id == R.id.btn_send) {
            if(mKeyBoardBarViewListener != null){
                mKeyBoardBarViewListener.OnSendBtnClick(et_chat.getText().toString());
            }
        }
        else if (id == R.id.btn_multimedia) {
            switch (mKeyboardState){
                case KEYBOARD_STATE_NONE:
                case KEYBOARD_STATE_BOTH:
                    show(FUNC_CHILLDVIEW_APPS);
                    btn_face.setImageResource(R.drawable.v5_icon_face_normal);
                    rl_input.setVisibility(VISIBLE);
                    btn_voice.setVisibility(GONE);
                    btn_voice_or_text.setImageResource(R.drawable.v5_btn_voice_or_text_bg);
//                    setManualOpen(true);
//                	Utils.closeSoftKeyboard(mContext);
//                    showAutoView();
                    break;
                case KEYBOARD_STATE_FUNC:
                    btn_face.setImageResource(R.drawable.v5_icon_face_normal);
                    if(mChildViewPosition == FUNC_CHILLDVIEW_APPS){
                        hideAutoView();
                    }
                    else {
                        show(FUNC_CHILLDVIEW_APPS);
                    }
                    break;
            }
            if(mKeyBoardBarViewListener != null){
                mKeyBoardBarViewListener.OnMultimediaBtnClick();
            }
        }
        else if (id == R.id.btn_voice_or_text) {
            if(rl_input.isShown()) {
                hideAutoView();
                rl_input.setVisibility(GONE);
                btn_voice.setVisibility(VISIBLE);
				btn_voice_or_text.setImageResource(R.drawable.v5_btn_softkeyboard_bg);
            } else {
                rl_input.setVisibility(VISIBLE);
                btn_voice.setVisibility(GONE);
                setEditableState(true);
                btn_voice_or_text.setImageResource(R.drawable.v5_btn_voice_or_text_bg);
                Utils.openSoftKeyboard(et_chat);
            }
        }
        else if (id == R.id.btn_voice) {
            if(mKeyBoardBarViewListener != null){
                mKeyBoardBarViewListener.OnVideoBtnClick();
            }
        }
    }

    public void add(View view){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ly_foot_func.addView(view,params);
    }

    public void showInputView() {
    	rl_input.setVisibility(VISIBLE);
        btn_voice.setVisibility(GONE);
        btn_voice_or_text.setImageResource(R.drawable.v5_btn_voice_or_text_bg);
    }
    
    public void show(int position){
    	// 封装show关联操作进行联动
    	showAutoView();
    	setManualOpen(true);
    	Utils.closeSoftKeyboard(mContext);
    	
        int childCount = ly_foot_func.getChildCount();
//        boolean oldShow = isKeyBoardFootShow();
        if(position < childCount){
            for(int i = 0 ; i < childCount ; i++){
                if(i == position){
                    ly_foot_func.getChildAt(i).setVisibility(VISIBLE);
                    mChildViewPosition  = i;
                } else{
                    ly_foot_func.getChildAt(i).setVisibility(GONE);
                }
            }
        }
        
        post(new Runnable() {
            @Override
            public void run() {
                if(mKeyBoardBarViewListener != null){
                    mKeyBoardBarViewListener.OnKeyBoardStateChange(mKeyboardState, -1);
                }
            }
        });
    }

    @Override
    public void OnSoftPop(final int height) {
        super.OnSoftPop(height);
        post(new Runnable() {
            @Override
            public void run() {
                btn_face.setImageResource(R.drawable.v5_icon_face_normal);
                if(mKeyBoardBarViewListener != null){
                    mKeyBoardBarViewListener.OnKeyBoardStateChange(mKeyboardState,height);
                }
            }
        });
    }

    @Override
    public void OnSoftClose(int height) {
        super.OnSoftClose(height);
        if(mKeyBoardBarViewListener != null){
            mKeyBoardBarViewListener.OnKeyBoardStateChange(mKeyboardState,height);
        }
    }

    @Override
    public void OnSoftChanegHeight(int height) {
        super.OnSoftChanegHeight(height);
        if(mKeyBoardBarViewListener != null){
            mKeyBoardBarViewListener.OnKeyBoardStateChange(mKeyboardState,height);
        }
    }

    KeyBoardBarViewListener mKeyBoardBarViewListener;
    public void setOnKeyBoardBarViewListener(KeyBoardBarViewListener l) { this.mKeyBoardBarViewListener = l; }

    @Override
    public void onToolBarItemClick(int position) {

    }

    public boolean isManualOpen() {
		return manualOpen;
	}

	public void setManualOpen(boolean isManualOpened) {
		this.manualOpen = isManualOpened;
	}

	public interface KeyBoardBarViewListener {
        public void OnKeyBoardStateChange(int state, int height);

        public void OnSendBtnClick(String msg);

        public void OnVideoBtnClick();

        public void OnMultimediaBtnClick();
    }

	@Override
	public void setOrientation(int orientation) {
		// TODO Auto-generated method stub
		super.setOrientation(orientation);
		if (mEmoticonsPageView != null) {
			mEmoticonsPageView.setOrientation(orientation);
			mEmoticonsPageView.invalidate();
			if (mEmoticonsPageView.getAdapter() != null) {
				mEmoticonsPageView.getAdapter().notifyDataSetChanged();
			}
		}
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) { 
			// px
			mAutoViewHeight = UIUtil.px2dp((UIUtil.getScreenHeight(getContext())/2 - 50), getContext());
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			// dp转px
			mAutoViewHeight = Utils.getDefKeyboardHeight(getContext());
		}
	}
}
