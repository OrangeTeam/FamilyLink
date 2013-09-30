/**
 *
 */
package org.orange.familylink.data;

import android.content.Context;
import android.net.Uri;
import junit.framework.TestCase;

/**
 * @author Team Orange
 *
 */
public class MessageTest extends TestCase {
	static final String TEST_CASE_BODY =
			"badsfo放假阿道夫拉法基propellerバトル作品の原点『あやかしびと』の続編にあたる" +
					"『あやかしびと2　あやかしびと異伝-雷鷲は天に羽ばたく-』の序盤部分を" +
					"PDFファイルにてダウンロードが可能になります。スタッフはもちろん" +
					"シナリオ：東出祐一郎原画　　：中央東口の『あやかしびと』コンビ。" +
					"『あやかしびと』を未プレイの方は、廉価版やお得な3本パックも発売され" +
					"お求め安くなっていますので、ぜひこの機会に一度遊んでみてくださいね。" +
					"\u1D11E";

	private Message mMessage;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Call the super constructor (required by JUnit)
		super.setUp();
		mMessage = new MockMessage();
	}

	public void testPreconditions() {
		assertNull(mMessage.getCode());
		assertNull(mMessage.getBody());
	}
	public void testCode() {
		mMessage.setCode(Message.Code.COMMAND);
		assertEquals(Message.Code.COMMAND, mMessage.getCode().intValue());
		mMessage.setCode(Message.Code.INFORM);
		assertEquals(Message.Code.INFORM, mMessage.getCode().intValue());
		mMessage.setCode(null);
		assertNull(mMessage.getCode());

		Integer code = null;
		code = Message.Code.INFORM | Message.Code.Extra.Inform.PULSE;
		mMessage.setCode(code);
		assertEquals(code, mMessage.getCode());
		code = Message.Code.COMMAND | Message.Code.Extra.Command.LOCATE_NOW;
		mMessage.setCode(code);
		assertEquals(code, mMessage.getCode());
		// bad code
		code = Message.Code.COMMAND | Message.Code.Extra.Inform.URGENT;
		mMessage.setCode(code);
		assertEquals(code, mMessage.getCode());

		try {
			mMessage.setCode(0x200);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalArgumentException);
			System.out.println(e.getMessage());
		}
		try {
			mMessage.setCode(-0x2);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalArgumentException);
			System.out.println(e.getMessage());
		}
	}
	public void testBody() {
		mMessage.setBody(TEST_CASE_BODY);
		assertEquals(TEST_CASE_BODY, mMessage.getBody());

		mMessage.setBody(null);
		assertNull(mMessage.getBody());
	}
	public void testConstructor() {

		Integer code = Message.Code.INFORM |Message.Code.Extra.Inform.PULSE
				| Message.Code.Extra.Inform.RESPONSE | Message.Code.Extra.Inform.URGENT;
		mMessage = new MockMessage().setCode(code).setBody(TEST_CASE_BODY);
		assertEquals(code, mMessage.getCode());
		assertEquals(TEST_CASE_BODY, mMessage.getBody());

		try {
			mMessage = new MockMessage().setCode(0x200).setBody(TEST_CASE_BODY);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalArgumentException);
			System.out.println(e.getMessage());
		}
		try {
			mMessage = new MockMessage().setCode(-2).setBody(TEST_CASE_BODY);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalArgumentException);
			System.out.println(e.getMessage());
		}
	}
	public void testEquals() {
		Message defaultValue = new MockMessage();

		assertFalse(mMessage.equals(null));
		assertFalse(mMessage.equals(new Object()));
		assertTrue(mMessage.equals(new MockMessage()));

		mMessage.setBody("a");
		assertFalse(mMessage.equals(defaultValue));

		mMessage.setBody(defaultValue.getBody());
		assertTrue(mMessage.equals(defaultValue));

		mMessage.setCode(Message.Code.COMMAND | Message.Code.Extra.Command.LOCATE_NOW);
		assertFalse(mMessage.equals(defaultValue));

		mMessage.setCode(null);
		assertTrue(defaultValue.equals(mMessage));
	}
	public void testClone() {
		Message defaultValue = new MockMessage();

		assertTrue(mMessage.equals(defaultValue));
		assertTrue(mMessage.equals(defaultValue.clone()));

		mMessage.setBody(TEST_CASE_BODY)
			.setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE);
		assertEquals(mMessage, mMessage.clone());

		// 验证 是深拷贝
		Message m = mMessage.clone();
		assertEquals(mMessage, m);
		m.clone().setBody(TEST_CASE_BODY).setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE);
		assertEquals(mMessage, m);
	}

	public void testJson() {
		String json = null;
		Message message2 = null;
		json = mMessage.toJson();
		System.out.println(json);
		message2 = new MockMessage().fromJson(json);
		assertTrue(mMessage.equals(message2));

		mMessage.setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE);
		mMessage.setBody(TEST_CASE_BODY);
		json = mMessage.toJson();
		System.out.println(json);
		message2 = new MockMessage().fromJson(json);
		assertTrue(mMessage.equals(message2));
	}

	public static class MockMessage extends Message {
		@Override
		public void send(Context context, Uri messageUri, String dest,
				String password) {
			throw new UnsupportedOperationException();
		}
		@Override
		public void receive(String receivedMessage, String password) {
			throw new UnsupportedOperationException();
		}
	}
}
