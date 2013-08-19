/**
 *
 */
package org.orange.familylink.fragment;

import org.orange.familylink.R;
import org.orange.familylink.TumbleWarningActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 求助{@link Fragment}
 * @author Team Orange
 */
public class SeekHelpFragment extends Fragment {
	
	private Button but ;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_seek_help, container, false);
		but = (Button) rootView.findViewById(R.id.warningTest);
		but.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(),TumbleWarningActivity.class);
				startActivity(intent);
			}
		});
		return rootView;
	}

}
