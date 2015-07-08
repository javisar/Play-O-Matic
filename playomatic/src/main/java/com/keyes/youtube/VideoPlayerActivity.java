package com.keyes.youtube;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * <p>Activity that will play a video from YouTube.  A specific video or the latest video in a YouTube playlist
 * can be specified in the intent used to invoke this activity.  The data of the intent can be set to a
 * specific video by using an Intent data URL of the form:</p>
 * <p/>
 * <pre>
 *     ytv://videoid
 * </pre>
 * <p/>
 * <p>where <pre>videoid</pre> is the ID of the YouTube video to be played.</p>
 * <p/>
 * <p>If the user wishes to play the latest video in a YouTube playlist, the Intent data URL should be of the
 * form:</p>
 * <p/>
 * <pre>
 *     ytpl://playlistid
 * </pre>
 * <p/>
 * <p>where <pre>playlistid</pre> is the ID of the YouTube playlist from which the latest video is to be played.</p>
 * <p/>
 * <p>Code used to invoke this intent should look something like the following:</p>
 * <p/>
 * <pre>
 *      Intent lVideoIntent = new Intent(null, Uri.parse("ytpl://"+YOUTUBE_PLAYLIST_ID), this, OpenYouTubePlayerActivity.class);
 *      startActivity(lVideoIntent);
 * </pre>
 * <p/>
 * <p>There are several messages that are displayed to the user during various phases of the video load process.  If
 * you wish to supply text other than the default english messages (e.g., internationalization, etc.), you can pass
 * the text to be used via the Intent's extended data.  The messages that can be customized include:
 * <p/>
 * <ul>
 * <li>com.keyes.video.msg.init        - activity is initializing.</li>
 * <li>com.keyes.video.msg.detect      - detecting the bandwidth available to download video.</li>
 * <li>com.keyes.video.msg.playlist    - getting latest video from playlist.</li>
 * <li>com.keyes.video.msg.token       - retrieving token from YouTube.</li>
 * <li>com.keyes.video.msg.loband      - buffering low-bandwidth.</li>
 * <li>com.keyes.video.msg.hiband      - buffering hi-bandwidth.</li>
 * <li>com.keyes.video.msg.error.title - dialog title displayed if anything goes wrong.</li>
 * <li>com.keyes.video.msg.error.msg   - message displayed if anything goes wrong.</li>
 * </ul>
 * <p/>
 * <p>For example:</p>
 * <p/>
 * <pre>
 *      Intent lVideoIntent = new Intent(null, Uri.parse("ytpl://"+YOUTUBE_PLAYLIST_ID), this, OpenYouTubePlayerActivity.class);
 *      lVideoIntent.putExtra("com.keyes.video.msg.init", getString("str_video_intro"));
 *      lVideoIntent.putExtra("com.keyes.video.msg.detect", getString("str_video_detecting_bandwidth"));
 *      ...
 *      startActivity(lVideoIntent);
 * </pre>
 *
 * @author David Keyes
 */
public class VideoPlayerActivity extends Activity {

    @Override
	public void onBackPressed() {
		YouTubeUtility.cancelled = true;
		VideoPlayerActivity.this.setResult(RESULT_CANCELED);
		VideoPlayerActivity.this.finish();
		//super.onBackPressed();
	}
    

	public static final String SCHEME_YOUTUBE_VIDEO = "ytv";
    public static final String SCHEME_YOUTUBE_PLAYLIST = "ytpl";
    public static final String SCHEME_FILE = "file";

    static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?&video_id=";
    static final String YOUTUBE_PLAYLIST_ATOM_FEED_URL = "http://gdata.youtube.com/feeds/api/playlists/";
    static final String YOUTUBE_USER_ATOM_FEED_URL = "https://gdata.youtube.com/feeds/api/users/";
    
	public static final int RESULT_ERROR = 10;
	public static final int RESULT_WARNING = 11;
	public static final int RESULT_VIDEO_ERROR = 12;

    protected VideoView mVideoView;


    /**
     * Background task on which all of the interaction with YouTube is done
     */
    //protected QueryYouTubeTask mQueryYouTubeTask;
    protected CheckNetworkConnection mCheckNetworkConnection;
    
    protected void onNetworkUnavailable(Object... params) {
    	//super.onNetworkUnavailable(params);
    	//YouTubeUtility.getCurrentPlaylist().getCurrentVideo().setPosition(mVideoView.getCurrentPosition());
    	VideoPlayerActivity.this.setResult(RESULT_WARNING);
    	this.finish();
    };
    
    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
    	
        super.onCreate(pSavedInstanceState);

        setTitle(this.getIntent().getStringExtra("videoTitle"));
        // create the layout of the view
        setupView();

       
        // extract the playlist or video id from the intent that started this video

        //Uri lVideoIdUri = this.getIntent().getData();
        Uri lVideoIdUri = this.getIntent().getData();

        if (lVideoIdUri == null) {
            Log.i(this.getClass().getSimpleName(), "No video ID was specified in the intent.  Closing video activity.");
            VideoPlayerActivity.this.setResult(RESULT_ERROR);
            finish();
        }
        String lVideoSchemeStr = lVideoIdUri.getScheme();
        String lVideoIdStr = lVideoIdUri.getEncodedSchemeSpecificPart();
        if (lVideoIdStr == null) {
            Log.i(this.getClass().getSimpleName(), "No video ID was specified in the intent.  Closing video activity.");
            VideoPlayerActivity.this.setResult(RESULT_ERROR);
            finish();
        }
        if (lVideoIdStr.startsWith("//")) {
            if (lVideoIdStr.length() > 2) {
                lVideoIdStr = lVideoIdStr.substring(2);
            } else {
                Log.i(this.getClass().getSimpleName(), "No video ID was specified in the intent.  Closing video activity.");
                VideoPlayerActivity.this.setResult(RESULT_ERROR);
                finish();
            }
        }

        ///////////////////
        // extract either a video id or a playlist id, depending on the uri scheme
        YouTubeId lYouTubeId = null;
        if (lVideoSchemeStr != null && lVideoSchemeStr.equalsIgnoreCase(SCHEME_YOUTUBE_PLAYLIST)) {
            lYouTubeId = new PlaylistId(lVideoIdStr);
        } else if (lVideoSchemeStr != null && lVideoSchemeStr.equalsIgnoreCase(SCHEME_YOUTUBE_VIDEO)) {
            lYouTubeId = new VideoId(lVideoIdStr);
        } else if (lVideoSchemeStr != null && lVideoSchemeStr.equalsIgnoreCase(SCHEME_FILE)) {
            lYouTubeId = new FileId(lVideoIdStr);
        }

        if (lYouTubeId == null) {
            Log.i(this.getClass().getSimpleName(), "Unable to extract video ID from the intent.  Closing video activity.");
            VideoPlayerActivity.this.setResult(RESULT_ERROR);
            finish();
        }

        //mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask().execute(lYouTubeId);
       // mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask().execute(this,mVideoView,lYouTubeId);
        //mCheckNetworkConnection = (CheckNetworkConnection) new CheckNetworkConnection().execute(this,mVideoView);
        //PlaylistActivity ac = (PlaylistActivity) getApplicationContext();
        //resumeVideo(getIntent().getStringExtra("quality"),YouTubeUtility.getCurrentPlaylist().getCurrentVideo());
        //new QueryYouTubeTask().execute(getIntent().getStringExtra("quality"),YouTubeUtility.getCurrentPlaylist().getCurrentVideo());
        resumeVideo(this.getIntent().getStringExtra("videoId"));
    }

    /**
     * Determine the messages to display during video load and initialization.
     */

    protected ProgressBar 	progressBar;
    protected TextView 		progressMessage;
    protected ViewGroup		layout;    
    
    /**
     * Create the view in which the video will be rendered.
     */
    private void setupView() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    	LinearLayout lLinLayout = new LinearLayout(this);
        //lLinLayout.setId(1);
        lLinLayout.setOrientation(LinearLayout.VERTICAL);
        lLinLayout.setGravity(Gravity.CENTER);
        lLinLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lLinLayoutParms = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        lLinLayout.setLayoutParams(lLinLayoutParms);

        this.setContentView(lLinLayout);


        RelativeLayout lRelLayout = new RelativeLayout(this);
        //lRelLayout.setId(2);
        lRelLayout.setGravity(Gravity.CENTER);
        lRelLayout.setBackgroundColor(Color.BLACK);
        android.widget.RelativeLayout.LayoutParams lRelLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lRelLayout.setLayoutParams(lRelLayoutParms);
        lLinLayout.addView(lRelLayout);               

        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setEnabled(true);
        //progressBar.setId(4);
        android.widget.RelativeLayout.LayoutParams lProgressBarLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressBarLayoutParms.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(lProgressBarLayoutParms);
        lRelLayout.addView(progressBar);

        progressMessage = new TextView(this);
        //progressMessage.setId(5);
        android.widget.RelativeLayout.LayoutParams lProgressMsgLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressMsgLayoutParms.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lProgressMsgLayoutParms.addRule(RelativeLayout.BELOW, 4);
        progressMessage.setLayoutParams(lProgressMsgLayoutParms);
        progressMessage.setTextColor(Color.LTGRAY);
        progressMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        progressMessage.setText("...");
        lRelLayout.addView(progressMessage);
        
        layout=lRelLayout;

        if (YouTubeUtility.currentPlayer.equals("default")) {
            mVideoView = new VideoView(this);
            //mVideoView.setId(3);
            android.widget.RelativeLayout.LayoutParams lVidViewLayoutParams = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lVidViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mVideoView.setLayoutParams(lVidViewLayoutParams);
            //lRelLayout.addView(mVideoView);
            layout.addView(mVideoView);
        }
        // determine the messages to be displayed as the view loads the video
        ProgressUpdateInfo.extractMessages((Activity)this);

        // set the flag to keep the screen ON so that the video can play without the screen being turned off
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);
        progressMessage.setText(ProgressUpdateInfo.mMsgInit);
        //BaseActivity.showProgess(this);

    }

    @Override
    protected void onDestroy() {
    	 super.onDestroy();

         //if (mQueryYouTubeTask != null) {
         //    mQueryYouTubeTask.cancel(true);
         //}
         //this.mQueryYouTubeTask = null;

       
        //YouTubeUtility.markVideoAsViewed(this, mVideoId);
    	YouTubeUtility.markVideoAsViewed(this, YouTubeUtility.getCurrentPlaylist().getCurrentVideo().mId);

        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }

        // clear the flag that keeps the screen ON
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

       this.mVideoView = null;
       
       	//Sample.myself.startPlaylist("ytv://", YouTubeUtility.getCurrentPlaylist().getCurrentVideo().mId);
        //Sample.myself.notifyDestroyYouTubeActivity();
       //BaseActivity.fireEvent(parent, BaseActivity.EVT_ACTIVITY_DESTROY, this);
       //if (getParent() instanceof PlaylistActivity) {
    	//   ((PlaylistActivity)getParent()).onActivityDestroy(this);
       //}
        /*
        if (YouTubeUtility.videoList.size()>0) {
        	if (!YouTubeUtility.cancelled) {
        		Sample.myself.startPlaylist("ytv://",YouTubeUtility.videoList.get(YouTubeUtility.videoCounter),"playPlaylist");
        	}
        }
        */
       //mCheckNetworkConnection.cancel(true);
	}


    public void prevVideo() {
    	/*
    	if (YouTubeUtility.videoCounter==0) return;
    	YouTubeUtility.currentPos = 0;
    	YouTubeUtility.videoCounter--;
    	*/
    	VideoId nv = YouTubeUtility.getCurrentPlaylist().getPrevVideo();
    	if (nv == null) {
    		VideoPlayerActivity.this.setResult(RESULT_CANCELED);
    	}
    	else {
    		VideoPlayerActivity.this.setResult(RESULT_OK);
    	}
    	VideoPlayerActivity.this.finish();
    }
    
    public void nextVideo() {
    	/*
    	if (YouTubeUtility.videoCounter == YouTubeUtility.videoList.size()-1) return;
    	
    	YouTubeUtility.currentPos = 0;
    	YouTubeUtility.videoCounter++;
    	*/
    	VideoId nv = YouTubeUtility.getCurrentPlaylist().getNextVideo();
    	if (nv == null) {
    		VideoPlayerActivity.this.setResult(RESULT_CANCELED);
    	}
    	else {
    		VideoPlayerActivity.this.setResult(RESULT_OK);
    	}
    	VideoPlayerActivity.this.finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void updateProgress(String pProgressMsg) {
        try {
            progressMessage.setText(pProgressMsg);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error updating video status!", e);
        }
    }
    public void resumeVideo(String lUriStr) {
        if (lUriStr == null) {

            VideoPlayerActivity.this.finish();
            return;
        }

        if (YouTubeUtility.currentPlayer.equals("default")) {
            resumeVideoDefault(lUriStr);
        }
        else if (YouTubeUtility.currentPlayer.equals("mxplayer")) {
            resumeVideoMxPlayer(lUriStr);
        }
    }


    public static int findPIDbyPackageName(Context cx, String packagename) {
        int result = -1;
        ActivityManager am = (ActivityManager) cx.getSystemService(Context.ACTIVITY_SERVICE);

        if (am != null) {
            for (ActivityManager.RunningAppProcessInfo pi : am.getRunningAppProcesses()){
                if (pi.processName.equalsIgnoreCase(packagename)) {
                    result = pi.pid;
                }
                if (result != -1) break;
            }
        } else {
            result = -1;
        }

        return result;
    }

    public static boolean isPackageRunning(Context cx, String packagename) {
        return findPIDbyPackageName(cx,packagename) != -1;
    }

    public static boolean killPackageProcesses(Context cx, String packagename) {
        boolean result = false;
        ActivityManager am = (ActivityManager) cx.getSystemService(Context.ACTIVITY_SERVICE);

        if (am != null) {
            am.killBackgroundProcesses(packagename);
            result = !isPackageRunning(cx,packagename);
        } else {
            result = false;
        }

        return result;
    }

    public static void launchMXPlayer (Activity act, String url, String subsUrl, String headers[])
    {
        // http://blog.rocapal.org/?p=506
        // https://sites.google.com/site/mxvpen/api
        try {
            boolean result = killPackageProcesses(act,"com.mxtech.videoplayer.ad");
            System.out.println("Kill MXPlayer = "+result);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        try{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setComponent(new ComponentName("com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.ad.ActivityScreen"));
            i.setData(Uri.parse(url));
            //i.putExtra("decode_mode",2);
            //i.setDataAndType(Uri.parse(url), "application/mp4");
            if (headers != null && headers.length >= 2) {
                i.putExtra("headers", headers);
            }
            if (subsUrl != null && subsUrl.length() > 0) {
                /*
                File subsDir = downloadSubs(subsUrl);
                if (subsDir != null) {
                    i.putExtra("subs",new Uri[]{Uri.parse(subsDir.getAbsolutePath())});
                    i.putExtra("video_list",new Uri[]{Uri.parse(url)});
                }
                */
            }

            act.startActivityForResult(i, 1);
        }
        catch (ActivityNotFoundException e){
            Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=com.mxtech.videoplayer.ad");
            Intent intent = new Intent (Intent.ACTION_VIEW, uri);
            act.startActivity(intent);
        }
        //YouTubePlayerActivity.this.finish()
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!YouTubeUtility.currentPlayer.equals("default")) return;

        Toast.makeText(VideoPlayerActivity.this, "requestCode: " + requestCode + ", resultCode: " + resultCode, Toast.LENGTH_LONG).show();

        if (requestCode == 1) {
            if (resultCode == 0) {
                nextVideo();
                //AndroidUtil.updateProgress(this.mProgressBar,this.mProgressMessage, "Waiting for next video...");
                //this.mQueryYouTubeTask = (GeneralTask) (new GeneralTask(this)).execute(new Object[] { "NEXT_VIDEO", ""});
            }
            else {
                VideoPlayerActivity.this.setResult(resultCode);
                VideoPlayerActivity.this.finish();
            }
        }

    }

    public void resumeVideoMxPlayer(String lUriStr) {
        String headers[] = new String[] {
                "User-Agent",
                "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7",
                "Cookie", "video=true" };
        launchMXPlayer(VideoPlayerActivity.this,lUriStr,null,headers);
    }

    public void resumeVideoDefault(String lUriStr) {
    	

    	mVideoView.setVideoURI(Uri.parse(lUriStr));
    	    	
    	// TODO:  add listeners for finish of video
        mVideoView.setOnCompletionListener(new OnCompletionListener() {

            public void onCompletion(MediaPlayer pMp) {
                //if (mQueryYouTubeTask.isCancelled())
                //    return;
                
                nextVideo();
                //resumeVideo(YouTubeUtility.getCurrentPlaylist().getCurrentVideo().);
            }

        });
        
        mVideoView.setOnErrorListener(new OnErrorListener() {
          
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				 	//if (mQueryYouTubeTask.isCancelled())
                    //    return false;
				 	//YouTubeUtility.currentPos = mVideoView.getCurrentPosition();
				 	//YouTubeUtility.getCurrentPlaylist().getCurrentVideo().setPosition( mVideoView.getCurrentPosition());
					VideoPlayerActivity.this.setResult(RESULT_ERROR);
					VideoPlayerActivity.this.finish();
                    return true;
			}

        });
    

        final MediaController lMediaController = new MediaController(this){
				            public boolean dispatchKeyEvent(KeyEvent event)
				            {
				                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				                    ((Activity) getContext()).finish();
				
				                return super.dispatchKeyEvent(event);
				            }
				        };
				        
        lMediaController.setPrevNextListeners(new View.OnClickListener() {
            public void onClick(View v) {
            	nextVideo();
            }
         }, new View.OnClickListener() {
            public void onClick(View v) {
            	//VideoId nv = YouTubeUtility.getCurrentPlaylist().getNextVideo();
            	//YouTubePlayerActivity.this.setResult(RESULT_OK);
            	//YouTubePlayerActivity.this.finish();
            	prevVideo();
            }
         });
        mVideoView.setMediaController(lMediaController);

        Bundle bundle = VideoPlayerActivity.this.getIntent().getExtras();

        boolean showControllerOnStartup = false;

        if (!(bundle == null))
            showControllerOnStartup = bundle.getBoolean("show_controller_on_startup", false);

        if (showControllerOnStartup) lMediaController.show(0);
        //mVideoView.setKeepScreenOn(true);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        	
            public void onPrepared(MediaPlayer pMp) {
                //if (mQueryYouTubeTask.isCancelled())
                //    return;
            	 
                progressBar.setVisibility(View.GONE);
                progressMessage.setVisibility(View.GONE);
                int cPos = YouTubeUtility.getCurrentPlaylist().getCurrentVideo().getPosition();
                if (cPos > 0) {
                	mVideoView.seekTo(cPos);
                }
                
            }

        });
      

        mVideoView.requestFocus();
        mVideoView.start();
    }

	public void onTaskPostExecute(Object result) {
		//mCheckNetworkConnection.cancel(true);
		//mCheckNetworkConnection = (CheckNetworkConnection) new CheckNetworkConnection().execute(this,mVideoView);
        
	}
}