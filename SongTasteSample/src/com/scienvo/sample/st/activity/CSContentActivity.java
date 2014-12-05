package com.scienvo.sample.st.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.orm.SugarRecord;
import com.scienvo.sample.st.DeviceUtil;
import com.scienvo.sample.st.OfflineDownloadService.DBCSItem;
import com.scienvo.sample.st.data.HistoryCSItem;
import com.scienvo.sample.st.model.BaseModel;
import com.scienvo.sample.st.widget.LoadMoreListView.BaseListViewOnScroll;
import com.scienvo.sample.st.wrapper.CSViewWrapper.CSItem;
import com.travo.sample.st.R;

public class CSContentActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener, BaseListViewOnScroll {
	SwipeRefreshLayout swipeLayout;
	RecyclerView listview;
	LinearLayoutManager llm;
	CSAdapter adapter;
	RequestQueue queue;
	String contentUrl = "http://www.weiduwu.net/app/getContentByid.json";
	File cacheDir;
	TextView tvTitle;
	private String argId;
	private String argCount;
	private String argTitle;
	private HistoryCSItem historyCSItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cs_content_main);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		listview = (RecyclerView) findViewById(R.id.listview);
		llm = new LinearLayoutManager(this);
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		listview.setLayoutManager(llm);
		swipeLayout.setColorSchemeResources(R.color.colorAccent);
		swipeLayout.setProgressViewOffset(false, DeviceUtil.getPxByDp(10),
				DeviceUtil.getPxByDp(60));
		swipeLayout.setOnRefreshListener(this);
		tvTitle = (TextView) findViewById(R.id.tv_cstitle);
		cacheDir = new File(
				android.os.Environment.getExternalStorageDirectory(),
				"Travo_SongTaste");
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		queue = Volley.newRequestQueue(this);
		argId = getIntent().getStringExtra(ARG_ID);
		argCount = getIntent().getStringExtra(ARG_COUNT);
		argTitle = getIntent().getStringExtra(ARG_TITLE);
		tvTitle.setText(argTitle);
		tvTitle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listview.scrollToPosition(0);
			}
		});
		swipeLayout.setRefreshing(false);
		listview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (dy > 5) {
					hideWhenScrollDown();
				} else if (dy < -5) {
					showWhenScrollUpOrHitBottom();
				}
			}
		});
		refresh();
	}

	@Override
	public void onRefresh() {
		swipeLayout.setRefreshing(false);
	}

	public void refresh() {
		if (paraCurrentItem != null && paraCurrentItem.content != null
				&& paraCurrentItem.content.length > 0) {
			updateData(paraCurrentItem.content);
			return;
		}
		StringRequest sr = new StringRequest(Request.Method.POST, contentUrl,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						updateData(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("page", String.valueOf(1));
				params.put("pageSize", argCount);
				params.put("id", argId);
				return params;
			}
		};
		queue.add(sr);
	}

	private void updateData(String json) {
		L("use remote");
		final ContentItem[] d = BaseModel.fromGson(json, ContentItem[].class);
		DBCSItem s = new DBCSItem();
		s.csId = argId;
		s.title = argTitle;
		s.cnt = getInt(argCount);
		s.contentsJson = json;
		s.save();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				swipeLayout.setRefreshing(false);
				if (adapter == null) {
					adapter = new CSAdapter();
					listview.setAdapter(adapter);
				}
				adapter.setData(d);
				;
				adapter.notifyDataSetChanged();
				List<HistoryCSItem> res = HistoryCSItem.find(
						HistoryCSItem.class, "CS_TOKEN = ?", argId);
				if (res != null && res.size() > 0) {
					historyCSItem = res.get(0);
					llm.scrollToPositionWithOffset(historyCSItem.pos,
							historyCSItem.fromTop);
				}
			}
		});
	}

	private int getInt(String v) {
		try {
			return Integer.valueOf(v);
		} catch (Exception e) {
			return 0;
		}
	}

	private void updateData(final ContentItem[] d) {
		L("use local");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				swipeLayout.setRefreshing(false);
				if (adapter == null) {
					adapter = new CSAdapter();
					listview.setAdapter(adapter);
				}
				adapter.setData(d);
				;
				adapter.notifyDataSetChanged();
				List<HistoryCSItem> res = HistoryCSItem.find(
						HistoryCSItem.class, "CS_TOKEN = ?", argId);
				if (res != null && res.size() > 0) {
					historyCSItem = res.get(0);
					llm.scrollToPositionWithOffset(historyCSItem.pos,
							historyCSItem.fromTop);
				}
			}
		});
	}

	public static class ContentItem extends SugarRecord<ContentItem> {
		public String rank;
		public String content;
		public int r = -1;

		public ContentItem() {
		}

		public int getMyRank() {
			if (r > 0)
				return r;
			r = getInt(rank);
			return r;
		}

		public int getInt(String rank) {
			try {
				return Integer.valueOf(rank);
			} catch (Exception e) {
				return 0;
			}
		}
	}

	private class CSAdapter extends RecyclerView.Adapter<ViewHolder> {
		final String blank = "    ";
		public List<ContentItem> data;

		public void setData(ContentItem[] d) {
			if (data == null) {
				data = new ArrayList<ContentItem>();
			} else {
				data.clear();
			}
			for (ContentItem i : d) {
				data.add(i);
			}
		}

		@Override
		public int getItemCount() {
			return data == null ? 1 : data.size() + 1;
		}

		@Override
		public void onBindViewHolder(ViewHolder viewholder, final int position) {
			if (position == 0 || position == getItemCount() - 1)
				return;
			viewholder.tvSetion.setText(data.get(position - 1).rank + ".");
			int r = data.get(position - 1).getMyRank();
			if (r < 10) {
				viewholder.tvContent.setText(blank + blank
						+ data.get(position - 1).content);
			} else if (r < 100) {
				viewholder.tvContent.setText(blank + blank + blank
						+ data.get(position - 1).content);
			} else if (r < 1000) {
				viewholder.tvContent.setText(blank + blank + blank + blank
						+ data.get(position - 1).content);
			} else {
				viewholder.tvContent.setText(blank + blank + blank + blank
						+ blank + data.get(position - 1).content);
			}
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			if (viewType == 0) {
				View itemView = LayoutInflater.from(viewGroup.getContext())
						.inflate(R.layout.empty, viewGroup, false);
				return new HeaderHolder(itemView);
			}
			if (viewType == 1) {
				View itemView = LayoutInflater.from(viewGroup.getContext())
						.inflate(R.layout.end, viewGroup, false);
				return new HeaderHolder(itemView);
			}
			View itemView = LayoutInflater.from(viewGroup.getContext())
					.inflate(R.layout.cell_cs_content_item, viewGroup, false);
			return new ViewHolder(itemView);
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0)
				return 0;
			if (position == getItemCount() - 1)
				return 1;
			return 2;
		};
	}

	private void L(String msg) {
		Log.d("SongTaste", msg);
	}

	private class ViewHolder extends RecyclerView.ViewHolder {
		public TextView tvSetion;
		public TextView tvContent;

		public ViewHolder(View itemView) {
			super(itemView);
			tvSetion = (TextView) itemView.findViewById(R.id.tv_section);
			tvContent = (TextView) itemView.findViewById(R.id.tv_content);
		}
	}

	private class HeaderHolder extends ViewHolder {
		public HeaderHolder(View itemView) {
			super(itemView);
		}
	}

	private static final String ARG_ID = "id";
	private static final String ARG_COUNT = "count";
	private static final String ARG_TITLE = "title";

	public static void startActivity(Context c, CSItem d) {
		paraCurrentItem = d;
		Intent intent = new Intent(c, CSContentActivity.class);
		intent.putExtra(ARG_ID, d.id);
		intent.putExtra(ARG_COUNT, String.valueOf(d.duan_count));
		intent.putExtra(ARG_TITLE, d.title);
		c.startActivity(intent);
	}

	public static CSItem paraCurrentItem;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onMove(int x, int y) {
		if (y > 5) {
			showWhenScrollUpOrHitBottom();
		} else if (y < -5) {
			hideWhenScrollDown();
		}
	}

	int state = 1;

	public void hideWhenScrollDown() {
		if (state == 0)
			return;
		state = 0;
		ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(tvTitle, "y", 0,
				-tvTitle.getHeight());
		hideAnimator.setDuration(200);
		hideAnimator.setInterpolator(new AccelerateInterpolator());
		hideAnimator.start();
	}

	public void showWhenScrollUpOrHitBottom() {
		if (state == 1)
			return;
		state = 1;
		ObjectAnimator showAnimator = ObjectAnimator.ofFloat(tvTitle, "y",
				-tvTitle.getHeight(), 0);
		showAnimator.setDuration(200);
		showAnimator.setInterpolator(new AccelerateInterpolator());
		showAnimator.start();
	}

	@Override
	public void onTouchEnd() {
	}

	@Override
	public void onTouchStart() {
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (listview != null) {
			View child = listview.getChildAt(0);
			if (child != null) {
				int p = llm.getPosition(child);
				int top = child.getTop();
				if (historyCSItem == null) {
					historyCSItem = new HistoryCSItem(argId, p, top);
				} else {
					historyCSItem.pos = p;
					historyCSItem.fromTop = top;
				}
				historyCSItem.save();
			}
		}
	}

	
}