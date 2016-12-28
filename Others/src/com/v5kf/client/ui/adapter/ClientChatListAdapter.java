package com.v5kf.client.ui.adapter;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.v5kf.client.R;
import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientAgent.ClientLinkType;
import com.v5kf.client.lib.V5ConfigSP;
import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.client.ui.ClientChatActivity;
import com.v5kf.client.ui.V5ChatBean;
import com.v5kf.client.ui.WebViewActivity;
import com.v5kf.client.ui.emojicon.EmojiconTextView;
import com.v5kf.client.ui.utils.DateUtil;
import com.v5kf.client.ui.utils.FileUtil;
import com.v5kf.client.ui.utils.ImageLoader;
import com.v5kf.client.ui.utils.MediaCache;
import com.v5kf.client.ui.utils.MediaLoader;
import com.v5kf.client.ui.utils.UIUtil;
import com.v5kf.client.ui.utils.V5Size;
import com.v5kf.client.ui.widget.ListLinearLayout;
import com.v5kf.client.ui.widget.ListLinearLayout.OnListLayoutClickListener;
import com.v5kf.client.ui.widget.RoundImageView;

public class ClientChatListAdapter extends BaseAdapter {

	protected static final String TAG = "ClientChatListAdapter";
	public static final int TYPE_LEFT_TEXT = 0;
	public static final int TYPE_RIGHT_TEXT = 1;
	public static final int TYPE_SINGLE_NEWS = 2;
	public static final int TYPE_NEWS = 3;
	public static final int TYPE_LOCATION_R = 4;
	public static final int TYPE_LOCATION_L = 5;
	public static final int TYPE_IMG_L = 6;
	public static final int TYPE_IMG_R = 7;
	public static final int TYPE_VOICE_L = 8;
	public static final int TYPE_VOICE_R = 9;
	public static final int TYPE_TIPS = 10;
	
	private LayoutInflater mInflater;
	private List<V5ChatBean> mDatas;
	private ClientChatActivity mChatActivity;
	private OnChatListClickListener mListener;
	// 语音
	private MediaPlayer mPlayer;
	private boolean showAvatar;

	public ClientChatListAdapter(ClientChatActivity context, List<V5ChatBean> messages, OnChatListClickListener listener, boolean avatar) {
		super();
		this.mChatActivity = context;
		this.mDatas = messages;
		this.mInflater = LayoutInflater.from(context);
		this.mListener = listener;
		this.showAvatar = avatar;
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
	public int getViewTypeCount() {
		return 11;
	}
	
	@Override
    public int getItemViewType(int position) {
		int msgType = mDatas.get(position).getMessage().getMessage_type();
    	int msgDir = mDatas.get(position).getMessage().getDirection();
    	
    	if (msgType == V5MessageDefine.MSG_TYPE_ARTICLES) {
    		V5ArticlesMessage msgContent = (V5ArticlesMessage) mDatas.get(position).getMessage();
    		if (msgContent != null && msgContent.getArticles() != null) {
	    		if (msgContent.getArticles().size() == 1) {
	    			return TYPE_SINGLE_NEWS;
	    		} else if (msgContent.getArticles().size() > 1) {
	    			return TYPE_NEWS;
	    		}
    		}
    	} else if (msgType == V5MessageDefine.MSG_TYPE_LOCATION) {
    		if (msgDir == V5MessageDefine.MSG_DIR_TO_CUSTOMER || 
    				msgDir == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_LOCATION_L;
    		} else if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
    			return TYPE_LOCATION_R;
    		}
    	} else if (msgType == V5MessageDefine.MSG_TYPE_IMAGE) {
    		if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
    			return TYPE_IMG_R;
    		} else if (msgDir == V5MessageDefine.MSG_DIR_TO_CUSTOMER ||
    				msgDir == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_IMG_L;
    		}    			
    	} else if (msgType == V5MessageDefine.MSG_TYPE_VOICE) {
    		if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
    			return TYPE_VOICE_R;
    		} else if (msgDir == V5MessageDefine.MSG_DIR_TO_CUSTOMER ||
    				msgDir == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_VOICE_L;
    		}  			
    	} else if (msgType > V5MessageDefine.MSG_TYPE_APP_URL) {
    		return TYPE_TIPS;
    	}
    	
		if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
			return TYPE_RIGHT_TEXT;
		} else { // QAODefine.MSG_DIR_TO_CUSTOMER 或 QAODefine.MSG_DIR_FROM_ROBOT
			return TYPE_LEFT_TEXT;
		}
    }
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		final int viewType = getItemViewType(position);
		V5Message message = mDatas.get(position).getMessage();
        if (convertView == null) {
        	switch (viewType) {
            case TYPE_LEFT_TEXT:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_from_msg, parent, false) ;
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            case TYPE_RIGHT_TEXT:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_to_msg, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            case TYPE_SINGLE_NEWS:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_single_news, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            case TYPE_NEWS:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_multi_news, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            case TYPE_LOCATION_R:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_to_location, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;

            case TYPE_LOCATION_L:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_from_location, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            case TYPE_IMG_L:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_from_img, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;

            case TYPE_IMG_R:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_to_img, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            case TYPE_VOICE_L:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_from_voice, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;

            case TYPE_VOICE_R:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_to_voice, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            case TYPE_TIPS:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_tips, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            	
            default:
            	convertView = mInflater.inflate(R.layout.v5_item_chat_to_msg, parent, false);
            	holder = new ViewHolder(viewType, convertView);
            	break;
            }
        	
        	convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();    
        }
        holder.setPosition(position);
        
        // 显示时间：第一条和时间差相差5min以上
 		if (position == 0 || 
 				(message.getCreate_time() - mDatas.get(position - 1).getMessage().getCreate_time()) > 300) {
 			holder.mDate.setVisibility(View.VISIBLE);
 			holder.mDate.setText(
 					DateUtil.timeFormat(message.getCreate_time() * 1000, false));
 		} else {
 			holder.mDate.setVisibility(View.GONE);
 		}
        switch (viewType) {
        case TYPE_SINGLE_NEWS: { // 单图文
			V5ArticlesMessage msgContent = (V5ArticlesMessage) message;
			V5ArticleBean article = (V5ArticleBean) msgContent.getArticles().get(0);
			holder.mNewsTitle.setText(article.getTitle());
			holder.mNewsContent.setText(article.getDescription());
			ImageLoader imgLoader = new ImageLoader(mChatActivity, true, R.drawable.v5_img_src_loading, new ImageLoader.ImageLoaderListener() {
				
				@Override
				public void onSuccess(String url, ImageView imageView, Bitmap bmp) {
					// TODO Auto-generated method stub
					if (!mDatas.get(position).isLoaded()) {
						mDatas.get(position).setLoaded(true);
						mChatActivity.sendEmptyMessage(ClientChatActivity.HDL_BOTTOM);
					}
				}
				
				@Override
				public void onFailure(ImageLoader imageLoader, String url,
						ImageView imageView) {
					// TODO Auto-generated method stub
					
				}
			});
        	imgLoader.DisplayImage(article.getPic_url(), holder.mNewsPic);
        	
        	convertView.findViewById(R.id.id_news_layout).setOnClickListener(new ItemClick(position, viewType, holder));
		}
		break;
		
		case TYPE_NEWS: { // 多图文
			V5ArticlesMessage msgContent = (V5ArticlesMessage) message;
			holder.mNewsAdapter = new NewsListAdapter(
					mChatActivity, 
					msgContent.getArticles());
			holder.mNewsListLayout.bindLinearLayout(holder.mNewsAdapter);
			holder.mNewsListLayout.setOnListLayoutClickListener(new OnListLayoutClickListener() {
				
				@Override
				public void onListLayoutClick(View v, int pos) {
					if (mListener != null) {
						mListener.onMultiNewsClick(v, position, viewType, pos);
					}
				}
			});
		}
		break;
		
		case TYPE_LOCATION_R: { // 位置-发出
			V5LocationMessage msgContent = (V5LocationMessage) message;
			ImageLoader mapImgLoader = new ImageLoader(mChatActivity, true, R.drawable.v5_img_src_loading);
        	double lat = msgContent.getX();
        	double lng = msgContent.getY();
        	String url = msgContent.getLocationImageURL();
        	
        	// 异步加载图片和位置描述信息
        	mapImgLoader.DisplayImage(url, holder.mMapIv);
        	holder.mLbsDescTv.setText(mChatActivity.getString(R.string.v5_loading));
        	UIUtil.getLocationTitle(mChatActivity, lat, lng, holder.mLbsDescTv);
        	
        	convertView.findViewById(R.id.id_right_location_layout)
					.setOnClickListener(new ItemClick(position, viewType, holder));
		}
		break;
		
		case TYPE_LOCATION_L: { // 位置-接收
			V5LocationMessage msgContent = (V5LocationMessage) message;
			ImageLoader mapImgLoader = new ImageLoader(mChatActivity, true, R.drawable.v5_img_src_loading);
        	double lat = msgContent.getX();
        	double lng = msgContent.getY();
        	String url = msgContent.getLocationImageURL();
        	Logger.i(TAG, "[地图] URL:" + url);
        	
        	// 异步加载图片和位置描述信息
        	mapImgLoader.DisplayImage(url, holder.mMapIv);
        	holder.mLbsDescTv.setText(mChatActivity.getString(R.string.v5_loading));
        	UIUtil.getLocationTitle(mChatActivity, lat, lng, holder.mLbsDescTv);
        	
        	convertView.findViewById(R.id.id_left_location_layout)
					.setOnClickListener(new ItemClick(position, viewType, holder));
		}
		break;
		
		case TYPE_IMG_R: { // 图片-发出
			V5ImageMessage msgContent = (V5ImageMessage) message;
			ImageLoader mapImgLoader = new ImageLoader(mChatActivity, true, R.drawable.v5_img_src_loading, new ImageLoader.ImageLoaderListener() {
				
				@Override
				public void onSuccess(String url, ImageView imageView, Bitmap bitmap) {
					// TODO Auto-generated method stub
					loadImage(imageView, bitmap);
					if (!mDatas.get(position).isLoaded()) {
						mDatas.get(position).setLoaded(true);
						mChatActivity.sendEmptyMessage(ClientChatActivity.HDL_BOTTOM);
					}
				}
				
				@Override
				public void onFailure(ImageLoader imageLoader, String url,
						ImageView imageView) {
					// TODO Auto-generated method stub
					
				}
			});
			if (msgContent.getFilePath() != null) {
        		mapImgLoader.DisplayImage(msgContent.getFilePath(), holder.mMapIv);
        	} else if (msgContent.getPic_url() != null) {
        		String pic_url = msgContent.getThumbnailPicUrl();
        		mapImgLoader.DisplayImage(pic_url, holder.mMapIv);
        	}
        	convertView.findViewById(R.id.id_right_location_layout)
        			.setOnClickListener(new ItemClick(position, viewType, holder));
		}
		break;
		
		case TYPE_IMG_L: { // 图片-接收
			V5ImageMessage msgContent = (V5ImageMessage) message;
			ImageLoader mapImgLoader = new ImageLoader(mChatActivity, true, R.drawable.v5_img_src_loading, new ImageLoader.ImageLoaderListener() {
				
				@Override
				public void onSuccess(String url, ImageView imageView, Bitmap bitmap) {
					// TODO Auto-generated method stub
					loadImage(imageView, bitmap);
					if (!mDatas.get(position).isLoaded()) {
						mDatas.get(position).setLoaded(true);
						mChatActivity.sendEmptyMessage(ClientChatActivity.HDL_BOTTOM);
					}
				}
				
				@Override
				public void onFailure(ImageLoader imageLoader, String url,
						ImageView imageView) {
					// TODO Auto-generated method stub
					
				}
			});
			String pic_url = msgContent.getThumbnailPicUrl();
			mapImgLoader.DisplayImage(pic_url, holder.mMapIv);
        	convertView.findViewById(R.id.id_left_location_layout)
        			.setOnClickListener(new ItemClick(position, viewType, holder));
		}
		break;
		
		case TYPE_VOICE_R: { // 语音-发出
        	convertView.findViewById(R.id.id_right_voice_layout)
			.setOnClickListener(new ItemClick(position, viewType, holder));
        	
			// 加载语音
			final V5VoiceMessage voiceMessage = (V5VoiceMessage)message;
//			Logger.d(TAG, position + " _R list load Voice ----- duration:" + voiceMessage.getDuration() + " filePath:" + voiceMessage.getFilePath());
			//double duration = Math.round(voiceMessage.getDuration()/1000.0f);
			double duration = voiceMessage.getDuration()/1000.0f;
			if (duration < 1) {
				duration = 1;
			}
        	holder.mVoiceSecondTv.setText(String.format("%.0f″", duration));
        	if (mDatas.get(position).isVoicePlaying()) {
        		holder.startVoicePlaying();
        	} else {
        		holder.finishVoicePlaying();
        	}
			if (voiceMessage.getFilePath() != null && voiceMessage.getDuration() > 0 &&
					FileUtil.isFileExists(voiceMessage.getFilePath())) { // 已加载则不需要loadMedia
//				Logger.d(TAG, "---已加载---");
				break;
			} else {
//				Logger.d(TAG, "---首次加载---");
			}
			
        	String url = voiceMessage.getUrl();
        	if (TextUtils.isEmpty(url)) {
        		if (voiceMessage.getFilePath() != null) {
        			url = voiceMessage.getFilePath();
        		}
        	}
//        	Logger.d(TAG, "_R Voice url:" + url + " sendState:" + voiceMessage.getState());
        	MediaLoader mediaLoader = new MediaLoader(mChatActivity, holder, new MediaLoader.MediaLoaderListener() {
				
				@Override
				public void onSuccess(V5Message msg, Object obj, MediaCache media) {
					((V5VoiceMessage)msg).setFilePath(media.getLocalPath());
					((V5VoiceMessage)msg).setDuration(media.getDuration());
//					Logger.w(TAG, "_R MediaLoader Voice onSuccess:" + media.getDuration() + media.getLocalPath());
					double duration = (media.getDuration()/1000.0f);
					if (duration < 1) {
						duration = 1;
					}
					if (obj != null) {
						((ViewHolder)obj).mVoiceSecondTv.setText(String.format("%.0f″", duration));
					}
				}
				
				@Override
				public void onFailure(final MediaLoader mediaLoader, final V5Message msg, Object obj) {
					Logger.w(TAG, "_R MediaLoader Voice onFailure");
					// [修改]魅族note2上出现死循环
					//notifyDataSetChanged();
				}
			});
        	// 加载语音并计算时长
        	mediaLoader.loadMedia(url, voiceMessage, null);
		}
		break;
		
		case TYPE_VOICE_L: { // 语音-接收
			convertView.findViewById(R.id.id_left_voice_layout)
			.setOnClickListener(new ItemClick(position, viewType, holder));
			
			// 加载语音
			final V5VoiceMessage voiceMessage = (V5VoiceMessage)message;
//			Logger.d(TAG, position + " _L list load Voice ----- duration:" + voiceMessage.getDuration() + " filePath:" + voiceMessage.getFilePath());
			//double duration = Math.round(voiceMessage.getDuration()/1000.0f);
			double duration = voiceMessage.getDuration()/1000.0f;
			if (duration < 1) {
				duration = 1;
			}
        	holder.mVoiceSecondTv.setText(String.format("%.0f″", duration));
        	if (mDatas.get(position).isVoicePlaying()) {
        		holder.startVoicePlaying();
        	} else {
        		holder.finishVoicePlaying();
        	}
			if (voiceMessage.getFilePath() != null && voiceMessage.getDuration() > 0 &&
					FileUtil.isFileExists(voiceMessage.getFilePath())) { // 已加载则不需要loadMedia
//				Logger.d(TAG, "---已加载---");
				break;
			} else {
//				Logger.d(TAG, "---首次加载---");
			}
			
        	String url = voiceMessage.getUrl();
        	if (TextUtils.isEmpty(url)) {
        		if (voiceMessage.getFilePath() != null) {
        			url = voiceMessage.getFilePath();
        		}
        	}
//        	Logger.d(TAG, "_L Voice url:" + url + " sendState:" + voiceMessage.getState());
        	MediaLoader mediaLoader = new MediaLoader(mChatActivity, holder, new MediaLoader.MediaLoaderListener() {
				
				@Override
				public void onSuccess(V5Message msg, Object obj, MediaCache media) {
//					Logger.w(TAG, "_L MediaLoader Voice onSuccess:" + media.getDuration() + media.getLocalPath());
					((V5VoiceMessage)msg).setFilePath(media.getLocalPath());
					((V5VoiceMessage)msg).setDuration(media.getDuration());
					double duration = (media.getDuration()/1000.0f);
					if (duration < 1) {
						duration = 1;
					}
					if (obj != null) {
						((ViewHolder)obj).mVoiceSecondTv.setText(String.format("%.0f″", duration));
					}
				}
				
				@Override
				public void onFailure(final MediaLoader mediaLoader, final V5Message msg, Object obj) {
					Logger.w(TAG, "_L MediaLoader Voice onFailure");
					notifyDataSetChanged();
				}
			});
        	// 加载语音并计算时长
        	mediaLoader.loadMedia(url, voiceMessage, null);
		}
		break;
		
		case TYPE_TIPS:
			holder.mMsg.setText(message.getDefaultContent(mChatActivity));
			break;
		
		default: {
			String content = message.getDefaultContent(mChatActivity) == null ? "" : message.getDefaultContent(mChatActivity);
			content = content.replaceAll("/::<", "/::&lt;");
			content = content.replaceAll("/:<", "/:&lt;");
	    	content = content.replaceAll("\n", "<br>");
			Spanned text = Html.fromHtml(content);
			holder.mMsg.setText(text);
			holder.mMsg.setMovementMethod(LinkMovementMethod.getInstance());
			
			if (message.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
				holder.mMsg.setBackgroundResource(R.drawable.v5_list_to_textview_bg);
			} else if (message.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT ||
					message.getDirection() == V5MessageDefine.MSG_DIR_COMMENT) {
				holder.mMsg.setBackgroundResource(R.drawable.v5_list_from_robot_bg);
			} else if (message.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
				holder.mMsg.setBackgroundResource(R.drawable.v5_list_from_textview_bg);
			}
			holder.mMsg.setOnClickListener(new ItemClick(position, viewType, holder));
			if (holder.mMsg instanceof EmojiconTextView) { // URL点击事件
            	((EmojiconTextView)holder.mMsg).setURLClickListener(new EmojiconTextView.OnURLClickListener() {
					
						@Override
						public void onClick(View v, String url) {
							boolean used = false;
							if (null != V5ClientAgent.getInstance().getURLClickListener()) {
								used = V5ClientAgent.getInstance().getURLClickListener().onURLClick(v.getContext(), ClientLinkType.clientLinkTypeURL, url);
							} 
							if (!used) {
								Intent intent = new Intent(mChatActivity, WebViewActivity.class);
								intent.putExtra("url", url);
								mChatActivity.startActivity(intent);
							}
						}
					});
	            }
			}
			break;
        }
        if (holder.mSendFailedIv != null && holder.mSendingPb != null) {
        	if (message.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
        		if (message.getState() == V5Message.STATE_FAILURE) {
        			holder.mSendingPb.setVisibility(View.GONE);
        			holder.mSendFailedIv.setVisibility(View.VISIBLE);
        		} else if (message.getState() == V5Message.STATE_SENDING) {
        			holder.mSendingPb.setVisibility(View.VISIBLE);
        			holder.mSendFailedIv.setVisibility(View.GONE);
        		} else {
        			holder.mSendFailedIv.setVisibility(View.GONE);
            		holder.mSendingPb.setVisibility(View.GONE);
        		}
        	} else {
        		holder.mSendFailedIv.setVisibility(View.GONE);
        		holder.mSendingPb.setVisibility(View.GONE);
        	}
        	holder.mSendFailedIv.setOnClickListener(new ItemClick(position, viewType, holder));
        }
        // 头像
        if (holder.mAvatar != null && !showAvatar) { // 不显示头像
        	holder.mAvatar.setVisibility(View.GONE);
        } else if (holder.mAvatar != null) { // 显示头像
        	holder.mAvatar.setVisibility(View.VISIBLE);
        	int photoDefaultId = 0;
        	String photoUrl = null;
        	if (message.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
        		photoDefaultId = R.drawable.v5_avatar_customer;
        		photoUrl = V5ClientAgent.getInstance().getConfig().getAvatar();
        	} else if (message.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT ||
					message.getDirection() == V5MessageDefine.MSG_DIR_COMMENT) {
				photoDefaultId = R.drawable.v5_avatar_robot;
				photoUrl = V5ClientAgent.getInstance().getConfig().getRobotPhoto();
			} else if (message.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
				photoDefaultId = R.drawable.v5_avatar_worker;
				V5ConfigSP configSP = new V5ConfigSP(mChatActivity);
				photoUrl = configSP.readPhoto(message.getW_id());
			}
        	ImageLoader mapImgLoader = new ImageLoader(mChatActivity, true, photoDefaultId, new ImageLoader.ImageLoaderListener() {
				
				@Override
				public void onSuccess(String url, ImageView imageView, Bitmap bmp) {
					// TODO Auto-generated method stub
					Logger.d(TAG, "ImageLoader->success url:" + url);
				}
				
				@Override
				public void onFailure(ImageLoader imageLoader, String url,
						ImageView imageView) {
					// TODO Auto-generated method stub
					Logger.w(TAG, "ImageLoader->failure url:" + url);
				}
			});
			mapImgLoader.DisplayImage(photoUrl, holder.mAvatar);
        }
        
		return convertView;
	}
	
	private void loadImage(ImageView iv, Bitmap bitmap) {
		// 控制宽高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Logger.i(TAG, "before ratio width:" + width + " height:" + height);
		V5Size size = ImageLoader.getScaledSize(mChatActivity, width, height);
		ViewGroup.LayoutParams params = iv.getLayoutParams();
		params.width = size.getWidth();
		params.height = size.getHeight();
		iv.setLayoutParams(params);
		iv.setScaleType(ScaleType.CENTER_CROP);
		Logger.i(TAG, " ratio width:" + params.width + " height:" + params.height);
	}

//    private void sendStateChange(ViewHolder holder, V5Message chatMessage) {
//    	if (holder.mSendFailedIv != null && holder.mSendingPb != null) {
//    		if (chatMessage.getState() == V5Message.STATE_FAILURE) {
//    			holder.mSendingPb.setVisibility(View.GONE);
//    			holder.mSendFailedIv.setVisibility(View.VISIBLE);
//    		} else if (chatMessage.getState() == V5Message.STATE_SENDING) {
//    			holder.mSendingPb.setVisibility(View.VISIBLE);
//    			holder.mSendFailedIv.setVisibility(View.GONE);
//    		} else {
//    			holder.mSendFailedIv.setVisibility(View.GONE);
//        		holder.mSendingPb.setVisibility(View.GONE);
//    		}
//        }
//    }
	
	class ViewHolder {
		private int mPosition;
		
		public TextView mDate;
		public ImageView mSendFailedIv;
		public ProgressBar mSendingPb;
		public RoundImageView mAvatar;
        
        /* 文本/tips */
        public TextView mMsg;
        
        /* 单图文news */
        public TextView mNewsTitle;
        public TextView mNewsContent;
		public ImageView mNewsPic;
        
        /* 多图文 */
		public ListLinearLayout mNewsListLayout;
        public NewsListAdapter mNewsAdapter;
        
        /* 位置 */
        public ImageView mMapIv;
        public TextView mLbsDescTv;
        
        /* 图片 */
        // mMapIv
        
        /* 语音 */
        public View mVoiceLayout;
        public ImageView mVoiceIv;
        public TextView mVoiceSecondTv;
        public AnimationDrawable mVoiceAnimDrawable;
        
        public ViewHolder(int viewType, View itemView) {
            switch (viewType) {
            case TYPE_LEFT_TEXT:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_from_msg_text);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_from_msg_avatar);
            	break;
            	
            case TYPE_RIGHT_TEXT:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_to_msg_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_to_msg_avatar);
            	break;
            	
            case TYPE_SINGLE_NEWS:
            	mDate = (TextView) itemView.findViewById(R.id.id_news_msg_date);
            	mNewsPic = (ImageView) itemView.findViewById(R.id.id_news_img);
            	mNewsTitle = (TextView) itemView.findViewById(R.id.id_news_title_inner_text);
            	mNewsContent = (TextView) itemView.findViewById(R.id.id_news_desc_text);
            	break;
            	
            case TYPE_NEWS:
            	mDate = (TextView) itemView.findViewById(R.id.id_news_msg_date);
            	mNewsListLayout = (ListLinearLayout) itemView.findViewById(R.id.id_news_layout);
            	break;
            	
            case TYPE_LOCATION_R:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_map_img_iv);
            	mLbsDescTv = (TextView) itemView.findViewById(R.id.id_map_address_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_to_msg_avatar);
            	break;
            	
            case TYPE_LOCATION_L:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_map_img_iv);
            	mLbsDescTv = (TextView) itemView.findViewById(R.id.id_map_address_text);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_from_msg_avatar);
            	break;
            	
            case TYPE_IMG_L:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_type_img_iv);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_from_msg_avatar);
            	break;

            case TYPE_IMG_R:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_type_img_iv);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_to_msg_avatar);
            	break;
            	
            case TYPE_VOICE_L:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mVoiceIv = (ImageView) itemView.findViewById(R.id.id_from_voice_iv);
            	mVoiceSecondTv = (TextView) itemView.findViewById(R.id.id_from_voice_tv);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_from_msg_avatar);
            	break;
            	
            case TYPE_VOICE_R:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mVoiceIv = (ImageView) itemView.findViewById(R.id.id_to_voice_iv);
            	mVoiceSecondTv = (TextView) itemView.findViewById(R.id.id_to_voice_tv);
            	
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mAvatar = (RoundImageView) itemView.findViewById(R.id.id_to_msg_avatar);
            	break;
            	
            case TYPE_TIPS:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mMsg = (TextView) itemView.findViewById(R.id.id_msg_tips);
            	break;
            	
            default:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_to_msg_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	break;
            }            
        }
        
        public void startVoicePlaying() {
			Logger.i(TAG, "UI - startVoicePlaying " + mPosition);
			V5Message msg = mDatas.get(mPosition).getMessage();
			mDatas.get(mPosition).setVoicePlaying(true);
			mVoiceIv.setBackgroundResource(R.anim.v5_anim_rightgray_voice);
			if (msg.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
				mVoiceIv.setBackgroundResource(R.anim.v5_anim_rightgray_voice);
			} else if (msg.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT ||
					msg.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
				mVoiceIv.setBackgroundResource(R.anim.v5_anim_leftwhite_voice);
			}
			mVoiceAnimDrawable = (AnimationDrawable)mVoiceIv.getBackground();
			mVoiceAnimDrawable.start();
		}
		
		public void finishVoicePlaying() {
			Logger.i(TAG, "UI - finishVoicePlaying " + mPosition);
			V5Message msg = mDatas.get(mPosition).getMessage();
//			for (V5ChatBean chatBean : mDatas) { // 重置全部状态
//				chatBean.setVoicePlaying(false);
//			}
			mDatas.get(mPosition).setVoicePlaying(false);
			if (mVoiceAnimDrawable != null) {
				mVoiceAnimDrawable.stop();
				mVoiceAnimDrawable = null;
			}
			if (msg.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
				mVoiceIv.setBackgroundResource(R.drawable.v5_chat_animation_right_gray3);
			} else if (msg.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT ||
					msg.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
				mVoiceIv.setBackgroundResource(R.drawable.v5_chat_animation_left_white3);
			}
		}
		
		private void startPlaying(V5VoiceMessage voiceMessage, OnCompletionListener completionListener) {
	    	Logger.i(TAG, "MediaPlayer - startPlaying " + mPosition);
	    	if (mPlayer != null) {
	    		mPlayer.release();
	    		mPlayer = null;
	    		resetOtherItemsExcept(voiceMessage);
	    	}
	    	mPlayer = new MediaPlayer();
	        try {
	            mPlayer.setDataSource(voiceMessage.getFilePath());
	            mPlayer.prepare();
	            mPlayer.start();
	            mPlayer.setOnErrorListener(new OnErrorListener() {
					
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						Logger.e(TAG, "MediaPlayer - onError");
						return false;
					}
				});
	            mPlayer.setOnCompletionListener(completionListener);
				startVoicePlaying();
	        } catch (IOException e) {
	            Logger.e(TAG, "MediaPlayer prepare() failed");
	            mPlayer.release();
	    		mPlayer = null;
	    		finishVoicePlaying(); // UI
	        }
	    }
	    
	    private void stopPlaying() {
	    	Logger.i(TAG, "MediaPlayer - stopPlayer " + mPosition);
	    	if (mPlayer != null) {
	    		mPlayer.stop();
	    		mPlayer.release();
	        	mPlayer = null;
	    	}
	    	finishVoicePlaying();
	    }

	    public void setPosition(int mPosition) {
			this.mPosition = mPosition;
		}
	}
	
	class ItemClick implements OnClickListener, OnLongClickListener {
		
		private int position;
		private int viewType;
		private ViewHolder viewHolder;
		
		public ItemClick(int pos, int type, ViewHolder holder) {
			this.position = pos;
			this.viewType = type;
			this.viewHolder = holder;
		}

		@Override
		public void onClick(View v) {
			V5Message msg = mDatas.get(position).getMessage();
			Logger.d(TAG, "Click position:" + position + " viewType:" + viewType + " msgType:" + msg.getMessage_type());
//			if (v.getId() == R.id.id_left_voice_layout ||
//					v.getId() == R.id.id_right_voice_layout) {
			if (msg.getMessage_type() == V5MessageDefine.MSG_TYPE_VOICE) {
				Logger.w(TAG, position + " voice message filepath:" + ((V5VoiceMessage)msg).getFilePath());
				if (mDatas.get(position).isVoicePlaying()) { // 停止播放
					viewHolder.stopPlaying();
				} else { // 开始播放
					if (viewHolder.mVoiceAnimDrawable != null) {
						viewHolder.mVoiceAnimDrawable.stop();
					}
					viewHolder.startPlaying((V5VoiceMessage)msg, new OnCompletionListener() {
						
						@Override
						public void onCompletion(MediaPlayer mp) {
							Logger.i(TAG, "MediaPlayer - completePlaying");
							viewHolder.finishVoicePlaying();
							mp.release();
							mp = null;
							mPlayer = null;
						}
					});
				}
			}
//			}
			
			if (mListener != null) {
				mListener.onChatItemClick(v, position, viewType);
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if (mListener != null) {
				mListener.onChatItemLongClick(v, position, viewType);
				return true;
			}
			return false;
		}	
	}
	
	private void resetOtherItemsExcept(V5Message bean) {
    	Logger.d(TAG, "resetOtherItems");
		for (V5ChatBean chatBean : mDatas) {
			chatBean.setVoicePlaying(false);
			if (bean == chatBean.getMessage()) {
				chatBean.setVoicePlaying(true);
			}
		}
		notifyDataSetChanged();
	}
    
    public void stopVoicePlaying() {
    	if (mPlayer != null) {
    		if (mPlayer.isPlaying()) {
    			mPlayer.stop();
    		}
    		mPlayer.release();
    		mPlayer = null;
    	}
    }
}
