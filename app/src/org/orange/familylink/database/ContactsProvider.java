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
 * 联系人的内容提供器
 * @author OrangeTeam
 *
 */
public class ContactsProvider extends ContentProvider {
	//所用的数据库创建的类生成一个对象对数据库进行操作
	private FamilyLinkDBOpenHelper dbHelper;

	//为查询设置一个投影映射
	private static HashMap<String, String> mContactsProjectionMap;

	//用常量‘1’代表uri为contacts表的集合
	private static final int CONTACTS = 1;
	//用常量‘2’代表uri为contacts表中的记录
	private static final int CONTACT_ID = 2;

	//用UriMatcher类来识别uri类型
	private static final UriMatcher mUriMatcher;

	static{
		//UriMatcher类的实例化
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		//添加uri与常量进行匹配
		mUriMatcher.addURI(Contract.Contacts.AUTHORITY, "contacts", CONTACTS);
		mUriMatcher.addURI(Contract.Contacts.AUTHORITY, "contacts/#", CONTACT_ID);

		//查询映射的添加
		mContactsProjectionMap = new HashMap<String, String>();
		mContactsProjectionMap.put(Contract.Contacts._ID, Contract.Contacts._ID);
		mContactsProjectionMap.put(Contract.Contacts.COLUMN_NAME_NAME, Contract.Contacts.COLUMN_NAME_NAME);
		mContactsProjectionMap.put(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER, Contract.Contacts.COLUMN_NAME_PHONE_NUMBER);
		mContactsProjectionMap.put(Contract.Contacts.COLUMN_NAME_PHOTO, Contract.Contacts.COLUMN_NAME_PHOTO);
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
	 * 对contacts表的删除操作
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		//条件子句
		String finalWhere;
		//用来保存删除的行
		int count;

		switch(mUriMatcher.match(uri)){
		case CONTACTS: //对集合类型的删除操作
			count = db.delete(Contract.DATABASE_CONTACTS_TABLE,
					selection,
					selectionArgs
			);
			break;
		case CONTACT_ID: //对记录类型的删除操作
			finalWhere = Contract.Contacts._ID + " = " + uri.getPathSegments().get(1);
			if(selection != null){
				finalWhere = finalWhere + " AND " + selection;
			}
			count = db.delete(Contract.DATABASE_CONTACTS_TABLE,
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
		case CONTACTS :
			//返回集合类型的mime
			return Contract.Contacts.CONTACTS_TYPE;
		case CONTACT_ID :
			//返回单项类型的mime
			return Contract.Contacts.CONTACTS_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI" + arg0);
		}
	}

	/**
	 * 对contacts表的插入操作
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//当uri不是代表contacts这个表时为不合法的uri
		if(mUriMatcher.match(uri) != CONTACTS){
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert(
				Contract.DATABASE_CONTACTS_TABLE,
				null,
				values
		);
		//如果插入操作成功返回插入的 uri，且进行通知
		if(rowId > 0){
			Uri contactUri = ContentUris.withAppendedId(Contract.Contacts.CONTACTS_URI, rowId);
			getContext().getContentResolver().notifyChange(contactUri, null);
			return contactUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	/**
	 * 对contacts表的查询操作
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		switch(mUriMatcher.match(uri)){
		case CONTACTS:
			//设置查询的表
			queryBuilder.setTables(Contract.DATABASE_CONTACTS_TABLE);
			//设置查询映射
			queryBuilder.setProjectionMap(mContactsProjectionMap);
			break;
		case CONTACT_ID:
			queryBuilder.setTables(Contract.DATABASE_CONTACTS_TABLE);
			queryBuilder.setProjectionMap(mContactsProjectionMap);
			queryBuilder.appendWhere(Contract.Contacts._ID + " = " +
			uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		if(TextUtils.isEmpty(sortOrder)){
			orderBy = Contract.Contacts.CONTACTS_DEFAULT_SORT_ORDER;
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
	 * 对contacts表的更新操作
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		String finalWhere;

		switch(mUriMatcher.match(uri)){
		case CONTACTS:
			count = db.update(Contract.DATABASE_CONTACTS_TABLE,
					values,
					selection,
					selectionArgs
			);
			break;
		case CONTACT_ID:
			finalWhere = Contract.Contacts._ID + " = " +uri.getPathSegments().get(1);
			if(selection != null){
				finalWhere = finalWhere + " AND " + selection;
			}
			count = db.update(Contract.DATABASE_CONTACTS_TABLE,
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
