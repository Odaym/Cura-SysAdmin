package com.cura.connection;

/*
 * Description: This class is for use in detecting the availability of a connection while using Cura. When the phone loses
 * connection to the Internet due to an error or a user choice, the user will be kicked back to the login screen and a popup
 * dialog will appear informing the user that they have lost Internet connection.
 */

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Vibrator;
import android.widget.Toast;

import com.cura.R;
import com.cura.main.Login_Activity;

public class ConnectionBroadcastReceiver extends BroadcastReceiver {
	Vibrator vibrator;

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean noConnectivity = intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if (noConnectivity && isMyServiceRunning(context)) {
			context.stopService(new Intent(context, ConnectionService.class));
			Toast.makeText(context, R.string.connectionTimeoutMessage,
					Toast.LENGTH_LONG).show();
			vibrator.vibrate(300);
			Intent closeAllActivities = new Intent(context, Login_Activity.class);
			closeAllActivities.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(closeAllActivities);
		}
	}

	private boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.cura.connection.ConnectionService".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}
}