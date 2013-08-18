/**
 *
 */
package org.orange.familylink.fragment;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;
import org.orange.familylink.R;
import org.orange.familylink.data.Position;
import org.orange.familylink.location.LocationInfo;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

/**
 * 定位{@link Fragment}
 * @author Team Orange
 */
public class LocationFragment extends Fragment {

	private TextView text ;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_location_test, container, false);
		text = (TextView) view.findViewById(R.id.locationText);
		
		Context cont = view.getContext();
		//通过context的getSystemService来实例化LocationManager
		LocationManager locationManager = (LocationManager) cont.getSystemService(Context.LOCATION_SERVICE);
		//实例化定位的操作类LocationInfo，把LocationMnager当做参数传入。
		LocationInfo locationInfo = new LocationInfo(locationManager, 1000);
		Position po =locationInfo.getCurrentLocationInfo() ;
		//通过locationInfo的getCurrentLocationInfo方法获得当前位置信息，返回Postion对象，转换为String输出
		text.setText(po.toString());
		return view;
	}

}
