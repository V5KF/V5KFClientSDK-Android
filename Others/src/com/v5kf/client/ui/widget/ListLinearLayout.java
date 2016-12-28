package com.v5kf.client.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.v5kf.client.ui.adapter.NewsListAdapter;

public class ListLinearLayout extends LinearLayout {

	private OnListLayoutClickListener mListener;
	
	public interface OnListLayoutClickListener {
		public void onListLayoutClick(View v, int pos);
	}
	
	public void setOnListLayoutClickListener(OnListLayoutClickListener l) {
		this.mListener = l;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	public ListLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (Build.VERSION.SDK_INT >= 11) {
			setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		}
	}

	/**
     * 绑定布局
     */
    public void bindLinearLayout(NewsListAdapter adapter) {
        int count = adapter.getCount();
        this.removeAllViews();
        for (int i = 0; i < count; i++) {
        	if (i > 0) {
        		addView(adapter.getDivider(this));
        	}
            View v = adapter.getView(i, null, this);
            v.setTag(i);
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mListener != null) {
						mListener.onListLayoutClick(v, (Integer)v.getTag());
					}
				}
			});
            addView(v);
        }
    }
}
