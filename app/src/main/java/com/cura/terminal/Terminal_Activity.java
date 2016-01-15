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

package com.cura.terminal;

/*
 * Description: In this Activity, we implement the means of talking to the server in a custom Terminal emulator that allows
 * the user to run any command that can be run on a live Linux command line screen. We also add a Favorites Screen here that 
 * will allow the user to add any number of their favorite commands to be executed when they choose one of them from the list.
 */

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Constants;
import com.cura.classes.Server;
import com.cura.connection.CommunicationInterface;
import com.cura.connection.ConnectionService;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import net.hockeyapp.android.CrashManager;

import java.text.DateFormat;
import java.util.Calendar;

public class Terminal_Activity extends Activity {

	private EditText input;
	private TextView resultsTV;
	private TextView userInfoTV;
	private ScrollView resultsSV;
	private Server serverTemp;
	private String username;
	private SpannableString resultingCommand, commandDate;
	private NotificationManager mNotificationManager;
	private CommunicationInterface conn;

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			conn = CommunicationInterface.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			conn = null;
			Toast.makeText(Terminal_Activity.this, R.string.serviceDisconnected,
					Toast.LENGTH_LONG);
		}
	};

	public void doBindService() {
		Intent i = new Intent(this, ConnectionService.class);
		getApplicationContext()
				.bindService(i, connection, Context.BIND_AUTO_CREATE);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_terminal);
		doBindService();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			serverTemp = extras.getParcelable("server");
		}
		if (serverTemp.getUsername().compareTo("root") == 0) {
			username = serverTemp.getUsername() + "@" + serverTemp.getDomain()
					+ ":~# ";
		} else {
			username = serverTemp.getUsername() + "@" + serverTemp.getDomain()
					+ ":~$ ";
		}

		userInfoTV = (TextView) findViewById(R.id.userInfoTV);
		userInfoTV.append(username);

		resultsTV = (TextView) findViewById(R.id.resultsTV);
		resultsSV = (ScrollView) findViewById(R.id.resultsSV);

		input = (EditText) findViewById(R.id.inputET);
		input.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					EasyTracker.getInstance(Terminal_Activity.this).send(
							MapBuilder.createEvent("Terminal_Activity", "button",
									"Terminal Command Issued", null).build());
					FlurryAgent.logEvent("Terminal_Issued_Command");
					String command = input.getText().toString();
					String results = "";
					try {
						results = conn.executeCommand(command);
					} catch (Exception e) {
						Log.d("Terminal", e.toString());
					}
					resultingCommand = new SpannableString("\n\u21B3 " + command + "\t\t");
					resultingCommand.setSpan(new StyleSpan(Typeface.BOLD), 0,
							resultingCommand.length(), 0);
					resultingCommand.setSpan(new RelativeSizeSpan(1.5f), 0,
							resultingCommand.length(), 0);
					resultingCommand.setSpan(
							new ForegroundColorSpan(Color.parseColor("#528b12")), 0,
							resultingCommand.length(), 0);

					String timeNow = DateFormat.getTimeInstance().format(
							Calendar.getInstance().getTime());
					commandDate = new SpannableString("[ " + timeNow + " ]");
					commandDate.setSpan(
							new ForegroundColorSpan(Color.parseColor("#614126")), 0,
							commandDate.length(), 0);

					resultsTV.append(resultingCommand);
					resultsTV.append(commandDate);
					resultsTV.append("\n\n");
					resultsTV.append(results);
					resultsSV.scrollBy(0, 500);
				}
				return false;
			}
		});
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

	}

	@Override
	protected void onDestroy() {
		super.onStop();
		getApplicationContext().unbindService(connection);
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStop(this);
		FlurryAgent.onStartSession(this, Constants.FLURRY_APP_ID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkForCrashes();
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStart(this);
		FlurryAgent.onEndSession(this);
	}

	private void checkForCrashes() {
		CrashManager.register(this, Constants.HOCKEY_APP_ID);
	}
}