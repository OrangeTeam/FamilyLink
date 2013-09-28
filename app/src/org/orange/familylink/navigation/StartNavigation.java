package org.orange.familylink.navigation;

import java.util.List;

import org.orange.familylink.R;
import org.orange.familylink.util.Network;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

/**
 * 这个开启导航类有个静态方法，这个方法是用于紧急求助时受监护人把定位坐标发送给监护人时，在监护人端就调用这个方法，把受监护人的定位信息传入给静态方法
 * 然后静态方法就会启动谷歌地图进行导航，当然也就说明除了用受监护人传入的定位信息，这里还用了监护人的定位信息，这样就能从监护人导航到受监护人的位置
 * @author Orange Team
 *
 */
public class StartNavigation {

	/**
	 * 防止实例化
	 */
	private StartNavigation(){}

	/**
	 * 主要是用于紧急求助时受监护人把定位坐标发送给监护人时，在监护人端就调用这个方法，把受监护人的定位信息传入给这个方法，然后这个方法就会开启谷歌地图进行导航
	 * 当然也就是说明除了用受监护人传入的定位信息，这里还用了监护人的定位信息，这样就能从监护人导航到受监护人的位置
	 * @param context 上下文环境
	 * @param latitude 纬度double类型
	 * @param longitude 经度double类型
	 */
	public static void toStartNavigationApp(Context context, FragmentManager manager, double latitude, double longitude){
		//导航用的定位信息，包括起点到终点位置信息
		Uri location = Uri.parse("http://maps.google.com/maps?f=dsaddr=startLat startLng&daddr=" + latitude + " " + longitude + "&hl=en");
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
		//专用启动谷歌地图
		mapIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

		//看是否有应用课用于启动
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
		boolean isIntentSafe = activities.size() > 0;

		if(Network.isConnected(context)){
			if(isIntentSafe)
				//如果网络可用且有应用可以被启动进行导航就开启，但然这个可被开启的应用这里就是指谷歌地图了
				context.startActivity(mapIntent);
			else{
				//没有谷歌地图时就提示是否现在安装谷歌地图，如果是就会跳到谷歌地图安装页面
				NoMapsAppDialogFragment mDialog = new NoMapsAppDialogFragment();
				mDialog.show(manager, "noMapsAppDialog");
			}
		}else{
			//没网络就会提示是否打开网络设置
			Network.openNoConnectionDialog(manager);
		}
	}

	/**
	 * 这个内部类是用于构建一个对话框，对话框功能是提示用户是否跳到安装谷歌地图的页面
	 * @author Orange Team
	 *
	 */
	public static class NoMapsAppDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.no_maps_app_title)
			.setMessage(R.string.no_maps_app_message)
			.setPositiveButton(android.R.string.yes, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=com.google.android.apps.maps"));
					startActivity(intent);
				}
			})
			.setNegativeButton(android.R.string.cancel, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			return builder.create();
		}
	}

}
