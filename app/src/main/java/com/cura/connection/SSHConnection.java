package com.cura.connection;

/*
 * Description: This is where the Connection Service is implemented. It's where we construct this service so that other 
 * activities can bind to it and..be connected via SSH to the server requested. We send it in an AsyncTask to establish that.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.cura.classes.Server;
import com.cura.terminal.Terminal;
import com.jcraft.jsch.JSchException;

public class SSHConnection extends AsyncTask<Server, String, String> {

	private final String connected = "cura.connected";
	private final String notConnected = "cura.not.connected";
	String result;
	Terminal terminal;

	@Override
	protected String doInBackground(Server... server) {
		try {
			terminal = new Terminal(server[0]);
			result = connected;
		} catch (JSchException e) {
			Log.d("Connection", e.toString());
			result = notConnected;
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		Log.d("Connection", result);
	}

	public synchronized String messageSender(String message) {
		return terminal.ExecuteCommand(message);
	}

	public boolean connected() {
		return terminal.connected();
	}

	public void closeConnection() {
		if (terminal != null)
			terminal.close();
	}
}