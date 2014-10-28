/*
 Copyright© 2010, 2011 Ahmad Balaa, Oday Maleh

 This file is part of Cura.

	Cura is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cura is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cura.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cura.sysmonitor;

/*
 * Description: This is the SysMonitor module Activity. In here we construct an ongoing chart that tracks down the exact
 * percentages of CPU and RAM usage for a pleasant and accurate server-monitoring experience. The menu options available 
 * for this activity are Pause (where the monitoring pauses at the last fetched values) and Resume (where it resumes).
 */

import java.util.Date;

import net.hockeyapp.android.CrashManager;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Bash;
import com.cura.classes.Constants;
import com.cura.classes.TitleFont_Customizer;
import com.cura.connection.CommunicationInterface;
import com.cura.connection.ConnectionService;
import com.cura.main.Login_Activity;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

public class SysMonitor_Activity extends Activity {

	private static TimeSeries timeSeriesCPU, timeSeriesRAM;
	private static XYMultipleSeriesDataset dataset;
	private static XYMultipleSeriesRenderer renderer;
	private static XYSeriesRenderer rendererSeriesCPU, rendererSeriesRAM;
	private static GraphicalView view;
	private static Thread mThread;
	private static boolean state = true;
	private NotificationManager mNotificationManager;
	private CommunicationInterface conn;

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			Log.d("ConnectionService", "Connected");
			conn = CommunicationInterface.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			conn = null;
			Toast.makeText(SysMonitor_Activity.this, R.string.serviceDisconnected,
					Toast.LENGTH_LONG);
		}
	};

	public synchronized void sendAndReceive() {
		String resultCPU = "";
		String resultRAM = "";

		try {
			resultCPU = conn.executeCommand(Bash.getCPU);
			resultRAM = conn.executeCommand(Bash.getRAM);
			if (!resultCPU.equalsIgnoreCase("") && !resultRAM.equalsIgnoreCase("")) {
				if (Double.parseDouble(resultCPU) > 100)
					resultCPU = "100";
				if (Double.parseDouble(resultRAM) > 100)
					resultRAM = "100";
				timeSeriesCPU.add(new Date(), Double.parseDouble(resultCPU));
				timeSeriesRAM.add(new Date(), Double.parseDouble(resultRAM));
				view.repaint();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void doBindService() {
		Intent i = new Intent(this, ConnectionService.class);
		getApplicationContext()
				.bindService(i, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		doBindService();

		dataset = new XYMultipleSeriesDataset();

		renderer = new XYMultipleSeriesRenderer();
		renderer.setAxesColor(Color.BLUE);
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(25);
		renderer.setFitLegend(false);
		renderer.setGridColor(Color.LTGRAY);
		renderer.setPanEnabled(true, false);
		renderer.setPointSize(5);
		renderer.setXTitle("Time");
		renderer.setYTitle("Number");
		renderer.setYAxisMax(100);
		renderer.setYAxisMin(0);
		renderer.setMargins(new int[] { 20, 30, 20, 30 });
		renderer.setZoomButtonsVisible(true);
		renderer.setBarSpacing(20);
		renderer.setAntialiasing(true);
		renderer.setShowGrid(true);

		rendererSeriesCPU = new XYSeriesRenderer();
		rendererSeriesCPU.setColor(Color.RED);
		rendererSeriesCPU.setFillPoints(true);
		rendererSeriesCPU.setPointStyle(PointStyle.CIRCLE);

		rendererSeriesRAM = new XYSeriesRenderer();
		rendererSeriesRAM.setColor(Color.GREEN);
		rendererSeriesRAM.setFillPoints(true);
		rendererSeriesRAM.setPointStyle(PointStyle.X);

		renderer.addSeriesRenderer(rendererSeriesCPU);
		renderer.addSeriesRenderer(rendererSeriesRAM);
		timeSeriesCPU = new TimeSeries("CPU");
		timeSeriesRAM = new TimeSeries("RAM");

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		startThread();
		dataset.addSeries(timeSeriesCPU);
		dataset.addSeries(timeSeriesRAM);
		view = ChartFactory
				.getTimeChartView(this, dataset, renderer, "Consumption");
		view.refreshDrawableState();
		view.repaint();
		setContentView(view);

	}

	public void startThread() {
		mThread = new Thread() {
			public void run() {
				while (state) {
					try {
						Thread.sleep(500);
						sendAndReceive();
					} catch (InterruptedException IE) {
						IE.printStackTrace();
					}
				}
			}
		};
		mThread.start();
	}

	public void stopThread() {
		mThread = null;
		state = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			new AlertDialog.Builder(this)
					.setTitle(
							TitleFont_Customizer.makeStringIntoTitle(getApplicationContext(),
									R.string.logoutConfirmationTitle))
					.setMessage(
							TitleFont_Customizer.makeStringIntoTitle(getApplicationContext(),
									R.string.logoutConfirmationContent))
					.setPositiveButton(
							TitleFont_Customizer.makeStringIntoTitle(getApplicationContext(),
									R.string.yes), new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									try {
										Log.d("Connection", "connection closed");
									} catch (Exception e) {
										Log.d("Connection", e.toString());
									}
									Intent closeAllActivities = new Intent(
											getApplicationContext(), Login_Activity.class);
									closeAllActivities.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
											| Intent.FLAG_ACTIVITY_CLEAR_TASK);
									startActivity(closeAllActivities);

									mNotificationManager.cancelAll();
								}
							})
					.setNegativeButton(
							TitleFont_Customizer.makeStringIntoTitle(getApplicationContext(),
									R.string.no), new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).show();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopThread();
		getApplicationContext().unbindService(connection);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("SYSMONITOR", "onResume()");
		state = true;
		startThread();
		checkForCrashes();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopThread();
		Log.d("SYSMONITOR", "onPause()");
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
		FlurryAgent.onStartSession(this, Constants.FLURRY_APP_ID);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
		FlurryAgent.onEndSession(this);
	}

	private void checkForCrashes() {
		CrashManager.register(this, Constants.HOCKEY_APP_ID);
	}
}