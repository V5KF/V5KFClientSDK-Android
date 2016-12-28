package com.v5kf.client.ui.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.v5kf.client.R;
import com.v5kf.client.ui.utils.UIUtil;

/**
 * @author yangyu
 *	功能描述：标题按钮上的弹窗（继承自PopupWindow）
 */
public class TitlePopup extends PopupWindow {
	private Context mContext;

	//列表弹窗的间隔
	protected int LIST_PADDING;
	protected int REC_PADDING = 40;
	
	//实例化一个矩形
	private Rect mRect = new Rect();
	
	//坐标的位置（x、y）
	private final int[] mLocation = new int[2];
	
	//屏幕的宽度和高度
//	private int mScreenWidth;

	//判断是否需要添加或更新列表子类项
	private boolean mIsDirty;
	
	//位置不在中心
	private int popupGravity = Gravity.NO_GRAVITY;	
	
	//弹窗子类项选中时的监听
	private OnItemOnClickListener mItemOnClickListener;
	
	//定义列表对象
	private ListView mListView;
	
	//定义弹窗子类项列表
	private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();			
	
	public TitlePopup(Context context){
		//设置布局的参数
		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}
	
	public TitlePopup(Context context, int width, int height){
		super(context);
		this.mContext = context;
		LIST_PADDING = UIUtil.dp2px(10, context);
		
		//设置可以获得焦点
		setFocusable(true);
		//设置弹窗内可点击
		setTouchable(true);	
		//设置弹窗外可点击
		setOutsideTouchable(true);
		
		//获得屏幕的宽度和高度
//		mScreenWidth = UIUtil.getScreenWidth(mContext);
		
		//设置弹窗的宽度和高度
		setWidth(width);
		setHeight(height);
		
		setBackgroundDrawable(new BitmapDrawable());
//		setBackgroundDrawable(null);
		
		//设置弹窗的布局界面
		setContentView(LayoutInflater.from(mContext).inflate(R.layout.v5_list_popup, null));
		
		initUI();
	}
		
	/**
	 * 初始化弹窗列表
	 */
	private void initUI(){
		mListView = (ListView) getContentView().findViewById(R.id.title_list);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
				//点击子类项后，弹窗消失
				dismiss();
				
				if(mItemOnClickListener != null)
					mItemOnClickListener.onItemClick(mActionItems.get(index), index);
			}
		}); 
	}
	
	/**
	 * 显示弹窗列表界面
	 */
	public void show(View view){
		//获得点击屏幕的位置坐标
		view.getLocationOnScreen(mLocation);
		
		//设置矩形的大小
		mRect.set(mLocation[0], mLocation[1], mLocation[0] + view.getWidth(), mLocation[1] + view.getHeight());
		
		//判断是否需要添加或更新列表子类项
		if(mIsDirty){
			populateActions();
		}
		
		//显示弹窗的位置
//		showAtLocation(view, popupGravity, mScreenWidth - LIST_PADDING - (getWidth()/2), mRect.bottom);
//		Logger.d("TitlePopup", "PopWindow:" + getWidth() + " viewWidth:" + view.getWidth() + " mLocation[0]:" + mLocation[0] + " mLocation[1]:" + mLocation[1]);
		showAtLocation(view, popupGravity, mLocation[0] + view.getWidth() / 2 - UIUtil.dp2px(136, mContext) + REC_PADDING, mRect.bottom);
//		showAsDropDown(view, 0, 0);
	}
	
	/**
	 * 设置弹窗列表子项
	 */
	private void populateActions(){
		mIsDirty = false;
		
		//设置列表的适配器
		mListView.setAdapter(new BaseAdapter() {			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = null;
				
				if(convertView == null){
					textView = new TextView(mContext);
					textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
					textView.setTextSize(15);
					//设置文本居中
					textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					//设置文本域的范围
					int pd_h = UIUtil.dp2px(4, mContext);
					int pd_v = UIUtil.dp2px(6, mContext);
					textView.setPadding(pd_h, pd_v, pd_h, pd_v);
					//设置文本在一行内显示（不换行）
					textView.setSingleLine(true);
				}else{
					textView = (TextView) convertView;
				}
				
				ActionItem item = mActionItems.get(position);
				
				//设置文本文字
				textView.setText(item.mTitle);
				//设置文字与图标的间隔
				textView.setCompoundDrawablePadding(UIUtil.dp2px(6, mContext));
				//设置在文字的左边放一个图标
				textView.setCompoundDrawablesWithIntrinsicBounds(item.mDrawableId, 0, 0, 0);
                
                return textView;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return mActionItems.get(position);
			}
			
			@Override
			public int getCount() {
				return mActionItems.size();
			}
		}) ;
	}
	
	/**
	 * 添加子类项
	 */
	public void addAction(ActionItem action){
		if(action != null){
			mActionItems.add(action);
			mIsDirty = true;
		}
	}
	
	/**
	 * 清除子类项
	 */
	public void cleanAction(){
		if(mActionItems.isEmpty()){
			mActionItems.clear();
			mIsDirty = true;
		}
	}
	
	/**
	 * 根据位置得到子类项
	 */
	public ActionItem getAction(int position){
		if(position < 0 || position > mActionItems.size())
			return null;
		return mActionItems.get(position);
	}			
	
	/**
	 * 设置监听事件
	 */
	public void setItemOnClickListener(OnItemOnClickListener onItemOnClickListener){
		this.mItemOnClickListener = onItemOnClickListener;
	}
	
	/**
	 * @author yangyu
	 *	功能描述：弹窗子类项按钮监听事件
	 */
	public static interface OnItemOnClickListener{
		public void onItemClick(ActionItem item , int position);
	}
}
