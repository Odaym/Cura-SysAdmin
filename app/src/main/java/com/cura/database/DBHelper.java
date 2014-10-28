package com.cura.database;

/*
 * Description: This class is used to access the database and write to it the information about user accounts and their
 * preferences plus the Favorite commands from the Terminal.
 */

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cura.classes.Server;

public class DBHelper extends SQLiteOpenHelper {
	public static final String databaseName = "userInfo.db";
	public static final int version = 3;
	public static final String SERVER_TABLE = "server";
	public static final String S_ID = "id";
	public static final String S_USERNAME = "username";
	public static final String S_DOMAIN = "domain";
	public static final String S_PORT = "port";
	public static final String S_PASSWORD = "password";
	public static final String S_PRIVATEKEY = "private_key";
	public static final String S_PASSPHRASE = "passphrase";

	private List<Server> servers;

	private File CuraDir, SyslogDir, NmapDir;
	Context context;

	public DBHelper(Context context) {
		super(context, databaseName, null, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SERVER_TABLE = "CREATE TABLE IF NOT EXISTS " + SERVER_TABLE
				+ " (" + S_USERNAME + " TEXT, " + S_DOMAIN + " TEXT, " + S_PORT
				+ " INTEGER, " + S_PASSWORD + " TEXT, " + S_PRIVATEKEY + " TEXT, "
				+ S_PASSPHRASE + " TEXT)";
		db.execSQL(CREATE_SERVER_TABLE);

		CuraDir = new File("/sdcard/Cura");
		CuraDir.mkdir();
		SyslogDir = new File("/sdcard/Cura/SysLog");
		SyslogDir.mkdir();
		NmapDir = new File("/sdcard/Cura/Nmap");
		NmapDir.mkdir();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (newVersion) {
		case 2:
			Log.d("DB", "Renaming table to Server");
			db.execSQL("ALTER TABLE user RENAME TO " + SERVER_TABLE);
			Log.d("DB", "Adding column to newly renamed table");
			db.execSQL("ALTER TABLE server ADD COLUMN password varchar(100)");
			Log.d("DB", "Dropping command table");
			db.execSQL("DROP TABLE commandTable");
			Log.d("DB", "Done upgrading to v2");
			break;
		case 3:
			// fix for servers constructed with V_2.7's constructor (without
			// privatekey
			// and passphrase)
			db.execSQL("ALTER TABLE server ADD COLUMN private_key varchar(100)");
			db.execSQL("ALTER TABLE server ADD COLUMN passphrase varchar(100)");
			servers = getAllServersForUpgrade(db);
			for (int i = 0; i < servers.size(); i++)
				clearServerKeys(servers.get(i).getUsername(), servers.get(i)
						.getDomain());

			Log.d("DB", "Done upgrading to v3");
			break;
		}
	}

	public void createServer(Server server) {
		SQLiteDatabase dbHandler = this.getWritableDatabase();
		ContentValues cv;

		cv = new ContentValues();
		cv.put(S_USERNAME, server.getUsername());
		cv.put(S_DOMAIN, server.getDomain());
		cv.put(S_PORT, server.getPort());
		cv.put(S_PASSWORD, "");
		cv.put(S_PRIVATEKEY, server.getPrivateKey());
		cv.put(S_PASSPHRASE, server.getPassphrase());
		dbHandler.insert(SERVER_TABLE, null, cv);
		dbHandler.close();
	}

	public List<Server> getAllServersForUpgrade(SQLiteDatabase dbHandler) {
		List<Server> servers = new LinkedList<Server>();

		String query = "SELECT * FROM " + SERVER_TABLE;
		Cursor cursor = dbHandler.rawQuery(query, null);
		Server server = null;
		if (cursor.moveToFirst()) {
			do {
				server = new Server(cursor.getString(0), cursor.getString(1),
						Integer.parseInt(cursor.getString(2)), cursor.getString(3),
						cursor.getString(4), cursor.getString(5));
				servers.add(server);
			} while (cursor.moveToNext());
		}

		cursor.close();

		return servers;
	}

	public List<Server> getAllServers() {
		SQLiteDatabase dbHandler = this.getReadableDatabase();
		List<Server> servers = new LinkedList<Server>();

		String query = "SELECT * FROM " + SERVER_TABLE;
		Cursor cursor = dbHandler.rawQuery(query, null);
		Server server = null;
		if (cursor.moveToFirst()) {
			do {
				server = new Server(cursor.getString(0), cursor.getString(1),
						Integer.parseInt(cursor.getString(2)), cursor.getString(3),
						cursor.getString(4), cursor.getString(5));
				servers.add(server);
			} while (cursor.moveToNext());
		}

		cursor.close();
		dbHandler.close();

		return servers;
	}

	public void modifyServer(Server server, String usernameCode, String domainCode) {
		SQLiteDatabase dbHandler = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(S_USERNAME, server.getUsername());
		values.put(S_DOMAIN, server.getDomain());
		values.put(S_PORT, server.getPort());
		values.put(S_PRIVATEKEY, server.getPrivateKey());
		values.put(S_PASSPHRASE, server.getPassphrase());

		String where = S_USERNAME + " = ? AND " + S_DOMAIN + " = ?";
		String[] whereArgs = { usernameCode, domainCode };
		try {
			dbHandler.update(SERVER_TABLE, values, where, whereArgs);
		} catch (Exception e) {
			Log.d("SQL", e.toString());
		}

		dbHandler.close();
	}

	public void deleteServer(String usernameCode, String domainCode) {
		SQLiteDatabase dbHandler = this.getWritableDatabase();
		try {
			String where = S_USERNAME + " = ? AND " + S_DOMAIN + " = ?";
			String[] whereArgs = { usernameCode, domainCode };
			dbHandler.delete(SERVER_TABLE, where, whereArgs);

		} catch (Exception e) {
			Log.d("SQL", e.toString());
		}
		dbHandler.close();
	}

	public void savePasswordOrModifyExisting(String password,
			String usernameCode, String domainCode) {
		SQLiteDatabase dbHandler = this.getWritableDatabase();
		ContentValues newValues = new ContentValues();

		newValues.put(S_PASSWORD, password);

		String where = S_USERNAME + " = ? AND " + S_DOMAIN + " = ?";
		String[] whereArgs = { usernameCode, domainCode };
		dbHandler.update(SERVER_TABLE, newValues, where, whereArgs);
		dbHandler.close();
	}

	public void savePassphrase(String passphrase, String usernameCode,
			String domainCode) {
		SQLiteDatabase dbHandler = this.getWritableDatabase();
		ContentValues newValues = new ContentValues();

		newValues.put(S_PASSPHRASE, passphrase);

		String where = S_USERNAME + " = ? AND " + S_DOMAIN + " = ?";
		String[] whereArgs = { usernameCode, domainCode };
		dbHandler.update(SERVER_TABLE, newValues, where, whereArgs);
		dbHandler.close();
	}

	public String getPrivatekeyPassphrase(String usernameCode, String domainCode) {
		SQLiteDatabase dbHandler = this.getReadableDatabase();
		String query = "SELECT " + S_PASSPHRASE + " FROM " + SERVER_TABLE
				+ " WHERE " + S_USERNAME + " = " + "\"" + usernameCode + "\"" + " AND "
				+ S_DOMAIN + " = " + "\"" + domainCode + "\"";
		Cursor cursor = dbHandler.rawQuery(query, null);

		String password = null;

		if (cursor.moveToFirst()) {
			if (cursor.getString(0) == null) {
				password = null;
			} else {
				password = cursor.getString(0);
			}
		}

		cursor.close();
		dbHandler.close();

		return password;
	}

	public void clearServerKeys(String usernameCode, String domainCode) {
		SQLiteDatabase dbHandler = this.getWritableDatabase();
		ContentValues newValues = new ContentValues();

		newValues.put(S_PRIVATEKEY, "");
		newValues.putNull(S_PASSPHRASE);

		String where = S_USERNAME + " = ? AND " + S_DOMAIN + " = ?";
		String[] whereArgs = { usernameCode, domainCode };
		dbHandler.update(SERVER_TABLE, newValues, where, whereArgs);
		dbHandler.close();
	}

	public String getServerPassword(String usernameCode, String domainCode) {
		SQLiteDatabase dbHandler = this.getReadableDatabase();
		String query = "SELECT " + S_PASSWORD + " FROM " + SERVER_TABLE + " WHERE "
				+ S_USERNAME + " = " + "\"" + usernameCode + "\"" + " AND " + S_DOMAIN
				+ " = " + "\"" + domainCode + "\"";
		Cursor cursor = dbHandler.rawQuery(query, null);

		String password = "";

		if (cursor.moveToFirst()) {
			if (cursor.getString(0) == null) {
				password = "";
			} else {
				password = cursor.getString(0);
			}
		}

		cursor.close();
		dbHandler.close();

		return password;
	}
}