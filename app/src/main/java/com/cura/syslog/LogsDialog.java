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

package com.cura.syslog;

/*
 * Description: This Activity is for the dialog creation that happens when a user chooses to select a certain chunk of data
 * from a certain file in the SysLog repository available on said server. This pops up a dialog containing the text that the
 * user requested to see.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cura.R;
import com.cura.classes.TitleFont_Customizer;

public class LogsDialog extends Activity implements OnClickListener {
	private Button close;
	private TextView logsTV;
	private String logs, logsFileName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logsdialog);

		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			logs = extra.getString("logsResult");
			logsFileName = extra.getString("logsFileName");
		}
		if (logs != null)
			setTitle(TitleFont_Customizer
					.makeStringIntoTitle(getApplicationContext(), getResources()
							.getString(R.string.logsFromTitle) + " " + logsFileName));
		else {
			setTitle(TitleFont_Customizer.makeStringIntoTitle(
					getApplicationContext(), R.string.noLogsFoundTitle));
			logs = getResources().getString(R.string.noLogsFoundContent);
		}

		close = (Button) findViewById(R.id.closeLogsDialog);
		close.setOnClickListener(this);
		logsTV = (TextView) findViewById(R.id.logsView);
		logsTV.setText(logs);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.closeLogsDialog:
			this.finish();
			break;
		}
	}
}
