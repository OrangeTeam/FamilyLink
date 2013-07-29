package org.orange.familylink.data;

import org.orange.familylink.data.MessageLogRecord.Direction;

import junit.framework.TestCase;

public class MessageLogRecordTest extends TestCase {
	MessageLogRecord mMessageLogRecord;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Call the super constructor (required by JUnit)
		super.setUp();
		mMessageLogRecord = new MessageLogRecord();
	}

	public void testPreconditions() {
		mMessageLogRecord.equals(MessageLogRecord.mDefaultValue);
	}
	public void testDefaultValue() {
		assertNull(MessageLogRecord.mDefaultValue.getId());
		assertNull(MessageLogRecord.mDefaultValue.getContact());
		assertNull(MessageLogRecord.mDefaultValue.getAddress());
		assertEquals(0, MessageLogRecord.mDefaultValue.getTimestamp());
		assertNull(MessageLogRecord.mDefaultValue.getDirection());
		assertNull(MessageLogRecord.mDefaultValue.hasRead());
		assertNull(MessageLogRecord.mDefaultValue.getMessage());
		MessageLogRecord defaultValue = MessageLogRecord.mDefaultValue;
		try {
			defaultValue.setId(null);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setContact(null);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setAddress(null);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setTimestamp(0);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setDirection(null);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setHasRead(null);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setMessage(new Message());
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
	}

	public void testId() {
		Long id = 348123498L;
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());

		id = Long.valueOf(0);
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());

		id = -348123498L;
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());

		id = Long.MAX_VALUE;
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());

		id = Long.MIN_VALUE;
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());

		id = null;
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());
	}
	//TODO 待完善
	public void testContact() {
		Contact contact = new Contact();
		mMessageLogRecord.setContact(contact);
		assertEquals(contact, mMessageLogRecord.getContact());

		mMessageLogRecord.setContact(null);
		assertNull(mMessageLogRecord.getContact());
	}
	public void testAdderss() {
		String address = "baijie1991@gmail.com";
		mMessageLogRecord.setAddress(address);
		assertEquals(address, mMessageLogRecord.getAddress());

		address = "15620906177";
		mMessageLogRecord.setAddress(address);
		assertEquals(address, mMessageLogRecord.getAddress());

		address = "";
		mMessageLogRecord.setAddress(address);
		assertEquals(address, mMessageLogRecord.getAddress());

		mMessageLogRecord.setAddress(null);
		assertNull(mMessageLogRecord.getAddress());
	}
	public void testTimestamp() {
		long time = System.currentTimeMillis();
		mMessageLogRecord.setTimestamp(time);
		assertEquals(time, mMessageLogRecord.getTimestamp());

		time = -324123;
		mMessageLogRecord.setTimestamp(time);
		assertEquals(time, mMessageLogRecord.getTimestamp());

		time = 0;
		mMessageLogRecord.setTimestamp(time);
		assertEquals(time, mMessageLogRecord.getTimestamp());
	}
	public void testDirection() {
		Direction d;
		d = Direction.RECEIVE;
		mMessageLogRecord.setDirection(d);
		assertEquals(d, mMessageLogRecord.getDirection());

		d = Direction.SEND;
		mMessageLogRecord.setDirection(d);
		assertEquals(d, mMessageLogRecord.getDirection());
		// 验证（伪） setDirection()是深拷贝
		d = Direction.RECEIVE;
		assertFalse(Direction.RECEIVE.equals(mMessageLogRecord.getDirection()));

		d = null;
		mMessageLogRecord.setDirection(d);
		assertNull(mMessageLogRecord.getDirection());
	}
	public void testHasRead() {
		Boolean hasRead = true;
		mMessageLogRecord.setHasRead(hasRead);
		assertEquals(hasRead, mMessageLogRecord.hasRead());

		hasRead = false;
		mMessageLogRecord.setHasRead(hasRead);
		assertEquals(hasRead, mMessageLogRecord.hasRead());

		mMessageLogRecord.setHasRead(null);
		assertNull(mMessageLogRecord.hasRead());
	}
	public void testMessage() {
		Message m;
		m = Message.mDefaultValue;
		mMessageLogRecord.setMessage(m);
		assertEquals(m, mMessageLogRecord.getMessage());

		// 验证 可以设置并取回null
		mMessageLogRecord.setMessage(null);
		assertNull(mMessageLogRecord.getMessage());

		m = new Message();
		mMessageLogRecord.setMessage(m);
		assertEquals(m, mMessageLogRecord.getMessage());

		m = new Message();
		m.setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE
				| Message.Code.Extra.Inform.RESPOND);
		assertFalse(m.equals(mMessageLogRecord.getMessage()));
		mMessageLogRecord.setMessage(m);
		assertEquals(m, mMessageLogRecord.getMessage());

		m.setBody(MessageTest.TEST_CASE_BODY);
		// 验证 setMessage() 是深拷贝
		assertFalse(m.equals(mMessageLogRecord.getMessage()));
		mMessageLogRecord.setMessage(m);
		assertEquals(m, mMessageLogRecord.getMessage());

		// 验证 getMessage() 是深拷贝
		m = mMessageLogRecord.getMessage();
		m.setCode(Message.Code.COMMAND | Message.Code.Extra.Command.LOCATE_NOW);
		assertFalse(m.equals(mMessageLogRecord.getMessage()));
		mMessageLogRecord.setMessage(m);
		assertEquals(m, mMessageLogRecord.getMessage());

		// 验证 getMessageToSet() 是引用
		m = mMessageLogRecord.getMessageToSet();
		m.setCode(Message.Code.INFORM | Message.Code.Extra.Inform.URGENT)
			.setBody(MessageTest.TEST_CASE_BODY);
		assertTrue(m.equals(mMessageLogRecord.getMessage()));
	}

	public void testConstructor() {
		mMessageLogRecord = new MessageLogRecord();
		assertEquals(mMessageLogRecord, MessageLogRecord.mDefaultValue);

		Long id = 5234543L;
		Contact contact = new Contact();
		String address = "baijie1991@gmail.com";
		long time = System.currentTimeMillis();
		Direction direction = Direction.RECEIVE;
		Boolean hasRead = true;
		Message message = new Message(Message.Code.INFORM, MessageTest.TEST_CASE_BODY);
		mMessageLogRecord = new MessageLogRecord().setId(id).setContact(contact)
				.setAddress(address).setTimestamp(time).setDirection(direction)
				.setHasRead(hasRead).setMessage(message);
		assertEquals(id, mMessageLogRecord.getId());
		assertEquals(contact, mMessageLogRecord.getContact());
		assertEquals(address, mMessageLogRecord.getAddress());
		assertEquals(time, mMessageLogRecord.getTimestamp());
		assertEquals(direction, mMessageLogRecord.getDirection());
		assertEquals(hasRead, mMessageLogRecord.hasRead());
		assertEquals(message, mMessageLogRecord.getMessage());

		time = -System.currentTimeMillis();
		mMessageLogRecord = new MessageLogRecord().setId(null).setContact(null)
				.setAddress(null).setTimestamp(time).setDirection(null)
				.setHasRead(null).setMessage(null);
		assertNull(mMessageLogRecord.getId());
		assertNull(mMessageLogRecord.getContact());
		assertNull(mMessageLogRecord.getAddress());
		assertEquals(time, mMessageLogRecord.getTimestamp());
		assertNull(mMessageLogRecord.getDirection());
		assertNull(mMessageLogRecord.hasRead());
		assertNull(mMessageLogRecord.getMessage());
	}

	public void testEquals() {
		assertFalse(mMessageLogRecord.equals(null));
		assertFalse(mMessageLogRecord.equals(new Object()));
		assertTrue(mMessageLogRecord.equals(new MessageLogRecord()));
		assertTrue(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setId(Long.MIN_VALUE);
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setContact(new Contact());
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setAddress("baijie1991@gmail.com");
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setTimestamp(System.currentTimeMillis());
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setDirection(Direction.RECEIVE);
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setHasRead(false);
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setMessage(new Message());
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord.setMessage(MessageLogRecord.mDefaultValue.getMessage());
		assertEquals(MessageLogRecord.mDefaultValue, mMessageLogRecord);
	}

	public void testClone() {
		assertEquals(mMessageLogRecord, MessageLogRecord.mDefaultValue);
		// 注意: 默认值是null,也应当能正确地拷贝null
		assertEquals(mMessageLogRecord, MessageLogRecord.mDefaultValue.clone());
		try {
			MessageLogRecord.mDefaultValue.clone().setTimestamp(0);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}

		Long id = 5234543L;
		Contact contact = new Contact();
		String address = "baijie1991@gmail.com";
		long time = System.currentTimeMillis();
		Direction direction = Direction.RECEIVE;
		Boolean hasRead = true;
		Message message = new Message(Message.Code.INFORM, MessageTest.TEST_CASE_BODY);
		mMessageLogRecord.setId(id).setContact(contact)
				.setAddress(address).setTimestamp(time).setDirection(direction)
				.setHasRead(hasRead).setMessage(message);

		MessageLogRecord record;

		// 验证 拷贝件和原件相同
		assertEquals(mMessageLogRecord, mMessageLogRecord.clone());

		// 验证 对 拷贝件的修改不影响原对象
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 修改拷贝件
		record.clone().setId(4312435L).setContact(null)
		.setAddress("fasdfadsf").setTimestamp(-4431534).setDirection(Direction.SEND)
		.setHasRead(false).setMessage(null);
		// 原对象不受影响
		assertEquals(mMessageLogRecord, record);

		//TODO 验证Contact是影子拷贝
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 如果是影子拷贝，修改拷贝件里的对象，原对象也会受影响
//		record.clone().getContact().set();
		// 原对受影响
//		assertFalse(mMessageLogRecord.equals(record));
		// Contact 是同一引用
		assertTrue(record.clone().getContact() == record.getContact());

		// 验证 Message是深拷贝
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 如果是影子拷贝，修改拷贝件里的对象，原对象也会受影响
		record.clone().getMessageToSet().setBody(MessageTest.TEST_CASE_BODY);
		// 原对象不受影响
		assertEquals(mMessageLogRecord, record);
	}
}
