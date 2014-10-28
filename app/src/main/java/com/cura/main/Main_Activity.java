package com.cura.main;

/*
 * Description: This is the activity that the user gets dropped to after they have logged in successfully and this is where
 * all of Cura's facilities are shown (Terminal, SysLog, SysMonitor, etc...). The options provided in this activity are
 * Server Info and Logout. 
 */

import net.hockeyapp.android.CrashManager;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Constants;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
import com.cura.connection.CommunicationInterface;
import com.cura.connection.ConnectionService;
import com.cura.nmap.NMAP_Activity;
import com.cura.serverstats.ServerStats_Activity;
import com.cura.syslog.Syslog_Activity;
import com.cura.sysmonitor.SysMonitor_Activity;
import com.cura.terminal.Terminal_Activity;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

public class Main_Activity extends TabActivity implements OnClickListener,
		OnTouchListener {

	private TabHost tabHost;
	private Server serverTemp;
	private CommunicationInterface conn;
	private NotificationManager mNotificationManager;

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			conn = CommunicationInterface.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			conn = null;
			Toast.makeText(Main_Activity.this, R.string.serviceDisconnected,
					Toast.LENGTH_LONG).show();
		}
	};

	public void doBindService() {
		Intent i = new Intent(this, ConnectionService.class);
		bindService(i, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);

		setContentView(R.layout.act_main);
		
		doBindService();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			serverTemp = extras.getParcelable("server");
		}

		setTitle(TitleFont_Customizer.makeStringIntoTitle(this,
				serverTemp.getUsername() + "@" + serverTemp.getDomain()));

		tabHost = (TabHost) findViewById(android.R.id.tabhost);

		TabSpec serverstats = tabHost.newTabSpec(getResources().getString(
				R.string.ServerStatsLabel));
		serverstats.setIndicator("",
				getResources().getDrawable(R.drawable.serverstats_tab_selector));
		Intent serverStatsIntent = new Intent(this, ServerStats_Activity.class);
		serverStatsIntent.putExtra("server", serverTemp);
		serverstats.setContent(serverStatsIntent);
		tabHost.addTab(serverstats);

		TabSpec sysLogSpec = tabHost.newTabSpec(getResources().getString(
				R.string.SysLogLabel));
		sysLogSpec.setIndicator("",
				getResources().getDrawable(R.drawable.syslog_tab_selector));
		Intent sysLogIntent = new Intent(this, Syslog_Activity.class);
		sysLogIntent.putExtra("server", serverTemp);
		sysLogSpec.setContent(sysLogIntent);
		tabHost.addTab(sysLogSpec);

		TabSpec sysMonitorSpec = tabHost.newTabSpec(getResources().getString(
				R.string.SysMonitorLabel));
		sysMonitorSpec.setIndicator("",
				getResources().getDrawable(R.drawable.sysmonitor_tab_selector));
		Intent sysMonitorIntent = new Intent(this, SysMonitor_Activity.class);
		sysMonitorIntent.putExtra("server", serverTemp);
		sysMonitorSpec.setContent(sysMonitorIntent);
		tabHost.addTab(sysMonitorSpec);

		TabSpec NmapSpec = tabHost.newTabSpec(getResources().getString(
				R.string.NMapLabel));
		NmapSpec.setIndicator("",
				getResources().getDrawable(R.drawable.nmap_tab_selector));
		Intent nmapIntent = new Intent(this, NMAP_Activity.class);
		nmapIntent.putExtra("server", serverTemp);
		NmapSpec.setContent(nmapIntent);
		tabHost.addTab(NmapSpec);

		TabSpec TerminalSpec = tabHost.newTabSpec(getResources().getString(
				R.string.TerminalLabel));
		TerminalSpec.setIndicator("",
				getResources().getDrawable(R.drawable.terminal_tab_selector));
		Intent terminalIntent = new Intent(this, Terminal_Activity.class);
		terminalIntent.putExtra("server", serverTemp);
		TerminalSpec.setContent(terminalIntent);
		tabHost.addTab(TerminalSpec);

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(Main_Activity.this, ConnectionService.class));
		unbindService(connection);
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			v.setBackgroundResource(R.drawable.moduleselectedhighlight);
			break;
		case MotionEvent.ACTION_UP:
			v.setBackgroundResource(0);
			break;
		case MotionEvent.ACTION_CANCEL:
			v.setBackgroundResource(0);
			break;
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		checkForCrashes();
	}

	@Override
	public void onClick(View arg0) {
	}

	@Override
	public void onStart() {
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
