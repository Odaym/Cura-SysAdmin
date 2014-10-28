package com.cura.about;

/*
 * Description: This activity includes the About section of the application which is listed under the Menus option in the 
 * Login Screen. It mentions the Author of this application, the application's version and its Changelog, a means of e-mailing and the developers and a link to the application's website.
 */

import java.util.Vector;

import net.hockeyapp.android.CrashManager;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cura.R;
import com.cura.classes.Constants;
import com.cura.classes.TitleFont_Customizer;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

public class About_Activity extends ListActivity {

	private final int TWITTER = 5;
	private final int EMAIL = 6;
//	private final int WEBSITE = 7;
	private AboutAdapter aboutAdapter;
	private Vector<AboutClass> info = new Vector<AboutClass>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(TitleFont_Customizer.makeStringIntoTitle(this,
				R.string.about));
		fillInfotoVector();
		aboutAdapter = new AboutAdapter(About_Activity.this, info);
		setListAdapter(aboutAdapter);
		ListView list = getListView();
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				switch (position) {
				case EMAIL:
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

					String EmailValue[] = { getResources().getString(R.string.appEmail) };

					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, EmailValue);
					emailIntent.putExtra(android.content.Intent.EXTRA_CC, "");
					emailIntent.putExtra(android.content.Intent.EXTRA_BCC, "");
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
					startActivity(emailIntent);
					break;
//				case WEBSITE:
//					Uri uriUrl = Uri.parse(getResources().getString(R.string.appWebsite));
//					Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
//					startActivity(launchBrowser);
//					break;
				case TWITTER:
					Uri twitterURL = Uri.parse("https://twitter.com/CuraApp");
					Intent launchBrowserForTwitter = new Intent(Intent.ACTION_VIEW,
							twitterURL);
					startActivity(launchBrowserForTwitter);
					break;
				}
			}
		});
	}

	protected void fillInfotoVector() {
		AboutClass ab = new AboutClass("separator", getResources().getString(
				R.string.appInformationTitle));
		info.add(ab);
		ab = new AboutClass(getResources().getString(R.string.authorTitle),
				getResources().getString(R.string.authorContent));
		info.add(ab);
		ab = new AboutClass(getResources().getString(R.string.versionTitle),
				getResources().getString(R.string.versionContent));
		info.add(ab);
		ab = new AboutClass("separator", getResources().getString(
				R.string.contactInformationTitle));
		info.add(ab);
		ab = new AboutClass("Twitter", getResources().getString(R.string.followUs)
				+ " @CuraApp");
		info.add(ab);
		ab = new AboutClass(getResources().getString(R.string.emailTitle),
				getResources().getString(R.string.emailContent));
		info.add(ab);
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