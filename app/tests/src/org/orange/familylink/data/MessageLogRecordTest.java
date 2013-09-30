package org.orange.familylink.data;

import java.util.Date;

import junit.framework.TestCase;

import org.orange.familylink.data.MessageLogRecord.Direction;
import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.data.MessageTest.MockMessage;
import org.orange.familylink.sms.SmsMessage;

import android.content.Context;
import android.net.Uri;

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
		assertNull(MessageLogRecord.mDefaultValue.getDate());
		assertNull(MessageLogRecord.mDefaultValue.getStatus());
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
			defaultValue.setDate(null);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setStatus(null);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}
		try {
			defaultValue.setMessage(new SmsMessage());
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
	public void testDate() {
		Date date = new Date();
		mMessageLogRecord.setDate(date);
		assertEquals(date, mMessageLogRecord.getDate());

		date = new Date(Long.MIN_VALUE);
		mMessageLogRecord.setDate(date);
		assertEquals(date, mMessageLogRecord.getDate());

		date = new Date(0);
		mMessageLogRecord.setDate(date);
		assertEquals(date, mMessageLogRecord.getDate());

		mMessageLogRecord.setDate(null);
		assertNull(mMessageLogRecord.getDate());
	}
	public void testStatus() {
		Status s;
		s = Status.UNREAD;
		mMessageLogRecord.setStatus(s);
		assertEquals(s, mMessageLogRecord.getStatus());
		assertEquals(Direction.RECEIVE, mMessageLogRecord.getStatus().getDirection());

		s = Status.HAVE_READ;
		mMessageLogRecord.setStatus(s);
		assertEquals(s, mMessageLogRecord.getStatus());
		assertEquals(Direction.RECEIVE, mMessageLogRecord.getStatus().getDirection());

		s = Status.SENDING;
		mMessageLogRecord.setStatus(s);
		assertEquals(s, mMessageLogRecord.getStatus());
		assertEquals(Direction.SEND, mMessageLogRecord.getStatus().getDirection());

		s = Status.SENT;
		mMessageLogRecord.setStatus(s);
		assertEquals(s, mMessageLogRecord.getStatus());
		assertEquals(Direction.SEND, mMessageLogRecord.getStatus().getDirection());

		s = Status.DELIVERED;
		mMessageLogRecord.setStatus(s);
		assertEquals(s, mMessageLogRecord.getStatus());
		assertEquals(Direction.SEND, mMessageLogRecord.getStatus().getDirection());

		s = Status.FAILED_TO_SEND;
		mMessageLogRecord.setStatus(s);
		assertEquals(s, mMessageLogRecord.getStatus());
		assertEquals(Direction.SEND, mMessageLogRecord.getStatus().getDirection());

		mMessageLogRecord.setStatus(null);
		assertNull(mMessageLogRecord.getStatus());
	}
	public void testMessage() {
		Message m = new Message() {
			@Override
			public void send(Context context, Uri messageUri, String dest,
					String password) {}
			@Override
			public void receive(String receivedMessage, String password) {}
		};
		mMessageLogRecord.setMessage(m);
		assertEquals(m, mMessageLogRecord.getMessage());

		// 验证 可以设置并取回null
		mMessageLogRecord.setMessage(null);
		assertNull(mMessageLogRecord.getMessage());

		m = new MockMessage();
		mMessageLogRecord.setMessage(m);
		assertEquals(m, mMessageLogRecord.getMessage());

		m = new MockMessage();
		m.setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE
				| Message.Code.Extra.Inform.RESPONSE);
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
		Date date = new Date();
		Status status = Status.DELIVERED;
		Message message = new MockMessage().setCode(Message.Code.INFORM).setBody(MessageTest.TEST_CASE_BODY);
		mMessageLogRecord = new MessageLogRecord().setId(id).setContact(contact)
				.setAddress(address).setDate(date)
				.setStatus(status).setMessage(message);
		assertEquals(id, mMessageLogRecord.getId());
		assertEquals(contact, mMessageLogRecord.getContact());
		assertEquals(address, mMessageLogRecord.getAddress());
		assertEquals(date, mMessageLogRecord.getDate());
		assertEquals(status, mMessageLogRecord.getStatus());
		assertEquals(message, mMessageLogRecord.getMessage());

		mMessageLogRecord = new MessageLogRecord().setId(null).setContact(null)
				.setAddress(null).setDate(null)
				.setStatus(null).setMessage(null);
		assertNull(mMessageLogRecord.getId());
		assertNull(mMessageLogRecord.getContact());
		assertNull(mMessageLogRecord.getAddress());
		assertNull(mMessageLogRecord.getDate());
		assertNull(mMessageLogRecord.getStatus());
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
		mMessageLogRecord.setDate(new Date());
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setStatus(Status.FAILED_TO_SEND);
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord = new MessageLogRecord();
		mMessageLogRecord.setMessage(new MockMessage());
		assertFalse(mMessageLogRecord.equals(MessageLogRecord.mDefaultValue));

		mMessageLogRecord.setMessage(MessageLogRecord.mDefaultValue.getMessage());
		assertEquals(MessageLogRecord.mDefaultValue, mMessageLogRecord);
	}

	public void testClone() {
		assertEquals(mMessageLogRecord, MessageLogRecord.mDefaultValue);
		// 注意: 默认值是null,也应当能正确地拷贝null
		assertEquals(mMessageLogRecord, MessageLogRecord.mDefaultValue.clone());
		try {
			MessageLogRecord.mDefaultValue.clone().setDate(new Date(0));
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalStateException);
			System.out.println(e.getMessage());
		}

		Long id = 5234543L;
		Contact contact = new Contact();
		String address = "baijie1991@gmail.com";
		Date date = new Date();
		Status status = Status.HAVE_READ;
		Message message = new MockMessage().setBody(MessageTest.TEST_CASE_BODY)
				.setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE);
		mMessageLogRecord.setId(id).setContact(contact)
				.setAddress(address).setDate(date)
				.setStatus(status).setMessage(message);

		MessageLogRecord record;

		// 验证 拷贝件和原件相同
		assertEquals(mMessageLogRecord, mMessageLogRecord.clone());

		// 验证 对 拷贝件的修改不影响原对象
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 修改拷贝件
		record.clone().setId(4312435L).setContact(null)
		.setAddress("fasdfadsf").setDate(new Date(4431534))
		.setStatus(status).setMessage(null);
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

		// 验证 Date是深拷贝
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 如果是影子拷贝，修改拷贝件里的对象，原对象也会受影响
		record.clone().getDate().setTime(0);
		// 原对象不受影响
		assertEquals(mMessageLogRecord, record);
		// Date 不是同一引用
		assertFalse(record.clone().getDate() == record.getDate());

		// 验证 Message是深拷贝
		record = mMessageLogRecord.clone();
		assertEquals(mMessageLogRecord, record);
		// 如果是影子拷贝，修改拷贝件里的对象，原对象也会受影响
		record.clone().getMessageToSet().setBody(MessageTest.TEST_CASE_BODY);
		// 原对象不受影响
		assertEquals(mMessageLogRecord, record);
		// Message 不是同一引用
		assertFalse(record.clone().getMessage() == record.getMessage());
	}
}
