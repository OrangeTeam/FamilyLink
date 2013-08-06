package org.orange.familylink.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 此类为常量容器
 * @author OrangeTeam
 *
 */
public class Contract {
	//scheme为uri中的头部，已由android规定
	public static final String SCHEME = "content://";

	//数据库名字
	public static final String DATABASE_NAME = "familylink.db";
	//以下两个为联系人和信息表的表名
	public static final String DATABASE_CONTACTS_TABLE = "contacts";
	public static final String DATABASE_MESSAGES_TABLE = "messages";
	//数据库的版本
	public static final int DATABASE_VERSION = 1;

	//这个私有的构造方法是为了防止实例化这个类
	private Contract(){}

	/**
	 * 这个内部类是对联系人表的常量进行定义
	 * @author OrangeTeam
	 *
	 */
	public static final  class Contacts implements BaseColumns{
		//防止实例化
		private Contacts(){}
		//authority为uri中主机地址
		public static final String AUTHORITY = "org.orange.familylink.provider.contactsprovider";
		//这是uri中contacts表的路径
		private static final String PATH_CONTACTS = "/contacts";
		//这是uri中contacts表中的记录的路径
		private static final String PATH_CONTACTS_ID = "/contacts/";
		public static final Uri CONTACTS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CONTACTS);
		public static final Uri CONTACTS_ID_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CONTACTS_ID);
		//contacts表中集合类型的mime
		public static final String CONTACTS_TYPE = "vnd.android.cursor.dir/vnd.familylink.contacts";
		//contacts表中单个项类型的mime
		public static final String CONTACTS_ITEM_TYPE = "vnd.android.cursor.item/vnd.familylink.contacts";
		/*
		 * contacts表中的字段
		 */
		public static final String COLUMN_NAME_SYSTEM_ID = "system_id";
		public static final String COLUMN_NAME_SYSTEM_LOOKUP_KEY = "system_lookup_key";


		public static final String CONTACTS_DEFAULT_SORT_ORDER = COLUMN_NAME_SYSTEM_ID + " DESC";

		//这个常量字符串是创建contacts表的sql语句
		public static final String CONTACTS_TABLE_CREATE = "create table " + DATABASE_CONTACTS_TABLE + " (" + 
		_ID + " integer primary key," + COLUMN_NAME_SYSTEM_ID + " integer," + COLUMN_NAME_SYSTEM_LOOKUP_KEY + 
		" varchar(100));";
	}

	/**
	 * 这个内部类是对信息表中的常量进行定义
	 * @author OrangeTeam
	 *
	 */
	public static final class Messages implements BaseColumns{
		//防止实例化
		private Messages(){}
		//authority为uri中主机地址
		public static final String AUTHORITY = "org.orange.familylink.provider.messagesprovider";
		//uri中messages表的路径
		public static final String PATH_MESSAGES = "/messages";
		//uri中messages表的记录的路径
		public static final String PATH_MESSAGES_ID = "/messages/";
		public static final Uri MESSAGES_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MESSAGES);
		public static final Uri MESSAGES_ID_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MESSAGES_ID);
		//messages表中集合类型的mime
		public static final String MESSAGES_TYPE = "vnd.android.cursor.dir/vnd.familylink.messages";
		//messages表中单项类型的mime
		public static final String MESSAGES_ITEM_TYPE = "vnd.android.cursor.item/vnd.familylink.messages";
		/*
		 * messages表中各个字段
		 */
		public static final String COLUMN_NAME_CONTACT_ID = "contact_id";
		public static final String COLUMN_NAME_ADDRESS = "address";
		public static final String COLUMN_NAME_TIME = "time";
		public static final String COLUMN_NAME_STATUS = "status";
		public static final String COLUMN_NAME_BODY = "body";
		public static final String COLUMN_NAME_CODE = "CODE";

		//这个常量字符串是创建messages表的sql语句
		public static final String MESSAGES_TABLE_CREATE = "create table " + DATABASE_MESSAGES_TABLE + " (" + 
		_ID + " integer primary key," + COLUMN_NAME_CONTACT_ID + " integer references contacts(_id)," + 
		COLUMN_NAME_ADDRESS + " varchar(20)," + COLUMN_NAME_TIME + " integer," + COLUMN_NAME_STATUS + " varchar(30),"
		+ COLUMN_NAME_BODY + " text," + COLUMN_NAME_CODE + " integer"
		+ ");";

		public static final String MESSAGES_DEFAULT_SORT_ORDER = COLUMN_NAME_TIME + " DESC";

		//为messages表中的time字段创建索引
		public static final String INDEX_CREATE = "create index messages_time_index on " + 
		DATABASE_MESSAGES_TABLE + "(" + COLUMN_NAME_TIME + ");";
	}

}
