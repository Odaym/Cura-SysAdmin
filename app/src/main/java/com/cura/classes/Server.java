package com.cura.classes;

/*
 * Description: This is just a User class and it's used to receive all of the user accounts' information and encapsulate 
 * them in an object.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class Server implements Parcelable {
	private String username;
	private String domain;
	private int port;
	private String password;
	private String privateKey;
	private String passPhrase;

	public Server(String username, String domain, int port, String password,
			String privateKey, String passPhrase) {
		this.username = username;
		this.domain = domain;
		this.port = port;
		this.password = password;
		this.privateKey = privateKey;
		this.passPhrase = passPhrase;
	}

	public Server(Parcel in) {
		username = in.readString();
		domain = in.readString();
		port = in.readInt();
		password = in.readString();
		privateKey = in.readString();
		passPhrase = in.readString();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public String getDomain() {
		return domain;
	}

	public int getPort() {
		return port;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPassphrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	public String getPassphrase() {
		return passPhrase;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(username);
		dest.writeString(domain);
		dest.writeInt(port);
		dest.writeString(password);
		dest.writeString(privateKey);
		dest.writeString(passPhrase);
	}

	public static final Parcelable.Creator<Server> CREATOR = new Parcelable.Creator<Server>() {
		public Server createFromParcel(Parcel in) {
			return new Server(in);
		}

		public Server[] newArray(int size) {
			return new Server[size];
		}
	};
}