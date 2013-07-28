/**
 *
 */
package org.orange.familylink.data;

import junit.framework.TestCase;

/**
 * @author Team Orange
 *
 */
public class MessageTest extends TestCase {
	private static final String TEST_CASE_BODY =
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
		mMessage = new Message();
	}

	public void testPreconditions() {
		assertEquals(Message.mDefaultValue.getCode(), mMessage.getCode());
		assertEquals(Message.mDefaultValue.getBody(), mMessage.getBody());
	}
	public void testDefaultValue() {
		assertEquals(Message.Code.UNDEFINED, Message.mDefaultValue.getCode());
		assertNull(Message.mDefaultValue.getBody());
	}
	public void testCode() {
		mMessage.setCode(Message.Code.COMMAND);
		assertEquals(Message.Code.COMMAND, mMessage.getCode());
		mMessage.setCode(Message.Code.INFORM);
		assertEquals(Message.Code.INFORM, mMessage.getCode());
		mMessage.setCode(Message.Code.UNDEFINED);
		assertEquals(Message.Code.UNDEFINED, mMessage.getCode());

		int code = Message.Code.UNDEFINED;
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
		mMessage = new Message();
		mMessage.equals(Message.mDefaultValue);

		int code = Message.Code.INFORM |Message.Code.Extra.Inform.PULSE
				| Message.Code.Extra.Inform.RESPOND | Message.Code.Extra.Inform.URGENT;
		mMessage = new Message(code, TEST_CASE_BODY);
		assertEquals(code, mMessage.getCode());
		assertEquals(TEST_CASE_BODY, mMessage.getBody());

		try {
			mMessage = new Message(0x200, TEST_CASE_BODY);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalArgumentException);
			System.out.println(e.getMessage());
		}
		try {
			mMessage = new Message(-2, TEST_CASE_BODY);
			fail( "Missing exception" );
		} catch(Exception e) {
			// Optionally make sure you get the correct Exception, too
			assertTrue(e instanceof IllegalArgumentException);
			System.out.println(e.getMessage());
		}
	}
	public void testEquals() {
		assertFalse(mMessage.equals(null));
		assertFalse(mMessage.equals(new Object()));
		assertTrue(mMessage.equals(new Message()));
		assertTrue(mMessage.equals(Message.mDefaultValue));

		mMessage.setBody("a");
		assertFalse(mMessage.equals(Message.mDefaultValue));

		mMessage.setBody(Message.mDefaultValue.getBody());
		assertTrue(mMessage.equals(Message.mDefaultValue));

		mMessage.setCode(Message.Code.COMMAND | Message.Code.Extra.Command.LOCATE_NOW);
		assertFalse(mMessage.equals(Message.mDefaultValue));

		mMessage.setCode(Message.Code.UNDEFINED);
		assertTrue(Message.mDefaultValue.equals(mMessage));
	}

	public void testJson() {
		String json = null;
		Message message2 = null;
		json = mMessage.toJson();
		System.out.println(json);
		message2 = Message.fromJson(json);
		assertTrue(mMessage.equals(message2));

		mMessage.setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE);
		mMessage.setBody(TEST_CASE_BODY);
		json = mMessage.toJson();
		System.out.println(json);
		message2 = Message.fromJson(json);
		assertTrue(mMessage.equals(message2));
	}
}
