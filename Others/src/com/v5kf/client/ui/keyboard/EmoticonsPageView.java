package com.v5kf.client.ui.keyboard;

import java.util.ArrayList;
import java.util.List;

import com.v5kf.client.lib.Logger;
import com.v5kf.client.ui.utils.UIUtil;

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

public class EmoticonsPageView extends ViewPager implements IEmoticonsKeyboard, IView {

    private Context mContext;
    private int mHeight = 0;
    private int mMaxEmoticonSetPageCount = 0;
    public int mOldPagePosition = -1;

    private List<EmoticonSetBean> mEmoticonSetBeanList;
    private EmoticonsViewPagerAdapter mEmoticonsViewPagerAdapter;
    private ArrayList<View> mEmoticonPageViews = new ArrayList<View>();
    
	private int mOrientation = 0;
	
	protected void setOrientation(int orientation) {
		this.mOrientation = orientation;
	}

    public EmoticonsPageView(Context context) {
        this(context, null);
    }

    public EmoticonsPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        EmoticonsPageView.this.post(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }

    private void updateView() {
        if (mEmoticonSetBeanList == null) {
            return;
        }

        if (mEmoticonsViewPagerAdapter == null) {
            mEmoticonsViewPagerAdapter = new EmoticonsViewPagerAdapter();
            setAdapter(mEmoticonsViewPagerAdapter);
            addOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

                @Override
                public void onPageSelected(int position) {
                    if (mOldPagePosition < 0) {
                        mOldPagePosition = 0;
                    }
                    int end = 0;
                    int pagerPosition = 0;
                    for (EmoticonSetBean emoticonSetBean : mEmoticonSetBeanList) {

                        int size = getPageCount(emoticonSetBean);

                        if (end + size > position) {
                            if(mOnEmoticonsPageViewListener != null){
                                mOnEmoticonsPageViewListener.emoticonsPageViewCountChanged(size);
                            }
                            // 
                            if (mOldPagePosition - end >= size) {
                                if (position - end >= 0) {
                                    if(mOnEmoticonsPageViewListener != null){
                                        mOnEmoticonsPageViewListener.playTo(position - end);
                                    }
                                }
                                if (mIViewListeners != null && !mIViewListeners.isEmpty()) {
                                    for (IView listener : mIViewListeners) {
                                        listener.onPageChangeTo(pagerPosition);
                                    }
                                }
                                break;
                            }
                            // 
                            if (mOldPagePosition - end < 0) {
                                if(mOnEmoticonsPageViewListener != null){
                                    mOnEmoticonsPageViewListener.playTo(0);
                                }
                                if (mIViewListeners != null && !mIViewListeners.isEmpty()) {
                                    for (IView listener : mIViewListeners) {
                                        listener.onPageChangeTo(pagerPosition);
                                    }
                                }
                                break;
                            }
                            // 
                            if(mOnEmoticonsPageViewListener != null){
                                mOnEmoticonsPageViewListener.playBy(mOldPagePosition - end, position - end);
                            }
                            break;
                        }
                        pagerPosition++;
                        end += size;
                    }
                    mOldPagePosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
        }

        int screenWidth = UIUtil.getScreenWidth(mContext); //Utils.getDisplayWidthPixels(mContext);
        int maxPagerHeight = mHeight;

        mEmoticonPageViews.clear();
        mEmoticonsViewPagerAdapter.notifyDataSetChanged();

        for (EmoticonSetBean bean : mEmoticonSetBeanList) {
            ArrayList<EmoticonBean> emoticonList = bean.getEmoticonList();
            if (emoticonList != null) {
                int emoticonSetSum = emoticonList.size();
                int row = bean.getRow();
                int line = bean.getLine();
                // TODO dp
                int itemPadding = bean.getItemPadding();
                int horizontalSpacing = bean.getHorizontalSpacing();
                int verticalSpacing = bean.getVerticalSpacing();
//                Logger.w("EmoticonsPageView", "BF orientation:" + mOrientation + " itemPadding:" + itemPadding + 
//                		" horizontalSpacing:" + horizontalSpacing + " verticalSpacing:" + verticalSpacing);
                if (horizontalSpacing == 0) {
                	horizontalSpacing = KeyboardConfig.EMOICON_ITEM_HSPACING_PORTRAIT;
                }
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) { // 横屏
                	itemPadding = KeyboardConfig.EMOICON_ITEM_PADDING_LANDSCAPE;
                	verticalSpacing = KeyboardConfig.EMOICON_ITEM_VSPACING_LANDSCAPE;
                	horizontalSpacing = KeyboardConfig.EMOICON_ITEM_HSPACING_LANDSCAPE;
                }
//                Logger.w("EmoticonsPageView", "AF orientation:" + mOrientation + " itemPadding:" + itemPadding + 
//                		" horizontalSpacing:" + horizontalSpacing + " verticalSpacing:" + verticalSpacing);
//                // TODO
//                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) { // 横屏
//                	row = 10;
//                	line = 2;
//                }

                int del = bean.isShowDelBtn() ? 1 : 0;
                int everyPageMaxSum = row * line - del;
                int pageCount = getPageCount(bean);

                mMaxEmoticonSetPageCount = Math.max(mMaxEmoticonSetPageCount, pageCount);

                int start = 0;
                int end = everyPageMaxSum > emoticonSetSum ? emoticonSetSum : everyPageMaxSum;

                RelativeLayout.LayoutParams gridParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                gridParams.addRule(ResizeLayout.CENTER_VERTICAL);

                int itemHeight = (screenWidth - (row + 1) * Utils.dip2px(mContext, horizontalSpacing)) / row; // 利用屏幕宽度来计算，高度maxPagerHeight不稳定，会变化
//                int itemHeight = Math.min((screenWidth - (row + 1) * Utils.dip2px(mContext, horizontalSpacing)) / row, (maxPagerHeight - (line + 1) * Utils.dip2px(mContext, verticalSpacing)) / line);
//                Logger.i("EmoticonsPageView", "orientation screenWidth:" + screenWidth + " maxPagerHeight:" + maxPagerHeight + " itemHeight:" + itemHeight);
//                Logger.i("EmoticonsPageView", "orientation itemWidth:" +(screenWidth - (row + 1) * Utils.dip2px(mContext, horizontalSpacing)) / row);
//                Logger.i("EmoticonsPageView", "orientation itemHeight:" + (maxPagerHeight - (line + 1) * Utils.dip2px(mContext, verticalSpacing)) / line);
                
//                // 计算行距
//                if (bean.getHeight() > 0) {
//                    int verticalspacing = Utils.dip2px(mContext, verticalSpacing);
//                    itemHeight = Math.min(itemHeight,Utils.dip2px(mContext, bean.getHeight()));
//                    while (verticalspacing > 0) {
//                        int userdefHeigth = (line - 1) * verticalspacing + Utils.dip2px(mContext, bean.getHeight()) * (line);
//                        if (userdefHeigth <= maxPagerHeight) {
//                            bean.setVerticalSpacing(Utils.px2dip(mContext, verticalspacing));
//                            break;
//                        }
//                        bean.setVerticalSpacing(Utils.px2dip(mContext, verticalspacing));
//                        verticalspacing = (int)Math.ceil((float)verticalspacing / 2);
//                    }
//                }

                for (int i = 0; i < pageCount; i++) {
                    RelativeLayout rl = new RelativeLayout(mContext);
                    GridView gridView = new GridView(mContext);
//					gridView.setMotionEventSplittingEnabled(false);
                    gridView.setNumColumns(row);
                    gridView.setBackgroundColor(Color.TRANSPARENT);
                    gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                    gridView.setCacheColorHint(0);
                    gridView.setHorizontalSpacing(Utils.dip2px(mContext, horizontalSpacing));
                    gridView.setVerticalSpacing(Utils.dip2px(mContext, verticalSpacing));
                    gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
                    gridView.setGravity(Gravity.CENTER);
                    gridView.setVerticalScrollBarEnabled(false);

                    List<EmoticonBean> list = new ArrayList<EmoticonBean>();
                    for (int j = start; j < end; j++) {
                        list.add(emoticonList.get(j));
                    }

                    // 删除按钮
                    if (bean.isShowDelBtn()) {
                        int count = line * row;
                        while (list.size() < count - 1) {
                            list.add(null);
                        }
                        list.add(new EmoticonBean(EmoticonBean.FACE_TYPE_DEL, "drawable://v5_icon_del", null));
                    } else {
                        int count = line * row;
                        while (list.size() < count) {
                            list.add(null);
                        }
                    }

                    EmoticonsAdapter adapter = new EmoticonsAdapter(mContext, list);
                    adapter.setHeight(itemHeight, Utils.dip2px(mContext, itemPadding));
                    gridView.setAdapter(adapter);
                    rl.addView(gridView, gridParams);
                    mEmoticonPageViews.add(rl);
                    adapter.setOnItemListener(this);

                    start = everyPageMaxSum + i * everyPageMaxSum;
                    end = everyPageMaxSum + (i + 1) * everyPageMaxSum;
                    if (end >= emoticonSetSum) {
                        end = emoticonSetSum;
                    }
                }
            }
        }
        mEmoticonsViewPagerAdapter.notifyDataSetChanged();

        if(mOnEmoticonsPageViewListener != null){
            mOnEmoticonsPageViewListener.emoticonsPageViewInitFinish(mMaxEmoticonSetPageCount);
        }
    }

    public void setPageSelect(int position) {
        if (getAdapter() != null && position >= 0 && position < mEmoticonSetBeanList.size()) {
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += getPageCount(mEmoticonSetBeanList.get(i));
            }
            setCurrentItem(count);
        }
    }

    public int getPageCount(EmoticonSetBean emoticonSetBean) {
        int pageCount = 0;
        if (emoticonSetBean != null && emoticonSetBean.getEmoticonList() != null) {
            int del = emoticonSetBean.isShowDelBtn() ? 1 : 0;
            int row = emoticonSetBean.getRow();
            int line = emoticonSetBean.getLine();
//          // TODO
//            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) { // 横屏
//            	row = 2;
//            	line = 10;
//            }
            
            int everyPageMaxSum = row * line - del;
            pageCount = (int) Math.ceil((double) emoticonSetBean.getEmoticonList().size() / everyPageMaxSum);
        }
        return pageCount;
    }

    @Override
    public void setBuilder(EmoticonsKeyboardBuilder builder) {
        mEmoticonSetBeanList = builder.builder.getEmoticonSetBeanList();
    }

    private class EmoticonsViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mEmoticonPageViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mEmoticonPageViews.get(arg1));
            return mEmoticonPageViews.get(arg1);
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


    @Override
    public void onItemClick(EmoticonBean bean) {
        if (mIViewListeners != null && !mIViewListeners.isEmpty()) {
            for (IView listener : mIViewListeners) {
                listener.onItemClick(bean);
            }
        }
    }

    @Override
    public void onItemDisplay(EmoticonBean bean) {

    }

    @Override
    public void onPageChangeTo(int position) {

    }

    private List<IView> mIViewListeners;

    public void addIViewListener(IView listener) {
        if (mIViewListeners == null) {
            mIViewListeners = new ArrayList<IView>();
        }
        mIViewListeners.add(listener);
    }

    public void setIViewListener(IView listener) {
        addIViewListener(listener);
    }

    private OnEmoticonsPageViewListener mOnEmoticonsPageViewListener;
    public void setOnIndicatorListener(OnEmoticonsPageViewListener listener) {
        mOnEmoticonsPageViewListener = listener;
    }
    public interface OnEmoticonsPageViewListener{
        void emoticonsPageViewInitFinish(int count);
        void emoticonsPageViewCountChanged(int count);
        void playTo(int position);
        void playBy(int oldPosition , int newPosition);
    }
}
