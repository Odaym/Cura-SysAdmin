package com.cura.classes;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;

public class TitleFont_Customizer {

	public static SpannableString makeStringIntoTitle(Context context, int resId) {
		SpannableString actionTitle = new SpannableString(context.getResources()
				.getString(resId));
		actionTitle.setSpan(new android.text.style.TypefaceSpan(Constants.CuraFont), 0,
				actionTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return actionTitle;
	}

	public static SpannableString makeStringIntoTitle(Context context,
			String title) {
		SpannableString actionTitle = new SpannableString(title);
		actionTitle.setSpan(new android.text.style.TypefaceSpan("Courier"), 0,
				actionTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return actionTitle;
	}
}
