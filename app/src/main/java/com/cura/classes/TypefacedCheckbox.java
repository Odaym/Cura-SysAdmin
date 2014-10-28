package com.cura.classes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.cura.R;

public class TypefacedCheckbox extends CheckBox {
	private final static int font = 0;

	public TypefacedCheckbox(Context context) {
		super(context);
	}

	public TypefacedCheckbox(Context context, AttributeSet attrs) {
		super(context, attrs);
		parseAttributes(context, attrs);
	}

	public TypefacedCheckbox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode())
			return;
		else
			parseAttributes(context, attrs);
	}

	private void parseAttributes(Context context, AttributeSet attrs) {
		TypedArray values = context.obtainStyledAttributes(attrs,
				R.styleable.TypefacedCheckbox);

		int typeface = values.getInt(R.styleable.TypefacedCheckbox_typeface, 0);
		Typeface fontFace = Font_Singleton.getInstance(getContext()).getTypeface();

		switch (typeface) {
		case font:
			setTypeface(fontFace);
			break;
		}
	}
}
