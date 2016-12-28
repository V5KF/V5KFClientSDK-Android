package com.v5kf.client.ui.keyboard;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.ui.utils.UIUtil;

public class AutoHeightLayout extends ResizeLayout implements ResizeLayout.OnResizeListener {

    private static final int ID_CHILD = 1;
    /**
     * 不显示
     */
    public static final int KEYBOARD_STATE_NONE = 100;
    /**
     * 显示表情、多媒体功能
     */
    public static final int KEYBOARD_STATE_FUNC = 102;
    /**
     * 显示键盘(覆盖表情、多媒体界面)
     */
    public static final int KEYBOARD_STATE_BOTH = 103;

    protected Context mContext;
    protected int mAutoHeightLayoutId;
    protected int mAutoViewHeight; // 单位dp
    protected View mAutoHeightLayoutView;
    protected int mKeyboardState = KEYBOARD_STATE_NONE;
	private int mOrientation = 0; // 当前状态横竖屏

	public AutoHeightLayout(Context context) {
		super(context);
		this.mContext = context;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mAutoViewHeight = Utils.getDefKeyboardHeight(mContext);
		} else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mAutoViewHeight = Utils.px2dip(context, UIUtil.getScreenHeight(context)/2 - 50);
		}
//        Logger.d("AutoHeoghtLayout", "orientation mAutoViewHeight(dp):" + mAutoViewHeight);
        setOnResizeListener(this);
	}
	
    public AutoHeightLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mAutoViewHeight = Utils.getDefKeyboardHeight(mContext);
		} else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mAutoViewHeight = Utils.px2dip(context, UIUtil.getScreenHeight(context)/2 - 50);
		}
//        Logger.d("AutoHeoghtLayout", "orientation mAutoViewHeight(dp):" + mAutoViewHeight);
        setOnResizeListener(this);
    }
    
    public AutoHeightLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mAutoViewHeight = Utils.getDefKeyboardHeight(mContext);
		} else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mAutoViewHeight = Utils.px2dip(context, UIUtil.getScreenHeight(context)/2 - 50);
		}
//        Logger.d("AutoHeoghtLayout", "orientation mAutoViewHeight(dp):" + mAutoViewHeight);
        setOnResizeListener(this);
	}

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int childSum = getChildCount();
        if (getChildCount() > 1) {
            throw new IllegalStateException("can host only one direct child");
        }
        super.addView(child, index, params);

        if (childSum == 0) {
            mAutoHeightLayoutId = child.getId();
            if (mAutoHeightLayoutId < 0) {
                child.setId(ID_CHILD);
                mAutoHeightLayoutId = ID_CHILD;
            }
            RelativeLayout.LayoutParams paramsChild = (LayoutParams) child.getLayoutParams();
            paramsChild.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            child.setLayoutParams(paramsChild);
        } else if (childSum == 1) {
            RelativeLayout.LayoutParams paramsChild = (LayoutParams) child.getLayoutParams();
            paramsChild.addRule(RelativeLayout.ABOVE, mAutoHeightLayoutId);
            child.setLayoutParams(paramsChild);
        }
    }

    public int getKeyboardState() {
    	return mKeyboardState;
    }
    
    public void setAutoHeightLayoutView(View view) {
        mAutoHeightLayoutView = view;
    }

    /**
     * px
     * @param height px
     */
    public void setAutoViewHeight(final int height) {
    	Logger.w("AutoHeightLayout", "orientation setAutoViewHeight(dp):" + Utils.px2dip(getContext(), height));
        int heightDp = Utils.px2dip(mContext, height);
        if (heightDp > 0 && heightDp != mAutoViewHeight) {
            mAutoViewHeight = heightDp;
            // TODO
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) { // 竖屏
            	Utils.setDefKeyboardHeight(mContext, mAutoViewHeight); //dp
            }
        }

        if (mAutoHeightLayoutView != null) {
            mAutoHeightLayoutView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAutoHeightLayoutView.getLayoutParams();
            params.height = height;
            mAutoHeightLayoutView.setLayoutParams(params);
        }
    }

    public void hideAutoView(){
        this.post(new Runnable() {
            @Override
            public void run() {
                Utils.closeSoftKeyboard(mContext);
                setAutoViewHeight(0);
                if (mAutoHeightLayoutView != null) {
                    mAutoHeightLayoutView.setVisibility(View.GONE);
                }
            }
        });
        mKeyboardState = KEYBOARD_STATE_NONE ;
    }

    public void showAutoView(){
        if (mAutoHeightLayoutView != null) {
            mAutoHeightLayoutView.setVisibility(VISIBLE);
            setAutoViewHeight(Utils.dip2px(mContext, mAutoViewHeight));
        }
        mKeyboardState = mKeyboardState == KEYBOARD_STATE_NONE ? KEYBOARD_STATE_FUNC : KEYBOARD_STATE_BOTH ;
    }

    @Override
    public void OnSoftPop(final int height) {
        mKeyboardState = KEYBOARD_STATE_BOTH;
        post(new Runnable() {
            @Override
            public void run() {
                setAutoViewHeight(height);
            }
        });
    }

    @Override
    public void OnSoftClose(int height) {
        mKeyboardState = mKeyboardState == KEYBOARD_STATE_BOTH ? KEYBOARD_STATE_FUNC : KEYBOARD_STATE_NONE ;
    }

    @Override
    public void OnSoftChanegHeight(final int height) {
        post(new Runnable() {
            @Override
            public void run() {
                setAutoViewHeight(height);
            }
        });
    }
    
    // TODO
    public void setOrientation(int orientation) {
		this.mOrientation = orientation;
	}

	public int getOrientation() {
		return mOrientation;
	}
}
