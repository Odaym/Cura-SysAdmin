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

package com.cura.syslog;

/*
 * Description: This is the actual SysLog module Activity. Here is where the user can choose from a drop-down list the file
 * that they choose to view from the list of files that SysLog usually dumps to on any Linux machine. After choosing that, 
 * the user can then choose whether to Tail (last 10 lines) or Head (first 10 lines) that file. Added to which, they can 
 * choose to Tail or Head that file according to a user-specified number (e.g. the first 45 lines, the last 20 lines).
 */

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Constants;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
import com.cura.connection.CommunicationInterface;
import com.cura.connection.ConnectionService;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import net.hockeyapp.android.CrashManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Syslog_Activity extends Activity implements
		android.view.View.OnClickListener {

	private String logReadingPositions[] = new String[] { "Head", "Tail" };
	private String filesParams[] = new String[] { "errors.log", "kernel.log",
			"boot", "auth.log", "daemon.log", "dmesg.log", "crond.log", "user.log",
			"Xorg.0.log" };
	private Spinner headTail, logFiles;
	private CheckBox checkBox;
	private EditText lineNumbers;
	private Button getLogsButton;
	private Button saveLogsButton;
	private static final int WAIT = 100;
	private String loader_message = "";
	private ProgressDialog loader;
	private Server server;
	private File syslogDir;
	private FileWriter target;
	private NotificationManager mNotificationManager;
	private CommunicationInterface conn;

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			conn = CommunicationInterface.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			conn = null;
		}
	};

	public String sendAndReceive(String command) {
		String result = "";

		try {
			result = conn.executeCommand(command);
		} catch (RemoteException e) {
			Log.d("SysLog", e.toString());
		}
		return result;
	}

	public void doBindService() {
		Intent i = new Intent(this, ConnectionService.class);
		getApplicationContext()
				.bindService(i, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_syslog);

		Bundle extras = getIntent().getExtras();
		if (extras != null)
			server = (Server) extras.get("server");
		doBindService();
		initSpinners();
		getLogsButton = (Button) findViewById(R.id.sysLogButton);
		getLogsButton.setOnClickListener(this);
		saveLogsButton = (Button) findViewById(R.id.sysLogSaveLogsButton);
		saveLogsButton.setOnClickListener(this);
		saveLogsButton.setEnabled(false);
		saveLogsButton.setAlpha(0.5f);
		checkBox = (CheckBox) findViewById(R.id.EnableLineNumber);
		checkBox.setOnClickListener(this);
		checkBox.setChecked(false);
		lineNumbers = (EditText) findViewById(R.id.LinesNumber);
		lineNumbers.setEnabled(false);
		syslogDir = new File("/sdcard/Cura/Syslog");
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
			loader.show();
			break;
		}
		return super.onCreateDialog(id);
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.sysLogButton:
			if (checkBox.isChecked()) {
				String numberOfLines = lineNumbers.getText().toString();
				if (numberOfLines.equalsIgnoreCase("")) {
					Toast.makeText(Syslog_Activity.this, R.string.SysLogLineNumberprompt,
							Toast.LENGTH_SHORT).show();
				} else {
					getLogs(false);
				}
			} else {
				getLogs(false);
			}
			FlurryAgent.logEvent("SysLog_Got_Logs");
			break;
		case R.id.sysLogSaveLogsButton:
			if (checkBox.isChecked()) {
				String numberOfLines = lineNumbers.getText().toString();
				if (numberOfLines.equalsIgnoreCase("")) {
					Toast.makeText(Syslog_Activity.this, R.string.SysLogLineNumberprompt,
							Toast.LENGTH_SHORT).show();
				} else {
					getLogs(true);
				}
			} else {
				getLogs(true);
			}
			FlurryAgent.logEvent("SysLog_Got_Logs_Saved");
			break;
		case R.id.EnableLineNumber:
			lineNumbers.setEnabled(checkBox.isChecked());
			break;
		}
	}

	public void initSpinners() {
		headTail = (Spinner) findViewById(R.id.headTail);
		Spinner_Adapter headTailAdapter = new Spinner_Adapter(this,
				R.layout.spinners_row, R.id.itemTV, logReadingPositions);
		headTail.setAdapter(headTailAdapter);

		logFiles = (Spinner) findViewById(R.id.logFiles);
		Spinner_Adapter logFilesAdapter = new Spinner_Adapter(this,
				R.layout.spinners_row, R.id.itemTV, filesParams);
		logFiles.setAdapter(logFilesAdapter);
	}

	public void getLogs(final boolean saveLogs) {
		new AsyncTask<String, String, String>() {
			String command;
			String pos;
			String file;
			String result;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if (!saveLogs)
					loader_message = getString(R.string.fetchingLogs);
				else
					loader_message = getString(R.string.savingLogs);
				showDialog(WAIT);
				command = "";
				pos = headTail.getSelectedItemPosition() == 0 ? "head" : "tail";
				file = filesParams[logFiles.getSelectedItemPosition()];
			}

			@Override
			protected String doInBackground(String... params) {
				if (!checkBox.isChecked()) {
					command = pos + " /var/log/" + file;
				} else {
					int numberOfLines = Integer
							.parseInt(lineNumbers.getText().toString());
					command = pos + " -n " + numberOfLines + " /var/log/" + file;
				}
				result = sendAndReceive(command);
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				loader.dismiss();
				if (result.equalsIgnoreCase("")) {
					result = null;
				} else {
					saveLogsButton.setEnabled(true);
					saveLogsButton.setAlpha(1f);
				}
				if (!saveLogs) {
					Intent openLogsDialog = new Intent(Syslog_Activity.this,
							LogsDialog.class);
					openLogsDialog.putExtra("logsResult", result);
					openLogsDialog.putExtra("logsFileName", file);
					startActivity(openLogsDialog);
					EasyTracker.getInstance(Syslog_Activity.this).send(
							MapBuilder.createEvent("SysLog_Activity", "event", "Got Logs",
									null).build());
				} else {
					EasyTracker.getInstance(Syslog_Activity.this).send(
							MapBuilder.createEvent("SysLog_Activity", "event",
									"Got Logs and Saved", null).build());
					if (!syslogDir.exists()) {
						syslogDir.mkdir();
					}
					try {
						Date date = new Date();
						String dateString = date.getMonth() + "_" + date.getDay() + "_"
								+ date.getHours() + "_" + date.getMinutes();
						String fileName = server.getUsername() + "_"
								+ filesParams[logFiles.getSelectedItemPosition()] + "_"
								+ dateString + ".txt";
						target = new FileWriter("/sdcard/Cura/SysLog/" + fileName);
						target.append(result);
						target.flush();
						target.close();
						Toast
								.makeText(
										Syslog_Activity.this,
										getString(R.string.logsSaved) + " \"/SysLog/" + fileName
												+ "\"", Toast.LENGTH_LONG).show();
					} catch (IOException e) {
						Toast.makeText(Syslog_Activity.this, R.string.noLogsFoundContent,
								Toast.LENGTH_LONG).show();
					}
				}
			}
		}.execute();
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
