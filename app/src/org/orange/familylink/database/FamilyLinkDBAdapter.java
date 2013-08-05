package org.orange.familylink.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.orange.familylink.data.Contact;
import org.orange.familylink.data.Message;
import org.orange.familylink.data.MessageLogRecord;
import org.orange.familylink.data.MessageLogRecord.Direction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库操作类，有contacts和messages表
 * @author OrangeTeam
 *
 */
public class FamilyLinkDBAdapter {
	SQLiteDatabase db;
	private final Context context;
	FamilyLinkDBOpenHelper dbHelper;

	/**
	 * 构造方法实例化数据库
	 * @param mContext
	 */
	public FamilyLinkDBAdapter(Context mContext){
		context = mContext;
		dbHelper = new FamilyLinkDBOpenHelper(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
	}

	/**
	 * 数据库构建的类
	 * @author OrangeTeam
	 *
	 */
	static class FamilyLinkDBOpenHelper extends SQLiteOpenHelper{
		public FamilyLinkDBOpenHelper(Context context, String name, CursorFactory factory, int version){
			super(context, name, factory, version);
		}

		public FamilyLinkDBOpenHelper(Context context){
			super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
		}

		/*
		 * 在数据库构建中系统调用的方法，用于执行sql语句，生成表和索引等
		 * (non-Javadoc)
		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
		 */
		@Override
		public void onCreate(SQLiteDatabase arg0) {
			arg0.execSQL(Contract.Contacts.CONTACTS_TABLE_CREATE);
			arg0.execSQL(Contract.Messages.MESSAGES_TABLE_CREATE);
			arg0.execSQL(Contract.Messages.INDEX_CREATE);
		}

		/*
		 * 在数据库构建中系统调用的方法，用于升级数据库
		 * (non-Javadoc)
		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			Log.w("FamilyLinkDBAdapter", "Upgrading from version " + arg1 + " to " + arg2 + ", which will destroy all data");

			arg0.execSQL("DROP TABLE IF EXISTS " + Contract.DATABASE_CONTACTS_TABLE);
			arg0.execSQL("DROP TABLE IF EXISTS " + Contract.DATABASE_MESSAGES_TABLE);


			onCreate(arg0);
		}
	}

	/**
	 * 打开一个可写的数据库
	 * @throws SQLiteException
	 */
	public void open() throws SQLiteException{
		try{
			db = dbHelper.getWritableDatabase();
		}catch(SQLiteException e){
			db = dbHelper.getReadableDatabase();
		}
	}

	/**
	 * 判断数据库是否打开
	 * @return 如果返回值是true，则说明数据库已经打开
	 */
	public boolean isOpen(){
		return (db != null && db.isOpen());
	}

	/**
	 * 关闭数据库
	 */
	public void close(){
		if(db.isOpen())
			db.close();
	}

	/**
	 * 用于向contacts表中插入数据
	 * @param systemId
	 * @param systemLookupKey
	 */
	public void insertContact(long systemId, String systemLookupKey){
		//如果提供的LOOKUPKEY为空就结束插入
		if(systemLookupKey == null)return;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_ID, systemId);
		contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY, systemLookupKey);
		//先查询一下contacts表中是否已经有这个LOOKUPKEY，有就不再插入
		Cursor cursor = db.query(Contract.DATABASE_CONTACTS_TABLE, null,
				Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY + " = '" + systemLookupKey + "'",
				null, null, null, null);
		if(cursor.getCount() == 0)
			db.insert(Contract.DATABASE_CONTACTS_TABLE, null, contentValues);
	}

	/**
	 * 用于向contacts表中插入数据
	 * @param contact
	 */
	public void insertContact(Contact contact){
		//如果提供的联系人是空就结束插入
		if(contact == null)return;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_ID, contact.getSystemId());
		contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY, contact.getSystemLookupKey());
		//先查询一下contacts表中是否已经有这个LOOKUPKEY，有就不再插入
		Cursor cursor = db.query(Contract.DATABASE_CONTACTS_TABLE, null,
				Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY + " = '" + contact.getSystemLookupKey() + "'",
				null, null, null, null);
		if(cursor.getCount() == 0)
			db.insert(Contract.DATABASE_CONTACTS_TABLE, null, contentValues);
	}

	/***
	 * 用于向contacts表中插入数据
	 * @param contacts
	 */
	public void inserListsContacts(List<Contact> contacts){
		//如果联系人是空就停止插入操作
		if(contacts == null || contacts.isEmpty())return;
		Cursor cursor = null;
		ContentValues contentValues = new ContentValues();
		for(Contact aContact : contacts){
			contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_ID, aContact.getSystemId());
			contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY, aContact.getSystemLookupKey());
			cursor = db.query(Contract.DATABASE_CONTACTS_TABLE, null,
					Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY + " = '" + aContact.getSystemLookupKey() + "'",
					null, null, null, null);
			if(cursor.getCount() == 0)
				db.insert(Contract.DATABASE_CONTACTS_TABLE, null, contentValues);
		}
	}

	/**
	 * 用于向messages表中插入数据
	 * @param messageLogRecord
	 */
	public void insertMessage(MessageLogRecord messageLogRecord){
		//如果消息日志为空就停止插入操作
		if(messageLogRecord == null)return;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Contract.Messages.COLUMN_NAME_CONTACT_ID, messageLogRecord.getContact().getId());
		contentValues.put(Contract.Messages.COLUMN_NAME_ADDRESS, messageLogRecord.getAddress());
		contentValues.put(Contract.Messages.COLUMN_NAME_TIME, messageLogRecord.getDate().getTime());
		contentValues.put(Contract.Messages.COLUMN_NAME_READ, messageLogRecord.hasRead());
		contentValues.put(Contract.Messages.COLUMN_NAME_DIRECTION, messageLogRecord.getDirection().toString());
		contentValues.put(Contract.Messages.COLUMN_NAME_BODY, messageLogRecord.getMessage().getBody());
		contentValues.put(Contract.Messages.COLUMN_NAME_CODE, messageLogRecord.getMessage().getCode());
		db.insert(Contract.DATABASE_MESSAGES_TABLE, null, contentValues);
	}

	/**
	 * 用于向messages表中插入数据
	 * @param messageLogRecords
	 */
	public void insertListsMessages(List<MessageLogRecord> messageLogRecords){
		if(messageLogRecords == null || messageLogRecords.isEmpty())return;
		ContentValues contentValues = new ContentValues();
		for(MessageLogRecord aMessageLogRecord : messageLogRecords){
			contentValues.put(Contract.Messages.COLUMN_NAME_CONTACT_ID, aMessageLogRecord.getContact().getId());
			contentValues.put(Contract.Messages.COLUMN_NAME_ADDRESS, aMessageLogRecord.getAddress());
			contentValues.put(Contract.Messages.COLUMN_NAME_TIME, aMessageLogRecord.getDate().getTime());
			contentValues.put(Contract.Messages.COLUMN_NAME_READ, aMessageLogRecord.hasRead());
			contentValues.put(Contract.Messages.COLUMN_NAME_DIRECTION, aMessageLogRecord.getDirection().toString());
			contentValues.put(Contract.Messages.COLUMN_NAME_BODY, aMessageLogRecord.getMessage().getBody());
			contentValues.put(Contract.Messages.COLUMN_NAME_CODE, aMessageLogRecord.getMessage().getCode());
			db.insert(Contract.DATABASE_MESSAGES_TABLE, null, contentValues);
		}
	}

	/**
	 * 删除一条联系人记录
	 * @param contact
	 * @return 删除是否成功，进行的删除的联系人参数为空也表示删除失败
	 */
	public boolean deleteContact(Contact contact){
		if(contact == null)return false;
		long mId = contact.getId();
		return (db.delete(Contract.DATABASE_CONTACTS_TABLE, Contract.Contacts._ID + " = " + mId, null) > 0);
	}

	/**
	 *  删除多个联系人记录
	 * @param contacts
	 * @return 表示删除是否成功，进行的删除的联系人参数为空也表示删除失败
	 */
	public boolean deleteListsContacts(List<Contact> contacts){
		if(contacts == null || contacts.isEmpty())return false;
		long mId;
		for(Contact aContact : contacts){
			mId = aContact.getId();
			return (db.delete(Contract.DATABASE_CONTACTS_TABLE, Contract.Contacts._ID + " = " + mId, null) > 0);
		}
		return true;
	}

	/**
	 *删除一条信息记录
	 * @param messageLogRecord
	 * @return 表示删除是否成功，但消息日志参数为空也表示删除不成功
	 */
	public boolean deleteMessage(MessageLogRecord messageLogRecord){
		if(messageLogRecord == null)return false;
		long mId = messageLogRecord.getId();
		return (db.delete(Contract.DATABASE_MESSAGES_TABLE, Contract.Messages._ID + " = " + mId, null) > 0);
	}

	/**
	 * 删除多条信息记录
	 * @param messageLogRecords
	 * @return 表示删除是否成功，但消息日志参数是空也表示删除不成功
	 */
	public boolean deleteListsMessages(List<MessageLogRecord> messageLogRecords){
		if(messageLogRecords == null || messageLogRecords.isEmpty())return false;
		long mId;
		for(MessageLogRecord aMessageLogRecord : messageLogRecords){
			mId = aMessageLogRecord.getId();
			return (db.delete(Contract.DATABASE_MESSAGES_TABLE, Contract.Messages._ID + " = " + mId, null) > 0);
		}
		return true;
	}

	/**
	 * 更新联系人
	 * @param contact
	 */
	public void updateContact(Contact contact){
		//参数为空是停止更新
		if(contact == null)return;
		ContentValues contentValues = new ContentValues();
		long mId = contact.getId();
		//先把游标指向要更新的那条记录
		Cursor cursor = db.query(Contract.DATABASE_CONTACTS_TABLE, null,
				Contract.Contacts._ID + " = " + mId, null, null, null, null);
		cursor.moveToFirst();
		//对应的字段如果不相同就更新
		if(cursor.getLong(cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_SYSTEM_ID)) != contact.getSystemId()){
			contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_ID, contact.getSystemId());
			db.update(Contract.DATABASE_CONTACTS_TABLE, contentValues, Contract.Contacts._ID + " = " + mId, null);
			contentValues.clear();
		}
		if(!cursor.getString(cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY))
				.equals(contact.getSystemLookupKey())){
			contentValues.put(Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY, contact.getSystemLookupKey());
			db.update(Contract.DATABASE_CONTACTS_TABLE, contentValues, Contract.Contacts._ID + " = " + mId, null);
			contentValues.clear();
		}
	}

	/**
	 * 更新信息记录
	 * @param messageLogRecord
	 */
	public void updateMessage(MessageLogRecord messageLogRecord){
		if(messageLogRecord == null)return;
		ContentValues contentValues = new ContentValues();
		long mId = messageLogRecord.getId();
		//先把游标指向要更新的那条记录
		Cursor cursor = db.query(Contract.DATABASE_MESSAGES_TABLE, null,
				Contract.Messages._ID + " = " + mId, null, null, null, null);
		cursor.moveToFirst();
		if(cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID))
				!= messageLogRecord.getContact().getId()){
			contentValues.put(Contract.Messages.COLUMN_NAME_CONTACT_ID, messageLogRecord.getContact().getId());
			db.update(Contract.DATABASE_MESSAGES_TABLE, contentValues, Contract.Messages._ID + " = " + mId, null);
			contentValues.clear();
		}
		if(!cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS))
				.equals(messageLogRecord.getAddress())){
			contentValues.put(Contract.Messages.COLUMN_NAME_ADDRESS, messageLogRecord.getAddress());
			db.update(Contract.DATABASE_MESSAGES_TABLE, contentValues, Contract.Messages._ID + " = " + mId, null);
			contentValues.clear();
		}
		if(cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_TIME))
				!= messageLogRecord.getDate().getTime()){
			contentValues.put(Contract.Messages.COLUMN_NAME_TIME, messageLogRecord.getDate().getTime());
			db.update(Contract.DATABASE_MESSAGES_TABLE, contentValues, Contract.Messages._ID + " = " + mId, null);
			contentValues.clear();
		}
		if(cursor.getInt(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_READ))
				!= Integer.parseInt(messageLogRecord.hasRead().toString())){
			contentValues.put(Contract.Messages.COLUMN_NAME_READ, messageLogRecord.hasRead());
			db.update(Contract.DATABASE_MESSAGES_TABLE, contentValues, Contract.Messages._ID + " = " + mId, null);
			contentValues.clear();
		}
		if(cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_DIRECTION))
				.equals(messageLogRecord.getDirection().toString())){
			contentValues.put(Contract.Messages.COLUMN_NAME_DIRECTION, messageLogRecord.getDirection().toString());
			db.update(Contract.DATABASE_MESSAGES_TABLE, contentValues, Contract.Messages._ID + " = " + mId, null);
			contentValues.clear();
		}
		if(!cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY))
				.equals(messageLogRecord.getMessage().getBody())){
			contentValues.put(Contract.Messages.COLUMN_NAME_BODY, messageLogRecord.getMessage().getBody());
			db.update(Contract.DATABASE_MESSAGES_TABLE, contentValues, Contract.Messages._ID + " = " + mId, null);
			contentValues.clear();
		}
		if(cursor.getInt(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE))
				!= messageLogRecord.getMessage().getCode()){
			contentValues.put(Contract.Messages.COLUMN_NAME_CODE, messageLogRecord.getMessage().getCode());
			db.update(Contract.DATABASE_MESSAGES_TABLE, contentValues, Contract.Messages._ID + " = " + mId, null);
			contentValues.clear();
		}
	}

	/**
	 * 查询一条联系人记录
	 * @param where 条件子句
	 * @param order to sort
	 * @return 返回一条联系人记录
	 * @throws SQLException 表示未能从数据库中获得查询结果
	 */
	public Contact getContact(String where, String order) throws SQLException{
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		contacts = getListsContacts(where, order);
		return contacts.get(0);
	}

	/**
	 * 查询多条联系人记录
	 * @param where 条件子句
	 * @param order to sort
	 * @return 返回多条联系人记录
	 * @throws SQLException 表示未能从数据库中获得查询结果
	 */
	public ArrayList<Contact> getListsContacts(String where, String order) throws SQLException{
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		Contact contact = new Contact();
		Cursor cursor = db.query(Contract.DATABASE_CONTACTS_TABLE, null, where, null, null, null, order);
		if(cursor.getCount() == 0 || !cursor.moveToFirst()){
			throw new SQLException("No record found from database");
		}else{
			for(int i = 0; i <= cursor.getCount(); i++){
				cursor.moveToPosition(i);
				long mId = cursor.getLong(cursor.getColumnIndex(Contract.Contacts._ID));
				long mSystemId = cursor.getLong(cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_SYSTEM_ID));
				String mSystemLookupKey = cursor.getString(cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_SYSTEM_LOOKUP_KEY));
				contact.setId(mId);
				contact.setSystemId(mSystemId);
				contact.setSystemLookupKey(mSystemLookupKey);
				contacts.add(contact.clone());
			}
		}
		return contacts;
	}

	/**
	 * 查询一条信息记录
	 * @param where 条件子句
	 * @param order to sort
	 * @return 返回一条信息记录
	 * @throws SQLException 没有查询成功
	 */
	public MessageLogRecord getMessage(String where, String order) throws SQLException{
		ArrayList<MessageLogRecord> messageLogRecords = new ArrayList<MessageLogRecord>();
		messageLogRecords = getListsMessages(where, order);
		return messageLogRecords.get(0);
	}

	/**
	 * 查询多条信息记录
	 * @param where 条件子句
	 * @param order to sort
	 * @return 返回多条信息记录
	 * @throws SQLException 没有查询成功
	 */
	public ArrayList<MessageLogRecord> getListsMessages(String where, String order) throws SQLException{
		ArrayList<MessageLogRecord> messageLogRecords = new ArrayList<MessageLogRecord>();
		MessageLogRecord messageLogRecord = new MessageLogRecord();
		Message aMessage = new Message();
		Cursor cursor = db.query(Contract.DATABASE_MESSAGES_TABLE, null, where, null, null, null, order);
		if(cursor.getCount() == 0 || !cursor.moveToFirst()){
			throw new SQLException("No record found from database");
		}else{
			for(int i= 0; i <= cursor.getCount(); i++){
				cursor.moveToPosition(i);
				long mId = cursor.getLong(cursor.getColumnIndex(Contract.Messages._ID));
				long mContactId = cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID));
				String mAddress = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS));
				long mTime = cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_TIME));
				int mRead = cursor.getInt(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_READ));
				String mDirection = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_DIRECTION));
				String mBody = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY));
				int mCode = cursor.getInt(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE));
				messageLogRecord.setId(mId);
				messageLogRecord.setContact(getContact(Contract.Contacts._ID + " = " + mContactId, null));
				messageLogRecord.setAddress(mAddress);
				messageLogRecord.setDate(new Date(mTime));
				messageLogRecord.setHasRead(Boolean.parseBoolean(String.valueOf(mRead)));
				messageLogRecord.setDirection(Direction.valueOf(mDirection));
				messageLogRecord.setMessage(aMessage.setBody(mBody).setCode(mCode));
				messageLogRecords.add(messageLogRecord.clone());
			}
		}
		return messageLogRecords;
	}
}
