package com.cura.about;

/*
 * Description: This class is used to automatically construct a list of information items for the About Activity activity.
 */

import java.util.Vector;

import com.cura.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class AboutAdapter extends ArrayAdapter {
	Context context;
	Vector<AboutClass> aboutVector;

	@SuppressWarnings("unchecked")
	public AboutAdapter(Context context, Vector aboutV) {
		super(context, R.layout.act_about, aboutV);
		this.context = context;
		aboutVector = aboutV;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.act_about, parent, false);
		if (aboutVector.get(position).getTitle().compareTo("separator") == 0) {
			rowView = inflater.inflate(R.layout.seperator, null);

			rowView.setOnClickListener(null);
			rowView.setOnLongClickListener(null);
			rowView.setLongClickable(false);

			final TextView sectionView = (TextView) rowView
					.findViewById(R.id.list_item_section_text);
			sectionView.setText(aboutVector.get(position).getSubtitle());
		} else {
			TextView title = (TextView) rowView.findViewById(R.id.titleTV);
			TextView subTitle = (TextView) rowView.findViewById(R.id.subtitleTV);
			title.setText(aboutVector.get(position).getTitle());
			subTitle.setText(aboutVector.get(position).getSubtitle());
		}
		return rowView;
	}
}
