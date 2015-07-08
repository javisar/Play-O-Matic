/**
 *
 */
package com.keyes.youtube;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class PlaylistId extends YouTubeId implements Parcelable {
	protected JSONObject pData = new JSONObject();
	protected ArrayList<VideoId> vList = new ArrayList<VideoId>();
	protected int vIndex = 0;
	protected String title = "";
	protected String plDescription = "";
	
    public PlaylistId(String pId) {
        super(pId);
    }
    
    public void setTitle(String t) {
    	this.title = t;
    }
    
    public String getTitle() {
    	return this.title;
    }
    public void setVideoList(ArrayList<VideoId> vl) {
        this.vList = vl;
    }
    
    public ArrayList<VideoId> getVideoList() {
        return vList;
    }
    
    public JSONObject getData() {
        return pData;
    }
        
    public JSONObject obtainData() {
    	JSONObject lYouTubeResponse = null;
    	try {
    		lYouTubeResponse = YouTubeUtility.queryPlaylistData(this,50,1);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
        if (lYouTubeResponse == null) {        	
        	return null;
        }
        
        setData(lYouTubeResponse);
        /*
        Uri next = Uri.parse("");
        JSONObject jnext = lYouTubeResponse;
        while (next != null) {
        	next =YouTubeUtility. getNextLink(jnext);
        	if (next != null) {
        		jnext = YouTubeUtility.queryLink(next);
        		if (jnext == null) break;
        		List<VideoId> vdlist = YouTubeUtility.getVideoList(jnext);
        		for (VideoId vid : vdlist) this.vList.add(vid);
        	}
        }
        */
        return lYouTubeResponse;
    }

    public void setData(JSONObject data) {
    	this.pData = data;    	
    	ArrayList<VideoId> videoList = new ArrayList<VideoId>();
    	List<VideoId> vdlist = YouTubeUtility.getVideoList(pData);
    	for (VideoId vid : vdlist) videoList.add(vid);
    	
    	//String desc = YouTubeUtility.getPlaylistDescription(pData);
    	
    	this.pData = data;    	
    	this.vList = videoList;
    	//this.plDescription = desc;
    }
 
    public void setCurrentVideo(int idx) {    	
    	this.vIndex=idx;
    }
    
    public VideoId getCurrentVideo() {    	
    	return this.vList.get(this.vIndex);
    }
    
    public void resetCurrentVideo() {    	
    	getCurrentVideo().setPosition(0);
    }
    
    public VideoId getNextVideo() {
    	int idx = this.vIndex+1;
    	//if (idx>this.vList.size()-1) return null;
    	if (idx>this.vList.size()-1) idx=0;
    	this.vIndex=idx;
    	resetCurrentVideo();
    	return getCurrentVideo();
    }
    
    public VideoId getPrevVideo() {
    	int idx = this.vIndex-1;
    	//if (idx<0) return null;
    	if (idx<0) idx=this.vList.size()-1;
    	this.vIndex=idx;
    	resetCurrentVideo();
    	return getCurrentVideo();
    }
    

    public static final Parcelable.Creator<PlaylistId> CREATOR
		    = new Parcelable.Creator<PlaylistId>() {
		public PlaylistId createFromParcel(Parcel in) {
			PlaylistId o = new PlaylistId(in.readString());
			o.setTitle(in.readString());
			o.setCurrentVideo(in.readInt());
			o.setVideoList(in.readArrayList(PlaylistId.class.getClassLoader()));
		    return o;
		}
		
		public PlaylistId[] newArray(int size) {
		    return new PlaylistId[size];
		}
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mId);
		dest.writeString(this.title);
		dest.writeInt(this.vIndex);
		dest.writeArray(this.vList.toArray());
	}
}