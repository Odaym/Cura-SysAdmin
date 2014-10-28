package com.cura.nmap;

/*
 * Description: This is the implementation of Nmap for Android. Its source can be found here: http://nmap.wjholden.com/src/
 */

import com.cura.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChangeLog extends ListActivity {

 private ListView lv;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.changelog);

  lv = (ListView) findViewById(R.layout.changelog);
  lv.setAdapter(new ArrayAdapter<String>(this, R.xml.changelog));
 }
}