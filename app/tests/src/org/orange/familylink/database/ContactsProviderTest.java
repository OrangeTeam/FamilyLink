package org.orange.familylink.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

/**
 * 测试联系人的content provider用的测试用例
 */
public class ContactsProviderTest extends ProviderTestCase2<ContactsProvider> {

	//测试无效的uri
	private static final Uri INVALID_URI = Uri.withAppendedPath(Contract.Contacts.CONTACTS_URI, "invalid");
	//provider测试中的帮助类
	private MockContentResolver mMockResolver;
	//用于获取数据库
	private SQLiteDatabase db;

	//测试联系人时用的一些记录
	private final ContactInfo[] TEST_CONTACTS = {
			new ContactInfo("one", "10086"),
			new ContactInfo("two", "10086"),
			new ContactInfo("tree", "10010"),
			new ContactInfo("four", "10010"),
			new ContactInfo("five", "15111012019"),
			new ContactInfo("six", "15111012019")
	};

	//匹配全部的mime
	private final static String MIME_TYPES_ALL = "*/*";
	//测试不存在的mime
	private final static String MIME_TYPES_NONE = "qwer/qwer";
	//测试单项的mime
	private final static String MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.familylink.contacts";

	/**
	 * 构造方法告诉测试类要测试的父类和权威路径
	 */
	public ContactsProviderTest() {
		super(ContactsProvider.class, Contract.Contacts.AUTHORITY);
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
		for(int i = 0; i < TEST_CONTACTS.length; i++){
			db.insertOrThrow(Contract.DATABASE_CONTACTS_TABLE, null, TEST_CONTACTS[i].getContentValues());
		}
	}

	/**
	 * 对uri和getType方法测试
	 */
	public void testUriAndGetType(){
		String mimeType = mMockResolver.getType(Contract.Contacts.CONTACTS_URI);
		assertEquals(Contract.Contacts.CONTACTS_TYPE, mimeType);
		Uri contactIdUri = ContentUris.withAppendedId(Contract.Contacts.CONTACTS_ID_URI, 1);
		mimeType = mMockResolver.getType(contactIdUri);
		assertEquals(Contract.Contacts.CONTACTS_ITEM_TYPE, mimeType);
		mimeType = mMockResolver.getType(INVALID_URI);
	}

	/**
	 * 对uri流类型操作
	 */
	public void testGetStreamTypes(){
		assertNull(mMockResolver.getStreamTypes(Contract.Contacts.CONTACTS_URI, MIME_TYPES_ALL));
		Uri testUri = Uri.withAppendedPath(Contract.Contacts.CONTACTS_ID_URI, "1");
		String mimeType[] = mMockResolver.getStreamTypes(testUri, MIME_TYPES_ALL);
		assertNull(mimeType);
		mimeType = mMockResolver.getStreamTypes(testUri, MIME_TYPES_NONE);
		assertNull(mimeType);
	}

	/**
	 * 测试查询，这里的查询用的Uri是集合类型的
	 */
	public void testQueriesOnContactsUri() {
		//测试的字段
        final String[] TEST_PROJECTION = {
            Contract.Contacts.COLUMN_NAME_NAME,
            Contract.Contacts.COLUMN_NAME_PHONE_NUMBER,
            Contract.Contacts.COLUMN_NAME_PHOTO
        };
        //测试的where条件
        final String TITLE_SELECTION = Contract.Contacts.COLUMN_NAME_NAME + " = " + "?";

        final String SELECTION_COLUMNS =
            TITLE_SELECTION + " OR " + TITLE_SELECTION + " OR " + TITLE_SELECTION;

        final String[] SELECTION_ARGS = { "one", "two", "tree" };

        //sort
        final String SORT_ORDER = Contract.Contacts.COLUMN_NAME_NAME + " ASC";

        //查询contacts的集合型uri也就是查询contacts表
        Cursor cursor = mMockResolver.query(
            Contract.Contacts.CONTACTS_URI, 
            null,
            null,
            null,
            null
        );

        //此时查询结果肯定为空
        assertEquals(0, cursor.getCount());

        //现在对contact表插入数据
        insertData();

        //进行再次的查询
        cursor = mMockResolver.query(
            Contract.Contacts.CONTACTS_URI,
            null,
            null,
            null,
            null
        );

        //这是查询结果的个数就为测试输入的用例的个数
        assertEquals(TEST_CONTACTS.length, cursor.getCount());

        //查询中加入‘project’就是查询时选着要查询的字段
        Cursor projectionCursor = mMockResolver.query(
              Contract.Contacts.CONTACTS_URI,
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
            Contract.Contacts.CONTACTS_URI,
            TEST_PROJECTION,
            SELECTION_COLUMNS,
            SELECTION_ARGS,
            SORT_ORDER
        );

        //此时的查询结果应为where条件给出所进行查询的结果的个数
        assertEquals(SELECTION_ARGS.length, projectionCursor.getCount());

        int index = 0;
        //因为查询结果进行了排序，所以进行对比的SELECTION_ARGS也要用相应的顺序
        final String[] SORT_SELECTION_ARGS = {"one", "tree", "two"};

        while (projectionCursor.moveToNext()) {
            assertEquals(SORT_SELECTION_ARGS[index], projectionCursor.getString(0));
            index++;
        }

        assertEquals(SELECTION_ARGS.length, index);

    }

	/**
	 * 测试查询，这里查询用的uri是单项记录类型
	 */
	public void testQueriesOnContactIdUri() {
	      final String SELECTION_COLUMNS = Contract.Contacts.COLUMN_NAME_NAME + " = " + "?";

	      final String[] SELECTION_ARGS = { "one" };

	      final String SORT_ORDER = Contract.Contacts.COLUMN_NAME_NAME + " ASC";

	      final String[] CONTACT_ID_PROJECTION = {
	           Contract.Contacts._ID,
	           Contract.Contacts.COLUMN_NAME_PHONE_NUMBER};

	      Uri contactIdUri = ContentUris.withAppendedId(Contract.Contacts.CONTACTS_ID_URI, 1);

	      Cursor cursor = mMockResolver.query(
	          contactIdUri,
	          null,
	          null,
	          null,
	          null
	      );

	      assertEquals(0,cursor.getCount());

	      insertData();

	      cursor = mMockResolver.query(
	          Contract.Contacts.CONTACTS_URI,
	          CONTACT_ID_PROJECTION,
	          SELECTION_COLUMNS,
	          SELECTION_ARGS,
	          SORT_ORDER
	      );

	      assertEquals(1, cursor.getCount());

	      assertTrue(cursor.moveToFirst());

	      int inputContactId = cursor.getInt(0);

	      contactIdUri = ContentUris.withAppendedId(Contract.Contacts.CONTACTS_ID_URI, inputContactId);

	      cursor = mMockResolver.query(contactIdUri,
	          CONTACT_ID_PROJECTION,
	          SELECTION_COLUMNS,
	          SELECTION_ARGS,
	          SORT_ORDER
	      );

	      assertEquals(1, cursor.getCount());

	      assertTrue(cursor.moveToFirst());

	      assertEquals(inputContactId, cursor.getInt(0));
	    }

	public void testInserts() {
        ContactInfo contact = new ContactInfo(
            "seven", 
            "7" 
        );

        Uri rowUri = mMockResolver.insert(
            Contract.Contacts.CONTACTS_URI,
            contact.getContentValues()
        );

        long contactId = ContentUris.parseId(rowUri);

        Cursor cursor = mMockResolver.query(
            Contract.Contacts.CONTACTS_URI,
            null,
            null,
            null,
            null
        );

        assertEquals(1, cursor.getCount());

        assertTrue(cursor.moveToFirst());

        int nameIndex = cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME);
        int phoneNumberIndex = cursor.getColumnIndex(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER);

        assertEquals(contact.name, cursor.getString(nameIndex));
        assertEquals(contact.phoneNumber, cursor.getString(phoneNumberIndex));

        ContentValues values = contact.getContentValues();

        values.put(Contract.Contacts._ID, (int) contactId);

        try {
            rowUri = mMockResolver.insert(Contract.Contacts.CONTACTS_URI, values);
            fail("Expected insert failure for existing record but insert succeeded.");
        } catch (Exception e) {

        }
    }

	/**
	 * 测试删除操作
	 */
	public void testDeletes() {

		//where条件
        final String SELECTION_COLUMNS = Contract.Contacts.COLUMN_NAME_NAME + " = " + "?";

        final String[] SELECTION_ARGS = { "one" };

        //进行删除
        int rowsDeleted = mMockResolver.delete(
            Contract.Contacts.CONTACTS_URI,
            SELECTION_COLUMNS,
            SELECTION_ARGS
        );

        //此时是没有删除任何的行，前面的插入对这个函数没影响
        assertEquals(0, rowsDeleted);

        //插入测试的用例
        insertData();

        //删除
        rowsDeleted = mMockResolver.delete(
            Contract.Contacts.CONTACTS_URI, 
            SELECTION_COLUMNS,
            SELECTION_ARGS
        );

        //此时是删除了一行
        assertEquals(1, rowsDeleted);

        //查询一下看是否真被删除
        Cursor cursor = mMockResolver.query(
            Contract.Contacts.CONTACTS_URI,
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

        final String SELECTION_COLUMNS = Contract.Contacts.COLUMN_NAME_NAME + " = " + "?";

        final String[] selectionArgs = { "one" };

        ContentValues values = new ContentValues();

        values.put(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER, "110");

        int rowsUpdated = mMockResolver.update(
            Contract.Contacts.CONTACTS_URI,
            values,
            SELECTION_COLUMNS,
            selectionArgs
        );

        assertEquals(0, rowsUpdated);

        insertData();

        rowsUpdated = mMockResolver.update(
            Contract.Contacts.CONTACTS_URI,
            values,
            SELECTION_COLUMNS,
            selectionArgs
        );

        assertEquals(1, rowsUpdated);

    }

	/**
	 * 内部类用来生成测试用例的
	 * @author orange team
	 *
	 */
	private static class ContactInfo{
		String name;
		String phoneNumber;
		Bitmap bitmap;
		public ContactInfo(String mName, String mPhoneNumber){
			name = mName;
			phoneNumber = mPhoneNumber;
		}

		public ContentValues getContentValues(){
			ContentValues v = new ContentValues();
			v.put(Contract.Contacts.COLUMN_NAME_NAME, name);
			v.put(Contract.Contacts.COLUMN_NAME_PHONE_NUMBER, phoneNumber);
			return v;
		}
	}

}
