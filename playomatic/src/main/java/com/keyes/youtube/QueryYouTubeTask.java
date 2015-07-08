package com.keyes.youtube;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;


/**
 * Task to figure out details by calling out to YouTube GData API.  We only use public methods that
 * don't require authentication.
 */
public class QueryYouTubeTask extends AsyncTask<Object, ProgressUpdateInfo, Object> {
   

	public QueryYouTubeTask(Activity ac) {
		this.activity = ac;
	}

	protected Activity activity = null;
	//private YouTubeId youtubeId = null;
	//private VideoView videoView = null;
	//protected String mVideoId = null;

   
	protected Object invoke(String name,Object... pParams) {
		try {
			if (pParams != null) {
				Method m =activity.getClass().getDeclaredMethod(name, pParams.getClass());
				Object res = m.invoke(activity, new Object[]{pParams});
				return res;
			}
			else {
				Method m =activity.getClass().getDeclaredMethod(name);
				Object res = m.invoke(activity);
				return res;
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    @Override
    protected Object doInBackground(Object... pParams) {
    	if (isCancelled())
    		return null;    	    	
    	
    	return invoke(BaseActivity.EVT_EXECUTE_TASK,pParams);
    }
    	
    @Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (isCancelled())
            return; 
	}

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if (isCancelled())
            return;
        invoke(BaseActivity.EVT_POST_TASK,result);
        
        if (isCancelled())
            return;             
    }

    @Override
    protected void onProgressUpdate(ProgressUpdateInfo... pValues) {
        super.onProgressUpdate(pValues);
        Log.d(activity.getClass().getSimpleName(), pValues[0].mMsg);
        //BaseActivity.updateProgess(activity,pValues[0].mMsg);
    }
 

}
    
