package com.keyes.youtube;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
 
public class PlaylistActivity extends Activity {
		
	protected ProgressBar mProgressBar;
    protected TextView mProgressMessage;
	protected YouTubePlayerActivity videoActivity;
	protected List<Button> buttonList = new ArrayList<Button>();
	

	protected QueryYouTubeTask mQueryYouTubeTask;
	protected String playMode = "show";
	protected PlaylistId playlist;
	protected int mode = 1;
	protected Parcelable openType;
    /** Called when the activity is first created. */
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent inte = getIntent();
        
        setContentView(R.layout.playlist);        
        LinearLayout lLinLayout = (LinearLayout) findViewById(R.id.listVideolist);
        RelativeLayout lRelLayout = new RelativeLayout(this);
        //lRelLayout.setId(2);
        lRelLayout.setGravity(Gravity.CENTER);
        lRelLayout.setBackgroundColor(Color.BLACK);
        android.widget.RelativeLayout.LayoutParams lRelLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lRelLayout.setLayoutParams(lRelLayoutParms);
        lLinLayout.addView(lRelLayout);
        
        mProgressBar = new ProgressBar(this);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setEnabled(true);
        //mProgressBar.setId(4);
        android.widget.RelativeLayout.LayoutParams lProgressBarLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressBarLayoutParms.addRule(RelativeLayout.CENTER_IN_PARENT);
        mProgressBar.setLayoutParams(lProgressBarLayoutParms);
        lRelLayout.addView(mProgressBar);

        mProgressMessage = new TextView(this);
        //mProgressMessage.setId(5);
        android.widget.RelativeLayout.LayoutParams lProgressMsgLayoutParms = new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressMsgLayoutParms.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lProgressMsgLayoutParms.addRule(RelativeLayout.BELOW, 4);
        mProgressMessage.setLayoutParams(lProgressMsgLayoutParms);
        mProgressMessage.setTextColor(Color.LTGRAY);
        mProgressMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mProgressMessage.setText("...");
        lRelLayout.addView(mProgressMessage);
        
        mProgressBar.bringToFront();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressMessage.setText("Loading...");
        
        if (YouTubeUtility.lYouTubeFmtQuality == null) YouTubeUtility.lYouTubeFmtQuality = prepareNetwork();
        this.playMode = inte.getStringExtra("mode");
        
        openType =  inte.getParcelableExtra("openType");
        String plTitle = "";
        if (openType instanceof PlaylistId) {
        	PlaylistId pl = (PlaylistId) openType;
        	this.playlist = pl;
        	plTitle = pl.getTitle();
        }
        else if (openType instanceof UserId) {
        	UserId ur = (UserId) openType;
        	plTitle = ur.getTitle();
        }
        
        setTitle(plTitle);
        
        mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask(this).execute(openType);
        /*
        String splId = inte.getStringExtra("playlistId");
        String plTitle = inte.getStringExtra("playlistTitle");
        if (splId.compareTo(plTitle)!=0) {
	        PlaylistId pl = new PlaylistId(splId);
	        this.playlist = new PlaylistId(splId);
	        this.playMode = inte.getStringExtra("mode");
	        
	        setContentView(R.layout.playlist);
	        setTitle(plTitle);
	        // Binding Click event to Button            
	        //mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask().execute(this,mVideoView,lYouTubeId);
	        mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask(this).execute(pl);    
        }
        else {
        	UserId ur = new UserId(splId);
	        this.playlist = new PlaylistId(splId);
	        this.playMode = inte.getStringExtra("mode");
	        
	        setContentView(R.layout.playlist);
	        setTitle(plTitle);
	        // Binding Click event to Button            
	        //mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask().execute(this,mVideoView,lYouTubeId);
	        mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask(this).execute(ur);
        }
        */
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reverse_order:
                reverseOrder();
                return true;
            case R.id.alphabetic_order:
                alphabeticOrder();
                return true;
            case R.id.uploads_playlist:
            	toggleUploadsPlaylists();
            	return true;
            case R.id.toggle_quality:
            	YouTubeUtility.toggleQuality(this);
            	return true;
            case R.id.toggle_player:
                YouTubeUtility.togglePlayer(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
   
    protected void toggleUploadsPlaylists() {
    	Log.i(PlaylistId.class.getSimpleName(), "toogleUploadsPlaylists()");

    	
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listVideolist);
        //LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        listPlaylist.removeAllViews();  
        
        if (mode == 0) mode = 1;
    	else if (mode == 1) mode = 0;
        mQueryYouTubeTask = (QueryYouTubeTask) new QueryYouTubeTask(this).execute(openType);
    	    	
    }
    
    protected void reverseOrder() {
    	Log.i(PlaylistId.class.getSimpleName(), "reverseOrder()");
    	
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listVideolist);
        //LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        listPlaylist.removeAllViews();  
        
        if (mode == 0) {
	        PlaylistId pl = YouTubeUtility.getCurrentPlaylist();
	        ArrayList<VideoId> vList = pl.getVideoList();
	        ArrayList<VideoId> vListN = new ArrayList<VideoId>();
	        Object[] vListA = vList.toArray();
	        int plIdx = 0;
	        for (int i=vListA.length-1;i>=0;i--) {
	        	VideoId vd = (VideoId) vListA[i];
	        	vd.plIdx = plIdx++;
	        	vListN.add(vd);
	        }
	        pl.setVideoList(vListN);
            YouTubeUtility.setCurrentPlaylist(pl);
	        refreshVideoList(pl);
        }
        else {
        	/*
        	Collections.sort(buttonList, new Comparator<Button>() {
    			@Override
    			public int compare(Button lhs, Button rhs) {
    				//return lhs.split("/")[1].toUpperCase().compareTo(rhs.split("/")[1].toUpperCase());
    				try {
    					return -((String)lhs.getText()).toUpperCase().compareTo(((String)rhs.getText()).toUpperCase());
    				}
    				catch (Exception ex) {
    					return 0;
    				}
    			}
    		});
        	       	
        	for (Button b : buttonList) {
        		listPlaylist.addView(b);
        	}  
        	*/
            /*
        	List<Button> btList = new ArrayList<Button>();
        	for (int i=buttonList.size()-1; i>=0;i--) {
        		listPlaylist.addView(buttonList.get(i));
        		btList.add(buttonList.get(i));
        	}
        	buttonList = btList;
        	*/
            PlaylistId pl = YouTubeUtility.getCurrentPlaylist();
            ArrayList<VideoId> vList = pl.getVideoList();
            ArrayList<VideoId> vListN = new ArrayList<VideoId>();
            Object[] vListA = vList.toArray();
            int plIdx = 0;
            for (int i=vListA.length-1;i>=0;i--) {
                VideoId vd = (VideoId) vListA[i];
                vd.plIdx = plIdx++;
                vListN.add(vd);
            }
            pl.setVideoList(vListN);
            YouTubeUtility.setCurrentPlaylist(pl);
            refreshVideoList(pl);
        }
    	
    }
    
    protected void alphabeticOrder() {
    	Log.i(PlaylistActivity.class.getSimpleName(), "alphabeticOrder()");
    	
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listVideolist);
    	listPlaylist.removeAllViews();
    	PlaylistId pl = YouTubeUtility.getCurrentPlaylist();
        ArrayList<VideoId> vList = pl.getVideoList();
        
    	Collections.sort(vList, new Comparator<VideoId>() {
			@Override
			public int compare(VideoId lhs, VideoId rhs) {
				//return lhs.split("/")[1].toUpperCase().compareTo(rhs.split("/")[1].toUpperCase());
				try {
					//return ((String)lhs.getText()).replace(" ","").toUpperCase().compareTo(((String)rhs.getText()).replace(" ","").toUpperCase());
					//return lhs.title.replace(" ","").toUpperCase().compareTo(rhs.title.replace(" ","").toUpperCase());
                    String r = rhs.title.replace(" ","").toUpperCase();
                    String l = lhs.title.replace(" ","").toUpperCase();
                    if (r.indexOf("]") > 0) r = r.substring(r.indexOf("]")+1);
                    if (l.indexOf("]") > 0) l = l.substring(l.indexOf("]")+1);
                    return l.compareTo(r);
				}
				catch (Exception ex) {
					return 0;
				}
			}
		});
    	
    	//pl.setVideoList(vList);
        refreshVideoList(pl);	
    }
    protected void resumePlaylist() {
		//String videoId = videoIdTextView.getText().toString();

			
		PlaylistId pl = YouTubeUtility.getCurrentPlaylist();		
		if (pl == null) return; 		
		VideoId vl = pl.getCurrentVideo();
        if (vl == null) return;
        
        YouTubeUtility.cancelled = false;

        //Intent lVideoIntent = new Intent(null, Uri.parse("ytv://" + videoId), Sample.this, OpenYouTubePlayerActivity.class);
        Intent lVideoIntent = new Intent("vplayer", Uri.parse("ytv://" + vl.getId()), PlaylistActivity.this, YouTubePlayerActivity.class);
        lVideoIntent.putExtra("quality", YouTubeUtility.lYouTubeFmtQuality);
        lVideoIntent.putExtra("videoTitle", vl.title);
        /*
        Activity vplayer = new LocalActivityManager(this, false).getActivity("vplayer");
        videoActivity = (YouTubePlayerActivity) vplayer;
        
		if (this.playMode.compareTo("play")==0) {
			videoActivity.nextVideo();
		}
		*/
                //.startActivity("ReferenceName",lVideoIntent
                //.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                //.getDecorView();
       //Your_group_cativity.this.setContentView(view); 
        //LocalActivityManager.this.getActivity(lVideoIntent.);
        //videoActivity = startActivity(lVideoIntent);
		//startActivity(lVideoIntent);
		startActivityForResult(lVideoIntent, 1);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
    	//Log.d(this.getClass().getSimpleName(),data.getData().toString());
    	
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
            	
                // Do something with the contact here (bigger example below)
            	//if (params[0] instanceof YouTubePlayerActivity) {
     		       //openPlaylist(YouTubeUtility.getCurrentPlaylist(),YouTubeUtility.playMode);
            	//if (this.playMode.compareTo("play")==0) {
            		resumePlaylist();
            	//}
            	//}
            }
            else if (resultCode == RESULT_CANCELED) {
            	if (this.playMode.compareTo("play")==0) {
            		this.finish();
            	}
            	
            }
            else if (resultCode == YouTubePlayerActivity.RESULT_VIDEO_ERROR) {
            	//if (this.playMode.compareTo("play")==0) {            		
            		resumePlaylist();
            	//}            	
            	
            }
            else if (resultCode == YouTubePlayerActivity.RESULT_ERROR) {
            	//if (this.playMode.compareTo("play")==0) {
            		try {
        	            Thread.sleep(10000);
        	        } catch (InterruptedException e) {
        	            //e.printStackTrace();
        	        }
            		resumePlaylist();
            	//}            	
            	
            }
            else if (resultCode == YouTubePlayerActivity.RESULT_WARNING) {
            	//if (this.playMode.compareTo("play")==0) {
            		try {
        	            Thread.sleep(10000);
        	        } catch (InterruptedException e) {
        	            e.printStackTrace();
        	        }
            		resumePlaylist();
            	//}              	
            }
        }
    }
    protected void onPreTask(Object... params) { };
    
    protected String prepareNetwork() {
    	String lYouTubeFmtQuality = "18";
        WifiManager lWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        TelephonyManager lTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        ////////////////////////////
        // if we have a fast connection (wifi or 3g), then we'll get a high quality YouTube video
        if ((lWifiManager.isWifiEnabled() && lWifiManager.getConnectionInfo() != null && lWifiManager.getConnectionInfo().getIpAddress() != 0) ||
                ((lTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS ||

		   /* icky... using literals to make backwards compatible with 1.5 and 1.6 */
                        lTelephonyManager.getNetworkType() == 9 /*HSUPA*/ ||
                        lTelephonyManager.getNetworkType() == 10 /*HSPA*/ ||
                        lTelephonyManager.getNetworkType() == 8 /*HSDPA*/ ||
                        lTelephonyManager.getNetworkType() == 5 /*EVDO_0*/ ||
                        lTelephonyManager.getNetworkType() == 6 /*EVDO A*/)

                        && lTelephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)
                ) {
            lYouTubeFmtQuality = "18";
        }
        return lYouTubeFmtQuality;

    }
    
    protected void openPlaylist(PlaylistId pl) {
		YouTubeUtility.playMode="show";
		Intent nextScreen = new Intent(null, Uri.parse("ytpl://" + pl.getId()), PlaylistActivity.this, PlaylistActivity.class);

		nextScreen.putExtra("openType", pl);
		//nextScreen.putExtra("playlistId", pl.getId());
		//nextScreen.putExtra("playlistTitle", title);
		//nextScreen.putExtra("mode", mode);
		nextScreen.putExtra("mode", YouTubeUtility.playMode);

        startActivity(nextScreen);            
	} 
    
    protected void refreshVideoList(List<PlaylistId> pll) {
    	if (pll == null) {
    		this.finish();
    		return;
    	}
    	buttonList.clear(); 
        LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listVideolist);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        listPlaylist.removeAllViews();    
        
        Collections.sort(pll, new Comparator<PlaylistId>() {
			@Override
			public int compare(PlaylistId lhs, PlaylistId rhs) {
				//return lhs.split("/")[1].toUpperCase().compareTo(rhs.split("/")[1].toUpperCase());
				try {
					return lhs.getTitle().toUpperCase().compareTo(rhs.getTitle().toUpperCase());
				}
				catch (Exception ex) {
					return 0;
				}
			}
		});
        
        //ArrayList<String> playlistData = inte.getStringArrayListExtra("playlistData");
        Iterator<PlaylistId> it = pll.iterator();
        //for (int i=0; i<playlistData.length;i++)
        while (it.hasNext())
        //for (int i=varray.length-1; i>=0;i--)
        {
        	final PlaylistId plID = it.next();
        	//final VideoId plID = (VideoId) varray[i];
        	Button plButton = new Button(this);
        	//plButton.setText(playlistData[i]);
        	plButton.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        	plButton.setText(plID.title);  
        	plButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View pV) {            	
                	//openPlaylist(new PlaylistId(getResources().getString(playlistId)),"show");                	
                	openPlaylist(plID);                	
                }
            });
        	
            listPlaylist.addView(plButton);
            buttonList.add(plButton);
        }
        
        
        
    }
       
    protected Object onExecuteTask(Object[] params) {    	
    	
    	Object pType = params[0];
    	
    	//if (!(pType instanceof PlaylistId)) return;    	
    	    
        
     	
        	this.updateProgress(ProgressUpdateInfo.mMsgDetect);
        	
        	this.updateProgress(ProgressUpdateInfo.mMsgPlaylist);            
            if (pType instanceof PlaylistId) {
            	PlaylistId pl = (PlaylistId) pType;    	
            	//lYouTubeVideoId = YouTubeUtility.queryLatestPlaylistVideo((PlaylistId) pParams[0]);
                JSONObject lYouTubeResponse = pl.obtainData();
                if (lYouTubeResponse == null) {
                	ProgressUpdateInfo.showErrorAlert(this,"Cannot load playlist data", null);
                	YouTubeUtility.resetCurrentPlaylist();
                	return null;
                }
                YouTubeUtility.setCurrentPlaylist(pl);                                          
            } else if (pType instanceof VideoId) {
            	PlaylistId pl = new PlaylistId("dummy");
            	pl.getVideoList().add((VideoId)pType);
            	YouTubeUtility.setCurrentPlaylist(pl);
                //lYouTubeVideoId = this.youtubeId.getId();
            } else if (pType instanceof UserId) {
            	if (mode == 0) {
	            	UserId ur = (UserId) pType;
	            	PlaylistId pl = new PlaylistId(ur.getId());
	            	//lYouTubeVideoId = YouTubeUtility.queryLatestPlaylistVideo((PlaylistId) pParams[0]);
	                List<VideoId> vlist = ur.obtainData();
	                
	                if (vlist == null || vlist.size() == 0) {
	                	//ProgressUpdateInfo.showErrorAlert(this,"Cannot load user uploads data", null);
	                	YouTubeUtility.resetCurrentPlaylist();
	                	UserId ur1 = (UserId) pType;
		            	PlaylistId pl1 = new PlaylistId(ur1.getId());
		            	//lYouTubeVideoId = YouTubeUtility.queryLatestPlaylistVideo((PlaylistId) pParams[0]);
		                List<PlaylistId> vlist1 = ur1.obtainPlaylist();
		                
		                if (vlist1 == null || vlist1.size() == 0) {
		                	ProgressUpdateInfo.showErrorAlert(this,"Cannot load user playlists data", null);
		                	//YouTubeUtility.resetCurrentPlaylist();
		                	return null;
		                }
		                
		               return vlist1;
	                }
	                /*
	                List<VideoId> vListOut = new ArrayList<VideoId>();
	                Object[] varray =  vlist.toArray();
	                for (int i=varray.length-1;i>=0;i--) {
	                	vListOut.add((VideoId) varray[i]);
	                }
	                */
	                pl.setVideoList((ArrayList<VideoId>) vlist);
	                YouTubeUtility.setCurrentPlaylist(pl);     
            	}
            	else if (mode == 1) {
            		UserId ur = (UserId) pType;
	            	PlaylistId pl = new PlaylistId(ur.getId());
	            	//lYouTubeVideoId = YouTubeUtility.queryLatestPlaylistVideo((PlaylistId) pParams[0]);
	                List<PlaylistId> vlist = ur.obtainPlaylist();
	                
	                if (vlist == null || vlist.size() == 0) {
	                	ProgressUpdateInfo.showErrorAlert(this,"Cannot load user playlists data", null);
	                	//YouTubeUtility.resetCurrentPlaylist();
	                	return null;
	                }
	                
	               return vlist;
            	}
            	
            }

            //mVideoId = lYouTubeVideoId;

            this.updateProgress(ProgressUpdateInfo.mMsgToken);

            if (mQueryYouTubeTask.isCancelled())
                return null;

            ////////////////////////////////////
            // calculate the actual URL of the video, encoded with proper YouTube token
            //String lUriStr = YouTubeUtility.calculateYouTubeUrl(lYouTubeFmtQuality, true, lYouTubeVideoId);
            

            //if (mQueryYouTubeTask.isCancelled())
            //    return; //return null;

            if (YouTubeUtility.lYouTubeFmtQuality.equals("17")) {
            	this.updateProgress(ProgressUpdateInfo.mMsgLowBand);
            } else {
            	this.updateProgress(ProgressUpdateInfo.mMsgHiBand);
            }

      
        return YouTubeUtility.getCurrentPlaylist();
        //Uri videoId = Uri.parse(pl.getCurrentVideo().getId());
        
        /*
    	if (pParams[1] instanceof FileId) {
            return Uri.parse(this.youtubeId.getId());
        } else {
            String lUriStr = null;
            String lYouTubeFmtQuality = "17";   // 3gpp medium quality, which should be fast enough to view over EDGE connection
            String lYouTubeVideoId = null;

            YouTubeUtility.getCurrentPlaylist().getCurrentVideo().setPosition(videoActivity.mVideoView.getCurrentPosition());
            
            BaseActivity.fireEvent(videoActivity, BaseActivity.EVT_ACTIVITY_DESTROY, this);
                      

            try {
            	this.updateProgress(ProgressUpdateInfo.mMsgDetect);
                //publishProgress(new ProgressUpdateInfo(ProgressUpdateInfo.mMsgDetect));

            	lYouTubeFmtQuality = prepareNetwork();
                ///////////////////////////////////
                // if the intent is to show a playlist, get the latest video id from the playlist, otherwise the video
                // id was explicitly declared.
                if (pParams[0] instanceof PlaylistId) {
                	this.updateProgress(ProgressUpdateInfo.mMsgPlaylist);
                    lYouTubeVideoId = YouTubeUtility.queryLatestPlaylistVideo((PlaylistId) pParams[0]);
                                                                                        
                } else if (pParams[0] instanceof VideoId) {
                    lYouTubeVideoId = this.youtubeId.getId();
                }

                mVideoId = lYouTubeVideoId;

                this.updateProgress(ProgressUpdateInfo.mMsgToken);

                if (mQueryYouTubeTask.isCancelled())
                    return; //return null;

                ////////////////////////////////////
                // calculate the actual URL of the video, encoded with proper YouTube token
                lUriStr = YouTubeUtility.calculateYouTubeUrl(lYouTubeFmtQuality, true, lYouTubeVideoId);

                if (mQueryYouTubeTask.isCancelled())
                    return; //return null;

                if (lYouTubeFmtQuality.equals("17")) {
                	this.updateProgress(ProgressUpdateInfo.mMsgLowBand);
                } else {
                	this.updateProgress(ProgressUpdateInfo.mMsgHiBand);
                }

            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error occurred while retrieving information from YouTube.", e);
            }

            if (lUriStr != null) {
                return; //return Uri.parse(lUriStr);
            } else {
                return; //return null;
            }
        }
        */
    };
    
    public void updateProgress(String pProgressMsg) {
        try {
            //progressMessage.setText(pProgressMsg);
        	Log.d(this.getClass().getSimpleName(), pProgressMsg);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error updating video status!", e);
        }
    }
    
    private static class ThumbnailTask extends AsyncTask {
    	// new ThumbnailTask(position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
		private int mPosition;
        private ViewHolder mHolder;

        @Override
		protected void onPostExecute(Object bitmap) {			
			super.onPostExecute(bitmap);
			 if (mHolder.position == mPosition) {
	                mHolder.thumbnail.setImageBitmap((Bitmap) bitmap);
	            }
		}

        public ThumbnailTask(int position, ViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }      

		@Override
		protected Object doInBackground(Object... params) {
			// Download bitmap here
			return null;
		}
    }

    private static class ViewHolder {
        public ImageView thumbnail;
        public int position;
    }
    
    protected void refreshVideoList(PlaylistId pl) {
    	if (pl == null) {
    		this.finish();
    		return;
    	}
    	buttonList.clear();
    	
        LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listVideolist);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        listPlaylist.removeAllViews();    
        
        //ArrayList<String> playlistData = inte.getStringArrayListExtra("playlistData");
        Iterator<VideoId> it = pl.vList.iterator();
        Object[] varray = pl.vList.toArray();
        //for (int i=0; i<playlistData.length;i++)
        while (it.hasNext())
        //for (int i=varray.length-1; i>=0;i--)
        {
        	final VideoId plID = it.next();
        	//final VideoId plID = (VideoId) varray[i];
        	Button plButton = new Button(this);
        	//plButton.setText(playlistData[i]);
        	plButton.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        	plButton.setText(plID.title);
        	plButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View pV) {            	
                	//openPlaylist(new PlaylistId(getResources().getString(playlistId)),"show");                	
                	
                	YouTubeUtility.setCurrentVideo(plID);
                	playMode="show";
                	resumePlaylist();
                }
            });
        	plButton.setOnLongClickListener(new View.OnLongClickListener() {
               
				@Override
				public boolean onLongClick(View v) {
					//openPlaylist(new PlaylistId(getResources().getString(playlistId)),"show");                	
                	
                	YouTubeUtility.setCurrentVideo(plID);
                	playMode="play";
                	resumePlaylist();
					return false;
				}
            });
        	buttonList.add(plButton);
            listPlaylist.addView(plButton);
            
        }
        
    }
        
	protected void onPostTask(Object[] result) {		
		mProgressBar.setVisibility(View.GONE);
        mProgressMessage.setVisibility(View.GONE);
        
		if (result == null || result.length <= 0) return;
        ////////////////////////////////////
        // calculate the actual URL of the video, encoded with proper YouTube token
		
		if (result[0] instanceof List) {
			List<PlaylistId> vlist = (List<PlaylistId>) result[0];
			refreshVideoList(vlist);            
			return;
		}
		
		
    	PlaylistId pl = (PlaylistId) result[0];
        if (this.playMode.compareTo("show")==0) {
        	refreshVideoList(pl);
        }
        else if (this.playMode.compareTo("play")==0) {
            //String lYouTubeVideoId = pl.getCurrentVideo().getId();
	        resumePlaylist();
	        
        }        
	}
			
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (mQueryYouTubeTask != null) {
            mQueryYouTubeTask.cancel(true);
        }
        this.mQueryYouTubeTask = null;
    }
    
    
    public void onBackPressed() {		
		this.finish();
	}

}
