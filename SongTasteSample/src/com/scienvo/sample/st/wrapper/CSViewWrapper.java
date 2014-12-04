package com.scienvo.sample.st.wrapper;

import java.io.File;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.scienvo.sample.st.NetUtil;
import com.scienvo.sample.st.OfflineDownloadService;
import com.scienvo.sample.st.activity.CSContentActivity;
import com.scienvo.sample.st.activity.CSContentActivity.ContentItem;
import com.scienvo.sample.st.model.CSModel;
import com.scienvo.sample.st.widget.BasePagerView;
import com.scienvo.sample.st.widget.LoadMoreListView;
import com.scienvo.sample.st.widget.LoadMoreListView.OnLoadMoreListener;
import com.travo.sample.st.R;

public class CSViewWrapper extends BasePagerView implements
		SwipeRefreshLayout.OnRefreshListener, OnLoadMoreListener {
	View rootView;
	Context context;
	SwipeRefreshLayout swipeLayout;
	LoadMoreListView listview;
	CSAdapter adapter;

	CSModel model;
	File cacheDir;
	DisplayImageOptions options;
	private int page = 1;

	private static final int REQUEST_REFRESH = 0;
	private static final int REQUEST_MORE = 1;

	public CSViewWrapper(Context context) {
		this.context = context;
		rootView = LayoutInflater.from(context).inflate(R.layout.csmain, null);
		swipeLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.swipe_refresh);
		listview = (LoadMoreListView) rootView.findViewById(R.id.listview);
		swipeLayout.setColorSchemeResources(R.color.colorAccent);
		swipeLayout.setOnRefreshListener(this);
		listview.setOnLoadMoreListener(this);
		cacheDir = new File(
				android.os.Environment.getExternalStorageDirectory(),
				"Travo_SongTaste");
		if (!cacheDir.exists())
			cacheDir.mkdirs();

		model = new CSModel(context, this.requesHandler);

		model.refresh(REQUEST_REFRESH);

		options = new DisplayImageOptions.Builder()
				.resetViewBeforeLoading(true) // default
				.cacheInMemory(true) // default
				.cacheOnDisk(true) // default
				.considerExifParams(false) // default
				.displayer(new SimpleBitmapDisplayer()) // default
				.build();

//		if (NetUtil.isWifiConnected(context)) {
//			OfflineDownloadService.start();
//		}
	}

	private void L(String msg) {
		Log.d("SongTaste", msg);
	}

	public View getView() {
		return rootView;
	}

	private class CSAdapter extends BaseAdapter {
		public CSItem[] data;

		public void setData(CSItem[] d) {
			this.data = d;
		}

		public void appentData(CSItem[] d) {
			if (d == null)
				return;
			if (data == null) {
				data = d;
			} else {
				int len = data.length + d.length;
				CSItem[] res = new CSItem[len];
				int i = 0;
				for (CSItem t : data) {
					res[i++] = t;
				}
				for (CSItem t : d) {
					res[i++] = t;
				}
				data = res;
			}

		}

		@Override
		public int getCount() {
			return data == null ? 0 : data.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View view = convertView;
			ViewHolder viewholder = null;
			if (view == null) {
				view = LayoutInflater.from(context).inflate(
						R.layout.cell_cs_item, parent, false);
				viewholder = new ViewHolder();
				viewholder.rootView = view;
				viewholder.tvTitle = (TextView) view.findViewById(R.id.tv_name);
				viewholder.tvDes = (TextView) view.findViewById(R.id.tv_des);
				viewholder.tvTime = (TextView) view.findViewById(R.id.tv_time);
				viewholder.ivThumb = (ImageView) view
						.findViewById(R.id.iv_thumb);
				view.setTag(viewholder);
			}
			viewholder = (ViewHolder) view.getTag();

			viewholder.tvTitle.setText(data[position].title);
			viewholder.tvDes.setText(data[position].description);
			if (data[position].content != null
					&& data[position].content.length > 0) {
				viewholder.tvTime.setText("已缓存 "
						+ data[position].content.length + "段 / "
						+ data[position].updatetime);
			} else {
				viewholder.tvTime.setText(data[position].updatetime);
			}

			ImageLoader.getInstance().displayImage(data[position].thumb,
					viewholder.ivThumb, options);

			viewholder.rootView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					requestContent(data[position]);
				}
			});

			return view;
		}
	}

	private void requestContent(final CSItem d) {
		CSContentActivity.startActivity(context, d);
	}

	private class ViewHolder {
		public View rootView;
		public TextView tvTitle;
		public TextView tvDes;
		public TextView tvTime;
		public ImageView ivThumb;
	}

	public void onDestroy() {
	}

	public static class Reply {
		public CSItem[] data;

	}

	public static class CSItem {
		public String id;
		public String category;
		public String catename;
		public String copyfrom;
		public String title;
		public String description;
		public String thumb;
		public String updatetime;
		public int duan_count;

		public ContentItem[] content;
	}

	@Override
	public void onRefresh() {
		model.update(REQUEST_REFRESH);
	}

	@Override
	public void onLoadMore() {
		model.getMore(REQUEST_MORE);
	}

	@Override
	public void onResponse(int id) {
		switch (id) {
		case REQUEST_REFRESH:
			swipeLayout.setRefreshing(false);
			if (adapter == null) {
				adapter = new CSAdapter();
				listview.setAdapter(adapter);
			}
			adapter.setData(model.getData());
			adapter.notifyDataSetChanged();
			break;
		case REQUEST_MORE:
			listview.onLoadMoreComplete();
			adapter.appentData(model.getData());
			adapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	public void onError(int id) {
		swipeLayout.setRefreshing(false);
		listview.onLoadMoreComplete();
		switch (id) {
		case REQUEST_REFRESH:
			break;
		case REQUEST_MORE:
			break;
		}
	}

}
