package com.v5kf.client.lib.entity;

public class V5MessageDefine {
	
//	public static final String CUSTOMER_ID = "customer_id";
//	public static final String SESSION_ID = "session_id";
//	public static final String ACCOUNT_ID = "account_id";
//	public static final String VISITOR_ID = "visitor_id"; // u_id
//	public static final String WORKER_ID = "worker_id";
//	public static final String CHANNEL = "channel";
//	public static final String INTERFACE = "interface";
//	public static final String SERVICE = "service";
//	public static final String NICKNAME = "nickname";
	
	/* QAO Message_Dir */
	public static final int MSG_DIR_TO_CUSTOMER = 0; /* 坐席发给客户 */
	public static final int MSG_DIR_TO_WORKER = 1; /* 客户发给坐席 */
	public static final int MSG_DIR_FROM_ROBOT = 2; /* 来自机器人 */
	public static final int MSG_DIR_RELATIVE_QUES = 8; /* 相关问题 */
	public static final int MSG_DIR_COMMENT = 9; /* 评价 */
	
	/* QAO Message */
//	public static final String C_ID = "c_id";
//	public static final String S_ID = "s_id";
//	public static final String M_ID = "m_id";
//	public static final String U_ID = "u_id";
//	public static final String F_ID = "f_id";
	public static final String W_ID = "w_id";
	public static final String HIT = "hit";
	public static final String CREATE_TIME = "create_time";
//	public static final String FIRST_TIME = "first_time";
	public static final String MESSAGE_ID = "message_id";
	public static final String DIRECTION = "direction";
	public static final String MESSAGE_TYPE = "message_type";
	public static final String CANDIDATE = "candidate";
	public static final String MSG_ID = "msg_id";
	
	/* QAO MessageContent of all type */
	public static final String MSG_CONTENT = "content";
	public static final String MSG_MEDIA_ID = "media_id";
	public static final String MSG_RECOGNITION = "recognition";
	public static final String MSG_FORMAT = "format";
	public static final String MSG_THUMB_ID = "thumb_id";
	public static final String MSG_X = "x";
	public static final String MSG_Y = "y";
	public static final String MSG_SCALE = "scale";
	public static final String MSG_LABEL = "label";	
	public static final String MSG_TITLE = "title";
	public static final String MSG_PIC_URL = "pic_url";
	public static final String MSG_MUSIC_URL = "music_url";
	public static final String MSG_HQ_MUSIC_URL = "hq_music_url";
	public static final String MSG_URL = "url";
	public static final String MSG_TOKEN = "token";
	public static final String MSG_DESCRIPTION = "description";
	public static final String MSG_MULTI_ARTICLE = "articles";
	public static final String MSG_ARTICLES = "articles";
	public static final String MSG_ARTICLE = "article";
	public static final String MSG_CODE = "code";
	public static final String MSG_ARGC = "argc";
	public static final String MSG_ARGV = "argv";
	public static final String MSG_NAME = "name";
	public static final String MSG_PHONE = "phone";
	public static final String MSG_EMAIL = "email";
	public static final String MSG_QQ = "qq";
	public static final String MSG_TEXT = "text";
	public static final String MSG_SUMMARY = "summary";
	
	
	/* QAO MessageType */
	public static final int MSG_TYPE_NULL = 0;
	public static final int MSG_TYPE_TEXT = 1;
	public static final int MSG_TYPE_IMAGE = 2;
	public static final int MSG_TYPE_LOCATION = 3;
	public static final int MSG_TYPE_LINK = 4;
	public static final int MSG_TYPE_EVENT = 5;
	public static final int MSG_TYPE_VOICE = 6;
	public static final int MSG_TYPE_VEDIO = 7;
	public static final int MSG_TYPE_SHORT_VEDIO = 8;
	public static final int MSG_TYPE_ARTICLES = 9; /* 图文news */
	public static final int MSG_TYPE_MUSIC = 10;	
	public static final int MSG_TYPE_WXCS = 11;	/* 切换到多客服 */
//	public static final int MSG_TYPE_RES = 20;
//	public static final int MSG_TYPE_VS = 21;
	public static final int MSG_TYPE_APP_URL = 22;	/* 第三方URL */
	public static final int MSG_TYPE_COMMENT = 23;	/* 评价 */
	public static final int MSG_TYPE_NOTE = 24;		/* 留言 */
	public static final int MSG_TYPE_CONTROL = 25;	/* 转人工客服等命令 */
	
	public static final int MSG_TYPE_TIPS = 30;	/* 提示性消息 */
	
}
