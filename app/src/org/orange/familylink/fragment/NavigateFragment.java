/**
 *
 */
package org.orange.familylink.fragment;

import org.orange.familylink.location.LocationTracker;
import org.orange.familylink.navigation.StartNavigation;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 导航{@link Fragment}
 * @author Team Orange
 */

public class NavigateFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LocationTracker mTracker = new LocationTracker(getActivity());
		final double latitude = mTracker.getLatitude();
		final double longitude = mTracker.getLongitude();
		Button button = new Button(getActivity());
		button.setTextSize(15);
		button.setText("to navigation");
		button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				StartNavigation.toStartNavigationApp(getActivity(), getFragmentManager(), latitude, longitude);
			}
		});
		return button;
	}

}
