package com.v5kf.client.ui.adapter;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.v5kf.client.R;
import com.v5kf.client.lib.entity.V5TextMessage;

/**
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午3:05:16
 * @package com.v5kf.mcss.ui.adapter of MCSS-Native
 * @file OnChatRecyclerAdapter.java 
 *
 */
public class QuesListAdapter extends BaseAdapter {

	protected static final String TAG = "RobotListAdapter";
	private LayoutInflater mInflater;
	private List<V5TextMessage> mDatas;
	private Activity mActivity;
	private OnQuesClickListener mListener;
	private HashMap<Integer, Boolean> mStates = new HashMap<Integer, Boolean>();
	
//	public interface QuestionHandler {
//		public void onQuestionSend(int position);
//		public void onQuestionAdd(int position);
//		public void onQuestionDel(int position);
//	}
	
	public interface OnQuesClickListener {
		public void onQuesItemClick(View v, int position, boolean isClicked);
	}
	
    public QuesListAdapter(Activity activity, List<V5TextMessage> msgs, OnQuesClickListener listener) {
        super();
        this.mDatas = msgs;
        this.mActivity = activity;
        this.mInflater = LayoutInflater.from(activity);
        this.mListener = listener;
        for (int i = 0; i < mDatas.size(); i++) {
			mStates.put(i, false);
		}
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
    public long getItemId(int position) {
    	return position;
    }
    
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
    	QuesViewHolder holder = null;
    	V5TextMessage message = mDatas.get(position);
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.v5_item_ques_text, parent, false) ;
        	holder = new QuesViewHolder(convertView);
        	        	
        	convertView.setTag(holder);
        } else {
            holder = (QuesViewHolder) convertView.getTag();    
        }
        
        holder.mMsg.setText(message.getContent());
        holder.mMsg.setOnClickListener(new QuseItemClick(position));
        holder.mMsg.setBackgroundResource(R.color.v5_ques_bg_normal);
        if (mStates.get(position) != null && mStates.get(position)) {
        	holder.mMsg.setBackgroundResource(R.color.v5_ques_bg_select);
        }
        
		return convertView;
	}

    
    class QuesViewHolder {

        /* 文本 */
        public TextView mMsg;

        public QuesViewHolder(View itemView) {
            mMsg = (TextView) itemView.findViewById(R.id.id_ques_text);
        }
    }
    
    class QuseItemClick implements OnClickListener {
		
		private int position;
		
		public QuseItemClick(int pos) {
			this.position = pos;
		}

		@Override
		public void onClick(View v) {
			boolean isAdd = mStates.get(position) == null ? false : mStates.get(position);
			for (int i = 0; i < mDatas.size(); i++) {
				mStates.put(i, false);
			}
			if (!isAdd) {
				mStates.put(position, true);
			}
			notifyDataSetChanged();
			if (mListener != null) {
				mListener.onQuesItemClick(v, position, !isAdd);
			}
		}
	}
    
    public void clearSelect() {
    	for (int i = 0; i < mDatas.size(); i++) {
			mStates.put(i, false);
		}
    	notifyDataSetChanged();
    }
}
