package org.orange.familylink;

import android.test.ActivityInstrumentationTestCase2;

/**
 * 功能测试用例类，使用标准系统上下文。
 * @author Team Orange
 * @see ActivityInstrumentationTestCase2
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
	private MainActivity mActivity;

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		// Call the super constructor (required by JUnit)
		super.setUp();

		mActivity = getActivity();
	}

	/**
	 * <blockquote>
	 * <p>Tests the initial values of key objects in the app under test, to ensure the initial
	 * conditions make sense. If one of these is not initialized correctly, then subsequent
	 * tests are suspect and should be ignored.</p>
	 * <p>译：测试被测试应用的关键对象的初始值，确保初始条件正常（有意义）。如果初始化不正常，那么随后的测试结果值得怀疑（不可信），应当忽略其结果。</p>
	 * </blockquote>
	 * <p>引用自<a href="http://developer.android.com/tools/samples/index.html">官方示例：SpinnerTest</a></p>
	 */
	public void testPreconditions() {
		assertTrue(mActivity.getActionBar() != null);
	}
}
