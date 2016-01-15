package com.cura.connection;

/*
 * Description: This is where the Connection service functionality is constructed. Meaning that this is where we implement
 * functions like executeCommand() which is used to execute a command at the terminal.
 */

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cura.R;
import com.cura.classes.Bash;
import com.cura.classes.Server;
import com.cura.main.Server_Home_Activity;
import com.cura.terminal.Terminal;

public class ConnectionService extends Service {

	private Server server;
	private SSHConnection sshconnection;
	private Terminal terminal;
	private Intent i = new Intent();
	private Handler mHandler = new Handler();
	private boolean run = true;
	private int usersNo = 0;
	private NotificationManager mNotificationManager;
	private Notification notification;
	private CharSequence contentTitle;
	private CharSequence contentText;
	private Context context;
	private int icon;
	private SharedPreferences prefs;
	private long timeInterval;

	private final CommunicationInterface.Stub mBinder = new CommunicationInterface.Stub() {
		public synchronized String executeCommand(String command)
				throws RemoteException {
			String result = "";
			try {
				result = sshconnection.messageSender(command);
			} catch (Exception e) {
				Log.d("ConnectionService", e.toString());
			}
			return result;
		}

		public void close() {
			sshconnection.closeConnection();
		}

		public boolean connected() {
			return terminal.connected();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		server = (Server) intent.getParcelableExtra("server");
		sshconnection = (SSHConnection) new SSHConnection().execute(server);
		try {
			i.setAction(sshconnection.get());
		} catch (Exception e) {
			Log.d("Connection", e.toString());
		}
		i.putExtra("server", server);
		sendBroadcast(i);

		new Thread(new Runnable() {

			public void run() {
				prefs = PreferenceManager
						.getDefaultSharedPreferences(ConnectionService.this);
				timeInterval = Long.parseLong(prefs.getString("minutes", "0"));
				if (timeInterval != 0) {
					try {
						usersNo = Integer.parseInt(mBinder
								.executeCommand("who | awk '{print $1}' | uniq | wc -l | xargs /bin/echo -n"));

					} catch (Exception e) {
						e.printStackTrace();
					}
					while (run) {
						try {
							Thread.sleep(timeInterval);
							if (run) {
								mHandler.post(new Runnable() {

									@SuppressWarnings("deprecation")
									public void run() {
										try {
											int users = usersNo;
											try {
												users = Integer.parseInt(mBinder
														.executeCommand(Bash.getCurrentUsers));
											} catch (NumberFormatException e) {
											}
											if (users > usersNo) {
												Log.d("Notification", "" + users);
												int newUsers = users - usersNo;
												usersNo = users;
												mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
												icon = R.drawable.curalogo;
												CharSequence tickerText = "Login Notification";
												long when = System.currentTimeMillis();

												notification = new Notification(icon, tickerText, when);
												context = getApplicationContext();
												contentTitle = "Cura";
												String msg;
												if (newUsers == 1)
													msg = "1 user has just logged in to "
															+ server.getDomain();
												else
													msg = newUsers + " users has just logged in to "
															+ server.getDomain();
												contentText = msg;

												Intent notificationIntent = new Intent(
														ConnectionService.this, Server_Home_Activity.class);
												notificationIntent.putExtra("server", server);
												notificationIntent
														.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
												PendingIntent contentIntent = PendingIntent
														.getActivity(ConnectionService.this, 0,
																notificationIntent, 0);

												notification.setLatestEventInfo(context, contentTitle,
														contentText, contentIntent);
												notification.defaults = Notification.DEFAULT_SOUND;
												notification.flags = Notification.FLAG_AUTO_CANCEL;
												mNotificationManager.notify(1, notification);
											} else
												usersNo = users;
										} catch (RemoteException e) {
											e.printStackTrace();
										}
									}
								});
							}
						} catch (Exception e) {
						}
					}
				}
			}
		}).start();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			run = false;
			mBinder.close();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		sshconnection = null;

		Log.d("Connection Service", "connection stopped ");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
}