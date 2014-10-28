package com.cura.serverstats;

/*
 * Description: Server Stats are general server information like its Vitals, Mounted Filesystems, Memory information,
 * Process Status and so on. The user will be able to refresh these stats while in the activity by going to Menu > Refresh. 
 */

import net.hockeyapp.android.CrashManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Bash;
import com.cura.classes.Constants;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
import com.cura.connection.CommunicationInterface;
import com.cura.connection.ConnectionService;
import com.cura.main.Login_Activity;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

public class ServerStats_Activity extends Activity {

	private final int WAIT = 2;
	private ProgressDialog loader;
	private String loader_message = "";

	private String hostnameResult, listeningIPResult, kernelversionResult,
			uptimeResult, lastbootResult, currentusersResult, nameOfUsersResult,
			loadaveragesResult, memoryoutputResult, filesystemsoutputResult,
			processstatusoutputResult;
	private String[] processIDs;
	private String processIDsingular;
	private String totalMem, freeMem, usedMem;
	private SwipeRefreshLayout swipeLayout;
	private Server serverTemp;

	private CommunicationInterface conn;

	private TextView hostname, listeningIP, kernelVersion, uptime, lastBoot,
			currentUsers, loadAverages, filesystemsOuput, processStatusOutput;

	private Button killProcessesButton;
	private NotificationManager mNotificationManager;

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			Log.d("ConnectionService", "Connected");
			conn = CommunicationInterface.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			conn = null;
			Toast.makeText(ServerStats_Activity.this, R.string.serviceDisconnected,
					Toast.LENGTH_LONG);
		}
	};

	public void doBindService() {
		Intent i = new Intent(this, ConnectionService.class);
		getApplicationContext()
				.bindService(i, connection, Context.BIND_AUTO_CREATE);
	}

	public synchronized String sendAndReceive(String command) {
		try {
			String result = conn.executeCommand(command);
			return result;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_serverstats);

		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(R.color.googleBlue, R.color.googleRed,
				R.color.googleYellow, R.color.googleGreen);
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				FlurryAgent.logEvent("ServerStats_Refresh");
				getStats(false);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						swipeLayout.setRefreshing(false);
					}
				}, 5000);
			}
		});

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			serverTemp = extras.getParcelable("server");
		}

		initView();
		doBindService();
		getStats(true);

		killProcessesButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ServerStats_Activity.this);
				builder.setTitle(TitleFont_Customizer.makeStringIntoTitle(
						ServerStats_Activity.this,
						getResources().getString(R.string.pickProcess)));
				builder.setItems(processIDs, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						sendAndReceive(Bash.getKillProcess + processIDs[item] + "`");
						FlurryAgent.logEvent("ServerStats_Killed_Process");
						getStats(true);
					}

				});
				builder.show();
			}
		});
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case WAIT:
			loader = new ProgressDialog(this);
			loader.setMessage(TitleFont_Customizer.makeStringIntoTitle(
					getApplicationContext(), loader_message));
			loader.setCancelable(false);
			return loader;
		}
		return super.onCreateDialog(id);
	}

	protected void getStats(final boolean firstTime) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				loader_message = getString(R.string.loader_message);
				if (firstTime)
					showDialog(WAIT);
				hostname.setAlpha(0.5f);
				listeningIP.setAlpha(0.5f);
				kernelVersion.setAlpha(0.5f);
				uptime.setAlpha(0.5f);
				lastBoot.setAlpha(0.5f);
				listeningIP.setAlpha(0.5f);
				currentUsers.setAlpha(0.5f);
				loadAverages.setAlpha(0.5f);
				filesystemsOuput.setAlpha(0.5f);
				processStatusOutput.setAlpha(0.5f);
			}

			@Override
			protected Void doInBackground(Void... params) {
				while (true) {
					if (conn != null) {
						listeningIPResult = serverTemp.getDomain();
						kernelversionResult = sendAndReceive(Bash.getKernelVersion);
						uptimeResult = sendAndReceive(Bash.getUptime);
						lastbootResult = sendAndReceive(Bash.getLastBootTime);
						currentusersResult = sendAndReceive(Bash.getCurrentUsers);
						nameOfUsersResult = sendAndReceive(Bash.getCurrentUsersNames);
						loadaveragesResult = sendAndReceive(Bash.getLoadAverages);
						memoryoutputResult = sendAndReceive(Bash.getMemoryOutput);
						hostnameResult = sendAndReceive(Bash.getHostname);
						filesystemsoutputResult = sendAndReceive(Bash.getFilesystems);
						processstatusoutputResult = sendAndReceive(Bash.getProcessStatus);
						processIDsingular = sendAndReceive(Bash.getProcessIDs);
						return null;
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (firstTime)
					loader.cancel();
				hostname.setText(getResources().getString(R.string.hostname));
				listeningIP.setText(getResources().getString(R.string.listeningIP));
				kernelVersion.setText(getResources().getString(R.string.kernelVersion));
				uptime.setText(getResources().getString(R.string.uptime));
				lastBoot.setText(getResources().getString(R.string.lastBoot));
				currentUsers.setText(getResources().getString(R.string.currentUsers));
				loadAverages.setText(getResources().getString(R.string.loadAverages));
				filesystemsOuput.setText("");
				processStatusOutput.setText("");
				hostname.append(" " + hostnameResult);
				listeningIP.append(" " + listeningIPResult);
				kernelVersion.append(" " + kernelversionResult);
				uptime.append(" " + uptimeResult);
				lastBoot.append(" " + lastbootResult);
				String usersResultsForAppending = " " + currentusersResult + " ( "
						+ nameOfUsersResult + " )";
				currentUsers.append(" " + usersResultsForAppending);
				loadAverages.append(" " + loadaveragesResult);
				createChartLayout(" " + memoryoutputResult);
				filesystemsOuput.append(" " + filesystemsoutputResult);
				processStatusOutput.append(" " + processstatusoutputResult);
				processIDs = processIDsingular.split("\n");
				hostname.setAlpha(1f);
				listeningIP.setAlpha(1f);
				kernelVersion.setAlpha(1f);
				uptime.setAlpha(1f);
				lastBoot.setAlpha(1f);
				listeningIP.setAlpha(1f);
				currentUsers.setAlpha(1f);
				loadAverages.setAlpha(1f);
				filesystemsOuput.setAlpha(1f);
				processStatusOutput.setAlpha(1f);
			}
		}.execute();
	}

	protected void initView() {
		hostname = (TextView) findViewById(R.id.hostname);
		listeningIP = (TextView) findViewById(R.id.listeningip);
		kernelVersion = (TextView) findViewById(R.id.kernelversion);
		uptime = (TextView) findViewById(R.id.uptime);
		lastBoot = (TextView) findViewById(R.id.lastboot);
		currentUsers = (TextView) findViewById(R.id.currentusers);
		loadAverages = (TextView) findViewById(R.id.loadaverages);
		filesystemsOuput = (TextView) findViewById(R.id.filesystemsoutput);
		processStatusOutput = (TextView) findViewById(R.id.processstatusoutput);
		killProcessesButton = (Button) findViewById(R.id.killprocessbutton);
	}

	public void createChartLayout(String s) {
		String data[] = s.split("--");
		try {
			totalMem = String.format("%.2f GB",
					Double.parseDouble(data[4].replaceAll("\\s", "")) / (1024 * 1024));
			usedMem = String.format("%.2f GB",
					Double.parseDouble(data[5].replaceAll("\\s", "")) / (1024 * 1024));
			freeMem = String.format("%.2f GB",
					Double.parseDouble(data[6].replaceAll("\\s", "")) / (1024 * 1024));
			TextView tv = (TextView) findViewById(R.id.totalMem);
			tv.setText(getResources().getString(R.string.totalMemory) + " "
					+ totalMem);
			tv = (TextView) findViewById(R.id.usedMem);
			tv.setText(getResources().getString(R.string.usedMemory) + " " + usedMem);
			tv = (TextView) findViewById(R.id.freeMem);
			tv.setText(getResources().getString(R.string.freeMemory) + " " + freeMem);
			LinearLayout memoryPieChartView = (LinearLayout) (findViewById(R.id.memoryPieChartView));
			memoryPieChartView.removeAllViews();
			memoryPieChartView.addView(new MemoryStatsPieChart().execute(this, data),
					new LayoutParams(300, 300));
		} catch (Exception e) {
			TextView tv = (TextView) findViewById(R.id.totalMem);
			tv.setText(getResources().getString(R.string.totalMemory) + " "
					+ totalMem);
			tv = (TextView) findViewById(R.id.usedMem);
			tv.setText(getResources().getString(R.string.usedMemory) + " " + usedMem);
			tv = (TextView) findViewById(R.id.freeMem);
			tv.setText(getResources().getString(R.string.freeMemory) + " " + freeMem);
		}
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
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
		FlurryAgent.onStartSession(this, Constants.FLURRY_APP_ID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkForCrashes();
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getApplicationContext().unbindService(connection);
	}

	private void checkForCrashes() {
		CrashManager.register(this, Constants.HOCKEY_APP_ID);
	}
}