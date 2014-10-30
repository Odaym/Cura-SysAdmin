package com.cura.about;

/*
 * Description: This activity includes the About section of the application which is listed under the Menus option in the 
 * Login Screen. It mentions the Author of this application, the application's version and its Changelog, a means of e-mailing and the developers and a link to the application's website.
 */

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.cura.R;
import com.cura.classes.Constants;
import com.cura.classes.TitleFont_Customizer;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

import net.hockeyapp.android.CrashManager;

public class About_Activity extends Activity {

    private TextView curaVersionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_about);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(TitleFont_Customizer.makeStringIntoTitle(this,
                R.string.about));

        curaVersionTV = (TextView) findViewById(R.id.curaVersionTV);
        try {
            curaVersionTV.setText(getResources().getString(R.string.versionTitle) + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

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

    private void checkForCrashes() {
        CrashManager.register(this, Constants.HOCKEY_APP_ID);
    }
}