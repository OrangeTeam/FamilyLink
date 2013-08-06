package org.orange.familylink.database;

import java.util.HashMap;

import org.orange.familylink.database.FamilyLinkDBAdapter.FamilyLinkDBOpenHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 信息的内容提供器
 * @author OrangeTeam
 *
 */
public class MessagesProvider extends ContentProvider{
	//所用的数据库创建的类生成一个对象对数据库进行操作
		private FamilyLinkDBOpenHelper dbHelper;

		//为查询设置一个投影映射
		private static HashMap<String, String> mMessagesProjectionMap;

		//用常量‘1’代表uri为messages表的集合
		private static final int MESSAGES = 1;
		//用常量‘2’代表uri为messages表中的记录
		private static final int MESSAGE_ID = 2;

		//用UriMatcher类来识别uri类型
		private static final UriMatcher mUriMatcher;

		static{
			//UriMatcher类的实例化
			mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			//添加uri与常量进行匹配
			mUriMatcher.addURI(Contract.Messages.AUTHORITY, "messages", MESSAGES);
			mUriMatcher.addURI(Contract.Messages.AUTHORITY, "messages/#", MESSAGE_ID);

			//查询映射的添加
			mMessagesProjectionMap = new HashMap<String, String>();
			mMessagesProjectionMap.put(Contract.Messages._ID, Contract.Messages._ID);
			mMessagesProjectionMap.put(Contract.Messages.COLUMN_NAME_CONTACT_ID, Contract.Messages.COLUMN_NAME_CONTACT_ID);
			mMessagesProjectionMap.put(Contract.Messages.COLUMN_NAME_ADDRESS, Contract.Messages.COLUMN_NAME_ADDRESS);
			mMessagesProjectionMap.put(Contract.Messages.COLUMN_NAME_TIME, Contract.Messages.COLUMN_NAME_TIME);
			mMessagesProjectionMap.put(Contract.Messages.COLUMN_NAME_STATUS, Contract.Messages.COLUMN_NAME_STATUS);
			mMessagesProjectionMap.put(Contract.Messages.COLUMN_NAME_BODY, Contract.Messages.COLUMN_NAME_BODY);
			mMessagesProjectionMap.put(Contract.Messages.COLUMN_NAME_CODE, Contract.Messages.COLUMN_NAME_CODE);
		}

		/**
		 * 系统调用这个方法时实例化数据库
		 */
		@Override
		public boolean onCreate() {
			dbHelper = new FamilyLinkDBOpenHelper(getContext());
			return true;
		}

		/**
		 * 对messages表的删除操作
		 */
		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			//条件子句
			String finalWhere;
			//用来保存删除的行
			int count;

			switch(mUriMatcher.match(uri)){
			case MESSAGES: //对集合类型的删除操作
				count = db.delete(Contract.DATABASE_MESSAGES_TABLE,
						selection,
						selectionArgs
				);
				break;
			case MESSAGE_ID: //对记录类型的删除操作
				finalWhere = Contract.Messages._ID + " = " + uri.getPathSegments().get(1);
				if(selection != null){
					finalWhere = finalWhere + " AND " + selection;
				}
				count = db.delete(Contract.DATABASE_MESSAGES_TABLE,
						finalWhere,
						selectionArgs
				);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			//设置通知
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		}

		/**
		 * 返回uri类型
		 */
		@Override
		public String getType(Uri arg0) {
			switch(mUriMatcher.match(arg0)){
			case MESSAGES :
				//返回集合类型的mime
				return Contract.Messages.MESSAGES_TYPE;
			case MESSAGE_ID :
				//返回单项类型的mime
				return Contract.Messages.MESSAGES_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI" + arg0);
			}
		}

		/**
		 * 对messages表的插入操作，数据库中messages表的各个字段（contactId long,address String,time long,status String,
		 * body String,code int）操作时应该注意数据的转换，contactId不是直接放入的是通过Contacts类中的getId方法得到的，time是Date.getTime()，
		 * status是要枚举类型转换为String型，body和code是从Messsages类中来的。
		 */
		@Override
		public Uri insert(Uri uri, ContentValues values) {
			//当uri不是代表contacts这个表时为不合法的uri
			if(mUriMatcher.match(uri) != MESSAGES){
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			SQLiteDatabase db = dbHelper.getWritableDatabase();
			long rowId = db.insert(
					Contract.DATABASE_MESSAGES_TABLE,
					null,
					values
			);
			//如果插入操作成功返回插入的 uri，且进行通知
			if(rowId > 0){
				Uri messageUri = ContentUris.withAppendedId(Contract.Messages.MESSAGES_URI, rowId);
				getContext().getContentResolver().notifyChange(messageUri, null);
				return messageUri;
			}
			throw new SQLException("Failed to insert row into " + uri);
		}

		/**
		 * 对messages表的查询操作数据库中messages表的各个字段（contactId long,address String,time long,status String,
		 * body String,code int）操作时应该注意数据的转换，得到的各个字段是要放到Contacts和Messages类中用的，那么time就要转换成Date,
		 * status要转成枚举，因为这里返回的是Cursor所以操作要由调用者自己
		 * 转换，而数据库操作类中的放回是放到了相应的类中返回的，所以调用者没有转换操作。
		 */
		@Override
		public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			switch(mUriMatcher.match(uri)){
			case MESSAGES:
				//设置查询的表
				queryBuilder.setTables(Contract.DATABASE_MESSAGES_TABLE);
				//设置查询映射
				queryBuilder.setProjectionMap(mMessagesProjectionMap);
				break;
			case MESSAGE_ID:
				queryBuilder.setTables(Contract.DATABASE_MESSAGES_TABLE);
				queryBuilder.setProjectionMap(mMessagesProjectionMap);
				queryBuilder.appendWhere(Contract.Messages._ID + " = " +
				uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			String orderBy;
			if(TextUtils.isEmpty(sortOrder)){
				orderBy = Contract.Messages.MESSAGES_DEFAULT_SORT_ORDER;
			}else{
				orderBy = sortOrder;
			}

			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = queryBuilder.query(
					db,
					projection,
					selection,
					selectionArgs,
					null, null,
					orderBy
			);
			//设置通知
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		}

		/**
		 * 对messages表的更新操作,数据库中messages表的各个字段（contactId long,address String,time long,status String,
		 * body String,code int）操作时应该注意数据的转换，contactId不是直接放入的是通过Contacts类中的getId方法得到的，time是Date.getTime()，
		 * status是要枚举类型转换为String型，body和code是从Messsages类中来的。
		 */
		@Override
		public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			int count;
			String finalWhere;

			switch(mUriMatcher.match(uri)){
			case MESSAGES:
				count = db.update(Contract.DATABASE_MESSAGES_TABLE,
						values,
						selection,
						selectionArgs
				);
				break;
			case MESSAGE_ID:
				finalWhere = Contract.Messages._ID + " = " +uri.getPathSegments().get(1);
				if(selection != null){
					finalWhere = finalWhere + " AND " + selection;
				}
				count = db.update(Contract.DATABASE_MESSAGES_TABLE,
						values,
						finalWhere,
						selectionArgs
				);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		}

		/**
		 * for the provider testing
		 * @return a instance of FamilyLinkDBOpenHelper
		 */
		public FamilyLinkDBOpenHelper getOpenHelperForTest(){
			return dbHelper;
		}

}
