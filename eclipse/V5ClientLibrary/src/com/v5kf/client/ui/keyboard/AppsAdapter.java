package com.v5kf.client.ui.keyboard;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.client.R;


public class AppsAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private List<AppBean> mDdata = new ArrayList<AppBean>();
    private FuncItemClickListener mFuncItemListener;

    public AppsAdapter(Context context, List<AppBean> data) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        if (data != null) {
            this.mDdata = data;
        }
    }

    @Override
    public int getCount() {
        return mDdata.size();
    }

    @Override
    public Object getItem(int position) {
        return mDdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.v5_item_app, parent, false);
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppBean appBean = mDdata.get(position);
        if (appBean != null) {
            int resID = mContext.getResources().getIdentifier(appBean.getIcon(), "drawable", mContext.getPackageName());
            viewHolder.iv_icon.setBackgroundResource(resID);
            viewHolder.tv_name.setText(appBean.getFuncName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mFuncItemListener) {
                    	mFuncItemListener.onFuncItemClick(v, appBean);
                    }
                }
            });
        }
        return convertView;
    }

    public FuncItemClickListener getFuncItemClickListener() {
		return mFuncItemListener;
	}

	public void setFuncItemClickListener(FuncItemClickListener mFuncItemListener) {
		this.mFuncItemListener = mFuncItemListener;
	}

	class ViewHolder {
        public ImageView iv_icon;
        public TextView tv_name;
    }
    
    public interface FuncItemClickListener {
    	public void onFuncItemClick(View v, AppBean bean);
    }
}
