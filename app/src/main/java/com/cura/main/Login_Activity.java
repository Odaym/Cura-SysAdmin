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
import android.graphics.Typeface;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cura.R;
import com.cura.about.About_Activity;
import com.cura.classes.Constants;
import com.cura.classes.Helper_Methods;
import com.cura.classes.Server;
import com.cura.classes.TitleFont_Customizer;
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

    private final int MANAGE_KEYS = 0;
    private final int SETTINGS = 1;
    private final int REPORT_BUGS = 2;
    private final int RATE_US = 3;
    private final int ABOUT = 4;

    private List<String> drawerItems = new ArrayList<String>();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Drawer_Adapter drawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mDrawerWidth;
    private View mContentView;
    private View mDrawerContentView;
    private float mDrawerContentOffset;

    private Button actOnServer;
    private SpannableString actOnServerTitle;
    private List<Server> servers;
    private EditText privateKeyInput;
    //    private Server serverTemp;
    private DBHelper DBHelper;
    //    private boolean isConnected = false;
//    private LinearLayout buttonsLayout;
    private BroadcastReceiver changeActivityGraphicsBR;
    //    private Intent goToMainActivity;
    private RegexValidator rv;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cura.R.layout.act_login);

        DBHelper = new DBHelper(this);

        servers = DBHelper.getAllServers();

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(TitleFont_Customizer.makeStringIntoTitle(this,
                R.string.home));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mDrawerLayout.setDrawerShadow(getResources().getDrawable(R.drawable.drawer_shadow), Gravity.START);
        mDrawerList = (ListView) findViewById(R.id.drawerListView);

        drawerItems.add(getResources().getString(R.string.navdrawer_manage_keys));
        drawerItems.add(getResources().getString(R.string.navdrawer_settings));
        drawerItems.add(getResources().getString(R.string.navdrawer_report_issues));
        drawerItems.add(getResources().getString(R.string.navdrawer_rate_us));
        drawerItems.add(getResources().getString(R.string.navdrawer_about));

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
                R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {
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

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case MANAGE_KEYS:
                        break;
                    case REPORT_BUGS:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        File sdCard = Environment.getExternalStorageDirectory();
                        File file = new File(sdCard.getAbsolutePath()
                                + "/Cura/Logs/Cura_Logs_DEBUG.txt");
                        Uri uri = Uri.fromFile(file);

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL,
                                new String[]{"cura.app@gmail.com"});
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        i.putExtra(Intent.EXTRA_SUBJECT, "Issue with: "
                                + android.os.Build.MODEL + " - "
                                + android.os.Build.VERSION.RELEASE);
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
                    case ABOUT:
                        startActivity(new Intent(Login_Activity.this, About_Activity.class));
                        break;
                }
            }
        });

        actOnServer = (Button) findViewById(R.id.actOnServer);
        if (servers.isEmpty())
            actOnServerTitle = new SpannableString(getResources().getString(
                    R.string.newServer));
        else
            actOnServerTitle = new SpannableString(getResources().getString(
                    R.string.newServer));

        actOnServerTitle.setSpan(new StyleSpan(Typeface.BOLD), 0,
                actOnServer.length(), 0);

        actOnServer.setText(actOnServerTitle);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        rv = new RegexValidator();

        changeActivityGraphicsBR = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle connectedExtras = intent.getExtras();
                boolean connected = connectedExtras.getBoolean("Connected");
                if (connected) {
                    ((ImageView) findViewById(R.id.serverImage))
                            .setImageResource(R.drawable.serverconnecting);
                    ((TextView) findViewById(R.id.connecting))
                            .setVisibility(View.VISIBLE);
                    actOnServer.setVisibility(View.INVISIBLE);
                } else {
                    ((ImageView) findViewById(R.id.serverImage))
                            .setImageResource(R.drawable.serveroffline);
                    ((TextView) findViewById(R.id.connecting)).setVisibility(View.GONE);
                    actOnServer.setVisibility(View.VISIBLE);
                    new AlertDialog.Builder(Login_Activity.this)
                            .setTitle(
                                    TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
                                            R.string.error))
                            .setMessage(
                                    TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
                                            getResources().getString(R.string.credentialsWrong)))
                            .setPositiveButton(
                                    TitleFont_Customizer.makeStringIntoTitle(Login_Activity.this,
                                            getResources().getString(R.string.ok)),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int arg1) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                }
            }
        };

        IntentFilter changeActivityGraphicsFilter = new IntentFilter();
        changeActivityGraphicsFilter.addAction("com.cura.changeActivityGraphics");
        registerReceiver(changeActivityGraphicsBR, changeActivityGraphicsFilter);
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
                    Server serverToInsert = new Server(username, domain, port, "",
                            privateKeyPath, null);
                    DBHelper.createServer(serverToInsert);
                    myDialog.cancel();
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
        servers = DBHelper.getAllServers();
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
//        if (isConnected) {
//            goToMainActivity = new Intent(Login_Activity.this, Main_Activity.class);
//            goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            goToMainActivity.putExtra("server", serverTemp);
//            startActivity(goToMainActivity);
//        }
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
        unregisterReceiver(changeActivityGraphicsBR);
        Helper_Methods.appendLog("wipe");
    }

    private void checkForCrashes() {
        CrashManager.register(this, Constants.HOCKEY_APP_ID);
    }
}