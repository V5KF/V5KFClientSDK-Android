package com.v5kf.client.ui.keyboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.v5kf.client.ui.emojicon.QFaceiconUtil;

public class EmoticonsUtils {

	/**
     * 初始化表情数据库
     * @param context
     */
    public static void initEmoticonsDB(final Context context) {
        if (!Utils.isInitDb(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBHelper dbHelper = new DBHelper(context);
                    
                    /**
                     * FROM DRAWABLE-QQ face
                     */
                    if (KeyboardConfig.ENABLE_QQ_FACE) {
	                    ArrayList<EmoticonBean> qfaceArray = ParseData(QFaceiconUtil.faceMap, EmoticonBean.FACE_TYPE_NOMAL, ImageBase.Scheme.DRAWABLE);
	                    EmoticonSetBean qfaceEmoticonSetBean = new EmoticonSetBean("qface", KeyboardConfig.EMOICON_LINE_PORTRAIT, KeyboardConfig.EMOICON_ROW_PORTRAIT);
	                    qfaceEmoticonSetBean.setIconUri("drawable://qf000");
	                    qfaceEmoticonSetBean.setItemPadding(KeyboardConfig.EMOICON_ITEM_PADDING_PORTRAIT);
	                    qfaceEmoticonSetBean.setVerticalSpacing(KeyboardConfig.EMOICON_ITEM_VSPACING_PORTRAIT);
	                    qfaceEmoticonSetBean.setShowDelBtn(true);
	                    qfaceEmoticonSetBean.setEmoticonList(qfaceArray);
	                    dbHelper.insertEmoticonSet(qfaceEmoticonSetBean);
                    }

//                    /**
//                     * FROM DRAWABLE-Emoji
//                     */
//                    if (KeyboardConfig.ENABLE_EMOJI) {
//	                    ArrayList<EmoticonBean> emojiArray = ParseData(UnicodeEmoticons.emojiArray, EmoticonBean.FACE_TYPE_NOMAL, ImageBase.Scheme.DRAWABLE);
//	                    EmoticonSetBean emojiEmoticonSetBean = new EmoticonSetBean("emoji", 3, 7);
//	                    emojiEmoticonSetBean.setIconUri("drawable://v5_icon_emoji");
//	                    emojiEmoticonSetBean.setItemPadding(20);
//	                    emojiEmoticonSetBean.setVerticalSpacing(10);
//	                    emojiEmoticonSetBean.setShowDelBtn(true);
//	                    emojiEmoticonSetBean.setEmoticonList(emojiArray);
//	                    dbHelper.insertEmoticonSet(emojiEmoticonSetBean);
//                    }

                    /**
                     * FROM ASSETS
                     */
                    if (KeyboardConfig.ENABLE_ASSETS_PIC) {
	                    ArrayList<EmoticonBean> xhsfaceArray = ParseData(xhsemojiArray, EmoticonBean.FACE_TYPE_NOMAL, ImageBase.Scheme.ASSETS);
	                    EmoticonSetBean xhsEmoticonSetBean = new EmoticonSetBean("xhs", 3, 7);
	                    xhsEmoticonSetBean.setIconUri("assets://xhsemoji_19.png");
	                    xhsEmoticonSetBean.setItemPadding(20);
	                    xhsEmoticonSetBean.setVerticalSpacing(10);
	                    xhsEmoticonSetBean.setShowDelBtn(true);
	                    xhsEmoticonSetBean.setEmoticonList(xhsfaceArray);
	                    dbHelper.insertEmoticonSet(xhsEmoticonSetBean);
                    }

//                    /**
//                     * FROM FILE
//                     */
//                    String filePath = Utils.getPackageFilePath(context) +"/wxemoticons";
//                    try{
//                        FileUtils.unzip(context.getAssets().open("wxemoticons.zip"), filePath);
//                    }catch(IOException e){
//                        e.printStackTrace();
//                    }
//                    XmlUtil xmlUtil = new XmlUtil(context);
//                    EmoticonSetBean bean =  xmlUtil.ParserXml(xmlUtil.getXmlFromSD(filePath + "/wxemoticons.xml"));
////					bean.setItemPadding(20);
////					bean.setVerticalSpacing(5);
////					bean.setIconUri("file://" + filePath + "/icon_030_cover.png");
//                    dbHelper.insertEmoticonSet(bean);

                    /**
                     * FROM HTTP/HTTPS
                     */


                    /**
                     * FROM CONTENT
                     */


                    /**
                     * FROM USER_DEFINED
                     */

                    dbHelper.cleanup();
                    Utils.setIsInitDb(context, true);
                }
            }).start();
        }
    }

    public static EmoticonsKeyboardBuilder getSimpleBuilder(Context context) {

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<EmoticonSetBean> mEmoticonSetBeanList = dbHelper.queryEmoticonSet("emoji","xhs");
        dbHelper.cleanup();

        return new EmoticonsKeyboardBuilder.Builder()
                .setEmoticonSetBeanList(mEmoticonSetBeanList)
                .build();
    }

    public static EmoticonsKeyboardBuilder getBuilder(Context context, boolean isLandscape) {

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<EmoticonSetBean> mEmoticonSetBeanList = dbHelper.queryAllEmoticonSet(isLandscape);
        dbHelper.cleanup();

        return new EmoticonsKeyboardBuilder.Builder()
                .setEmoticonSetBeanList(mEmoticonSetBeanList)
                .build();
    }

    public static ArrayList<EmoticonBean> ParseData(String[] arry, long eventType, ImageBase.Scheme scheme) {
        try {
            ArrayList<EmoticonBean> emojis = new ArrayList<EmoticonBean>();
            for (int i = 0; i < arry.length; i++) {
                if (!TextUtils.isEmpty(arry[i])) {
                    String temp = arry[i].trim().toString();
                    String[] text = temp.split(",");
                    if (text != null && text.length == 2) {
                        String fileName = null;
                        if (scheme == ImageBase.Scheme.DRAWABLE) {
                            if(text[0].contains(".")){
                                fileName = scheme.toUri(text[0].substring(0, text[0].lastIndexOf(".")));
                            }
                            else {
                                fileName = scheme.toUri(text[0]);
                            }
                        } else {
                            fileName = scheme.toUri(text[0]);
                        }
                        String content = text[1];
                        EmoticonBean bean = new EmoticonBean(eventType, fileName, content);
                        emojis.add(bean);
                    }
                }
            }
            return emojis;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static ArrayList<EmoticonBean> ParseData(Map<String, String> map, long eventType, ImageBase.Scheme scheme) {
        try {
            ArrayList<EmoticonBean> emojis = new ArrayList<EmoticonBean>();
            Iterator<String> it = map.keySet().iterator();  
            while (it.hasNext()) {
            	String key = it.next();
            	EmoticonBean bean = new EmoticonBean(eventType, scheme.toUri(map.get(key)), key);
                emojis.add(bean);
            }
            return emojis;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    

    /*
    小红书表情
     */
    public static String[] xhsemojiArray = {
            "xhsemoji_1.png,[无语]",
            "xhsemoji_2.png,[汗]",
            "xhsemoji_3.png,[瞎]",
            "xhsemoji_4.png,[口水]",
            "xhsemoji_5.png,[酷]",
            "xhsemoji_6.png,[哭] ",
            "xhsemoji_7.png,[萌]",
            "xhsemoji_8.png,[挖鼻孔]",
            "xhsemoji_9.png,[好冷]",
            "xhsemoji_10.png,[白眼]",
            "xhsemoji_11.png,[晕]",
            "xhsemoji_12.png,[么么哒]",
            "xhsemoji_13.png,[哈哈]",
            "xhsemoji_14.png,[好雷]",
            "xhsemoji_15.png,[啊]",
            "xhsemoji_16.png,[嘘]",
            "xhsemoji_17.png,[震惊]",
            "xhsemoji_18.png,[刺瞎]",
            "xhsemoji_19.png,[害羞]",
            "xhsemoji_20.png,[嘿嘿]",
            "xhsemoji_21.png,[嘻嘻]"};

}
