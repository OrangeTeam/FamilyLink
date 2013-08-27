package org.orange.familylink.database;

import java.util.Date;

import org.orange.familylink.data.Contact;
import org.orange.familylink.data.Message;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.sms.SmsMessage;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

public class MessagesProviderTest extends ProviderTestCase2<MessagesProvider> {

	//测试无效的uri
	private static final Uri INVALID_URI = Uri.withAppendedPath(Contract.Messages.MESSAGES_URI, "invalid");
	//provider测试中的帮助类
	private MockContentResolver mMockResolver;
	//用于获取数据库
	private SQLiteDatabase db;

	//测试联系人时用的一些记录
	private final MessageInfo[] TEST_MESSAGES = {
			new MessageInfo("12345678900"),
			new MessageInfo("12345678900"),
			new MessageInfo("222-7135"),
			new MessageInfo("222-7135"),
			new MessageInfo("10086"),
			new MessageInfo("10086"),
			new MessageInfo("10000"),
			new MessageInfo("10000")
	};

	//匹配全部的mime
	private final static String MIME_TYPES_ALL = "*/*";
	//测试不存在的mime
	private final static String MIME_TYPES_NONE = "qwer/qwer";
	//测试单项的mime
	private final static String MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.familylink.messages";

	/**
	 * 构造方法告诉测试类要测试的父类和权威路径
	 */
	public MessagesProviderTest() {
		super(MessagesProvider.class, Contract.Messages.AUTHORITY);
	}

	/**
	 * 在这个方法中初始化帮助类和数据库的操作
	 */
	@Override
	protected void setUp() throws Exception{
		super.setUp();
		mMockResolver = getMockContentResolver();
		db = getProvider().getOpenHelperForTest().getWritableDatabase();
	}

	/**
	 * 结束时调用
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 这是个私有的方法内部调用，在测试类中只有为public且为无参数的方法才会被测试调用
	 */
	private void insertData(){
		for(int i = 0; i < TEST_MESSAGES.length; i++){
			Contact mContact = new Contact();
			mContact.setId((long)i);
			Message mMessage = new SmsMessage();
			mMessage.setBody("here" + i);
			mMessage.setCode(Code.EXTRA_BITS);
			TEST_MESSAGES[i].setContactId(mContact);
			TEST_MESSAGES[i].setTime(new Date(2013/8/5));
			TEST_MESSAGES[i].setStatus(Status.DELIVERED);
			TEST_MESSAGES[i].setBody(mMessage);
			TEST_MESSAGES[i].setCode(mMessage);
			db.insertOrThrow(Contract.DATABASE_MESSAGES_TABLE, null, TEST_MESSAGES[i].getContentValues());
		}
	}

	/**
	 * 对uri和getType方法测试
	 */
	public void testUriAndGetType(){
		String mimeType = mMockResolver.getType(Contract.Messages.MESSAGES_URI);
		assertEquals(Contract.Messages.MESSAGES_TYPE, mimeType);
		Uri messageIdUri = ContentUris.withAppendedId(Contract.Messages.MESSAGES_ID_URI, 1);
		mimeType = mMockResolver.getType(messageIdUri);
		assertEquals(Contract.Messages.MESSAGES_ITEM_TYPE, mimeType);
		mimeType = mMockResolver.getType(INVALID_URI);
	}

	/**
	 * 对uri流类型操作
	 */
	public void testGetStreamTypes(){
		assertNull(mMockResolver.getStreamTypes(Contract.Messages.MESSAGES_URI, MIME_TYPES_ALL));
		Uri testUri = Uri.withAppendedPath(Contract.Messages.MESSAGES_ID_URI, "1");
		String mimeType[] = mMockResolver.getStreamTypes(testUri, MIME_TYPES_ALL);
		assertNull(mimeType);
		mimeType = mMockResolver.getStreamTypes(testUri, MIME_TYPES_NONE);
		assertNull(mimeType);
	}

	/**
	 * 测试查询，这里的查询用的Uri是集合类型的
	 */
	public void testQueriesOnMessagesUri() {
		//测试的字段
        final String[] TEST_PROJECTION = {
        		Contract.Messages.COLUMN_NAME_CONTACT_ID,
        		Contract.Messages.COLUMN_NAME_ADDRESS,
        		Contract.Messages.COLUMN_NAME_TIME,
        		Contract.Messages.COLUMN_NAME_STATUS,
        		Contract.Messages.COLUMN_NAME_BODY,
        		Contract.Messages.COLUMN_NAME_CODE
        };
        //测试的where条件
        final String TITLE_SELECTION = Contract.Messages.COLUMN_NAME_ADDRESS + " = " + "?";

        final String SELECTION_COLUMNS =
            TITLE_SELECTION + " OR " + TITLE_SELECTION + " OR " + TITLE_SELECTION;

        final String[] SELECTION_ARGS = { "12345678900", "10086", "10000" };

        //sort
        final String SORT_ORDER = Contract.Messages.COLUMN_NAME_TIME + " ASC";

        //查询contacts的集合型uri也就是查询contacts表
        Cursor cursor = mMockResolver.query(
        	Contract.Messages.MESSAGES_URI, 
            null,
            null,
            null,
            null
        );

        //此时查询结果肯定为空
        assertEquals(0, cursor.getCount());

        //现在对message表插入数据
        insertData();

        //进行再次的查询
        cursor = mMockResolver.query(
        	Contract.Messages.MESSAGES_URI,
            null,
            null,
            null,
            null
        );

        //这是查询结果的个数就为测试输入的用例的个数
        assertEquals(TEST_MESSAGES.length, cursor.getCount());

        //查询中加入‘project’就是查询时选着要查询的字段
        Cursor projectionCursor = mMockResolver.query(
        	  Contract.Messages.MESSAGES_URI,
              TEST_PROJECTION,
              null,
              null,
              null
        );

        //判断查询结果的字段个数是否真确
        assertEquals(TEST_PROJECTION.length, projectionCursor.getColumnCount());

        //对查询结果的字段名检查
        assertEquals(TEST_PROJECTION[0], projectionCursor.getColumnName(0));
        assertEquals(TEST_PROJECTION[1], projectionCursor.getColumnName(1));

        //综合的查询
        projectionCursor = mMockResolver.query(
        	Contract.Messages.MESSAGES_URI,
            TEST_PROJECTION,
            SELECTION_COLUMNS,
            SELECTION_ARGS,
            SORT_ORDER
        );

        //此时的查询结果应为where条件给出所进行查询的结果的个数
        assertEquals(SELECTION_ARGS.length*2, projectionCursor.getCount());

        int index = 0;

        while (projectionCursor.moveToNext()) {
            assertEquals(SELECTION_ARGS[index], projectionCursor.getString(1));
            index++;
            projectionCursor.moveToNext();
        }

        assertEquals(SELECTION_ARGS.length, index);

    }

	/**
	 * 测试查询，这里查询用的uri是单项记录类型
	 */
	public void testQueriesOnMessageIdUri() {
	      final String SELECTION_COLUMNS = Contract.Messages.COLUMN_NAME_ADDRESS + " = " + "?";

	      final String[] SELECTION_ARGS = { "10086" };

	      final String SORT_ORDER = Contract.Messages.COLUMN_NAME_TIME + " ASC";

	      final String[] MESSAGE_ID_PROJECTION = {
	    	   Contract.Messages._ID,
	    	   Contract.Messages.COLUMN_NAME_CONTACT_ID,
	    	   Contract.Messages.COLUMN_NAME_TIME,
	    	   Contract.Messages.COLUMN_NAME_STATUS,
	    	   Contract.Messages.COLUMN_NAME_BODY,
	    	   Contract.Messages.COLUMN_NAME_CODE};

	      Uri messageIdUri = ContentUris.withAppendedId(Contract.Messages.MESSAGES_ID_URI, 1);

	      Cursor cursor = mMockResolver.query(
	          messageIdUri,
	          null,
	          null,
	          null,
	          null
	      );

	      assertEquals(0,cursor.getCount());

	      insertData();

	      cursor = mMockResolver.query(
	    	  Contract.Messages.MESSAGES_URI,
	          MESSAGE_ID_PROJECTION,
	          SELECTION_COLUMNS,
	          SELECTION_ARGS,
	          SORT_ORDER
	      );

	      assertEquals(2, cursor.getCount());

	      assertTrue(cursor.moveToFirst());

	      int inputMessageId = cursor.getInt(0);

	      messageIdUri = ContentUris.withAppendedId(Contract.Messages.MESSAGES_ID_URI, inputMessageId);

	      cursor = mMockResolver.query(messageIdUri,
	          MESSAGE_ID_PROJECTION,
	          SELECTION_COLUMNS,
	          SELECTION_ARGS,
	          SORT_ORDER
	      );

	      assertEquals(1, cursor.getCount());

	      assertTrue(cursor.moveToFirst());

	      assertEquals(inputMessageId, cursor.getInt(0));
	    }

	public void testInserts() {
        MessageInfo message = new MessageInfo("10086");

        Contact mContact = new Contact();
        mContact.setId((long)10);
        Message mMessage = new SmsMessage();
        mMessage.setBody("here" + "10");
        mMessage.setCode(Code.EXTRA_BITS);
        message.setContactId(mContact);
        message.setTime(new Date(2013/8/5));
        message.setStatus(Status.DELIVERED);
        message.setBody(mMessage);
        message.setCode(mMessage);

        Uri rowUri = mMockResolver.insert(
        	Contract.Messages.MESSAGES_URI,
            message.getContentValues()
        );

        long messageId = ContentUris.parseId(rowUri);

        Cursor cursor = mMockResolver.query(
        	Contract.Messages.MESSAGES_URI,
            null,
            null,
            null,
            null
        );

        assertEquals(1, cursor.getCount());

        assertTrue(cursor.moveToFirst());

        int addressIndex = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS);
        int contactIdIndex = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID);
        int timeIndex = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_TIME);
        int directionIndex = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_STATUS);
        int bodyIndex = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY);
        int codeIndex = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE);

        assertEquals(message.address, cursor.getString(addressIndex));
        assertEquals(message.contactId, cursor.getLong(contactIdIndex));
        assertEquals(message.time, cursor.getLong(timeIndex));
        assertEquals(String.valueOf(message.status), cursor.getString(directionIndex));
        assertEquals(message.body, cursor.getString(bodyIndex));
        assertEquals(message.code, cursor.getInt(codeIndex));

        ContentValues values = message.getContentValues();

        values.put(Contract.Messages._ID, (int) messageId);

        try {
            rowUri = mMockResolver.insert(Contract.Messages.MESSAGES_URI, values);
            fail("Expected insert failure for existing record but insert succeeded.");
        } catch (Exception e) {

        }
    }

	/**
	 * 测试删除操作
	 */
	public void testDeletes() {

		//where条件
        final String SELECTION_COLUMNS = Contract.Messages.COLUMN_NAME_ADDRESS + " = " + "?";

        final String[] SELECTION_ARGS = { "10086" };

        //进行删除
        int rowsDeleted = mMockResolver.delete(
            Contract.Messages.MESSAGES_URI,
            SELECTION_COLUMNS,
            SELECTION_ARGS
        );

        //此时是没有删除任何的行，前面的插入对这个函数没影响
        assertEquals(0, rowsDeleted);

        //插入测试的用例
        insertData();

        //删除
        rowsDeleted = mMockResolver.delete(
        	Contract.Messages.MESSAGES_URI, 
            SELECTION_COLUMNS,
            SELECTION_ARGS
        );

        //此时是删除了一行
        assertEquals(2, rowsDeleted);

        //查询一下看是否真被删除
        Cursor cursor = mMockResolver.query(
        	Contract.Messages.MESSAGES_URI,
            null,
            SELECTION_COLUMNS,
            SELECTION_ARGS,
            null
        );

        //这里可得知真的被删除
        assertEquals(0, cursor.getCount());
    }

	/**
	 * 测试更新操作
	 */
	public void testUpdates() {

        final String SELECTION_COLUMNS = Contract.Messages.COLUMN_NAME_ADDRESS + " = " + "?";

        final String[] selectionArgs = { "10000" };

        ContentValues values = new ContentValues();

        values.put(Contract.Messages.COLUMN_NAME_STATUS, Status.SENT.name());

        int rowsUpdated = mMockResolver.update(
        	Contract.Messages.MESSAGES_URI,
            values,
            SELECTION_COLUMNS,
            selectionArgs
        );

        assertEquals(0, rowsUpdated);

        insertData();

        rowsUpdated = mMockResolver.update(
        	Contract.Messages.MESSAGES_URI,
            values,
            SELECTION_COLUMNS,
            selectionArgs
        );

        assertEquals(2, rowsUpdated);

    }

	/**
	 * 内部类用来生成测试用例的
	 * @author orange team
	 *
	 */
	private static class MessageInfo{
		long contactId;
		String address;
		long time;
		String status;
		String body;
		int code;
		public MessageInfo(String mAddress){
			address = mAddress;
		}

		void setContactId(Contact mContact){
			contactId = mContact.getId();
		}

		void setTime(Date mDate){
			time = mDate.getTime();
		}

		void setStatus(Status mStatus){
			status = mStatus.name();
		}

		void setBody(Message mMessage){
			body = mMessage.getBody();
		}

		void setCode(Message mMessage){
			code = mMessage.getCode();
		}

		public ContentValues getContentValues(){
			ContentValues v = new ContentValues();
			v.put(Contract.Messages.COLUMN_NAME_CONTACT_ID, contactId);
			v.put(Contract.Messages.COLUMN_NAME_ADDRESS, address);
			v.put(Contract.Messages.COLUMN_NAME_TIME, time);
			v.put(Contract.Messages.COLUMN_NAME_STATUS, status);
			v.put(Contract.Messages.COLUMN_NAME_BODY, body);
			v.put(Contract.Messages.COLUMN_NAME_CODE, code);
			return v;
		}
	}

}
