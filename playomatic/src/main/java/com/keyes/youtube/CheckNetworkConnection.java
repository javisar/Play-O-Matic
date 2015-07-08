package com.keyes.youtube;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.VideoView;


@SuppressWarnings("rawtypes")
public class CheckNetworkConnection extends AsyncTask {
	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		if (this.activity instanceof YouTubePlayerActivity) {
    		((YouTubePlayerActivity)this.activity).onTaskPostExecute(result);
    	}
	}

	private Activity activity = null;
	private VideoView videoView = null;


	private boolean networkIsAvailable() {
		
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}    

	@Override
	protected Object doInBackground(Object... params) {
		 this.activity = (Activity) params[0];
		 this.videoView = (VideoView) params[1];
		 
		 while (true) {
			 try {
 	            Thread.sleep(4000);
 	        } catch (InterruptedException e) {
 	            //e.printStackTrace();
 	            //return "";
 	        }
			 if (videoView.isPlaying()) {
				 	int cPos =  videoView.getCurrentPosition();
				 	if (cPos>0) {
				 		YouTubeUtility.getCurrentPlaylist().getCurrentVideo().setPosition(cPos);
				 	}
	    	        
	    	        if ( !networkIsAvailable()) {
	    	        	/*
	    	        	YouTubeUtility.getCurrentPlaylist().getCurrentVideo().setPosition(videoView.getCurrentPosition());
	    	        	//YouTubeUtility.currentPos = mVideoView.getCurrentPosition();
	    	        	videoView.stopPlayback();
	    	        	activity.finish();
	    	        	*/
	    	        	//BaseActivity.fireEvent(activity, BaseActivity.EVT_NETWORK_UNAVAILABLE, params);
	    	        	if (this.activity instanceof YouTubePlayerActivity) {
	    	        		((YouTubePlayerActivity)this.activity).onNetworkUnavailable(params);
	    	        	}
	    	        }
	    	        if (isCancelled()) break;
	    	    }
	    	    
			 }
		 return "";
	}
}
