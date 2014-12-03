package com.scienvo.sample.st;

import com.scienvo.sample.st.framework.MyApplication;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceUtil {
	public static float getDensity()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) MyApplication.getSugarContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    public static int getPxByDp(int dp)
    {
        return (int) (getDensity() * dp);
    }
}
