package com.cura.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Helper_Methods;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
import com.cura.connection.ConnectionService;
import com.cura.database.DBHelper;
import com.cura.gridview.BaseDynamicGridAdapter;
import com.cura.gridview.DynamicGridView;
import com.cura.gridview.Item;
import com.cura.validation.RegexValidator;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Select_Server_Activity extends Activity {

    private final int FILE_SELECT_CODE = 0;

    private DBHelper dbHelper;
    private List<Item> totalItems;
    private DynamicGridView gridView;
    private Cells_Adapter cellsAdapter;

    private List<Server> servers;
    private AsyncTask<String, String, String> loginTask;
    private boolean passwordShown = false;
    private AlertDialog alert;

    private ProgressDialog loggingInDialog;
    private BroadcastReceiver connectionBR, serverPropertiesChangedBR, serverSettingsClickedBR;

    private EditText privateKeyInput;
    private TextView loginpromptTV;
    private RegexValidator rv;
    private Vibrator vibrator;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ActionBar actionBar = getActionBar();
        actionBar.setTitle(TitleFont_Customizer.makeStringIntoTitle(this,
                R.string.selectServerTitle));
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.act_select_server);

        dbHelper = new DBHelper(this);
        servers = dbHelper.getAllServers();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        rv = new RegexValidator();

        gridView = (DynamicGridView) findViewById(R.id.dynamic_grid);
        cellsAdapter = new Cells_Adapter(this,
                prepareServers(), 2);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                if (cellsAdapter.getItem(position).getPrivateKey().compareTo("") == 0) {
                    AlertDialog.Builder passwordAlert = new AlertDialog.Builder(
                            Select_Server_Activity.this);

                    passwordAlert.setTitle(TitleFont_Customizer.makeStringIntoTitle(
                            Select_Server_Activity.this, getResources()
                                    .getString(R.string.login) + " as " + cellsAdapter.getItem(position).getUsername() + "@" + cellsAdapter.getItem(position).getDomain()));

                    LayoutInflater inflater = LayoutInflater.from(Select_Server_Activity.this);
                    view = inflater.inflate(R.layout.password_dialog, null);
                    passwordAlert.setView(view);

                    final EditText passField = (EditText) view
                            .findViewById(R.id.passwordprompt);

                    final ImageView showPassword_Eye = (ImageView) view
                            .findViewById(R.id.showpassword_eye);

                    showPassword_Eye.setAlpha(0.2f);
                    showPassword_Eye.setOnClickListener(new View.OnClickListener() {
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
                                    Select_Server_Activity.this, R.string.connect),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, int whichButton) {
                                    InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    keyboard.hideSoftInputFromWindow(passField.getWindowToken(), 0);
                                    loginTask = new AsyncTask<String, String, String>() {
                                        Intent passServerObjectToService;

                                        @Override
                                        protected void onPreExecute() {
                                            dialog.dismiss();
                                            loggingInDialog = ProgressDialog.show(Select_Server_Activity.this, "",
                                                    "Logging in...", true);
                                        }

                                        @Override
                                        protected String doInBackground(String... params) {
                                            if (savePassword.isChecked()) {
                                                dbHelper.savePasswordOrModifyExisting(passField.getText()
                                                        .toString(), cellsAdapter.getItem(position).getId());
                                            }
                                            String pass = passField.getText().toString();
                                            Server tempServer = new Server(cellsAdapter.getItem(position).getUsername(), cellsAdapter.getItem(position).getDomain(), cellsAdapter.getItem(position).getPort(), cellsAdapter.getItem(position).getPassword(), cellsAdapter.getItem(position).getPrivateKey(), pass, cellsAdapter.getItem(position).getOrder());
                                            passServerObjectToService = new Intent(
                                                    Select_Server_Activity.this, ConnectionService.class);
                                            passServerObjectToService.putExtra("server", tempServer);
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(String result) {
                                            EasyTracker.getInstance(Select_Server_Activity.this).send(
                                                    MapBuilder.createEvent("Select_Server", "button",
                                                            "Logged In", null).build());
                                            FlurryAgent.logEvent("Login_Logged_In");
                                            startService(passServerObjectToService);
                                        }
                                    };
                                    loginTask.execute();
                                }
                            });

                    passwordAlert.setNegativeButton(TitleFont_Customizer.makeStringIntoTitle(
                                    Select_Server_Activity.this, R.string.cancel),

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

                    String serverPassword = dbHelper.getServerPassword(cellsAdapter.getItem(position).getId());
                    if (serverPassword.length() > 0) {
                        passField.setText(serverPassword);
                        alert.getButton(Dialog.BUTTON1).setEnabled(true);
                    } else {
                        alert.getButton(Dialog.BUTTON1).setEnabled(false);
                    }
                    // The server does have a keyfile associated with it
                } else if (cellsAdapter.getItem(position).getPrivateKey().compareTo("") != 0) {
                    // but no passphrase was saved
                    if (dbHelper.getPrivatekeyPassphrase(cellsAdapter.getItem(position).getId()) == null) {
                        AlertDialog.Builder passwordAlert = new AlertDialog.Builder(
                                Select_Server_Activity.this);

                        passwordAlert.setTitle(TitleFont_Customizer.makeStringIntoTitle(
                                Select_Server_Activity.this, getResources()
                                        .getString(R.string.login) + " as " + cellsAdapter.getItem(position).getUsername() + "@" + cellsAdapter.getItem(position).getDomain()));

                        LayoutInflater inflater = LayoutInflater
                                .from(Select_Server_Activity.this);
                        view = inflater.inflate(R.layout.password_dialog, null);
                        passwordAlert.setView(view);

                        final TextView loginPromptTV = (TextView) view
                                .findViewById(R.id.loginpromptTV);

                        loginPromptTV.setText(TitleFont_Customizer.makeStringIntoTitle(
                                Select_Server_Activity.this, R.string.LoginScreenPassphrasePrompt));

                        final EditText passField = (EditText) view
                                .findViewById(R.id.passwordprompt);

                        final ImageView showPassword_Eye = (ImageView) view
                                .findViewById(R.id.showpassword_eye);

                        showPassword_Eye.setAlpha(0.2f);
                        showPassword_Eye.setOnClickListener(new View.OnClickListener() {
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
                                Select_Server_Activity.this, R.string.savePassphrase));

                        passwordAlert.setPositiveButton(TitleFont_Customizer
                                        .makeStringIntoTitle(Select_Server_Activity.this, R.string.connect),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, int whichButton) {
                                        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        keyboard.hideSoftInputFromWindow(passField.getWindowToken(), 0);
                                        loginTask = new AsyncTask<String, String, String>() {
                                            Intent passServerObjectToService;

                                            @Override
                                            protected void onPreExecute() {
                                                dialog.dismiss();
                                                loggingInDialog = ProgressDialog.show(Select_Server_Activity.this, "",
                                                        "Logging in...", true);
                                            }

                                            @Override
                                            protected String doInBackground(String... params) {
                                                if (savePassphrase.isChecked())
                                                    dbHelper.savePassphrase(passField.getText().toString(),
                                                            cellsAdapter.getItem(position).getId());
                                                String pass = passField.getText().toString();
                                                Server tempServer = new Server(cellsAdapter.getItem(position).getUsername(), cellsAdapter.getItem(position).getDomain(), cellsAdapter.getItem(position).getPort(), cellsAdapter.getItem(position).getPassword(), cellsAdapter.getItem(position).getPrivateKey(), pass, cellsAdapter.getItem(position).getOrder());
                                                passServerObjectToService = new Intent(
                                                        Select_Server_Activity.this, ConnectionService.class);
                                                passServerObjectToService.putExtra("server", tempServer);
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(String result) {
                                                EasyTracker.getInstance(Select_Server_Activity.this).send(
                                                        MapBuilder.createEvent("Select_Server", "button",
                                                                "Logged In With Key", null).build());
                                                FlurryAgent.logEvent("Login_Logged_In_With_Key");
                                                startService(passServerObjectToService);
                                            }
                                        };
                                        loginTask.execute();
                                    }
                                });

                        passwordAlert.setNegativeButton(TitleFont_Customizer
                                        .makeStringIntoTitle(Select_Server_Activity.this, R.string.cancel),

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                });

                        alert = passwordAlert.create();
                        alert.show();
                        // The server does have a keyfile associated with it
                    } else {
                        // and a passphrase is already saved
                        loginTask = new AsyncTask<String, String, String>() {
                            Intent passServerObjectToService;

                            @Override
                            protected void onPreExecute() {
                                loggingInDialog = ProgressDialog.show(Select_Server_Activity.this, "",
                                        "Logging in...", true);
                            }

                            @Override
                            protected String doInBackground(String... params) {
                                Server tempServer = new Server(cellsAdapter.getItem(position).getUsername(), cellsAdapter.getItem(position).getDomain(), cellsAdapter.getItem(position).getPort(), cellsAdapter.getItem(position).getPassword(), cellsAdapter.getItem(position).getPrivateKey(), cellsAdapter.getItem(position).getPassPhrase(), cellsAdapter.getItem(position).getOrder());
                                passServerObjectToService = new Intent(Select_Server_Activity.this,
                                        ConnectionService.class);
                                passServerObjectToService.putExtra("server", tempServer);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(String result) {
                                EasyTracker.getInstance(Select_Server_Activity.this).send(
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
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                gridView.startEditMode();
                return false;
            }
        });

        gridView.setOnDropListener(new DynamicGridView.OnDropListener() {
            @Override
            public void onActionDrop() {
                for (int i = 0; i < totalItems.size()
                        - BaseDynamicGridAdapter.returnDeletedItems(); i++) {

                    dbHelper.updateServerOrder(cellsAdapter.getItem(i).getOrder(), cellsAdapter.getItem(i).getId());
                }
                gridView.stopEditMode();
            }
        });

        gridView.setAdapter(cellsAdapter);

        connectionBR = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                loggingInDialog.dismiss();
            }
        };

        serverPropertiesChangedBR = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                servers = dbHelper.getAllServers();

                cellsAdapter = new Cells_Adapter(Select_Server_Activity.this,
                        prepareServers(), 2);
                gridView.setAdapter(cellsAdapter);
            }
        };

        serverSettingsClickedBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle extras = intent.getExtras();
                setProgressBarIndeterminateVisibility(false);

                int serverPosition_fromIntent = 0;

                if (extras != null) {
                    serverPosition_fromIntent = extras.getInt("serverPosition");
                }

                EasyTracker.getInstance(context).send(
                        MapBuilder.createEvent("Select_Server_Activity", "button", "Modify Server",
                                null).build());
                FlurryAgent.logEvent("Login_Modify_Server");
                final Dialog myDialog;
                myDialog = new Dialog(context);
                myDialog.setContentView(R.layout.add_server_dialog);
                myDialog.setTitle(TitleFont_Customizer.makeStringIntoTitle(
                        context, R.string.DialogTitle));
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

                TextWatcher watcher = new TextWatcher() {

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

                usernameInput.setText(cellsAdapter.getItem(serverPosition_fromIntent).getUsername());
                domainInput.setText(cellsAdapter.getItem(serverPosition_fromIntent).getDomain());
                portInput.setText(cellsAdapter.getItem(serverPosition_fromIntent).getPort() + "");
                privateKeyInput.setText(cellsAdapter.getItem(serverPosition_fromIntent).getPrivateKey());

                final int saveServerPosition = serverPosition_fromIntent;

                editServerBTN.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String username = usernameInput.getText().toString();
                        String domain = domainInput.getText().toString();
                        String privateKeyPath = privateKeyInput.getText().toString();
                        int serverOrder = cellsAdapter.getItem(saveServerPosition).getOrder();
                        int port;

                        try {
                            port = Integer.parseInt(portInput.getText().toString());
                        } catch (Exception e) {
                            port = cellsAdapter.getItem(saveServerPosition).getPort();
                            Toast.makeText(Select_Server_Activity.this,
                                    R.string.portErrorModify, Toast.LENGTH_LONG).show();
                        }

                        String oldPrivateKeyPath = cellsAdapter.getItem(saveServerPosition).getPrivateKey();
                        String finalPassphrase = "";

                        // if a different private key was entered, wipe the existing
                        // passphrase
                        if (oldPrivateKeyPath != null)
                            if (privateKeyPath.compareTo(oldPrivateKeyPath) != 0)
                                finalPassphrase = null;

                        dbHelper.modifyServer(new Server(username, domain, port, "",
                                privateKeyPath, finalPassphrase, serverOrder), cellsAdapter.getItem(saveServerPosition).getId());


                        cellsAdapter = new Cells_Adapter(Select_Server_Activity.this,
                                prepareServers(), 2);
                        gridView.setAdapter(cellsAdapter);

                        myDialog.cancel();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        myDialog.cancel();
                    }
                });

                myDialog.show();
            }
        };

        IntentFilter serverSettingsClickedFilter = new IntentFilter();
        String serverSettingsClicked = "com.cura.serverSettingsClicked";
        serverSettingsClickedFilter.addAction(serverSettingsClicked);
        registerReceiver(serverSettingsClickedBR, serverSettingsClickedFilter);

        IntentFilter serverPropertiesChangedFilter = new IntentFilter();
        String serverPropertiesChangedString = "com.cura.serverPropertiesChanged";
        serverPropertiesChangedFilter.addAction(serverPropertiesChangedString);
        registerReceiver(serverPropertiesChangedBR, serverPropertiesChangedFilter);

        IntentFilter connectionIntentFilter = new IntentFilter();
        String connected = "cura.connected";
        connectionIntentFilter.addAction(connected);
        String notConnected = "cura.not.connected";
        connectionIntentFilter.addAction(notConnected);
        registerReceiver(connectionBR, connectionIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionBR);
        unregisterReceiver(serverPropertiesChangedBR);
        unregisterReceiver(serverSettingsClickedBR);
    }

    public List<Item> prepareServers() {
        List<Server> allServers = dbHelper.getAllServers();

        totalItems = new ArrayList<Item>();

        for (int i = 0; i < allServers.size(); i++) {
            Item item = new Item(allServers.get(i).getId(), allServers.get(i).getUsername(), allServers.get(i).getDomain(),
                    allServers.get(i).getPort(), allServers.get(i).getPrivateKey(), allServers.get(i).getPassphrase(), allServers.get(i).getPassword(), R.drawable.server, allServers.get(i).getOrder());

            totalItems.add(item);
        }

        return totalItems;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                        new AlertDialog.Builder(Select_Server_Activity.this)
                                .setTitle(
                                        TitleFont_Customizer.makeStringIntoTitle(Select_Server_Activity.this,
                                                getResources().getString(R.string.invalidkeypath)))
                                .setMessage(
                                        TitleFont_Customizer.makeStringIntoTitle(Select_Server_Activity.this,
                                                getResources()
                                                        .getString(R.string.privatekey_sdcard_error)))
                                .setPositiveButton(
                                        TitleFont_Customizer.makeStringIntoTitle(Select_Server_Activity.this,
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
                            new AlertDialog.Builder(Select_Server_Activity.this)
                                    .setTitle(
                                            TitleFont_Customizer.makeStringIntoTitle(
                                                    Select_Server_Activity.this, R.string.cannotProceed))
                                    .setMessage(
                                            TitleFont_Customizer.makeStringIntoTitle(
                                                    Select_Server_Activity.this,
                                                    getResources().getString(
                                                            R.string.dontkeepactivitieserror)))
                                    .setPositiveButton(
                                            TitleFont_Customizer.makeStringIntoTitle(
                                                    Select_Server_Activity.this,
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

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Browse Files"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast
                    .makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public boolean isFound(String username, String domain) {
        String userValue = "";
        String dom = "";
        servers = dbHelper.getAllServers();
        for (int i = 0; i < servers.size(); i++) {
            userValue = servers.get(i).getUsername();
            dom = servers.get(i).getDomain();
            if (userValue.compareTo(username) == 0 && dom.compareTo(domain) == 0)
                return true;
        }
        return false;
    }

}
