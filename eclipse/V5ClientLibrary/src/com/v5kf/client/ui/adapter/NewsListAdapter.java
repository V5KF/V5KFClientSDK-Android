package com.v5kf.client.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.client.R;
import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.ui.utils.ImageLoader;

/**
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午3:05:16
 * @package com.v5kf.mcss.ui.adapter of MCSS-Native
 * @file OnChatRecyclerAdapter.java 
 *
 */
public class NewsListAdapter extends BaseAdapter {

	private static final int VIEW_TYPE_HEAD = 1;
	private static final int VIEW_TYPE_OTHER = 2;
	private LayoutInflater mInflater;
	private List<V5ArticleBean> mDatas;
	private Context mContext;
	
    public NewsListAdapter(Context context, List<V5ArticleBean> articles) {
        super();
        this.mDatas = articles;
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
    }
    

	@Override
	public int getCount() {
		return mDatas.size();
	}


	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}
	
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}


    @Override
    public int getItemViewType(int position) {
    	if (0 == position) {
			return VIEW_TYPE_HEAD;
		} else {
			return VIEW_TYPE_OTHER;
		}
    }	
	
    
    @Override
    public long getItemId(int position) {
    	return position;
    }
    
    
    public View getDivider(ViewGroup parent) {
    	View div = mInflater.inflate(R.layout.v5_item_divider, parent, false);
    	return div;
    }
    

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;					   
        if (convertView == null) {
        	if (getItemViewType(position) == VIEW_TYPE_HEAD) {
        		convertView = mInflater.inflate(R.layout.v5_item_chat_news_head, parent, false);
    			holder = new ViewHolder(convertView);
    		} else {
    			convertView = mInflater.inflate(R.layout.v5_item_chat_news_item, parent, false);
				holder = new ViewHolder(convertView);
    		}
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();    
        }
        
        V5ArticleBean article = mDatas.get(position);    
    	holder.mTitleTv.setText(article.getTitle());
    	ImageLoader imgLoader = new ImageLoader(mContext, true, R.drawable.v5_empty_img);
    	imgLoader.DisplayImage(article.getPic_url(), holder.mPicIv);
        		
		return convertView;
	}
	
    
    class ViewHolder {
    	public TextView mTitleTv;
    	public ImageView mPicIv;
    	
    	public ViewHolder(View itemView) {
			mTitleTv = (TextView) itemView.findViewById(R.id.id_news_title_text);
			mPicIv = (ImageView) itemView.findViewById(R.id.ic_news_img_iv);
		}

    }

}
