package com.keyes.youtube;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.LinearLayout.LayoutParams;

public class BaseActivity extends Activity implements Serializable {
	protected static boolean mShowedError = false;
	protected Activity 		parent;
    protected ProgressBar 	progressBar;
    protected TextView 		progressMessage;
    protected ViewGroup		layout;    

    public static final String EVT_NETWORK_UNAVAILABLE = "onNetworkUnavailable";
    public static final String EVT_ACTIVITY_DESTROY = "onActivityDestroy";
	public static final String EVT_PRE_TASK = "onPreTask";
	public static final String EVT_POST_TASK = "onPostTask";
	public static final String EVT_EXECUTE_TASK = "onExecuteTask";
	public static final String EVT_ON_ERROR = "onError";
	   
    protected void onNetworkUnavailable(Object... params) {};
    protected void onActivityDestroy(Object... params) {};
    protected void onPreTask(Object... params) {};
    protected void onError(Object... params) {};
    protected Object onExecuteTask(Object... params) { return null; };
    protected void onPostTask(Object... params) {};

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
    	this.onCreate(pSavedInstanceState);
    	this.parent = (Activity) this.getIntent().getSerializableExtra("parent");
    	onCreateSetupView();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	this.parent = (Activity) this.getIntent().getSerializableExtra("parent");
    }
    
    private void onCreateSetupView() {  
    	LinearLayout lLinLayout = new LinearLayout(this);
        lLinLayout.setId(1);
        lLinLayout.setOrientation(LinearLayout.VERTICAL);
        lLinLayout.setGravity(Gravity.CENTER);
        lLinLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lLinLayoutParms = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        lLinLayout.setLayoutParams(lLinLayoutParms);

        this.setContentView(lLinLayout);


        RelativeLayout lRelLayout = new RelativeLayout(this);
        lRelLayout.setId(2);
        lRelLayout.setGravity(Gravity.CENTER);
        lRelLayout.setBackgroundColor(Color.BLACK);
        android.widget.RelativeLayout.LayoutParams lRelLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lRelLayout.setLayoutParams(lRelLayoutParms);
        lLinLayout.addView(lRelLayout);               

        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setEnabled(true);
        progressBar.setId(4);
        android.widget.RelativeLayout.LayoutParams lProgressBarLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressBarLayoutParms.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(lProgressBarLayoutParms);
        lRelLayout.addView(progressBar);

        progressMessage = new TextView(this);
        progressMessage.setId(5);
        android.widget.RelativeLayout.LayoutParams lProgressMsgLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressMsgLayoutParms.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lProgressMsgLayoutParms.addRule(RelativeLayout.BELOW, 4);
        progressMessage.setLayoutParams(lProgressMsgLayoutParms);
        progressMessage.setTextColor(Color.LTGRAY);
        progressMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        progressMessage.setText("...");
        lRelLayout.addView(progressMessage);
        
        layout=lRelLayout;
    }

    public void updateProgress(String pProgressMsg) {
        try {
            progressMessage.setText(pProgressMsg);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error updating video status!", e);
        }
    }
    
    public static void hideProgess(Activity activity) {
    	if (activity instanceof BaseActivity) {
    		BaseActivity ba = (BaseActivity) activity;
    		ba.progressBar.setVisibility(View.GONE);
            ba.progressMessage.setVisibility(View.GONE);
    	}
    }
    
    public static void showProgess(Activity activity) {
    	if (activity instanceof BaseActivity) {
    		BaseActivity ba = (BaseActivity) activity;
	    	ba.progressBar.bringToFront();
	    	ba.progressBar.setVisibility(View.VISIBLE);
    	}
    }
        
    public static void fireEvent(Activity activity, String event,Object... params) {
    	if (params[0] != activity) return;
    	
    	if (activity instanceof BaseActivity) {
    		BaseActivity ba = (BaseActivity) activity;
    		if (event.compareTo(EVT_NETWORK_UNAVAILABLE)==0)  ba.onNetworkUnavailable(params);
    		if (event.compareTo(EVT_ACTIVITY_DESTROY)==0)  ba.onActivityDestroy();
    		if (event.compareTo(EVT_PRE_TASK)==0)  ba.onPreTask(params);
    		if (event.compareTo(EVT_POST_TASK)==0)  ba.onPostTask(params);
    		if (event.compareTo(EVT_EXECUTE_TASK)==0)  ba.onExecuteTask(params);
    		if (event.compareTo(EVT_ON_ERROR)==0)  ba.onError(params);
    	}
    }
  
}
