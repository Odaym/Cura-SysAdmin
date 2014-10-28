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
 * Description: This class is used to automatically construct a list of user accounts for the Login Screen activity.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.cura.R;
import com.cura.classes.TypefacedTextView;

@SuppressWarnings("rawtypes")
public class Spinner_Adapter extends ArrayAdapter {
	private String[] objects;
	private LayoutInflater inflater;

	@SuppressWarnings("unchecked")
	public Spinner_Adapter(Context context, int layoutResourceId,
			int textViewResourceId, String[] objects) {
		super(context, layoutResourceId, textViewResourceId, objects);
		this.objects = objects;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = inflater.inflate(R.layout.spinners_row, parent, false);

		TypefacedTextView itemTV = (TypefacedTextView) rowView
				.findViewById(R.id.itemTV);
		itemTV.setText(objects[position]);

		return rowView;
	}
}
