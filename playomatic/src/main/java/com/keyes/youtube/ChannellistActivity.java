package com.keyes.youtube;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ChannellistActivity extends Activity {
	

	protected String channelId;
	protected String channelTitle;
	protected List<Button> buttonList = new ArrayList<Button>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Intent in = getIntent();
	    
	    channelId = in.getStringExtra("channelId");	    
	    channelTitle = in.getStringExtra("channelTitle");	   
	    setContentView(R.layout.channellist);
	    setTitle(channelTitle);
	    int c=1;
	    loadPlaylist(channelId);
	}
	
	protected boolean loadPlaylist(String tabId) {
		if (tabId.contains("mysubscriptions")) {
			return loadPlaylistSubscriptions();
		}
		
		return loadPlaylistNetFile(Playomatic.dataConfig.get("gdrive_playomatic_doc"),tabId);

	}
	

    protected boolean loadPlaylistSubscriptions() {
    	buttonList.clear(); 
    	
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listPlaylist);    	
    	
    	UserId userId = new UserId(Playomatic.dataConfig.get("subscription_user"));
    	List<UserId> subs = userId.obtainSubscriptions();
    	
    	Collections.sort(subs, new Comparator<UserId>() {
			@Override
			public int compare(UserId arg0, UserId arg1) {
				return arg0.getTitle().toLowerCase().compareTo(arg1.getTitle().toLowerCase());
			}
		});
    	
    	for (UserId sub : subs) {
    		//String pr = (String) itUp.next();

    		//String[] ppr = pr.split("/");
        	Button plButton = new Button(this);
        	//plButton.setText(playlistData[i]);
        	plButton.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        	plButton.setText(sub.getTitle());
        	addClickEvent_User(plButton, sub.getId(),sub.getTitle());
        	listPlaylist.addView(plButton);
        	buttonList.add(plButton);
    		
    	}    	    
    	    	
    	return true;
	}
	
    protected boolean loadPlaylistNetFile(String fileName, String tab) {
    	List<Button> bts = new ArrayList<Button>();
    	buttonList.clear(); 
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listPlaylist);
    	String data[][] = GDriveUtil.getSpreadSheet(fileName,tab);
    	
    	for (String[] row : data) { 
    			if (row.length < 5) continue;
        		if (!row[0].equals("UPLOADED") && !row[0].equals("OK")) continue;
        		if (!row[4].contains("=")) continue;
        		
        		Button plButton = new Button(this);
        		plButton.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            	//plButton.setText(playlistData[i]);
            	plButton.setText("["+row[1]+"] "+row[2]);
            	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                params.weight = 1.0f;
                params.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
                plButton.setLayoutParams(params);
            	addClickEvent_Playlist(plButton, row[4].split("=")[1],row[2]);
            	bts.add(plButton);        	    		    
    	}
    	
    	Collections.sort(bts, new Comparator<Button>() {
			@Override
			public int compare(Button lhs, Button rhs) {				
				return (""+lhs.getText()).compareTo(""+rhs.getText());
			}    		
		});
    	for (Button btn : bts) {
    		listPlaylist.addView(btn);        	
    	}
    	buttonList.addAll(bts);
    	return true;
    }
    
    protected boolean loadPlaylistFile(String fileName) {
    	buttonList.clear(); 
    	Properties properties = new Properties();
    	try {
			properties.load(Playomatic.class.getResourceAsStream("/assets/channels/"+fileName));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listPlaylist);
    	//LinearLayout listUploads = (LinearLayout) findViewById(R.id.listUploads);
    	
    	List<String> allPl = new ArrayList<String>();
    	List<String> allUp = new ArrayList<String>();
    	
    	int c=1;
    	while (true) {
    		String pr = (String) properties.get("pl"+c);
    		if (pr != null) {
    			allPl.add(pr);
			}
    		else {
    			break;
    		}
    		c++;
    	}
    	c=1;
    	while (true) {
    		String pr = (String) properties.get("up"+c);
    		if (pr != null) {
    			allUp.add(pr);    			
			}
    		else {
    			break;
    		}
    		c++;
    	}
    	
    	Collections.sort(allPl, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.split("/")[1].toUpperCase().compareTo(rhs.split("/")[1].toUpperCase());
			}
		});
    	
    	Collections.sort(allUp, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.split("/")[1].toUpperCase().compareTo(rhs.split("/")[1].toUpperCase());
			}
		});
    	
    	Iterator itPl = allPl.iterator();
    	while (itPl.hasNext()) {
    		String pr = (String) itPl.next();

			String[] ppr = pr.split("/");
        	Button plButton = new Button(this);
        	plButton.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        	//plButton.setText(playlistData[i]);
        	plButton.setText(ppr[1]);
        	addClickEvent_Playlist(plButton, ppr[0],ppr[1]);
        	listPlaylist.addView(plButton);
        	buttonList.add(plButton);
    		
    	}
    	
    	Iterator itUp = allUp.iterator();
    	while (itUp.hasNext()) {
    		String pr = (String) itUp.next();

    		String[] ppr = pr.split("/");
        	Button plButton = new Button(this);
        	plButton.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        	//plButton.setText(playlistData[i]);
        	plButton.setText(ppr[1]);
        	addClickEvent_User(plButton, ppr[0],ppr[1]);
        	listPlaylist.addView(plButton);
        	buttonList.add(plButton);
    		
    	}
    	return true;
	}

    protected void addClickEvent_Playlist(Button playlistButton, final String playlistId,final String title) {
    	playlistButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pV) {
            	PlaylistId pl = new PlaylistId(playlistId);
            	pl.setTitle(title);
            	openPlaylist(pl);
            }
        });
    }
    

    protected void addClickEvent_User(Button playlistButton, final String playlistId,final String title) {
    	playlistButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pV) {
            	UserId ur = new UserId(playlistId);
            	ur.setTitle(title);
            	openUploads(ur);
            }
        });
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
	        case R.id.alphabetic_order:
	        	alphabeticOrder();
	        	return true;
            case R.id.tag_order:
            	tagOrder();
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

    protected void tagOrder() {
    	Log.i(PlaylistId.class.getSimpleName(), "tagOrder()");
    	
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listPlaylist);
    	listPlaylist.removeAllViews();
    	Collections.sort(buttonList, new Comparator<Button>() {
			@Override
			public int compare(Button lhs, Button rhs) {
				//return lhs.split("/")[1].toUpperCase().compareTo(rhs.split("/")[1].toUpperCase());
				try {
					return ((String)lhs.getText()).split("\\[")[1].replace("]","").toUpperCase().compareTo(((String)rhs.getText()).split("\\[")[1].replace("]","").toUpperCase());
				}
				catch (Exception ex) {
					return 0;
				}
			}
		});
    	
    	for (Button b : buttonList) {
    		listPlaylist.addView(b);
    	}
    	
    	

        //LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	/*
        listPlaylist.removeAllViews();


        PlaylistId pl = YouTubeUtility.getCurrentPlaylist();
        List<VideoId> vList = pl.getVideoList();



  		List<VideoId> vListN = new ArrayList<VideoId>();
        Object[] vListA = vList.toArray();
        int plIdx = 0;
        for (int i=vListA.length-1;i>=0;i--) {
        	VideoId vd = (VideoId) vListA[i];
        	vd.plIdx = plIdx++;
        	vListN.add(vd);
        }
        */
        //pl.setVideoList(vListN);
        //refreshVideoList(pl);
    	
    }
    
    protected void alphabeticOrder() {
    	Log.i(ChannellistActivity.class.getSimpleName(), "alphabeticOrder()");
    	
    	LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listPlaylist);
    	listPlaylist.removeAllViews();
    	Collections.sort(buttonList, new Comparator<Button>() {
			@Override
			public int compare(Button lhs, Button rhs) {
				//return lhs.split("/")[1].toUpperCase().compareTo(rhs.split("/")[1].toUpperCase());
				try {
					//return ((String)lhs.getText()).replace(" ","").toUpperCase().compareTo(((String)rhs.getText()).replace(" ","").toUpperCase());
                    String r = ((String)rhs.getText()).replace(" ","").toUpperCase();
                    String l =((String)lhs.getText()).replace(" ","").toUpperCase();
                    if (r.indexOf("]") > 0) r = r.substring(r.indexOf("]")+1);
                    if (l.indexOf("]") > 0) l = l.substring(l.indexOf("]")+1);
                    return l.compareTo(r);
				}
				catch (Exception ex) {
					return 0;
				}
			}
		});
    	
    	for (Button b : buttonList) {
    		listPlaylist.addView(b);
    	}    	
    }
    
    protected void openUploads(UserId ur) {
		YouTubeUtility.playMode="show";
		Intent nextScreen = new Intent(null, Uri.parse("ytpl://" + ur.getId()), ChannellistActivity.this, PlaylistActivity.class);

		nextScreen.putExtra("openType", ur);
		//nextScreen.putExtra("playlistId", ur.getId());
		//nextScreen.putExtra("playlistTitle", title);
		//nextScreen.putExtra("mode", mode);
		nextScreen.putExtra("mode", YouTubeUtility.playMode);

        startActivity(nextScreen);            
	} 
    
    protected void openPlaylist(PlaylistId pl) {
		YouTubeUtility.playMode="show";
		Intent nextScreen = new Intent(null, Uri.parse("ytpl://" + pl.getId()), ChannellistActivity.this, PlaylistActivity.class);

		nextScreen.putExtra("openType", pl);
		//nextScreen.putExtra("playlistId", pl.getId());
		//nextScreen.putExtra("playlistTitle", title);
		//nextScreen.putExtra("mode", mode);
		nextScreen.putExtra("mode", YouTubeUtility.playMode);

        startActivity(nextScreen);            
	} 
	
	@Override
	protected void onDestroy() {
		super.onDestroy();		
	}
	
	
	public void onBackPressed() {		
		this.finish();
	}

}
