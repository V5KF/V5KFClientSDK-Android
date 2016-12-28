package com.v5kf.client.ui.emojicon;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.SpannableString;
import android.widget.TextView;

public class QFaceiconUtil {
	
	public static Map<String, String> faceMap = new LinkedHashMap<String, String>();
	
	static {
		faceMap.put("/::)", "qf000");
		faceMap.put("/::~", "qf001");
		faceMap.put("/::B", "qf002");
		faceMap.put("/::|", "qf003");
		faceMap.put("/:8-)", "qf004");
		faceMap.put("/::<", "qf005");
		faceMap.put("/::$", "qf006"); //
		faceMap.put("/::X", "qf007");
		faceMap.put("/::Z", "qf008");
		faceMap.put("/::'(", "qf009");
		faceMap.put("/::-|", "qf010");
		faceMap.put("/::@", "qf011");
		faceMap.put("/::P", "qf012");
		faceMap.put("/::D", "qf013");
		faceMap.put("/::O", "qf014");
		faceMap.put("/::(", "qf015");
		faceMap.put("/::+", "qf016");
		faceMap.put("/:--b", "qf017");
		faceMap.put("/::Q", "qf018");
		faceMap.put("/::T", "qf019");
		faceMap.put("/:,@P", "qf020");
		faceMap.put("/:,@-D", "qf021");
		faceMap.put("/::d", "qf022");
		faceMap.put("/:,@o", "qf023");
		faceMap.put("/::g", "qf024");
		faceMap.put("/:|-)", "qf025");
		faceMap.put("/::!", "qf026");
		faceMap.put("/::L", "qf027");
		faceMap.put("/::>", "qf028");
		faceMap.put("/::,@", "qf029");
		faceMap.put("/:,@f", "qf030");
		faceMap.put("/::-S", "qf031");
		faceMap.put("/:?", "qf032");
		faceMap.put("/:,@x", "qf033");
		faceMap.put("/:,@@", "qf034");
		faceMap.put("/::8", "qf035");
		faceMap.put("/:,@!", "qf036");
		faceMap.put("/:!!!", "qf037");
		faceMap.put("/:xx", "qf038");
		faceMap.put("/:bye", "qf039");
		faceMap.put("/:wipe", "qf040");
		faceMap.put("/:dig", "qf041");
		faceMap.put("/:handclap", "qf042");
		faceMap.put("/:&-(", "qf043");
		faceMap.put("/:B-)", "qf044");
		faceMap.put("/:<@", "qf045");
		faceMap.put("/:@>", "qf046");
		faceMap.put("/::-O", "qf047");
		faceMap.put("/:>-|", "qf048");
		faceMap.put("/:P-(", "qf049");
		faceMap.put("/::'|", "qf050");
		faceMap.put("/:X-)", "qf051");
		faceMap.put("/::*", "qf052");
		faceMap.put("/:@x", "qf053");
		faceMap.put("/:8*", "qf054");
		faceMap.put("/:pd", "qf055");
		faceMap.put("/:<W>", "qf056");
		faceMap.put("/:beer", "qf057");
		faceMap.put("/:basketb", "qf058");
		faceMap.put("/:oo", "qf059");
		faceMap.put("/:coffee", "qf060");
		faceMap.put("/:eat", "qf061");
		faceMap.put("/:pig", "qf062");
		faceMap.put("/:rose", "qf063");
		faceMap.put("/:fade", "qf064");
		faceMap.put("/:showlove", "qf065");
		faceMap.put("/:heart", "qf066");
		faceMap.put("/:break", "qf067");
		faceMap.put("/:cake", "qf068");
		faceMap.put("/:li", "qf069");
		faceMap.put("/:bome", "qf070");
		faceMap.put("/:kn", "qf071");
		faceMap.put("/:footb", "qf072");
		faceMap.put("/:ladybug", "qf073");
		faceMap.put("/:shit", "qf074");
		faceMap.put("/:moon", "qf075");
		faceMap.put("/:sun", "qf076");
		faceMap.put("/:gift", "qf077");
		faceMap.put("/:hug", "qf078");
		faceMap.put("/:strong", "qf079");
		faceMap.put("/:weak", "qf080");
		faceMap.put("/:share", "qf081");
		faceMap.put("/:v", "qf082");
		faceMap.put("/:@)", "qf083");
		faceMap.put("/:jj", "qf084");
		faceMap.put("/:@@", "qf085");
		faceMap.put("/:bad", "qf086");
		faceMap.put("/:lvu", "qf087");
		faceMap.put("/:no", "qf088");
		faceMap.put("/:ok", "qf089");
		faceMap.put("/:love", "qf090");
		faceMap.put("/:<L>", "qf091");
		faceMap.put("/:jump", "qf092");
		faceMap.put("/:shake", "qf093");
		faceMap.put("/:<O>", "qf094");
		faceMap.put("/:circle", "qf095");
		faceMap.put("/:kotow", "qf096");
		faceMap.put("/:turn", "qf097");
		faceMap.put("/:skip", "qf098");
		faceMap.put("/:&>", "qf099");
		faceMap.put("/:#-0", "qf100");
		faceMap.put("/:hiphot", "qf101");
		faceMap.put("/:kiss", "qf102");
		faceMap.put("/:<&", "qf103");
		faceMap.put("/:oY", "qf104");
	};
	
//	public static Map<String, Integer> faceResIdMap = new LinkedHashMap<String, Integer>();
//	static {
//		faceResIdMap.put("/::)", R.drawable.qf000);
//		faceResIdMap.put("/::~", R.drawable.qf001);
//		faceResIdMap.put("/::B", R.drawable.qf002);
//		faceResIdMap.put("/::|", R.drawable.qf003);
//		faceResIdMap.put("/:8-)", R.drawable.qf004);
//		faceResIdMap.put("/::<", R.drawable.qf005);
//		faceResIdMap.put("/::$", R.drawable.qf006); //
//		faceResIdMap.put("/::X", R.drawable.qf007);
//		faceResIdMap.put("/::Z", R.drawable.qf008);
//		faceResIdMap.put("/::'(", R.drawable.qf009);
//		faceResIdMap.put("/::-|", R.drawable.qf010);
//		faceResIdMap.put("/::@", R.drawable.qf011);
//		faceResIdMap.put("/::P", R.drawable.qf012);
//		faceResIdMap.put("/::D", R.drawable.qf013);
//		faceResIdMap.put("/::O", R.drawable.qf014);
//		faceResIdMap.put("/::(", R.drawable.qf015);
//		faceResIdMap.put("/::+", R.drawable.qf016);
//		faceResIdMap.put("/:--b", R.drawable.qf017);
//		faceResIdMap.put("/::Q", R.drawable.qf018);
//		faceResIdMap.put("/::T", R.drawable.qf019);
//		faceResIdMap.put("/:,@P", R.drawable.qf020);
//		faceResIdMap.put("/:,@-D", R.drawable.qf021);
//		faceResIdMap.put("/::d", R.drawable.qf022);
//		faceResIdMap.put("/:,@o", R.drawable.qf023);
//		faceResIdMap.put("/::g", R.drawable.qf024);
//		faceResIdMap.put("/:|-)", R.drawable.qf025);
//		faceResIdMap.put("/::!", R.drawable.qf026);
//		faceResIdMap.put("/::L", R.drawable.qf027);
//		faceResIdMap.put("/::>", R.drawable.qf028);
//		faceResIdMap.put("/::,@", R.drawable.qf029);
//		faceResIdMap.put("/:,@f", R.drawable.qf030);
//		faceResIdMap.put("/::-S", R.drawable.qf031);
//		faceResIdMap.put("/:?", R.drawable.qf032);
//		faceResIdMap.put("/:,@x", R.drawable.qf033);
//		faceResIdMap.put("/:,@@", R.drawable.qf034);
//		faceResIdMap.put("/::8", R.drawable.qf035);
//		faceResIdMap.put("/:,@!", R.drawable.qf036);
//		faceResIdMap.put("/:!!!", R.drawable.qf037);
//		faceResIdMap.put("/:xx", R.drawable.qf038);
//		faceResIdMap.put("/:bye", R.drawable.qf039);
//		faceResIdMap.put("/:wipe", R.drawable.qf040);
//		faceResIdMap.put("/:dig", R.drawable.qf041);
//		faceResIdMap.put("/:handclap", R.drawable.qf042);
//		faceResIdMap.put("/:&-(", R.drawable.qf043);
//		faceResIdMap.put("/:B-)", R.drawable.qf044);
//		faceResIdMap.put("/:<@", R.drawable.qf045);
//		faceResIdMap.put("/:@>", R.drawable.qf046);
//		faceResIdMap.put("/::-O", R.drawable.qf047);
//		faceResIdMap.put("/:>-|", R.drawable.qf048);
//		faceResIdMap.put("/:P-(", R.drawable.qf049);
//		faceResIdMap.put("/::'|", R.drawable.qf050);
//		faceResIdMap.put("/:X-)", R.drawable.qf051);
//		faceResIdMap.put("/::*", R.drawable.qf052);
//		faceResIdMap.put("/:@x", R.drawable.qf053);
//		faceResIdMap.put("/:8*", R.drawable.qf054);
//		faceResIdMap.put("/:pd", R.drawable.qf055);
//		faceResIdMap.put("/:<W>", R.drawable.qf056);
//		faceResIdMap.put("/:beer", R.drawable.qf057);
//		faceResIdMap.put("/:basketb", R.drawable.qf058);
//		faceResIdMap.put("/:oo", R.drawable.qf059);
//		faceResIdMap.put("/:coffee", R.drawable.qf060);
//		faceResIdMap.put("/:eat", R.drawable.qf061);
//		faceResIdMap.put("/:pig", R.drawable.qf062);
//		faceResIdMap.put("/:rose", R.drawable.qf063);
//		faceResIdMap.put("/:fade", R.drawable.qf064);
//		faceResIdMap.put("/:showlove", R.drawable.qf065);
//		faceResIdMap.put("/:heart", R.drawable.qf066);
//		faceResIdMap.put("/:break", R.drawable.qf067);
//		faceResIdMap.put("/:cake", R.drawable.qf068);
//		faceResIdMap.put("/:li", R.drawable.qf069);
//		faceResIdMap.put("/:bome", R.drawable.qf070);
//		faceResIdMap.put("/:kn", R.drawable.qf071);
//		faceResIdMap.put("/:footb", R.drawable.qf072);
//		faceResIdMap.put("/:ladybug", R.drawable.qf073);
//		faceResIdMap.put("/:shit", R.drawable.qf074);
//		faceResIdMap.put("/:moon", R.drawable.qf075);
//		faceResIdMap.put("/:sun", R.drawable.qf076);
//		faceResIdMap.put("/:gift", R.drawable.qf077);
//		faceResIdMap.put("/:hug", R.drawable.qf078);
//		faceResIdMap.put("/:strong", R.drawable.qf079);
//		faceResIdMap.put("/:weak", R.drawable.qf080);
//		faceResIdMap.put("/:share", R.drawable.qf081);
//		faceResIdMap.put("/:v", R.drawable.qf082);
//		faceResIdMap.put("/:@)", R.drawable.qf083);
//		faceResIdMap.put("/:jj", R.drawable.qf084);
//		faceResIdMap.put("/:@@", R.drawable.qf085);
//		faceResIdMap.put("/:bad", R.drawable.qf086);
//		faceResIdMap.put("/:lvu", R.drawable.qf087);
//		faceResIdMap.put("/:no", R.drawable.qf088);
//		faceResIdMap.put("/:ok", R.drawable.qf089);
//		faceResIdMap.put("/:love", R.drawable.qf090);
//		faceResIdMap.put("/:<L>", R.drawable.qf091);
//		faceResIdMap.put("/:jump", R.drawable.qf092);
//		faceResIdMap.put("/:shake", R.drawable.qf093);
//		faceResIdMap.put("/:<O>", R.drawable.qf094);
//		faceResIdMap.put("/:circle", R.drawable.qf095);
//		faceResIdMap.put("/:kotow", R.drawable.qf096);
//		faceResIdMap.put("/:turn", R.drawable.qf097);
//		faceResIdMap.put("/:skip", R.drawable.qf098);
//		faceResIdMap.put("/:&>", R.drawable.qf099);
//		faceResIdMap.put("/:#-0", R.drawable.qf100);
//		faceResIdMap.put("/:hiphot", R.drawable.qf101);
//		faceResIdMap.put("/:kiss", R.drawable.qf102);
//		faceResIdMap.put("/:<&", R.drawable.qf103);
//		faceResIdMap.put("/:oY", R.drawable.qf104);
//	};

	public static void showQFaceText(TextView tv, CharSequence str, int size) {		
		// 判断QQ表情的正则表达式 
	    String qqfaceRegex = "/::\\)|/::~|/::B|/::\\||/:8-\\)|/::<|/::\\$|/::X|/::Z|/::" +
	    		"'\\(|/::-\\||/::@|/::P|/::D|/::O|/::\\(|/::\\+|/:--b|/::Q|/::T|/:,@P" +
	    		"|/:,@-D|/::d|/:,@o|/::g|/:\\|-\\)|/::!|/::L|/::>|/::,@|/:,@f|/::-S|/" +
	    		":\\?|/:,@x|/:,@@|/::8|/:,@!|/:!!!|/:xx|/:bye|/:wipe|/:dig|/:handclap" +
	    		"|/:&-\\(|/:B-\\)|/:<@|/:@>|/::-O|/:>-\\||/:P-\\(|/::'\\||/:X-\\)|/::" +
	    		"\\*|/:@x|/:8\\*|/:pd|/:<W>|/:beer|/:basketb|/:oo|/:coffee|/:eat|/:pi" +
	    		"g|/:rose|/:fade|/:showlove|/:heart|/:break|/:cake|/:li|/:bome|/:kn|/" +
	    		":footb|/:ladybug|/:shit|/:moon|/:sun|/:gift|/:hug|/:strong|/:weak|/:" +
	    		"share|/:v|/:@\\)|/:jj|/:@@|/:bad|/:lvu|/:no|/:ok|/:love|/:<L>|/:jump" +
	    		"|/:shake|/:<O>|/:circle|/:kotow|/:turn|/:skip|/:oY|/:#-0|/:hiphot|/:" +
	    		"kiss|/:<&|/:&>";
	    
		try {
			SpannableString spannableString = QFaceExpressionUtil
					.getExpressionString(tv.getContext(), str, qqfaceRegex, size);
			tv.setText(spannableString);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	public static SpannableString getQFaceText(Context context, CharSequence str, int size) {
		if (null == str || str.length() == 0) {
			return null;
		}
		
		// 判断QQ表情的正则表达式 
	    String qqfaceRegex = "/::\\)|/::~|/::B|/::\\||/:8-\\)|/::<|/::\\$|/::X|/::Z|/::" +
	    		"'\\(|/::-\\||/::@|/::P|/::D|/::O|/::\\(|/::\\+|/:--b|/::Q|/::T|/:,@P" +
	    		"|/:,@-D|/::d|/:,@o|/::g|/:\\|-\\)|/::!|/::L|/::>|/::,@|/:,@f|/::-S|/" +
	    		":\\?|/:,@x|/:,@@|/::8|/:,@!|/:!!!|/:xx|/:bye|/:wipe|/:dig|/:handclap" +
	    		"|/:&-\\(|/:B-\\)|/:<@|/:@>|/::-O|/:>-\\||/:P-\\(|/::'\\||/:X-\\)|/::" +
	    		"\\*|/:@x|/:8\\*|/:pd|/:<W>|/:beer|/:basketb|/:oo|/:coffee|/:eat|/:pi" +
	    		"g|/:rose|/:fade|/:showlove|/:heart|/:break|/:cake|/:li|/:bome|/:kn|/" +
	    		":footb|/:ladybug|/:shit|/:moon|/:sun|/:gift|/:hug|/:strong|/:weak|/:" +
	    		"share|/:v|/:@\\)|/:jj|/:@@|/:bad|/:lvu|/:no|/:ok|/:love|/:<L>|/:jump" +
	    		"|/:shake|/:<O>|/:circle|/:kotow|/:turn|/:skip|/:oY|/:#-0|/:hiphot|/:" +
	    		"kiss|/:<&|/:&>";
	    SpannableString spannableString = null;
		try {
			spannableString = QFaceExpressionUtil.getExpressionString(context, str, qqfaceRegex, size);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		return spannableString;
	}
	
	/** 
	 * 判断是否是QQ表情 
	 *  
	 * @param content 
	 * @return 
	 */ 
	public static boolean isQqFace(CharSequence content) { 
	    boolean result = false; 
	 
	    // 判断QQ表情的正则表达式 
	    String qqfaceRegex = "/::\\)|/::~|/::B|/::\\||/:8-\\)|/::<|/::\\$|/::X|/::Z|/::" +
	    		"'\\(|/::-\\||/::@|/::P|/::D|/::O|/::\\(|/::\\+|/:--b|/::Q|/::T|/:,@P" +
	    		"|/:,@-D|/::d|/:,@o|/::g|/:\\|-\\)|/::!|/::L|/::>|/::,@|/:,@f|/::-S|/" +
	    		":\\?|/:,@x|/:,@@|/::8|/:,@!|/:!!!|/:xx|/:bye|/:wipe|/:dig|/:handclap" +
	    		"|/:&-\\(|/:B-\\)|/:<@|/:@>|/::-O|/:>-\\||/:P-\\(|/::'\\||/:X-\\)|/::" +
	    		"\\*|/:@x|/:8\\*|/:pd|/:<W>|/:beer|/:basketb|/:oo|/:coffee|/:eat|/:pi" +
	    		"g|/:rose|/:fade|/:showlove|/:heart|/:break|/:cake|/:li|/:bome|/:kn|/" +
	    		":footb|/:ladybug|/:shit|/:moon|/:sun|/:gift|/:hug|/:strong|/:weak|/:" +
	    		"share|/:v|/:@\\)|/:jj|/:@@|/:bad|/:lvu|/:no|/:ok|/:love|/:<L>|/:jump" +
	    		"|/:shake|/:<O>|/:circle|/:kotow|/:turn|/:skip|/:oY|/:#-0|/:hiphot|/:" +
	    		"kiss|/:<&|/:&>";
	    Pattern p = Pattern.compile(qqfaceRegex, Pattern.CASE_INSENSITIVE);
	    Matcher m = p.matcher(content); 
	    if (m.find()) { 
	        result = true; 
	    } 
	    return result; 
	}
	
	public static String getQQFaceImgName(String qqFaceKey) {
		
		return faceMap.get(qqFaceKey);
	}

//	public static int getQQFaceImgId(String qqFaceKey) {
//		
//		return faceResIdMap.get(qqFaceKey);
//	}
}
