package com.v5kf.client.ui.keyboard;

public class KeyboardConfig {

	/**
	 * 一下设置项仅在首次安装应用时检测生效，已安装升级无效，需要卸载重新安装
	 */
	public static final boolean ENABLE_QQ_FACE = true; // 支持微信、qq表情
	public static final boolean ENABLE_EMOJI = true; // 支持Emoji表情
	public static final boolean ENABLE_ASSETS_PIC = false; // 支持assets文件夹中的自定义图片表情
	
	// 单位dp
	public static final int EMOICON_ITEM_HSPACING_LANDSCAPE = 20; // 横屏时水平间距
	public static final int EMOICON_ITEM_HSPACING_PORTRAIT = 0;
	public static final int EMOICON_ITEM_VSPACING_LANDSCAPE = 10;
	public static final int EMOICON_ITEM_VSPACING_PORTRAIT = 10;
	public static final int EMOICON_ITEM_PADDING_LANDSCAPE = 10;
	public static final int EMOICON_ITEM_PADDING_PORTRAIT = 20;
	public static final int EMOICON_ROW_LANDSCAPE = 11;
	public static final int EMOICON_LINE_LANDSCAPE = 2;
	public static final int EMOICON_ROW_PORTRAIT = 7;
	public static final int EMOICON_LINE_PORTRAIT = 3;
	
	public static final int APPFUNC_ROW_LANDSCAPE = 6;
	public static final int APPFUNC_LINE_LANDSCAPE = 1;
	public static final int APPFUNC_ROW_PORTRAIT = 4;
	public static final int APPFUNC_LINE_PORTRAIT = 2;
	
}
