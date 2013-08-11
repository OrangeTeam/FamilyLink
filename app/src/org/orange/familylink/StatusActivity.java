/**
 *
 */
package org.orange.familylink;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

/**
 * 显示应用的当前状态
 * @author Team Orange
 */
public class StatusActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}
	/**
	 * 初始化配置{@link ActionBar}
	 */
	protected void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

}
