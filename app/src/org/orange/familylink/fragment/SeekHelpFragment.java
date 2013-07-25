/**
 *
 */
package org.orange.familylink.fragment;

import org.orange.familylink.ListContactsActivity;
import org.orange.familylink.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * 求助{@link Fragment}
 * @author Team Orange
 */
public class SeekHelpFragment extends SherlockFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_seek_help, container, false);
		rootView.findViewById(R.id.contact).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listContacts();
			}
		});
		return rootView;
	}

	protected void listContacts(){
		Intent intent = new Intent(getActivity(), ListContactsActivity.class);
		startActivity(intent);
	}

}
