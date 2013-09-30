/**
 *
 */
package org.orange.familylink.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.orange.familylink.data.Message.Code;
import static org.orange.familylink.data.Message.Code.EXTRA_BITS;

import junit.framework.TestCase;

/**
 * {@link Code}的测试用例
 * @author Team Orange
 */
public class MessageCodeTest extends TestCase {

	// ------------------ Main Code ------------------
	public void testIsLegalCode()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		int min = Code.INFORM;
		int max = Code.COMMAND | EXTRA_BITS;
		testValidRange(min, max, Code.class.getMethod("isLegalCode", int.class));
	}
	public void testIsInform()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		int min = Code.INFORM;
		int max = Code.INFORM | EXTRA_BITS;
		testValidRange(min, max, Code.class.getMethod("isInform", int.class));
	}
	public void testIsCommand()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		int min = Code.COMMAND;
		int max = Code.COMMAND | EXTRA_BITS;
		testValidRange(min, max, Code.class.getMethod("isCommand", int.class));
	}
	// ------------------ Extra ------------------
	// ---------- Inform ----------
	public void testHasSetRespond()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testFlag(Code.INFORM, Code.Extra.Inform.RESPONSE,
				Code.Extra.Inform.class.getMethod("hasSetRespond", int.class));
	}
	public void testHasSetPulse()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testFlag(Code.INFORM, Code.Extra.Inform.PULSE,
				Code.Extra.Inform.class.getMethod("hasSetPulse", int.class));
	}
	public void testHasSetUrgent()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testFlag(Code.INFORM, Code.Extra.Inform.URGENT,
				Code.Extra.Inform.class.getMethod("hasSetUrgent", int.class));
	}
	// ---------- Command ----------
	public void testHasSetEcho()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testFlag(Code.COMMAND, Code.Extra.Command.ECHO,
				Code.Extra.Command.class.getMethod("hasSetEcho", int.class));
	}
	public void testHasSetLocateNow()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testFlag(Code.COMMAND, Code.Extra.Command.LOCATE_NOW,
				Code.Extra.Command.class.getMethod("hasSetLocateNow", int.class));
	}

	// ------------------ Helper ------------------
	public static void testValidRange(int min, int max, Method testedMethod)
			throws IllegalAccessException, InvocationTargetException {
		for(int i = 1 ; i <= 100 ; i++)
			assertFalse((Boolean)testedMethod.invoke(null, min - i * 100));
		assertFalse((Boolean)testedMethod.invoke(null, min - 3));
		assertFalse((Boolean)testedMethod.invoke(null, min - 2));
		assertFalse((Boolean)testedMethod.invoke(null, min - 1));
		for(int i = min; i <= max ; i++)
			assertTrue((Boolean)testedMethod.invoke(null, i));
		assertFalse((Boolean)testedMethod.invoke(null, max + 1));
		assertFalse((Boolean)testedMethod.invoke(null, max + 2));
		assertFalse((Boolean)testedMethod.invoke(null, max + 3));
		for(int i = 1; i <= 100 ; i++)
			assertFalse((Boolean)testedMethod.invoke(null, max + i * 100));
	}
	public void testFlag(int mainCode, int testedFlag, Method testedMethod)
			throws IllegalAccessException, InvocationTargetException {
		int min = mainCode;
		int max = mainCode | EXTRA_BITS;
		for(int i = 1 ; i <= 100 ; i++)
			assertFalse((Boolean)testedMethod.invoke(null, (min - i * 100)));
		assertFalse((Boolean)testedMethod.invoke(null, min - 3));
		assertFalse((Boolean)testedMethod.invoke(null, min - 2));
		assertFalse((Boolean)testedMethod.invoke(null, min - 1));

		// 无flag
		int code = mainCode;
		assertFalse((Boolean)testedMethod.invoke(null, code));
		// 正好有待测flag
		code |= testedFlag;
		assertTrue((Boolean)testedMethod.invoke(null, code));
		// 全flag
		code |= EXTRA_BITS;
		assertTrue((Boolean)testedMethod.invoke(null, code));
		// 除待测flag外，全部set
		code = (mainCode | EXTRA_BITS) & (~testedFlag);
		assertFalse((Boolean)testedMethod.invoke(null, code));

		assertFalse((Boolean)testedMethod.invoke(null, max + 1));
		assertFalse((Boolean)testedMethod.invoke(null, max + 2));
		assertFalse((Boolean)testedMethod.invoke(null, max + 3));
		for(int i = 1; i <= 100 ; i++)
			assertFalse((Boolean)testedMethod.invoke(null, max + i * 100));
	}
}
