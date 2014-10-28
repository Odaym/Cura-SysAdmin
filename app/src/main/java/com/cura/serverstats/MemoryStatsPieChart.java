package com.cura.serverstats;

/*
 * Description:  This is the class responsible for generating the pie chart seen in the Server Stats module under the "Memory 
 * Usage" section. It produces a chart displaying Used, Free and Total amount of memory on the server.
 */

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.cura.R;

public class MemoryStatsPieChart {

	private float total, used, free, usedPercentage, freePercentage;

	public View execute(Context context, String[] data) {
		int[] colors = new int[] { Color.GREEN, Color.RED };
		DefaultRenderer renderer = buildCategoryRenderer(colors);
		total = Float.parseFloat(data[4].replaceAll("\\s", ""));
		used = Float.parseFloat(data[5].replaceAll("\\s", ""));
		free = Float.parseFloat(data[6].replaceAll("\\s", ""));
		usedPercentage = (used * 100) / total;
		freePercentage = (free * 100) / total;

		CategorySeries categorySeries = new CategorySeries("Memory Chart");
		categorySeries.add(context.getApplicationContext().getResources()
				.getString(R.string.freeMemory), freePercentage);
		categorySeries.add(context.getApplicationContext().getResources()
				.getString(R.string.usedMemory), usedPercentage);
		renderer.setPanEnabled(false);
		renderer.setZoomEnabled(false);
		renderer.setInScroll(false);
		renderer.setClickEnabled(false);
		return ChartFactory.getPieChartView(context, categorySeries, renderer);
	}

	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}
}