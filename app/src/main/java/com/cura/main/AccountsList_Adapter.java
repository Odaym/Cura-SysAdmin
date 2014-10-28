package com.cura.main;

/*
 * Description: This class is used to automatically construct a list of user accounts for the Login Screen activity.
 */

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cura.R;
import com.cura.classes.Server;

@SuppressWarnings("rawtypes")
public class AccountsList_Adapter extends ArrayAdapter {
	private Context context;
	private List<Server> servers;
	private LayoutInflater inflater;

	@SuppressWarnings("unchecked")
	public AccountsList_Adapter(Context context, List<Server> servers) {
		super(context, R.layout.accounts_list, servers);
		this.context = context;
		this.servers = servers;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = inflater.inflate(R.layout.accounts_list_item, parent, false);

		TextView userAndDomain = (TextView) rowView
				.findViewById(R.id.userAndDomain);
		TextView port = (TextView) rowView.findViewById(R.id.port);
		userAndDomain.setText(servers.get(position).getUsername() + "@"
				+ servers.get(position).getDomain());
		port.setText(context.getResources().getString(R.string.connectsThrough)
				+ " " + servers.get(position).getPort());
		if (userAndDomain.getText().length() > 24) {
			int lengthDif = userAndDomain.getText().length() - 24;
			Animation mAnimation = new TranslateAnimation(0f, -(17f * lengthDif),
					0.0f, 0.0f);
			mAnimation.setDuration(1000 * lengthDif);
			mAnimation.setRepeatCount(1);
			mAnimation.setRepeatMode(Animation.REVERSE);
			userAndDomain.setAnimation(mAnimation);
		}
		return rowView;
	}
}
