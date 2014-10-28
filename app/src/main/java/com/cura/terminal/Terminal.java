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
 * Description: This class describes the way that we have chosen to interact with the server after having established an
 * SSH connection to it. We use a JSch (Java Secure channel) object to send/receive messages to/from the server and we do
 * this using a thread that can be ran/paused/etc...
 */

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import android.util.Log;

import com.cura.classes.Server;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Terminal extends Thread {

	private JSch jsch;
	private Session session;
	private Channel channel;

	private String username;
	private String host;
	private String password;
	private int port;
	private StringWriter writer;
	private InputStream in;
	private String result = "";
	int i = 0;

	public Terminal(final Server server) throws JSchException {
		writer = new StringWriter();
		username = server.getUsername();
		host = server.getDomain();
		password = server.getPassword();
		port = server.getPort();
		jsch = new JSch();

		// no keyfile
		if (server.getPrivateKey().compareTo("") == 0) {
			session = jsch.getSession(username, host, port);
			session.setPassword(password);
			// keyfile exists
		} else if (server.getPrivateKey().compareTo("") != 0) {
			jsch.addIdentity(server.getPrivateKey(), server.getPassphrase());
			session = jsch.getSession(username, host, port);
		}

		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		Log.i("Terminal", "connected");
		channel = session.openChannel("exec");
	}

	public synchronized String ExecuteCommand(String command) {

		try {
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.connect();

			in = channel.getInputStream();

			writer.getBuffer().setLength(0);
			IOUtils.copy(in, writer);
			result = writer.toString();

		} catch (Exception i) {
			return "";
		}
		return result;
	}

	public void close() {
		channel.disconnect();
		session.disconnect();
	}

	public boolean connected() {
		return session.isConnected();
	}
}