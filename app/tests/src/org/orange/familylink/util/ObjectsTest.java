package org.orange.familylink.util;

import junit.framework.TestCase;

public class ObjectsTest extends TestCase {
	public void testCompare() {
		String s1 = null, s2 = null;
		assertTrue(Objects.compare(s1, s2));

		s1 = "String 1 中文";
		assertFalse(Objects.compare(s1, s2));

		s1 = null;
		s2 = "";
		assertFalse(Objects.compare(s1, s2));

		s1 = "测试 Test！";
		s2 = "测试 Test！";
		assertTrue(Objects.compare(s1, s2));

		Object o1 = s2;
		assertTrue(Objects.compare(s1, o1));

		CharSequence s3 = new StringBuilder(s1);
		assertFalse(Objects.compare(s1, s3));
	}
}
