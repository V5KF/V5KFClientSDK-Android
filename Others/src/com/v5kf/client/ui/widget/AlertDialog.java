package com.v5kf.client.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.v5kf.client.R;

public class AlertDialog {
	private Context context;
	private Dialog dialog;
	private LinearLayout lLayout_bg;
	private TextView txt_title;
	private TextView txt_msg;
	private Button btn_neg;
	private Button btn_pos;
	private ImageView img_line;
	private LinearLayout mCustomLayout;
	private boolean showTitle = false;
	private boolean showMsg = false;
	private boolean showPosBtn = false;
	private boolean showNegBtn = false;

	public AlertDialog(Context context) {
		this.context = context;
	}

	public AlertDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(
				R.layout.v5_view_alertdialog, null);

		// 获取自定义Dialog布局中的控件
		lLayout_bg = (LinearLayout) view.findViewById(R.id.lLayout_bg);
		mCustomLayout = (LinearLayout) view.findViewById(R.id.custom_layout);
		txt_title = (TextView) view.findViewById(R.id.txt_title);
		txt_title.setVisibility(View.GONE);
		txt_msg = (TextView) view.findViewById(R.id.txt_msg);
		txt_msg.setVisibility(View.GONE);
		btn_neg = (Button) view.findViewById(R.id.btn_neg);
		btn_neg.setVisibility(View.GONE);
		btn_pos = (Button) view.findViewById(R.id.btn_pos);
		btn_pos.setVisibility(View.GONE);
		img_line = (ImageView) view.findViewById(R.id.img_line);
		img_line.setVisibility(View.GONE);

		// 定义Dialog布局和参数
		dialog = new Dialog(context, R.style.V5_alertDialogStyle);
		dialog.setContentView(view);

		// 调整dialog背景大小
		int dialogWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
		int maxWidth = (int) context.getResources().getDimension(R.dimen.v5_dialog_width);
		if (dialogWidth > maxWidth) {
			dialogWidth = maxWidth;
		}
		lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT));

		return this;
	}

	public AlertDialog setView(View v) {
		mCustomLayout.setVisibility(View.VISIBLE);
		mCustomLayout.addView(v);
		return this;
	}
	
	public AlertDialog setTitle(String title) {
		showTitle = true;
		if ("".equals(title) || title == null) {
			txt_title.setText(R.string.v5_tips);
		} else {
			Spanned text = Html.fromHtml(title);
			txt_title.setText(text);
		}
		return this;
	}

	public AlertDialog setMsg(String msg) {
		showMsg = true;
		if ("".equals(msg) || msg == null) {
			txt_msg.setText(R.string.v5_tips);
		} else {
			msg.replace("\\n", "<br>");
			Spanned text = Html.fromHtml(msg);
			txt_msg.setText(text);
		}
		return this;
	}

	public AlertDialog setTitle(int title) {
		showTitle = true;
		if (0 == title) {
			txt_title.setText(R.string.v5_tips);
		} else {
			txt_title.setText(title);
		}
		return this;
	}
	
	public AlertDialog setMsg(int msg) {
		showMsg = true;
		if (msg == 0) {
			txt_msg.setText(R.string.v5_tips);
		} else {
			
			txt_msg.setText(msg);
		}
		return this;
	}

	public AlertDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}
	
	public Dialog getDialog() {
		return dialog;
	}
	
	public AlertDialog setWindowType(int windowType) {
		dialog.getWindow().setType(windowType);
		return this;
	}

	public AlertDialog setPositiveButton(String text,
			final OnClickListener listener) {
		showPosBtn = true;
		if ("".equals(text)) {
			btn_pos.setText(R.string.v5_btn_confirm);
		} else {
			btn_pos.setText(text);
		}
		btn_pos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onClick(v);
				}
			}
		});
		return this;
	}

	public AlertDialog setNegativeButton(String text,
			final OnClickListener listener) {
		showNegBtn = true;
		if ("".equals(text)) {
			btn_neg.setText(R.string.v5_btn_cancel);
		} else {
			btn_neg.setText(text);
		}
		btn_neg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onClick(v);
				}
			}
		});
		return this;
	}

	public AlertDialog setPositiveButton(int text,
			final OnClickListener listener) {
		showPosBtn = true;
		if (0 == (text)) {
			btn_pos.setText(R.string.v5_btn_confirm);
		} else {
			btn_pos.setText(text);
		}
		btn_pos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onClick(v);
				}
			}
		});
		return this;
	}
	
	public AlertDialog setNegativeButton(int text,
			final OnClickListener listener) {
		showNegBtn = true;
		if (0 == (text)) {
			btn_neg.setText(R.string.v5_btn_cancel);
		} else {
			btn_neg.setText(text);
		}
		btn_neg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onClick(v);
				}
			}
		});
		return this;
	}

	private void setLayout() {
		if (!showTitle && !showMsg) {
			txt_title.setText(R.string.v5_tips);
			txt_title.setVisibility(View.VISIBLE);
		}

		if (showTitle) {
			txt_title.setVisibility(View.VISIBLE);
		}

		if (showMsg) {
			txt_msg.setVisibility(View.VISIBLE);
		}

		if (!showPosBtn && !showNegBtn) {
			btn_pos.setText(R.string.v5_btn_confirm);
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setBackgroundResource(R.drawable.v5_alertdialog_single_selector);
			btn_pos.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}

		if (showPosBtn && showNegBtn) {
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setBackgroundResource(R.drawable.v5_alertdialog_right_selector);
			btn_neg.setVisibility(View.VISIBLE);
			btn_neg.setBackgroundResource(R.drawable.v5_alertdialog_left_selector);
			img_line.setVisibility(View.VISIBLE);
		}

		if (showPosBtn && !showNegBtn) {
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setBackgroundResource(R.drawable.v5_alertdialog_single_selector);
		}

		if (!showPosBtn && showNegBtn) {
			btn_neg.setVisibility(View.VISIBLE);
			btn_neg.setBackgroundResource(R.drawable.v5_alertdialog_single_selector);
		}
	}

	public void show() {
		setLayout();
		if (dialog != null) {
			dialog.show();
		}
	}
	
	public void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	
	public boolean isShowing() {
		if (dialog != null) {
			return dialog.isShowing();
		} else {
			return false;
		}
	}
}
