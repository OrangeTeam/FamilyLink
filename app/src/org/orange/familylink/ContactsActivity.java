/**
 *
 */
package org.orange.familylink;

import org.orange.familylink.fragment.ContactsFragment;

import android.app.Activity;
import android.os.Bundle;

/**
 * 联系人{@link Activity}
 * @author Team Orange
 * @see ContactsFragment
 */
public class ContactsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
	}

}
