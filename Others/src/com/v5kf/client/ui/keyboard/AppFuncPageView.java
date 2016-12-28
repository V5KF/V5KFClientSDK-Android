package com.v5kf.client.ui.keyboard;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.v5kf.client.ui.keyboard.AppsAdapter.FuncItemClickListener;

public class AppFuncPageView extends ViewPager {

    private Context mContext;
    private int mMaxAppFuncPageCount = 0;
    public int mOldPagePosition = -1;

    private List<AppBean> mAppBeanList;
    private AppViewPagerAdapter mAppViewPagerAdapter;
    private ArrayList<View> mAppPageViews = new ArrayList<View>();
    
    private FuncItemClickListener mFuncItemClickListener;
	private EmoticonsIndicatorView mIndicatorView;
	private int mOrientation = 0;

    public AppFuncPageView(Context context) {
        this(context, null);
    }

    public AppFuncPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        AppFuncPageView.this.post(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }

    private void updateView() {
        if (mAppBeanList == null) {
            return;
        }

        if (mAppViewPagerAdapter == null) {
            mAppViewPagerAdapter = new AppViewPagerAdapter();
            setAdapter(mAppViewPagerAdapter);
            addOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

                @Override
                public void onPageSelected(int position) {
                    if (mOldPagePosition < 0) {
                        mOldPagePosition = 0;
                    }
                    if (mIndicatorView != null) {
                    	mIndicatorView.playBy(mOldPagePosition, position);
                    }
                    mOldPagePosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
        }

        mAppPageViews.clear();
        mAppViewPagerAdapter.notifyDataSetChanged();

            if (mAppBeanList != null) {
                int emoticonSetSum = mAppBeanList.size();
                int row = KeyboardConfig.APPFUNC_ROW_PORTRAIT;
                int line = KeyboardConfig.APPFUNC_LINE_PORTRAIT;
                // TODO
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) { // 横屏
                	row = KeyboardConfig.APPFUNC_ROW_LANDSCAPE;
                	line = KeyboardConfig.APPFUNC_LINE_LANDSCAPE;
                }

                int everyPageMaxSum = row * line;
                int pageCount = getItemCount();

                mMaxAppFuncPageCount = Math.max(mMaxAppFuncPageCount, pageCount);

                int start = 0;
                int end = everyPageMaxSum > emoticonSetSum ? emoticonSetSum : everyPageMaxSum;

                RelativeLayout.LayoutParams gridParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                gridParams.addRule(ResizeLayout.CENTER_VERTICAL);

                for (int i = 0; i < pageCount; i++) {
                    RelativeLayout rl = new RelativeLayout(mContext);
                    GridView gridView = new GridView(mContext);
//					gridView.setMotionEventSplittingEnabled(false);
                    gridView.setNumColumns(row);
                    gridView.setBackgroundColor(Color.TRANSPARENT);
                    gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                    gridView.setCacheColorHint(0);
                    gridView.setHorizontalSpacing(Utils.dip2px(mContext, 6));
                    gridView.setVerticalSpacing(Utils.dip2px(mContext, 20));
                    gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
                    gridView.setGravity(Gravity.CENTER);
                    gridView.setVerticalScrollBarEnabled(false);

                    List<AppBean> list = new ArrayList<AppBean>();
                    for (int j = start; j < end; j++) {
                        list.add(mAppBeanList.get(j));
                    }

                    int count = line * row;
                    while (list.size() < count) {
                        list.add(null);
                    }

                    AppsAdapter adapter = new AppsAdapter(mContext, list);
                    gridView.setAdapter(adapter);
                    rl.addView(gridView, gridParams);
                    mAppPageViews.add(rl);
                    adapter.setFuncItemClickListener(mFuncItemClickListener);

                    start = everyPageMaxSum + i * everyPageMaxSum;
                    end = everyPageMaxSum + (i + 1) * everyPageMaxSum;
                    if (end >= emoticonSetSum) {
                        end = emoticonSetSum;
                    }
                }
            }
            
        mAppViewPagerAdapter.notifyDataSetChanged();

        if (mIndicatorView != null) {
        	mIndicatorView.init(getItemCount());
        	if (getItemCount() <= 1) {
        		mIndicatorView.setVisibility(View.INVISIBLE);
        	} else {
        		mIndicatorView.setVisibility(View.VISIBLE);
        	}
        }
    }

    private int getItemCount() {
    	if (mAppBeanList == null) {
    		return 0;
    	}
    	// TODO
    	int total = KeyboardConfig.APPFUNC_LINE_PORTRAIT * KeyboardConfig.APPFUNC_ROW_PORTRAIT;
    	if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
    		total = KeyboardConfig.APPFUNC_LINE_LANDSCAPE * KeyboardConfig.APPFUNC_ROW_LANDSCAPE;
    	}
		return mAppBeanList.size() / total + (mAppBeanList.size() % total > 0 ? 1 : 0);
	}

	public void setPageSelect(int position) {
        if (getAdapter() != null && position >= 0 && position < mAppBeanList.size()) {
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += mAppBeanList.size() / 8 + (mAppBeanList.size() % 8 > 0 ? 1 : 0);
            }
            setCurrentItem(count);
        }
    }

    public void setAppBeanList(List<AppBean> list) {
        mAppBeanList = list;
    }
    
    public void setIndicatorView(EmoticonsIndicatorView indicatorView) {
    	this.mIndicatorView = indicatorView;
    }

    private class AppViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mAppPageViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mAppPageViews.get(arg1));
            return mAppPageViews.get(arg1);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    public FuncItemClickListener getFuncItemClickListener() {
		return mFuncItemClickListener;
	}

	public void setFuncItemClickListener(FuncItemClickListener mFuncItemClickListener) {
		this.mFuncItemClickListener = mFuncItemClickListener;
	}

	public int getOrientation() {
		return mOrientation;
	}

	public void setOrientation(int mOrientation) {
		this.mOrientation = mOrientation;
	}

}
