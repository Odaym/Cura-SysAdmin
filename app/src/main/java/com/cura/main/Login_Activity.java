package com.cura.main;

/*
 * Description: This is the login screen and this is Cura's main first screen where the user will be dropped to upon accessing the 
 * application. Here is where we offer the user the ability to select a server account from the ones that they've added, add new ones or
 * modify existing server accounts. Also offered in this screen (through the menu) is the Settings tab and the About tab.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cura.R;
import com.cura.classes.Constants;
import com.cura.classes.Helper_Methods;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
import com.cura.connection.ConnectionService;
import com.cura.database.DBHelper;
import com.cura.validation.RegexValidator;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import net.hockeyapp.android.CrashManager;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Login_Activity extends Activity {

    private final int FILE_SELECT_CODE = 0;

    //    private final int MANAGE_KEYS = 0;
    private final int SETTINGS = 0;
    private final int REPORT_BUGS = 1;
    private final int RATE_US = 2;

    private List<String> drawerItems = new ArrayList<String>();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Drawer_Adapter drawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mDrawerWidth;
    private View mContentView;
    private View mDrawerContentView;
    private float mDrawerContentOffset;
    private TextView aboutTV;

    private Server tempServer;
    private BroadcastReceiver connectionBR, serverPropertiesChangedBR;

    private final String connected = "cura.connected";
    private final String notConnected = "cura.not.connected";
    private final String serverPropertiesChangedString = "com.cura.serverPropertiesChanged";

    private List<Server> servers;
    private EditText privateKeyInput;
    private DBHelper dbHelper;
    private RegexValidator rv;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cura.R.layout.act_login);

        dbHelper = new DBHelper(this);
        servers = dbHelper.getAllServers();

        final ActionBar actionBar = getActionBar();
        actionBar.setTitle(TitleFont_Customizer.makeStringIntoTitle(this,
                R.string.home));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawerListView);

//        drawerItems.add(getResources().getString(R.string.navdrawer_manage_keys));
        drawerItems.add(getResources().getString(R.string.navdrawer_settings));
        drawerItems.add(getResources().getString(R.string.navdrawer_report_issues));
        drawerItems.add(getResources().getString(R.string.navdrawer_rate_us));

        drawerAdapter = new Drawer_Adapter(this, drawerItems);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mContentView = mDrawerLayout.getChildAt(0);

        mDrawerContentView = mDrawerLayout
                .findViewById(R.id.drawer_content_container);

        mDrawerWidth = getResources().getDimensionPixelSize(
                R.dimen.drawer_width);
        mDrawerContentOffset = mDrawerWidth
                - getResources().getDimensionPixelSize(R.dimen.drawer_slide_out);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.home, R.string.home) {
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                float moveFactor = (mDrawerWidth * slideOffset);
                mDrawerContentView.setTranslationX(mDrawerContentOffset
                        * (1 - slideOffset));
                mContentView.setTranslationX(moveFactor);
            }

            public void onDrawerClosed(View view) {
                mContentView.setTranslationX(0);
                mDrawerContentView.setTranslationX(mDrawerContentOffset);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                mContentView.setTranslationX(mDrawerWidth);
                mDrawerContentView.setTranslationX(0);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setAdapter(drawerAdapter);

        aboutTV = (TextView) findViewById(R.id.aboutTV);
        try {
            SpannableString authorBoldPart = new SpannableString(getResources().getString(R.string.authorTitle));
            authorBoldPart.setSpan(new StyleSpan(Typeface.BOLD),
                    0, authorBoldPart.length(), 0);
            aboutTV.setText(getResources().getString(R.string.versionName) + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n\nby " + authorBoldPart);
        } catch (PackageManager.NameNotFoundException e) {
        }

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
//                    case MANAGE_KEYS:
//                        break;
                    case REPORT_BUGS:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        File sdCard = Environment.getExternalStorageDirectory();
                        File file = new File(sdCard.getAbsolutePath()
                                + "/Cura/Logs/Cura_Logs_DEBUG.txt");
                        Uri uri = Uri.fromFile(file);

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL,
                                new String[]{"support@cura.tools"});
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        i.putExtra(Intent.EXTRA_SUBJECT, "Issue with: "
                                + Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.DEVICE + ") - "
                                + Build.VERSION.RELEASE);
                        i.putExtra(Intent.EXTRA_TEXT, "What went wrong?\n\n");
                        try {
                            startActivity(Intent.createChooser(i, "Send e-mail through"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(Login_Activity.this,
                                    "There are no email clients installed.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        break;
                    case SETTINGS:
                        startActivity(new Intent(Login_Activity.this, Preference_Screen.class));
                        EasyTracker.getInstance(Login_Activity.this).send(
                                MapBuilder.createEvent("Login_Activity_Options", "button", "Settings",
                                        null).build());
                        FlurryAgent.logEvent("Login_Options");
                        break;
                    case RATE_US:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse("market://details?id=" + Constants.APP_MARKET_NAME)));
                        break;
                }
            }
        });

        SpannableString newServerTitleSpan = new SpannableString(getResources().getString(R.string.newServer));
        SpannableString selectServerTitleSpan = new SpannableString(getResources().getString(R.string.selectServer));
        Button newServer = (Button) findViewById(R.id.newServer);
        Button selectServer = (Button) findViewById(R.id.selectServer);

        newServerTitleSpan.setSpan(new StyleSpan(Typeface.BOLD), 0,
                newServerTitleSpan.length(), 0);
        newServer.setText(newServerTitleSpan);
        newServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addServer();
            }
        });

        selectServerTitleSpan.setSpan(new StyleSpan(Typeface.BOLD), 0,
                selectServerTitleSpan.length(), 0);
        selectServer.setText(selectServerTitleSpan);
        selectServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (servers.isEmpty())
                    addServer();
                else
                    startActivity(new Intent(Login_Activity.this, Select_Server_Activity.class));
            }
        });

        connectionBR = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                setProgressBarIndeterminateVisibility(false);
                if (extras != null) {
                    tempServer = extras.getParcelable("server");
                }
                if (intent.getAction().compareTo(connected) == 0) {
                    Intent goToMainActivity = new Intent(Login_Activity.this,
                            Server_Home_Activity.class);
                    goToMainActivity.putExtra("server", tempServer);
                    startActivity(goToMainActivity);
                } else {
                    stopService(new Intent(Login_Activity.this,
                            ConnectionService.class));
                }
            }
        };

        serverPropertiesChangedBR = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                servers = dbHelper.getAllServers();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(connected);
        intentFilter.addAction(notConnected);
        registerReceiver(connectionBR, intentFilter);

        IntentFilter serverPropertiesChangedFilter = new IntentFilter();
        serverPropertiesChangedFilter.addAction(serverPropertiesChangedString);
        registerReceiver(serverPropertiesChangedBR, serverPropertiesChangedFilter);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        rv = new RegexValidator();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    protected void addServer() {
        final Dialog myDialog;
        myDialog = new Dialog(Login_Activity.this);
        myDialog.setContentView(R.layout.add_server_dialog);
        myDialog.setTitle(TitleFont_Customizer.makeStringIntoTitle(
                getApplicationContext(), R.string.DialogTitle));
        myDialog.setCancelable(true);
        myDialog.setCanceledOnTouchOutside(true);

        final Button addServerBTN = (Button) myDialog
                .findViewById(R.id.addOrModifyBTN);
        addServerBTN.setText(R.string.addNewServer);
        addServerBTN.setEnabled(false);
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

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String username = usernameInput.getText().toString();
                String domain = domainInput.getText().toString();
                String port = portInput.getText().toString();
                if (rv.validateUsername(username) && !domain.equalsIgnoreCase("")
                        && !port.equalsIgnoreCase(""))
                    addServerBTN.setEnabled(true);
                else
                    addServerBTN.setEnabled(false);
            }
        };
        usernameInput.addTextChangedListener(watcher);
        domainInput.addTextChangedListener(watcher);
        portInput.addTextChangedListener(watcher);

        addServerBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EasyTracker.getInstance(Login_Activity.this).send(
                        MapBuilder.createEvent("Login_Activity", "event", "Server Added",
                                null).build());
                String username = usernameInput.getText().toString();
                String domain = domainInput.getText().toString();
                String privateKeyPath = privateKeyInput.getText().toString();
                int port;
                try {
                    port = Integer.parseInt(portInput.getText().toString());
                } catch (Exception e) {
                    port = 22;
                    Toast.makeText(Login_Activity.this, R.string.portError,
                            Toast.LENGTH_LONG).show();
                }

                if (!isFound(username, domain)) {
                    Server serverToInsert = new Server();
                    serverToInsert.setUsername(username);
                    serverToInsert.setDomain(domain);
                    serverToInsert.setPort(port);
                    serverToInsert.setPassword("");
                    serverToInsert.setPrivateKey(privateKeyPath);
                    serverToInsert.setPassphrase(null);
                    serverToInsert.setOrder(0);

                    dbHelper.createServer(serverToInsert);
                    servers = dbHelper.getAllServers();
                    myDialog.cancel();

                    Intent goToSelectServer_Activity = new Intent(Login_Activity.this, Select_Server_Activity.class);
                    startActivity(goToSelectServer_Activity);
                    FlurryAgent.logEvent("Login_Server_Added");
                } else {
                    Login_Activity.this.vibrator.vibrate(300);
                    new AlertDialog.Builder(Login_Activity.this)
                            .setMessage(
                                    TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
                                            getResources().getString(R.string.serverExists)))
                            .setPositiveButton(
                                    TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
                                            getResources().getString(R.string.ok)),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int arg1) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                    usernameInput.setText("");
                    domainInput.setText("");
                    portInput.setText("");
                    privateKeyInput.setText("");
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.cancel();
            }
        });
        myDialog.show();
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
                        new AlertDialog.Builder(Login_Activity.this)
                                .setTitle(
                                        TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
                                                getResources().getString(R.string.invalidkeypath)))
                                .setMessage(
                                        TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
                                                getResources()
                                                        .getString(R.string.privatekey_sdcard_error)))
                                .setPositiveButton(
                                        TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
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
                            new AlertDialog.Builder(Login_Activity.this)
                                    .setTitle(
                                            TitleFont_Customizer.makeStringIntoTitle(
                                                    Login_Activity.this, R.string.cannotProceed))
                                    .setMessage(
                                            TitleFont_Customizer.makeStringIntoTitle(
                                                    Login_Activity.this,
                                                    getResources().getString(
                                                            R.string.dontkeepactivitieserror)))
                                    .setPositiveButton(
                                            TitleFont_Customizer.makeStringIntoTitle(
                                                    Login_Activity.this,
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

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
    }

    @Override
    protected void onStart() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionBR);
        unregisterReceiver(serverPropertiesChangedBR);
        Helper_Methods.appendLog("wipe");
    }

    private void checkForCrashes() {
        CrashManager.register(this, Constants.HOCKEY_APP_ID);
    }
}