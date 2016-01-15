package com.cura.gridview;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: alex askerov Date: 9/7/13 Time: 10:49 PM
 */
public abstract class BaseDynamicGridAdapter extends AbstractDynamicGridAdapter {
	private Context mContext;

	private ArrayList<Item> mItems = new ArrayList<Item>();
	private int mColumnCount;
	private static int itemsDeletedCounter;

	protected BaseDynamicGridAdapter(Context context, int columnCount) {
		this.mContext = context;
		this.mColumnCount = columnCount;
	}

	public BaseDynamicGridAdapter(Context context, List<Item> items,
			int columnCount) {
		mContext = context;
		mColumnCount = columnCount;
		init(items);
	}

	private void init(List<Item> items) {
		addAllStableId(items);
		this.mItems.addAll(items);
	}

	public void set(List<Item> items) {
		clear();
		init(items);
		notifyDataSetChanged();
	}

	public void clear() {
		clearStableIdMap();
		mItems.clear();
		notifyDataSetChanged();
	}

	public void add(Item item) {
		addStableId(item);
		mItems.add(item);
		notifyDataSetChanged();
	}

	public void add(List<Item> items) {
		addAllStableId(items);
		this.mItems.addAll(items);
		notifyDataSetChanged();
	}

	public void remove(Item item) {
		mItems.remove(item);
		itemsDeletedCounter++;
		removeStableID(item);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Item getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public int getColumnCount() {
		return mColumnCount;
	}

	public void setColumnCount(int columnCount) {
		this.mColumnCount = columnCount;
		notifyDataSetChanged();
	}

	@Override
	public void reorderItems(int originalPosition, int newPosition) {
		if (newPosition < getCount()) {
			com.cura.gridview.DynamicGridUtils.reorder(mItems, originalPosition, newPosition);
			notifyDataSetChanged();
		}
	}

	public List getItems() {
		return mItems;
	}

	protected Context getContext() {
		return mContext;
	}

	public static int returnDeletedItems() {
		return itemsDeletedCounter;
	}
}
