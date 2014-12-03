package com.scienvo.sample.st.wrapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.scienvo.sample.st.data.SongTasteItem;
import com.scienvo.sample.st.model.SongTasteModel;
import com.scienvo.sample.st.widget.BasePagerView;
import com.scienvo.sample.st.widget.LoadMoreListView;
import com.scienvo.sample.st.widget.LoadMoreListView.OnLoadMoreListener;
import com.travo.sample.st.R;

public class PlazaViewWrapper extends BasePagerView implements SwipeRefreshLayout.OnRefreshListener,OnLoadMoreListener{
	final static int REQUEST_ID_REFRESH = 0;
	final static int REQUEST_ID_MORE = 1;
	final static int REQUEST_ID_MP3 = 2;
	
	View rootView;
	Context context;
	SwipeRefreshLayout swipeLayout;
	LoadMoreListView  listview;
	Button btnPlay,btnPre,btnNext; 
	int seekLen = -1;
	ProgressBar progressBar;
	Timer progressTimer;
	int playMode = 0;
	final int MODE_SINGLE = 1;
	final int MODE_LINEAR = 2;
	
	int songMode = 0;
	final int SONG_LOADING = 1;
	final int SONG_PLAYING = 2;
	final int SONG_STOP = 3;
	private int currentPosion = -1;
	
	File cacheDir;
	
	SongTasteModel model;
	MediaPlayer mPlayer;
	STAdapter adapter;
	
	public PlazaViewWrapper(Context context){
		this.context = context;
		model = new SongTasteModel(context,this.requesHandler);
		cacheDir = new File(
				android.os.Environment.getExternalStorageDirectory(),
				"Travo_SongTaste");
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		
		initView();
		model.refresh(REQUEST_ID_REFRESH);
	}
	
	private void initView(){
		rootView = LayoutInflater.from(context).inflate(R.layout.main, null);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
		listview = (LoadMoreListView ) rootView.findViewById(R.id.listview);
		progressBar = (ProgressBar) rootView.findViewById(R.id.progressbar);
		swipeLayout.setColorSchemeResources (R.color.colorAccent);
		swipeLayout.setOnRefreshListener(this);
		listview.setOnLoadMoreListener(this);
		btnPlay = (Button) rootView.findViewById(R.id.btn_play);
		btnPre = (Button) rootView.findViewById(R.id.btn_pre);
		btnNext = (Button) rootView.findViewById(R.id.btn_next);
		btnPre.setText("单曲循环");
		btnNext.setText("顺序播放");
		btnPlay.setText("木有歌曲播放");
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mPlayer != null && mPlayer.isPlaying()){
					mPlayer.pause();
					seekLen = mPlayer.getCurrentPosition();
					btnPlay.setText("继续播放");
				}
				else if(mPlayer != null && !mPlayer.isPlaying()){
					if(seekLen > 0){
						mPlayer.seekTo(seekLen);
					}
					mPlayer.start();
					btnPlay.setText("停止播放");
				}
			}
		});
		btnPre.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playMode = MODE_SINGLE;
			}
		});
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playMode = MODE_LINEAR;
			}
		});
	}
	
	public View getView(){
		return rootView;
	}
	
	private class STAdapter extends BaseAdapter {
		public SongTasteItem[] data;
		public void play(int pos){
			seekLen = -1;
			if(data == null) return;
			if(pos >= 0 && pos < data.length)
				model.getMp3File(REQUEST_ID_MP3, data[pos].getMp3Xml());
		}
		public void setData(SongTasteItem[] d) {
			this.data = d;
		}
		public void appentData(SongTasteItem[] d){
			if(d == null) return;
			if(data == null){
				data = d;
			}
			else{
				int len = data.length+d.length;
				SongTasteItem[] res = new SongTasteItem[len];
				int i = 0;
				for(SongTasteItem t : data){
					res[i++] = t;
				}
				for(SongTasteItem t : d){
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
						R.layout.cell_item, parent, false);
				viewholder = new ViewHolder();
				viewholder.tvName = (TextView) view.findViewById(R.id.tv_name);
				viewholder.tvSinger = (TextView) view
						.findViewById(R.id.tv_singer);

				view.setTag(viewholder);
			}

			viewholder = (ViewHolder) view.getTag();

			viewholder.tvName.setText(data[position].Name);
			viewholder.tvSinger.setText(data[position].Singer);
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					seekLen = -1;
					currentPosion = position;
					model.getMp3File(REQUEST_ID_MP3, data[position].getMp3Xml());
				}
			});
			return view;
		}
	}
	
	private void play(String mp3Url) {
		if(mp3Url == null){
			return;
		}
		L("mp3 = " + mp3Url);
		String playUrl = mp3Url;
		File toFile = new File(cacheDir, String.valueOf(mp3Url
				.hashCode()) + ".mp3");
		if(toFile.exists()){
			T("cached in local");
			playUrl = toFile.getAbsolutePath();
		}
		else{
//			downloadFile(mp3Url);
		}
		
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mPlayer.setDataSource(playUrl);
		} catch (IllegalArgumentException e) {
		} catch (SecurityException e) {
		} catch (IllegalStateException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mPlayer.prepare();
		} catch (IllegalStateException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPlayer.start();
		mPlayer.setOnCompletionListener(new OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
            	if(playMode == MODE_SINGLE){
            		adapter.play(currentPosion);
            	}
            	else if(playMode == MODE_LINEAR){
            		adapter.play(currentPosion+1);
            	}
            }
        });
		mPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				songMode = SONG_PLAYING;
				btnPlay.setText("停止播放");
				if(progressTimer != null){
					progressTimer.cancel();
				}
				final int duration = mp.getDuration();
			    final int amoungToupdate = duration / 100;
			    progressTimer = new Timer();
			    progressBar.setProgress(0);
			    reSchedule(amoungToupdate,duration);			
			}
		});
	}
	private void reSchedule(final int amoungToupdate,final int duration){
		progressTimer.schedule(new TimerTask() {

	        @Override
	        public void run() {
	        	((ActionBarActivity)context).runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	                	int cur = mPlayer.getCurrentPosition();
	                	if (cur <= duration){
	                        progressBar.setProgress(cur*100/duration);
	                        reSchedule(amoungToupdate,duration);
	                    }
	                }
	            });
	        };
	    }, 1000);		
	}

	private class ViewHolder {
		public TextView tvName;
		public TextView tvSinger;
	}

	private void L(String msg) {
		Log.d("SongTaste", msg);
	}

	private void T(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;
			try {
				L("start downloading ...");
				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();
				InputStream input = new BufferedInputStream(url.openStream());
				File toFile = new File(cacheDir, String.valueOf(aurl[0]
						.hashCode()) + ".mp3");
				OutputStream output = new FileOutputStream(
						toFile.getAbsoluteFile());
				byte data[] = new byte[1024];
				while ((count = input.read(data)) != -1) {
					output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();
				

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onProgressUpdate(String... progress) {
		}

		@Override
		protected void onPostExecute(String unused) {
		}
	}

	@Override
	public void onRefresh() {
		model.update(REQUEST_ID_REFRESH);
	}

	@Override
	public void onLoadMore() {
		model.getMore(REQUEST_ID_MORE);
	}
	
	@Override
	public void onResponse(int id) {
		if (adapter == null) {
			adapter = new STAdapter();
			listview.setAdapter(adapter);
		}
		switch(id){
			case REQUEST_ID_REFRESH:
				swipeLayout.setRefreshing(false);
				adapter.setData(model.getData());
				adapter.notifyDataSetChanged();
				break;
			case REQUEST_ID_MORE:
				listview.onLoadMoreComplete();
				adapter.appentData(model.getData());
				adapter.notifyDataSetChanged();
				break;
			case REQUEST_ID_MP3:
				play(model.getCurrentMp3File());
				break;
		}
	}
	@Override
	public void onError(int id) {
		swipeLayout.setRefreshing(false);
		listview.onLoadMoreComplete();
		switch(id){
		case REQUEST_ID_REFRESH:
			break;
		case REQUEST_ID_MORE:
			break;
		case REQUEST_ID_MP3:
			songMode = 0;
			btnPlay.setText("木有歌曲播放");
			break;
		}
	}
	
	public void onDestroy() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
