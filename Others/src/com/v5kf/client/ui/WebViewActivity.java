package com.v5kf.client.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.v5kf.client.R;
import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.ui.widget.ActionItem;
import com.v5kf.client.ui.widget.TitlePopup;
import com.v5kf.client.ui.widget.TitlePopup.OnItemOnClickListener;

public class WebViewActivity extends Activity {
	/* Title Action Bar */
	private LinearLayout mLeftLayout;
	private TextView mTitleTv;
	
	private WebView mWebView;
	private String mUrl;
	private int mTitleId;
	private LinearLayout mLoadingLl;
	
	// 标题栏弹窗
	private TitlePopup mTitlePopup;
	
	@Override
	public void onBackPressed() {
		if (mWebView != null && mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			finish();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getSupportActionBar().hide(); // 隐藏ActionBar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.v5_activity_web_view);
		
		handleIntent();
		findView();
		initView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		V5ClientAgent.getInstance().onStart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		V5ClientAgent.getInstance().onStop();
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mUrl = intent.getStringExtra("url");
		mTitleId = intent.getIntExtra("title", 0);
		if (null == mUrl || mUrl.isEmpty()) {
			Logger.w("webViewActivity", "Got null url.");
			finish();
			return;
		}
		Logger.w("webViewActivity", "Got url:" + mUrl);
	}

	private void findView() {
		mLeftLayout = (LinearLayout) findViewById(R.id.header_layout_leftview_container);
		mTitleTv = (TextView) findViewById(R.id.header_htv_subtitle);
		
		mWebView = (WebView) findViewById(R.id.id_web_view);
		mLoadingLl = (LinearLayout) findViewById(R.id.layout_container_empty);
	}

	private void initView() {
		initTitleBar();
		initPopupMenu();
		
		WebChromeClient wvcc = new WebChromeClient() {  
            @Override  
            public void onReceivedTitle(WebView view, String title) {  
                super.onReceivedTitle(view, title);  
                Logger.d("ANDROID_LAB", "TITLE=" + title); 
                if (title != null && !title.isEmpty()) {
                	mTitleTv.setText(title);  
                }
            }  
  
        };  
        // 设置setWebChromeClient对象  
        mWebView.setWebChromeClient(wvcc);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				mWebView.setVisibility(View.GONE);
				mLoadingLl.setVisibility(View.VISIBLE);
		        return true;
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mWebView.setVisibility(View.VISIBLE);
				mLoadingLl.setVisibility(View.GONE);
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				view.loadUrl("file:///android_asset/404.html");
			}
		});
		mWebView.loadUrl(mUrl);
	}

	private void initTitleBar() {
		if (mTitleId == 0) {
			mTitleId = R.string.v5_chat_title;
		}
		Button mRightBtn = ((Button)findViewById(R.id.header_right_btn));
		mRightBtn.setBackgroundResource(R.drawable.v5_action_bar_more);
		
		mTitleTv.setText(mTitleId);
		mLeftLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mWebView.canGoBack()) {
					mWebView.goBack();
				} else {
					finish();
				}
			}
		});
		mRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTitlePopup.show(v);
			}
		});
	}
	
	/**
     * 初始化弹出菜单栏
     * @param initPopupMenu MainTabActivity 
     * @return void
     */
    private void initPopupMenu(){
    	// 实例化标题栏弹窗
    	mTitlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	
    	mTitlePopup.addAction(new ActionItem(this, R.string.v5_refresh, R.drawable.v5_popmenu_refresh));
    	mTitlePopup.addAction(new ActionItem(this, R.string.v5_open_by_browser, R.drawable.v5_popmenu_browser));
    	
    	mTitlePopup.setItemOnClickListener(new OnItemOnClickListener() {
			
			@Override
			public void onItemClick(ActionItem item, int position) {
				switch (position) {
				case 0:
					mWebView.reload();
					break;
				case 1:
					Intent intent = new Intent();   
			        intent.setAction("android.intent.action.VIEW");    
			        Uri content_url = Uri.parse(mUrl);   
			        intent.setData(content_url);
			        startActivity(intent);
				}
			}
		});
	}

}
