package com.scienvo.sample.st.activity;

import java.io.File;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.scienvo.sample.st.wrapper.CSViewWrapper;
import com.scienvo.sample.st.wrapper.PlazaViewWrapper;
import com.travo.sample.st.R;

public class MainActivity extends ActionBarActivity{
	
	ViewPager pager;
	MyAdapter adapter;
	PlazaViewWrapper plazaWrapper;
	CSViewWrapper csWrapper;
	TextView[] tvTab;
	final int TAB_CNT = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m1);
		pager = (ViewPager) findViewById(R.id.viewpager);
		tvTab = new TextView[TAB_CNT];
		tvTab[0] = (TextView) findViewById(R.id.tab1);
		tvTab[1] = (TextView) findViewById(R.id.tab2);
		tvTab[2] = (TextView) findViewById(R.id.tab3);
		tvTab[3] = (TextView) findViewById(R.id.tab4);
		adapter = new MyAdapter();
		pager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		setTab(0);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int p) {
				setTab(p);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
		initImageLoader();
	}
	
	private void initImageLoader(){
		File cacheDir = StorageUtils.getCacheDirectory(this);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
		        .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
		        .diskCacheExtraOptions(480, 800, null)
		        .threadPoolSize(3) // default
		        .threadPriority(Thread.NORM_PRIORITY - 2) // default
		        .tasksProcessingOrder(QueueProcessingType.FIFO) // default
		        .denyCacheImageMultipleSizesInMemory()
		        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		        .memoryCacheSize(2 * 1024 * 1024)
		        .memoryCacheSizePercentage(13) // default
		        .diskCache(new UnlimitedDiscCache(cacheDir)) // default
		        .diskCacheSize(50 * 1024 * 1024)
		        .diskCacheFileCount(100)
		        .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
		        .imageDownloader(new BaseImageDownloader(this)) // default
		        .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
		        .writeDebugLogs()
		        .build();
		ImageLoader.getInstance().init(config);
	}
	
	private void setTab(int p){
		for(int i=0;i<TAB_CNT;++i){
			if(i == p){
				tvTab[i].setTextColor(0xFFFFFFFF);
			}
			else{
				tvTab[i].setTextColor(0x99FFFFFF);
			}
		}
	}
	
	private class MyAdapter extends PagerAdapter{
		public final String[] ts = new String[]{"叉烧推荐","专辑","下载","收藏"};
		@Override
		public int getCount() {
			return TAB_CNT;
		}
		
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1; 
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return ts[position];
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			if(position == 0){
				if(plazaWrapper == null){
					plazaWrapper = new PlazaViewWrapper(MainActivity.this);
				}
				View v = plazaWrapper.getView();
				container.addView(v);
				return v;
			}
			if(position == 1){
				if(csWrapper == null){
					csWrapper = new CSViewWrapper(MainActivity.this);
				}
				View v = csWrapper.getView();
				container.addView(v);
				return v;
			}
			TextView textView = new TextView(MainActivity.this);
			textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			textView.setTextColor(0xFF00FF00);
			textView.setText(String.valueOf(position));
			container.addView(textView);;
			return textView;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(plazaWrapper != null){
			plazaWrapper.onDestroy();
		}
	}
	
	
}
