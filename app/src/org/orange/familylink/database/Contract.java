package org.orange.familylink.database;

import org.orange.familylink.data.MessageLogRecord.Direction;
import org.orange.familylink.data.MessageLogRecord.Status;

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
	/**
	 * 联系人表名“contacts"
	 */
	public static final String DATABASE_CONTACTS_TABLE = "contacts";
	/**
	 * 信息表名“messages”
	 */
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
		/**
		 * 联系人表中的联系人名字  String
		 */
		public static final String COLUMN_NAME_NAME = "name";
		/**
		 * 联系人表中的联系人电话号码  String
		 */
		public static final String COLUMN_NAME_PHONE_NUMBER = "phone_number";
		/**
		 * 联系人表中的联系人照片，存储是blob，但用数据库操作类进行操作时是传入bitmap
		 */
		public static final String COLUMN_NAME_PHOTO = "photo";


		public static final String CONTACTS_DEFAULT_SORT_ORDER = COLUMN_NAME_PHONE_NUMBER + " DESC";

		//这个常量字符串是创建contacts表的sql语句
		public static final String CONTACTS_TABLE_CREATE = "create table " + DATABASE_CONTACTS_TABLE + " (" + 
		_ID + " integer primary key," + COLUMN_NAME_NAME + " varchar(60)," + COLUMN_NAME_PHONE_NUMBER + 
		" varchar(20)," + COLUMN_NAME_PHOTO + " blob);";
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
		/**
		 * 信息表中的‘联系人记录的id’，也就是这个字段是跟联系人表关联，来自联系人表的id字段  long
		 */
		public static final String COLUMN_NAME_CONTACT_ID = "contact_id";
		/**
		 * 信息表中的地址也就是手机号  String
		 */
		public static final String COLUMN_NAME_ADDRESS = "address";
		/**
		 * 信息表中的时间字段用于存储发送或接受短信的时间，存储是用long，但如果用数据库操作类进行操作时是用Date
		 */
		public static final String COLUMN_NAME_TIME = "time";
		/**
		 * 信息表中的状态存储信息是‘正在发送’、‘已发送’、‘已送达’、‘发送失败’等情况  String
		 */
		public static final String COLUMN_NAME_STATUS = "status";
		/**
		 * 信息表中的信息所发送的内容就是短信内容  String
		 */
		public static final String COLUMN_NAME_BODY = "body";
		/**
		 * 信息表中的代表短信级别的码  int
		 */
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

		/**
		 * 取得与指定条件对应的SQL where子句（不包括“WHERE”自身）
		 * @param status 筛选条件：消息状态
		 * @return SQL where子句，满足此子句的消息的状态都为status
		 */
		public static String getWhereClause(Status status) {
			return COLUMN_NAME_STATUS + " = '" + status.name() + "'";
		}
		/**
		 * 取得与指定条件对应的SQL where子句（不包括“WHERE”自身）
		 * @param direction 筛选条件：消息方向
		 * @return SQL where子句，满足此子句的消息的{@link Direction}都为direction
		 */
		public static String getWhereClause(Direction direction) {
			switch (direction) {
			case RECEIVE:
				return "(" + getWhereClause(Status.HAVE_READ) + ") OR ("
						+ getWhereClause(Status.UNREAD) + ")";
			case SEND:
				return "(" + getWhereClause(Status.DELIVERED) + ") OR ("
						+ getWhereClause(Status.SENT) + ") OR ("
						+ getWhereClause(Status.SENDING) + ") OR ("
						+ getWhereClause(Status.FAILED_TO_SEND) + ")";
			default:
				throw new UnsupportedOperationException("unsupported direction: " + direction);
			}
		}
	}

}
