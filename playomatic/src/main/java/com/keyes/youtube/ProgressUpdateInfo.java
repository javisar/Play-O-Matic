package com.keyes.youtube;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;


public class ProgressUpdateInfo {

    public String mMsg;
    public static boolean mShowedError = false;

    public ProgressUpdateInfo(String pMsg) {
        mMsg = pMsg;
    }
    
    public final static String MSG_INIT = "com.keyes.video.msg.init";
    protected static String mMsgInit = "Initializing";

    public final static String MSG_DETECT = "com.keyes.video.msg.detect";
    protected static String mMsgDetect = "Detecting Bandwidth";

    public final static String MSG_PLAYLIST = "com.keyes.video.msg.playlist";
    protected static String mMsgPlaylist = "Determining Videos of YouTube Playlist";

    public final static String MSG_TOKEN = "com.keyes.video.msg.token";
    protected static String mMsgToken = "Retrieving YouTube Video Token";

    public final static String MSG_LO_BAND = "com.keyes.video.msg.loband";
    protected static String mMsgLowBand = "Buffering Low-bandwidth Video";

    public final static String MSG_HI_BAND = "com.keyes.video.msg.hiband";
    protected static String mMsgHiBand = "Buffering High-bandwidth Video";

    public final static String MSG_ERROR_TITLE = "com.keyes.video.msg.error.title";
    protected static String mMsgErrorTitle = "Error";

    public final static String MSG_ERROR_MSG = "com.keyes.video.msg.error.msg";
    protected static String mMsgError = "An error has occurred.";
    
    public static void extractMessages(Activity activity) {
    	if (activity == null) return;
        Intent lInvokingIntent = activity.getIntent();
        String lMsgInit = lInvokingIntent.getStringExtra(MSG_INIT);
        if (lMsgInit != null) {
            mMsgInit = lMsgInit;
        }
        String lMsgDetect = lInvokingIntent.getStringExtra(MSG_DETECT);
        if (lMsgDetect != null) {
            mMsgDetect = lMsgDetect;
        }
        String lMsgPlaylist = lInvokingIntent.getStringExtra(MSG_PLAYLIST);
        if (lMsgPlaylist != null) {
            mMsgPlaylist = lMsgPlaylist;
        }
        String lMsgToken = lInvokingIntent.getStringExtra(MSG_TOKEN);
        if (lMsgToken != null) {
            mMsgToken = lMsgToken;
        }
        String lMsgLoBand = lInvokingIntent.getStringExtra(MSG_LO_BAND);
        if (lMsgLoBand != null) {
            mMsgLowBand = lMsgLoBand;
        }
        String lMsgHiBand = lInvokingIntent.getStringExtra(MSG_HI_BAND);
        if (lMsgHiBand != null) {
            mMsgHiBand = lMsgHiBand;
        }
        String lMsgErrTitle = lInvokingIntent.getStringExtra(MSG_ERROR_TITLE);
        if (lMsgErrTitle != null) {
            mMsgErrorTitle = lMsgErrTitle;
        }
        String lMsgErrMsg = lInvokingIntent.getStringExtra(MSG_ERROR_MSG);
        if (lMsgErrMsg != null) {
            mMsgError = lMsgErrMsg;
        }
    }
   
    

    public static void showErrorAlert(final Activity act, String sms, Exception ex) {
    	if (mShowedError) return;
    	final String fsms = sms;
    	if (ex != null) Log.e(act.getClass().getSimpleName(), sms, ex);
        
        try {        	
            Builder lBuilder = new AlertDialog.Builder(act);
            lBuilder.setTitle(ProgressUpdateInfo.mMsgErrorTitle);
            lBuilder.setCancelable(false);
            lBuilder.setMessage(ProgressUpdateInfo.mMsgError+": "+sms);

            lBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            	
                public void onClick(DialogInterface pDialog, int pWhich) {
                    //act.finish();
                	// BaseActivity.fireEvent(act, BaseActivity.EVT_ON_ERROR, fsms);        
                }

            });

            AlertDialog lDialog = lBuilder.create();
            lDialog.show();
            
        	//YouTubeUtility.currentPos = mVideoView.getCurrentPosition();
        	//YouTubeUtility.getCurrentPlaylist().getCurrentVideo().setPosition(videoView.getCurrentPosition());
        	//activity.finish();           
        } catch (Exception e) {
            Log.e(ProgressUpdateInfo.class.getSimpleName(), "Problem showing error dialog.", e);
        }
    }

}
