package com.wallpaper.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemLongClickListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.wallpaper.activity.R;
import com.wallpaper.core.Adapter.OnGetViewListener;
import com.wallpaper.core.com.jess.ui.TwoWayAdapterView.OnItemClickListener;
import com.wallpaper.core.com.jess.ui.TwoWayGridView;

import java.util.ArrayList;

public abstract class GridFragment extends SherlockFragment implements OnItemLongClickListener, OnGetViewListener, OnItemClickListener {

	private TwoWayGridView mGridView;
	private View mView;
	private Adapter mAdapter;

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (container == null) {
			return null;
		}

		this.mView = inflater.inflate(R.layout.fragment_grid, container, false);
		this.mGridView = (TwoWayGridView) mView.findViewById(R.id.grid);
		return mView;
	}

	public void setAdapter (Adapter adapter) {
		this.mAdapter = adapter;
		this.mGridView.setAdapter(this.mAdapter);
		this.mGridView.setNumColumns(super.getResources().getInteger(R.integer.column_count));
		this.mGridView.setNumRows(super.getResources().getInteger(R.integer.column_count));
		this.mGridView.setOnItemClickListener(this);
		this.mGridView.setLongClickable(false);
		this.mGridView.setClickable(true);
	}

	public void setData (ArrayList<?> data) {
		this.setAdapter(new Adapter(this, super.getActivity(), data));
	}

}
