package com.scienvo.sample.st.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.v4.sourcecode.MaterialProgressDrawable;
import com.travo.sample.st.R;

/*
 * Copyright (C) 2012 Fabian Leon Ortega <http://orleonsoft.blogspot.com/,
 *  http://yelamablog.blogspot.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class LoadMoreListView extends ListView implements OnScrollListener {

	private static final String TAG = "LoadMoreListView";

	/**
	 * Listener that will receive notifications every time the list scrolls.
	 */
	private OnScrollListener mOnScrollListener;
	private LayoutInflater mInflater;

	// footer view
	private RelativeLayout mFooterView;
	private TextView mLabLoadMore;
	private ImageView mProgressBarLoadMore;

	// Listener to process load more items when user reaches the end of the list
	private OnLoadMoreListener mOnLoadMoreListener;
	private BaseListViewOnScroll mOnListViewMoveListener;
	// To know if the list is loading more items
	private boolean mIsLoadingMore = false;

	private boolean mCanLoadMore = true;
	private int mCurrentScrollState;
	MaterialProgressDrawable mpd;

	public LoadMoreListView(Context context) {
		super(context);
		init(context);
	}

	public LoadMoreListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// footer
		mFooterView = (RelativeLayout) mInflater.inflate(
				R.layout.load_more_footer, this, false);
		mLabLoadMore = (TextView) mFooterView
				.findViewById(R.id.no_more_textView);
		mProgressBarLoadMore = (ImageView) mFooterView
				.findViewById(R.id.load_more_progressBar);

		addFooterView(mFooterView);
		mpd = new MaterialProgressDrawable(context, mFooterView);
		mpd.updateSizes(10);
		mpd.setAlpha(255);
		mpd.setColorSchemeColors(context.getResources().getColor(
				R.color.colorAccent));
		mProgressBarLoadMore.setImageDrawable(mpd);
		super.setOnScrollListener(this);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	/**
	 * Set the listener that will receive notifications every time the list
	 * scrolls.
	 * 
	 * @param l
	 *            The scroll listener.
	 */
	@Override
	public void setOnScrollListener(AbsListView.OnScrollListener l) {
		mOnScrollListener = l;
	}

	/**
	 * Register a callback to be invoked when this list reaches the end (last
	 * item be visible)
	 * 
	 * @param onLoadMoreListener
	 *            The callback to run.
	 */

	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
		mOnLoadMoreListener = onLoadMoreListener;
	}
	public void setOnListViewMoveListener(BaseListViewOnScroll s){
		mOnListViewMoveListener = s;
	}

	@SuppressLint("ShowToast")
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem,
					visibleItemCount, totalItemCount);
		}

		if (mOnLoadMoreListener != null) {

			if (visibleItemCount == totalItemCount) {
				mProgressBarLoadMore.setVisibility(View.GONE);
				mLabLoadMore.setVisibility(View.GONE);
				return;
			}

			boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

			if (!mIsLoadingMore && loadMore
					&& mCurrentScrollState != SCROLL_STATE_IDLE) {
				if (!mCanLoadMore) {
					mLabLoadMore.setVisibility(View.VISIBLE);
					return;
				}
				mProgressBarLoadMore.setVisibility(View.VISIBLE);
				mpd.start();
				mLabLoadMore.setVisibility(View.INVISIBLE);
				mIsLoadingMore = true;
				onLoadMore();
			}

		}

	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mCurrentScrollState = scrollState;

		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}

	}

	public void setCanLoadMore(boolean canLoadMore) {
		mCanLoadMore = canLoadMore;
		mLabLoadMore.setVisibility(View.INVISIBLE);
		mpd.stop();
	}

	public void onLoadMore() {
		Log.d(TAG, "onLoadMore");
		if (mOnLoadMoreListener != null) {
			mOnLoadMoreListener.onLoadMore();
		}
	}

	/**
	 * Notify the loading more operation has finished
	 */
	public void onLoadMoreComplete() {
		mIsLoadingMore = false;
		mProgressBarLoadMore.setVisibility(View.GONE);
	}

	int oldX, oldY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			oldX = x;
			oldY = y;
			if (mOnListViewMoveListener != null) {
				mOnListViewMoveListener.onTouchStart();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (oldX == -1 && oldY == -1) {
				oldY = y;
				oldX = x;
				break;
			}
			int deltaY = y - oldY;
			int deltaX = x - oldX;
			if (mOnListViewMoveListener != null) {
				mOnListViewMoveListener.onMove(deltaX, deltaY);
			}
			oldY = y;
			oldX = x;
			break;
		case MotionEvent.ACTION_UP:
			if (mOnListViewMoveListener != null) {
				mOnListViewMoveListener.onTouchEnd();
			}
			oldX = oldY = -1;
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * Interface definition for a callback to be invoked when list reaches the
	 * last item (the user load more items in the list)
	 */
	public interface OnLoadMoreListener {
		/**
		 * Called when the list reaches the last item (the last item is visible
		 * to the user)
		 */
		public void onLoadMore();
	}

	public static interface BaseListViewOnScroll {
		public   void onMove(int x, int y);

		public   void onTouchEnd();

		public   void onTouchStart();
	}

}