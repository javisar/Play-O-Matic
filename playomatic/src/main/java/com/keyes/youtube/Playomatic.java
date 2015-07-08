package com.keyes.youtube;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Playomatic extends Activity {
	protected QueryYouTubeTask mQueryYouTubeTask;

	public static Config mainListConfig = new Config("/assets/mainlist.properties");
	public static Config dataConfig = new Config("/assets/config.properties");

	@Override
    protected void onDestroy() {
        super.onDestroy();

        if (mQueryYouTubeTask != null) {
            mQueryYouTubeTask.cancel(true);
        }
        this.mQueryYouTubeTask = null;
        
	}
	
	@Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        setContentView(R.layout.playomatic);
		dataConfig.load();
		mainListConfig.load();
        loadMainList();
    }



    protected void loadMainList() {
		/*
    	Properties properties = new Properties();
    	try {
			properties.load(Playomatic.class.getResourceAsStream("/assets/mainlist.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		*/
		Properties properties = mainListConfig.getConfig();

		LinearLayout listPlaylist = (LinearLayout) findViewById(R.id.listChannellist);
		if (properties != null) {
			List<String> all = new ArrayList<String>();

			int c = 1;
			while (true) {
				String pr = (String) properties.get("ch" + c);
				if (pr != null) {
					all.add(pr);
				} else {
					break;
				}
				c++;
			}

			//Collections.sort(all);

			Iterator it = all.iterator();
			while (it.hasNext()) {
				String pr = (String) it.next();


				String[] ppr = pr.split("/");
				Button plButton = new Button(this);
				//plButton.setText(playlistData[i]);
				plButton.setText(ppr[1]);
				addClickEvent(plButton, ppr[0], ppr[1]);
				listPlaylist.addView(plButton);
			}
		}
    	
    	//load from gdrive
    	String data[][] = GDriveUtil.getSpreadSheet(dataConfig.get("gdrive_playomatic_doc"),dataConfig.get("gdrive_playomatic_tab"));
    	
    	for (String[] row : data) { 
    			if (row.length < 3) continue;
        		if (!row[0].equals("OK")) continue;        		
        		
        		Button plButton = new Button(this);
        		plButton.setText(row[1]);
        		addClickEvent(plButton, row[2],row[1]);
            	listPlaylist.addView(plButton);   
    	}    	
	}

    protected void addClickEvent(Button playlistButton, final String channelId, final String channelTitle) {
    	playlistButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pV) {      
            	openChannel(channelId,channelTitle);
            	//openPlaylist(new PlaylistId(playlistId),"show");
            }
        });
        playlistButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View pV) {
            	//openPlaylist(new PlaylistId(playlistId),"play");
            	openChannel(channelId,channelTitle);
                return true;
            }
			
        });
    }
    
    protected void openChannel(String channelId, String channelTitle) {
		Intent nextScreen = new Intent(null, Uri.parse("ytch://" + channelId), Playomatic.this, ChannellistActivity.class);

		nextScreen.putExtra("channelId", channelId);
		nextScreen.putExtra("channelTitle", channelTitle);
        startActivity(nextScreen); 
    }
    
    protected Date waitingForExit;
  
    @Override
    public void onBackPressed() {			   
		if (waitingForExit  == null) {
			waitingForExit = new Date();
			Toast.makeText(Playomatic.this, "Press back again to exit", Toast.LENGTH_LONG).show();
			return;
		}
		else {
			Date next = new Date(waitingForExit.getTime() + (2000));
			if (new Date().before(next)) {
				waitingForExit = new Date();
				
				boolean killSafely = true;
				if (killSafely) {
		            System.runFinalizersOnExit(true);
		            System.exit(0);
		        } else {
		            android.os.Process.killProcess(android.os.Process.myPid());
		        }
			}
			else {
				waitingForExit = new Date();
				Toast.makeText(Playomatic.this, "Press back again to exit", Toast.LENGTH_LONG).show();
				//finish();
				return;
			}
		}

		super.onBackPressed();
	}
    
}
