package com.cura.classes;

import android.content.Context;
import android.graphics.Typeface;

public class Font_Singleton {
	private static Font_Singleton mInstance = null;

	private Typeface fontFace;

	private Font_Singleton(Context context) {
		fontFace = Typeface.create(Constants.CuraFont, Typeface.NORMAL);
	}

	public static Font_Singleton getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Font_Singleton(context);
		}
		return mInstance;
	}

	public Typeface getTypeface() {
		return this.fontFace;
	}

	public void setTypeface(Typeface value) {
		fontFace = value;
	}
}