package com.v5kf.client.ui.emojicon;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.v5kf.client.R;
import com.v5kf.client.ui.utils.UIUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;

/**
 * 微信表情正则判断：将对应表情文本替换成表情图标
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午4:37:46
 * @package com.v5kf.mcss.utils of MCSS-Native
 * @file ExpressionUtil.java 
 *
 */
public class QFaceExpressionUtil {
	
	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
    public static void dealExpression(Context context, SpannableString spannableString, Pattern patten, int start, int size) 
    		throws SecurityException, NoSuchFieldException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
    	Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            if (matcher.start() < start) {
                continue;
            }
            String value = QFaceiconUtil.getQQFaceImgName(key);
            Field field = R.drawable.class.getDeclaredField(value);
			int resId = Integer.parseInt(field.get(null).toString());		//通过上面匹配得到的字符串来生成图片资源id
//            int resId = UIUtil.getIdByName(context, "drawable", value);
//            int resId = QFaceiconUtil.getQQFaceImgId(key);
            if (resId != 0) {
            	Drawable drawable = context.getResources().getDrawable(resId);
                drawable.setBounds(2, 0, size, size);//这里设置图片的大小
                ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM); //通过图片资源id来得到bitmap，用一个ImageSpan来包装
                int end = matcher.start() + key.length();					//计算该图片名字的长度，也就是要替换的字符串的长度
                spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);	//将该图片替换字符串中规定的位置中
                if (end < spannableString.length()) {						//如果整个字符串还未验证完，则继续。。
                    dealExpression(context, spannableString, patten, end, size);
                }
                break;
            }
        }
    }
    
    
    /**
     * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
     * @param context
     * @param str
     * @return
     */
    public static SpannableString getExpressionString(Context context, CharSequence str, String zhengze, int size){
    	SpannableString spannableString = new SpannableString(str);
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);		//通过传入的正则表达式来生成一个pattern
        try {
            dealExpression(context, spannableString, sinaPatten, 0, size);
        } catch (Exception e) {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }
	
}
