/**
 *
 */
package org.orange.familylink.fragment;

import org.orange.familylink.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 导航{@link Fragment}
 * @author Team Orange
 */

public class NavigateFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		TextView textView = new TextView(getActivity());
		textView.setText(R.string.navigate);
		return textView;
	}

}
