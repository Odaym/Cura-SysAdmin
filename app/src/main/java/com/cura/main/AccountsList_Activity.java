package com.cura.main;

/*
 * Description: This is the accounts list activity, this activity is shown whenever the user presses the "Select Server" button at the 
 * login screen. Here we show the user the list of server accounts that are available and from here, the user can tap one of the accounts 
 * to get the password prompt for it and be able to access that server.
 */

import java.net.URISyntaxException;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Helper_Methods;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
import com.cura.connection.ConnectionService;
import com.cura.database.DBHelper;
import com.cura.validation.RegexValidator;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class AccountsList_Activity extends ListActivity {

	private static final int FILE_SELECT_CODE = 0;

	private final String connected = "cura.connected";
	private final String notConnected = "cura.not.connected";
	private final int MODIFY_SERVER = 4;
	private final int DELETE_SERVER = 5;
	private final int CLEAR_SERVER_KEY = 6;
	private AsyncTask<String, String, String> loginTask;
	private boolean passwordShown = false;
	private EditText privateKeyInput;
	private TextView loginpromptTV;
	private DBHelper DBHelper;
	private AlertDialog alert;
	private Intent changeGraphicsIntent;

	private List<Server> servers;
	private Server serverTemp;
	private AccountsList_Adapter array;
	private Intent goToMainActivity;
	private BroadcastReceiver connectionBR, finishActivityBR;
	private RegexValidator rv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(TitleFont_Customizer.makeStringIntoTitle(this,
				R.string.selectServerTitle));

		DBHelper = new DBHelper(this);
		servers = DBHelper.getAllServers();

		array = new AccountsList_Adapter(this, servers);
		setListAdapter(array);

		registerForContextMenu(getListView());

		rv = new RegexValidator();

		// fix for servers constructed with V_2.7's constructor (without privatekey
		// and passphrase)
		for (int i = 0; i < servers.size(); i++) {
			if (servers.get(i).getPrivateKey() == null) {
				DBHelper.clearServerKeys(servers.get(i).getUsername(), servers.get(i)
						.getDomain());
				FlurryAgent.logEvent("Deprecated_Server_Found : "
						+ servers.get(i).getUsername() + " " + servers.get(i).getDomain());
			}
		}

		finishActivityBR = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				finishActivity();
			}
		};

		connectionBR = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle extras = intent.getExtras();
				setProgressBarIndeterminateVisibility(false);
				if (extras != null) {
					serverTemp = extras.getParcelable("server");
				}
				if (intent.getAction().compareTo(connected) == 0) {
					goToMainActivity = new Intent(AccountsList_Activity.this,
							Main_Activity.class);
					goToMainActivity.putExtra("server", serverTemp);
					startActivity(goToMainActivity);
				} else {

					changeGraphicsIntent = new Intent();
					changeGraphicsIntent.setAction("com.cura.changeActivityGraphics");
					changeGraphicsIntent.putExtra("Connected", false);
					sendBroadcast(changeGraphicsIntent);

					stopService(new Intent(AccountsList_Activity.this,
							ConnectionService.class));
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(connected);
		intentFilter.addAction(notConnected);
		registerReceiver(connectionBR, intentFilter);

		IntentFilter finishActivityFilter = new IntentFilter();
		finishActivityFilter.addAction("com.cura.finishActivity");
		registerReceiver(finishActivityBR, finishActivityFilter);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
		// The server does not have a keyfile associated with it
		if (servers.get(position).getPrivateKey().compareTo("") == 0) {
			AlertDialog.Builder passwordAlert = new AlertDialog.Builder(
					AccountsList_Activity.this);

			passwordAlert.setTitle(TitleFont_Customizer.makeStringIntoTitle(
					getApplicationContext(), getApplicationContext().getResources()
							.getString(R.string.login)));

			LayoutInflater inflater = LayoutInflater.from(AccountsList_Activity.this);
			View view = inflater.inflate(R.layout.password_dialog, null);
			passwordAlert.setView(view);

			final EditText passField = (EditText) view
					.findViewById(R.id.passwordprompt);

			final ImageView showPassword_Eye = (ImageView) view
					.findViewById(R.id.showpassword_eye);

			showPassword_Eye.setAlpha(0.2f);
			showPassword_Eye.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!passwordShown) {
						passField.setTransformationMethod(null);
						showPassword_Eye.setAlpha(1f);
						passwordShown = true;
					} else {
						passField.setTransformationMethod(PasswordTransformationMethod
								.getInstance());
						showPassword_Eye.setAlpha(0.2f);
						passwordShown = false;
					}
				}
			});

			final CheckBox savePassword = (CheckBox) view
					.findViewById(R.id.savePassword);

			passwordAlert.setPositiveButton(TitleFont_Customizer.makeStringIntoTitle(
					getApplicationContext(), R.string.connect),
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, int whichButton) {
							InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							keyboard.hideSoftInputFromWindow(passField.getWindowToken(), 0);
							loginTask = new AsyncTask<String, String, String>() {
								Intent passServerObjectToService;

								@Override
								protected void onPreExecute() {
									dialog.dismiss();

									setProgressBarIndeterminateVisibility(true);

									Intent finishActivity = new Intent();
									finishActivity.setAction("com.cura.finishActivity");
									sendBroadcast(finishActivity);

									changeGraphicsIntent = new Intent();
									changeGraphicsIntent
											.setAction("com.cura.changeActivityGraphics");
									changeGraphicsIntent.putExtra("Connected", true);
									sendBroadcast(changeGraphicsIntent);
								}

								@Override
								protected String doInBackground(String... params) {
									if (savePassword.isChecked()) {
										DBHelper.savePasswordOrModifyExisting(passField.getText()
												.toString(), servers.get(position).getUsername(),
												servers.get(position).getDomain());
									}
									String pass = passField.getText().toString();
									servers.get(position).setPassword(pass);
									serverTemp = servers.get(position);
									passServerObjectToService = new Intent(
											AccountsList_Activity.this, ConnectionService.class);
									passServerObjectToService.putExtra("server", serverTemp);
									return null;
								}

								@Override
								protected void onPostExecute(String result) {
									EasyTracker.getInstance(AccountsList_Activity.this).send(
											MapBuilder.createEvent("Accounts_List", "button",
													"Logged In", null).build());
									FlurryAgent.logEvent("Login_Logged_In");
									startService(passServerObjectToService);
								}
							};
							loginTask.execute();
						}
					});

			passwordAlert.setNegativeButton(TitleFont_Customizer.makeStringIntoTitle(
					getApplicationContext(), R.string.cancel),

			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});

			passField.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
					String pass = passField.getText().toString();
					if (pass.length() > 0)
						alert.getButton(Dialog.BUTTON1).setEnabled(true);
					else if (pass.length() == 0)
						alert.getButton(Dialog.BUTTON1).setEnabled(false);
				}

				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}

				public void afterTextChanged(Editable s) {
				}
			});

			passField
					.setOnEditorActionListener(new TextView.OnEditorActionListener() {

						@Override
						public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
							alert.getButton(Dialog.BUTTON1).performClick();
							return false;
						}
					});

			alert = passwordAlert.create();
			alert.show();

			String serverPassword = DBHelper.getServerPassword(servers.get(position)
					.getUsername(), servers.get(position).getDomain());
			if (serverPassword.length() > 0) {
				passField.setText(serverPassword);
				alert.getButton(Dialog.BUTTON1).setEnabled(true);
			} else {
				alert.getButton(Dialog.BUTTON1).setEnabled(false);
			}
			// The server does have a keyfile associated with it
		} else if (servers.get(position).getPrivateKey().compareTo("") != 0) {
			// but no passphrase was entered
			if (DBHelper.getPrivatekeyPassphrase(servers.get(position).getUsername(),
					servers.get(position).getDomain()) == null) {
				AlertDialog.Builder passwordAlert = new AlertDialog.Builder(
						AccountsList_Activity.this);

				passwordAlert.setTitle(TitleFont_Customizer.makeStringIntoTitle(
						getApplicationContext(), getApplicationContext().getResources()
								.getString(R.string.login)));

				LayoutInflater inflater = LayoutInflater
						.from(AccountsList_Activity.this);
				View view = inflater.inflate(R.layout.password_dialog, null);
				passwordAlert.setView(view);

				final TextView loginPromptTV = (TextView) view
						.findViewById(R.id.loginpromptTV);

				loginPromptTV.setText(TitleFont_Customizer.makeStringIntoTitle(
						AccountsList_Activity.this, R.string.LoginScreenPassphrasePrompt));

				final EditText passField = (EditText) view
						.findViewById(R.id.passwordprompt);

				final ImageView showPassword_Eye = (ImageView) view
						.findViewById(R.id.showpassword_eye);

				showPassword_Eye.setAlpha(0.2f);
				showPassword_Eye.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!passwordShown) {
							passField.setTransformationMethod(null);
							showPassword_Eye.setAlpha(1f);
							passwordShown = true;
						} else {
							passField.setTransformationMethod(PasswordTransformationMethod
									.getInstance());
							showPassword_Eye.setAlpha(0.2f);
							passwordShown = false;
						}
					}
				});

				final CheckBox savePassphrase = (CheckBox) view
						.findViewById(R.id.savePassword);

				savePassphrase.setText(TitleFont_Customizer.makeStringIntoTitle(
						AccountsList_Activity.this, R.string.savePassphrase));

				passwordAlert.setPositiveButton(TitleFont_Customizer
						.makeStringIntoTitle(getApplicationContext(), R.string.connect),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, int whichButton) {
								InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								keyboard.hideSoftInputFromWindow(passField.getWindowToken(), 0);
								loginTask = new AsyncTask<String, String, String>() {
									Intent passServerObjectToService;

									@Override
									protected void onPreExecute() {
										dialog.dismiss();

										setProgressBarIndeterminateVisibility(true);

										Intent finishActivity = new Intent();
										finishActivity.setAction("com.cura.finishActivity");
										sendBroadcast(finishActivity);

										changeGraphicsIntent = new Intent();
										changeGraphicsIntent
												.setAction("com.cura.changeActivityGraphics");
										changeGraphicsIntent.putExtra("Connected", true);
										sendBroadcast(changeGraphicsIntent);
									}

									@Override
									protected String doInBackground(String... params) {
										if (savePassphrase.isChecked())
											DBHelper.savePassphrase(passField.getText().toString(),
													servers.get(position).getUsername(),
													servers.get(position).getDomain());
										String pass = passField.getText().toString();
										servers.get(position).setPassphrase(pass);
										serverTemp = servers.get(position);
										passServerObjectToService = new Intent(
												AccountsList_Activity.this, ConnectionService.class);
										passServerObjectToService.putExtra("server", serverTemp);
										return null;
									}

									@Override
									protected void onPostExecute(String result) {
										EasyTracker.getInstance(AccountsList_Activity.this).send(
												MapBuilder.createEvent("Accounts_List", "button",
														"Logged In With Key", null).build());
										FlurryAgent.logEvent("Login_Logged_In_With_Key");
										startService(passServerObjectToService);
									}
								};
								loginTask.execute();
							}
						});

				passwordAlert.setNegativeButton(TitleFont_Customizer
						.makeStringIntoTitle(getApplicationContext(), R.string.cancel),

				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

				alert = passwordAlert.create();
				alert.show();
			} else {
				loginTask = new AsyncTask<String, String, String>() {
					Intent passServerObjectToService;

					@Override
					protected void onPreExecute() {

						setProgressBarIndeterminateVisibility(true);

						Intent finishActivity = new Intent();
						finishActivity.setAction("com.cura.finishActivity");
						sendBroadcast(finishActivity);

						changeGraphicsIntent = new Intent();
						changeGraphicsIntent.setAction("com.cura.changeActivityGraphics");
						changeGraphicsIntent.putExtra("Connected", true);
						sendBroadcast(changeGraphicsIntent);
					}

					@Override
					protected String doInBackground(String... params) {
						serverTemp = servers.get(position);
						passServerObjectToService = new Intent(AccountsList_Activity.this,
								ConnectionService.class);
						passServerObjectToService.putExtra("server", serverTemp);
						return null;
					}

					@Override
					protected void onPostExecute(String result) {
						EasyTracker.getInstance(AccountsList_Activity.this).send(
								MapBuilder.createEvent("Accounts_List", "button",
										"Logged In With Key", null).build());
						FlurryAgent.logEvent("Login_Logged_In_With_Key");
						startService(passServerObjectToService);
					}
				};
				loginTask.execute();
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(TitleFont_Customizer.makeStringIntoTitle(
				getApplicationContext(), servers.get((int) info.id).getUsername() + "@"
						+ servers.get((int) info.id).getDomain()));
		menu.add(0, MODIFY_SERVER, 0, R.string.editServer).setIcon(
				R.drawable.ic_menu_edit);
		menu.add(0, CLEAR_SERVER_KEY, 0, R.string.clearKey);
		menu.add(0, DELETE_SERVER, 0, R.string.deleteServer).setIcon(
				R.drawable.ic_menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final int userIDint = (int) info.id;
		final String usernameCode = servers.get(userIDint).getUsername();
		final String domainCode = servers.get(userIDint).getDomain();
		final String privateKeyCode = servers.get(userIDint).getPrivateKey();
		final String passphraseCode = servers.get(userIDint).getPassphrase();
		final int portCode = servers.get(userIDint).getPort();

		switch (item.getItemId()) {
		case MODIFY_SERVER:
			EasyTracker.getInstance(this).send(
					MapBuilder.createEvent("Accounts_List", "button", "Modify Server",
							null).build());
			FlurryAgent.logEvent("Login_Modify_Server");
			final Dialog myDialog;
			myDialog = new Dialog(AccountsList_Activity.this);
			myDialog.setContentView(R.layout.add_server_dialog);
			myDialog.setTitle(TitleFont_Customizer.makeStringIntoTitle(
					getApplicationContext(), R.string.DialogTitle));
			myDialog.setCancelable(true);
			myDialog.setCanceledOnTouchOutside(true);

			final Button editServerBTN = (Button) myDialog
					.findViewById(R.id.addOrModifyBTN);
			editServerBTN.setText(R.string.applyEditServer);
			Button cancelButton = (Button) myDialog.findViewById(R.id.cancelBTN);

			final EditText usernameInput = (EditText) myDialog
					.findViewById(R.id.usernameTextField);
			final EditText domainInput = (EditText) myDialog
					.findViewById(R.id.domainTextField);
			final EditText portInput = (EditText) myDialog
					.findViewById(R.id.portTextField);
			privateKeyInput = (EditText) myDialog
					.findViewById(R.id.privateKeyTextField);

			final Button browsePrivateKey = (Button) myDialog
					.findViewById(R.id.browsePrivKeyBTN);

			browsePrivateKey.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					showFileChooser();
				}
			});

			TextWatcher watcher = null;
			watcher = new TextWatcher() {

				public void afterTextChanged(Editable s) {
				}

				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}

				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
					String username = usernameInput.getText().toString();
					String domain = domainInput.getText().toString();
					String port = portInput.getText().toString();
					if (rv.validateUsername(username) && !domain.equalsIgnoreCase("")
							&& !port.equalsIgnoreCase(""))
						editServerBTN.setEnabled(true);
					else
						editServerBTN.setEnabled(false);
				}
			};

			usernameInput.addTextChangedListener(watcher);
			domainInput.addTextChangedListener(watcher);
			portInput.addTextChangedListener(watcher);

			usernameInput.setText(usernameCode);
			domainInput.setText(domainCode);
			portInput.setText(portCode + "");
			privateKeyInput.setText(privateKeyCode);

			editServerBTN.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String username = usernameInput.getText().toString();
					String domain = domainInput.getText().toString();
					String privateKeyPath = privateKeyInput.getText().toString();
					int port;
					try {
						port = Integer.parseInt(portInput.getText().toString());
					} catch (Exception e) {
						port = servers.get(userIDint).getPort();
						Toast.makeText(AccountsList_Activity.this,
								R.string.portErrorModify, Toast.LENGTH_LONG).show();
					}

					String finalPassphrase = passphraseCode;

					// if a different private key was entered, wipe the existing
					// passphrase
					if (privateKeyCode != null)
						if (privateKeyPath.compareTo(privateKeyCode) != 0)
							finalPassphrase = null;

					DBHelper.modifyServer(new Server(username, domain, port, "",
							privateKeyPath, finalPassphrase), usernameCode, domainCode);
					servers = DBHelper.getAllServers();
					array = new AccountsList_Adapter(AccountsList_Activity.this, servers);
					setListAdapter(array);

					myDialog.cancel();
				}
			});

			cancelButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					myDialog.cancel();
				}
			});
			myDialog.show();
			return true;

		case CLEAR_SERVER_KEY:
			EasyTracker.getInstance(this).send(
					MapBuilder.createEvent("Accounts_List", "button", "Clear Server Key",
							null).build());
			FlurryAgent.logEvent("Login_Clear_Server_Key");
			DBHelper.clearServerKeys(usernameCode, domainCode);
			servers = DBHelper.getAllServers();
			new AlertDialog.Builder(AccountsList_Activity.this)
					.setTitle(
							TitleFont_Customizer.makeStringIntoTitle(
									AccountsList_Activity.this,
									getResources().getString(R.string.success)))
					.setMessage(
							TitleFont_Customizer.makeStringIntoTitle(
									AccountsList_Activity.this,
									getResources().getString(R.string.clearKeyResult)))
					.setPositiveButton(
							TitleFont_Customizer.makeStringIntoTitle(
									AccountsList_Activity.this,
									getResources().getString(R.string.ok)),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int arg1) {
									dialog.dismiss();
								}
							}).show();
			break;
		case DELETE_SERVER:
			EasyTracker.getInstance(this).send(
					MapBuilder.createEvent("Accounts_List", "button", "Delete Server",
							null).build());
			FlurryAgent.logEvent("Login_Delete_Server");
			DBHelper.deleteServer(usernameCode, domainCode);
			servers = DBHelper.getAllServers();
			if (servers.isEmpty())
				finish();
			else {
				array = new AccountsList_Adapter(AccountsList_Activity.this, servers);
				setListAdapter(array);
			}

			return true;
		}
		return true;
	}

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try {
			startActivityForResult(Intent.createChooser(intent, TitleFont_Customizer
					.makeStringIntoTitle(AccountsList_Activity.this, getResources()
							.getString(R.string.browsefiles))), FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(
					this,
					TitleFont_Customizer.makeStringIntoTitle(AccountsList_Activity.this,
							getResources().getString(R.string.installfilemanager)),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String path = null;
				try {
					path = Helper_Methods.getPath(this, uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				if (path == null) {
					new AlertDialog.Builder(AccountsList_Activity.this)
							.setMessage(
									TitleFont_Customizer.makeStringIntoTitle(
											AccountsList_Activity.this,
											getResources()
													.getString(R.string.privatekey_sdcard_error)))
							.setPositiveButton(
									TitleFont_Customizer.makeStringIntoTitle(
											AccountsList_Activity.this,
											getResources().getString(R.string.ok)),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int arg1) {
											dialog.dismiss();
										}
									}).show();
				} else {
					try {
						privateKeyInput.setText(path);
					} catch (NullPointerException e) {
						new AlertDialog.Builder(AccountsList_Activity.this)
								.setTitle(
										TitleFont_Customizer.makeStringIntoTitle(
												AccountsList_Activity.this, R.string.cannotProceed))
								.setMessage(
										TitleFont_Customizer.makeStringIntoTitle(
												AccountsList_Activity.this,
												getResources().getString(
														R.string.dontkeepactivitieserror)))
								.setPositiveButton(
										TitleFont_Customizer.makeStringIntoTitle(
												AccountsList_Activity.this,
												getResources().getString(R.string.ok)),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int arg1) {
												startActivityForResult(new Intent(
														android.provider.Settings.ACTION_SETTINGS), 0);
												finish();
											}
										}).show();
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void finishActivity() {
		super.finish();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(connectionBR);
		unregisterReceiver(finishActivityBR);
	}
}