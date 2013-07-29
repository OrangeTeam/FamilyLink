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
		assertEquals(MessageLogRecord.mDefaultValue.getTimestamp(), mMessageLogRecord.getTimestamp());
		assertEquals(MessageLogRecord.mDefaultValue.getDirection(), mMessageLogRecord.getDirection());
		assertEquals(MessageLogRecord.mDefaultValue.getMessage(), mMessageLogRecord.getMessage());
	}
	public void testDefaultValue() {
		assertNull(MessageLogRecord.mDefaultValue.getId());
		assertEquals(0, MessageLogRecord.mDefaultValue.getTimestamp());
		assertNull(MessageLogRecord.mDefaultValue.getDirection());
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
			defaultValue.setMessage(new Message());
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
	}

	public void testId() {
		Long id = Long.valueOf(348123498);
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());

		id = Long.valueOf(0);
		mMessageLogRecord.setId(id);
		assertEquals(id, mMessageLogRecord.getId());

		id = Long.valueOf(-348123498);
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
		assertEquals(d, mMessageLogRecord.getDirection());
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

		Long id = Long.valueOf(5234543);
		long time = System.currentTimeMillis();
		Direction direction = Direction.RECEIVE;
		Message message = new Message(Message.Code.INFORM, MessageTest.TEST_CASE_BODY);
		mMessageLogRecord = new MessageLogRecord(id, time, direction, message);
		assertEquals(id, mMessageLogRecord.getId());
		assertEquals(time, mMessageLogRecord.getTimestamp());
		assertEquals(direction, mMessageLogRecord.getDirection());
		assertEquals(message, mMessageLogRecord.getMessage());

		id = null;
		time = -System.currentTimeMillis();
		direction = null;
		message = null;
		mMessageLogRecord = new MessageLogRecord(id, time, direction, message);
		assertEquals(id, mMessageLogRecord.getId());
		assertEquals(time, mMessageLogRecord.getTimestamp());
		assertEquals(direction, mMessageLogRecord.getDirection());
		assertEquals(message, mMessageLogRecord.getMessage());
	}

	public void testEquals() {
		assertFalse(mMessageLogRecord.equals(null));
		assertFalse(mMessageLogRecord.equals(new Object()));
		assertTrue(mMessageLogRecord.equals(new MessageLogRecord()));
		assertTrue(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord.setDirection(Direction.RECEIVE);
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setMessage(new Message());
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setId(Long.MIN_VALUE);
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setTimestamp(System.currentTimeMillis());
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord.setTimestamp(MessageLogRecord.mDefaultValue.getTimestamp());
		assertTrue(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));
	}

	public void testClone() {
		assertEquals(mMessageLogRecord, MessageLogRecord.mDefaultValue);
		// 注意: 默认值中的Messga是null,也应当能正确地拷贝null
		assertEquals(mMessageLogRecord, MessageLogRecord.mDefaultValue.clone());
		try {
			MessageLogRecord.mDefaultValue.clone().setTimestamp(0);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}

		MessageLogRecord record;
		mMessageLogRecord.setMessage(new Message(
				Message.Code.INFORM | Message.Code.Extra.Inform.PULSE,
				MessageTest.TEST_CASE_BODY));
		mMessageLogRecord.setId(Long.MAX_VALUE);
		mMessageLogRecord.setDirection(Direction.SEND);
		mMessageLogRecord.setTimestamp(System.currentTimeMillis());

		// 验证 拷贝件和原件相同
		assertEquals(mMessageLogRecord, mMessageLogRecord.clone());

		// 验证 对 拷贝件的修改不影响原对象
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 修改拷贝件
		record.clone().setMessage(null).setDirection(Direction.RECEIVE).setTimestamp(System.currentTimeMillis()).setId(Long.MAX_VALUE);
		// 原对象不受影响
		assertEquals(mMessageLogRecord, record);

		// 验证 是深拷贝
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 如果是影子拷贝，修改拷贝件里的对象，原对象也会受影响
		record.clone().getMessageToSet().setBody(MessageTest.TEST_CASE_BODY);
		// 原对象不受影响
		assertEquals(mMessageLogRecord, record);
	}
}
