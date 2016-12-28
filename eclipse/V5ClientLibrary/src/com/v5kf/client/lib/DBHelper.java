package com.v5kf.client.lib;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;

public class DBHelper extends SQLiteOpenHelper {
	private static final int DB_VERSION = 5;
	// 数据库名称
    private static final String DB_NAME = "v5_client.db";
    // 表名称
    private String mTableName;
    // 创建表SQL语句 hit:机器人命中与否，[修改]增加字段message_id(text)
    private static final String CREATE_TBL_FMT = " create table if not exists %s " +
    		"(_id integer primary key autoincrement, session_start integer, w_id integer, message_id text, hit integer,state integer,direction integer ,json_content text, create_time integer)";
    // SQLiteDatabase实例
    private SQLiteDatabase db;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mTableName =  "v5_message_" + V5ClientConfig.getInstance(context).getV5VisitorId();
	}

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		mTableName =  "v5_message_" + V5ClientConfig.getInstance(context).getV5VisitorId();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		//db.execSQL(String.format(CREATE_TBL_FMT, mTableName));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.d("DbHelper", "[onUpgrade] " + oldVersion + " -> " + newVersion);
		if (oldVersion == 4 && newVersion == 5) {
			// 修改表，添加列
			try {
			Cursor cur = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
			while (cur.moveToNext()) {
				String tableName = cur.getString(cur
						.getColumnIndex("name"));
				if (tableName.startsWith("v5_message")) {
					Logger.w("DBHelper", "[onUpgrade] alter table:" + tableName);
					db.execSQL("alter table " + tableName + " add column w_id integer not null default '0'");
				}
			}
			cur.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (oldVersion == 3 && newVersion == 4) {
			// 修改表，添加列
			try {
			Cursor cur = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
			while (cur.moveToNext()) {
				String tableName = cur.getString(cur
						.getColumnIndex("name"));
				if (tableName.startsWith("v5_message")) {
					Logger.w("DBHelper", "[onUpgrade] alter table:" + tableName);
					db.execSQL("alter table " + tableName + " add column message_id text not null default ''");
				}
			}
			cur.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { // if (oldVersion <= 3)
			db.execSQL("drop table v5_message");
			db.execSQL(String.format(CREATE_TBL_FMT, mTableName));
		}
	}
	
	public boolean insert(V5Message message) {
		return this.insert(message, false);
	}
	
	public boolean insert(V5Message message, boolean force) {
		if (!V5ClientAgent.getInstance().cacheLocalMsg) {
			return false;
		}
		
		if (message == null || message.getDirection() == V5MessageDefine.MSG_DIR_RELATIVE_QUES ||
				message.getMessage_type() == V5MessageDefine.MSG_TYPE_WXCS ||
				(message.getMessage_type() == V5MessageDefine.MSG_TYPE_CONTROL)) { //[修改] && ((V5ControlMessage)message).getCode() != 1
			// 不保存[相关问题]和非[转人工客服]的控制消息 -> [修改]不保存控制消息
			Logger.d("v5client", "DbHelper insert message type not accept");
			return false;
		}
		String json_content = null;
		try {
			json_content = message.toJson();
			Logger.d("DBHelper", "json_content:" + json_content);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return false;
		}
		
		if (!force) {
			// 发出消息利用msg_id保存，接收消息利用message_id保存
			if (message.getMsg_id() != 0) { // && message.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER
				if (message.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) { // 过滤开场白提问消息，不做保存
					Logger.w("v5client", "DbHelper openQuestion message NO");
					return false;
				}
				if (message.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) { // 仅发出的消息使用自定义ID
					message.setMessage_id(String.valueOf(message.getMsg_id()));
				}
			}
				
			if (hasMessageId(message.getMessage_id())) {
//				Logger.d("v5client", "DbHelper already has message:" + message.getMessage_id());
				if (message.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
					updateMessage(message.getMessage_id(), json_content, message);
				}
				return false;
			}
		}
		Logger.d("DBHelper", "DbHelper insert message:" + json_content);
		ContentValues values = new ContentValues();
		values.put("w_id", message.getW_id());
		values.put("message_id", message.getMessage_id());
		values.put("hit", message.getHit());
		values.put("state", message.getState());
		values.put("direction", message.getDirection());
		values.put("session_start", message.getSession_start());
		// [修改]create_time为插入时间
		if (message.getCreate_time() == 0) {
			message.setCreate_time(V5Util.getCurrentLongTime() / 1000);
		}
		//values.put("create_time", message.getCreate_time());
		if (message.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID && message.getMsg_id() > 0) {
			// 开场问题提前1s
			values.put("create_time", message.getCreate_time() - 1);
		} else {
			values.put("create_time", message.getCreate_time());
		}
		values.put("json_content", json_content);
		
		long id = insert(values);
		Logger.w("v5client", "DbHelper insert id:" + id);
		message.setId(id);
		return true;
	}
	
	/**
	 * 更新指定ID的create_time
	 * @param message_id
	 */
	public void updateMessage(String message_id, String json_content, V5Message msg) {
		// 获得SQLiteDatabase实例
        SQLiteDatabase db = getWritableDatabase();
        // 插入
        ContentValues values = new ContentValues();
        values.put("create_time", msg.getCreate_time());
        values.put("json_content", json_content);
        db.update(mTableName, values, "message_id=?", new String[]{message_id});
        Logger.d("DBHelper", "updateMessage message_id:" + message_id);
        // 关闭
        close();
	}
	
	/*
     * 插入方法
     */
    protected long insert(ContentValues values) {
        // 获得SQLiteDatabase实例
        SQLiteDatabase db = getWritableDatabase();
        
        // 插入
        long id = db.insert(mTableName, null, values);
        Logger.d("DBHelper", "Insert ID:" + id);
        // 关闭
        close();
        return id;
    }
    
    /*
     * 查询全部message
     */
    protected Cursor queryAll() {
        // 获得SQLiteDatabase实例
        SQLiteDatabase db = getWritableDatabase();
        // 查询获得Cursor
        Cursor c = db.rawQuery("select * from " + mTableName + " order by create_time asc", null);
        return c;
    }
    
    /**
     * 查询session_start
     * @return
     */
    protected Cursor querySession() {
    	// 获得SQLiteDatabase实例
        SQLiteDatabase db = getWritableDatabase();
        // 查询获得Cursor
        Cursor c = db.rawQuery("select session_start from " + mTableName + " group by session_start order by session_start desc", null);
        return c;
    }

    /**
     * 查询指定session_start的消息
     * @param session_start
     * @return
     */
    public Cursor queryMessage(long session_start) {
    	// 获得SQLiteDatabase实例
    	SQLiteDatabase db = getWritableDatabase();
    	// 查询获得Cursor
    	Cursor c = db.rawQuery("select * from " + mTableName + " where session_start=" + session_start + " order by create_time desc, direction desc", null);
    	return c;
    }
    
    /**
     * 数据库是否存在消息记录
     * @return
     */
    public boolean hasMessages() {
    	SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("select _id from " + mTableName, null);
        if (c.moveToNext()) {
        	if(c != null && !c.isClosed()){
                c.close();
            }
        	close();
        	return true;
        } else {
        	if(c != null && !c.isClosed()){
                c.close();
            }
        	close();        	
        	return false;
        }
    }

    public boolean hasMessageContent(String jsContent) {
    	SQLiteDatabase db = getWritableDatabase();
    	Cursor c = db.rawQuery("select _id from " + mTableName + 
    			" where json_content='" + jsContent.replaceAll("'", "''") + "'", null);
    	if (c.moveToNext()) {
    		if(c != null && !c.isClosed()){
                c.close();
            }
    		close();
    		return true;
    	} else {
    		if(c != null && !c.isClosed()){
                c.close();
            }
    		close();        	
    		return false;
    	}
    }
    
    public boolean hasMessageId(String messageId) {
    	if (messageId == null) {
    		return false;
    	}
    	SQLiteDatabase db = getWritableDatabase();
    	Cursor c = db.rawQuery("select _id from " + mTableName + 
    			" where message_id='" + messageId + "'", null);
    	if (c.moveToNext()) {
    		if(c != null && !c.isClosed()){
                c.close();
            }
    		close();
    		return true;
    	} else {
    		if(c != null && !c.isClosed()){
                c.close();
            }
    		close();        	
    		return false;
    	}
    }
    
    /**
     * 查询指定顺序的会话的所有消息
     * @deprecated
     * @param msgs
     * @param order
     */
    public void querySession(List<V5Message> msgs, int order) {
    	if (msgs == null) {
    		return;
    	}
    	Cursor curSession = querySession();
    	if (curSession.getCount() < order || order < 1) {
    		return;
    	}
    	long sessionStart = 0;
    	if (curSession.moveToPosition(order - 1)) {
    		Logger.d("DBHelper", "moveToPosition：" + order);
    		sessionStart = curSession.getLong(curSession.
    				getColumnIndex("session_start"));
    	}
    	curSession.close();
    	Logger.d("DBHelper", "sessionStart：" + sessionStart);
    	
    	Cursor cur = queryMessage(sessionStart);
    	while (cur.moveToNext()) {
			int hit = cur.getInt(cur
					.getColumnIndex("hit"));
			int state = cur.getInt(cur
					.getColumnIndex("state"));
			long wid = cur.getLong(cur
					.getColumnIndex("w_id"));
			long session_start = cur.getLong(cur
					.getColumnIndex("session_start"));
			String content = cur.getString(cur
						.getColumnIndex("json_content"));
			if (content == null || content.isEmpty()) {
				continue;
			}
			try {
				Logger.d("DBHelper", "[querySession1] json_content:" + content);
				JSONObject jsonMsg = new JSONObject(content);
				V5Message message = V5MessageManager.getInstance().receiveMessage(jsonMsg);
				message.setHit(hit);
				message.setState(state);
				message.setW_id(wid);
				message.setSession_start(session_start);
				msgs.add(message);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    	if(cur != null && !cur.isClosed()){
            cur.close();
        }
    	close();
    }

    /**
     * 查询指定位置起始的指定数量消息
     * @param msgs
     * @param offset
     * @param size
     */
	public boolean querySession(List<V5Message> msgs, int offset, int size) {
		Logger.d("DBHelper", "[querySession] offset:" + offset + " size:" + size);
		// 获得SQLiteDatabase实例
    	SQLiteDatabase db = getReadableDatabase();
    	// 查询获得Cursor
//    	Cursor cur = db.rawQuery("select * from " + mTableName + " order by _id desc, create_time desc," +
//    			" direction desc limit " + size + " offset " + offset, null);
    	Cursor cur = db.rawQuery("select * from " + mTableName + " order by _id desc" +
    			" limit " + size + " offset " + offset, null);
    	boolean finish = cur.getCount() < size ? true : false;
    	while (cur.moveToNext()) {
			int hit = cur.getInt(cur
					.getColumnIndex("hit"));
			int state = cur.getInt(cur
					.getColumnIndex("state"));
			long session_start = cur.getLong(cur
					.getColumnIndex("session_start"));
			long wid = cur.getLong(cur
					.getColumnIndex("w_id"));
			String content = cur.getString(cur
						.getColumnIndex("json_content"));
			long createTime = cur.getLong(cur
					.getColumnIndex("create_time"));
			if (content == null || content.isEmpty()) {
				continue;
			}
			try {
//				Logger.d("DBHelper", "[querySession] create_time:" + createTime + " json_content:" + content);
				JSONObject jsonMsg = new JSONObject(content);
				V5Message message = V5MessageManager.getInstance().receiveMessage(jsonMsg);
				message.setHit(hit);
				message.setState(state);
				message.setW_id(wid);
				message.setSession_start(session_start);
				message.setCreate_time(createTime);
				msgs.add(message);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    	if(cur != null && !cur.isClosed()){
            cur.close();
        }
    	close();
    	
    	return finish;
	}
    
	/**
	 * @deprecated
	 * @param msgs
	 */
    public void queryAll(List<V5Message> msgs) {
    	if (msgs == null) {
    		return;
    	}
    	Cursor cur = queryAll();
    	while (cur.moveToNext()) {
			int hit = cur.getInt(cur
					.getColumnIndex("hit"));
			int state = cur.getInt(cur
					.getColumnIndex("state"));
			long wid = cur.getLong(cur
					.getColumnIndex("w_id"));
			long session_start = cur.getLong(cur
					.getColumnIndex("session_start"));
			String content = cur.getString(cur
						.getColumnIndex("json_content"));
			if (content == null || content.isEmpty()) {
				continue;
			}
			try {
//				Logger.d("DBHelper", "[queryAll] json_content:" + content);
				JSONObject jsonMsg = new JSONObject(content);
				V5Message message = V5MessageManager.getInstance().receiveMessage(jsonMsg);
				message.setHit(hit);
				message.setState(state);
				message.setW_id(wid);
				message.setSession_start(session_start);
				msgs.add(message);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
    	cur.close();
    	close();
    }
    
    /*
     * 删除方法
     */
    public void del(int id) {
        if(db == null) {
            // 获得SQLiteDatabase实例
            db = getWritableDatabase();           
        }
        // 执行删除
        db.delete(mTableName, "_id=?", new String[]{String.valueOf(id)});
        db.close();
        db = null;
    }
    
    /**
     * 清空数据
     */
    public void delAll() {
    	if(db == null) {
            // 获得SQLiteDatabase实例
            db = getWritableDatabase();           
        }
        // 执行删除
    	db.delete(mTableName, null, null);
//    	db.close();
//    	db = null;
    	close();
    }
    
    /*
     * 关闭数据库
     */
    @Override
    public void close() {
    	super.close();
        if(db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }
    
    /*
     * 更新表名
     */
    public void setTableName(String table) {
    	this.mTableName = table;
    	createTable(table);
    	Logger.i("DBHelper", "Use table:" + table);
    }

	private void createTable(String table) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(String.format(CREATE_TBL_FMT, table));
		db.close();
	}
}
