package com.keyes.youtube;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class UserId extends YouTubeId implements Parcelable {

	public UserId(String pId) {
		super(pId);
	}
	
	protected String title = "";
	
    
    public void setTitle(String t) {
    	this.title = t;
    }
    
    public String getTitle() {
    	return this.title;
    }

	public List<VideoId> obtainData() {
    	JSONObject lYouTubeResponse = null;
    	try {
    		lYouTubeResponse = YouTubeUtility.queryUserUploadsData(getId(), 50, 1);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
        if (lYouTubeResponse == null) {        	
        	return null;
        }
        
        List<VideoId> vList = YouTubeUtility.getVideoList(lYouTubeResponse);
        int c=0;
        Uri next = Uri.parse("");
        JSONObject jnext = lYouTubeResponse;
        while (next != null && c++<5) {
        	next = YouTubeUtility.getNextLink(jnext);
        	if (next != null) {
        		jnext = YouTubeUtility.queryLink(next);
        		if (jnext == null) break;
        		List<VideoId> vdlist = YouTubeUtility.getVideoList(jnext);
        		for (VideoId vid : vdlist) vList.add(vid);
        	}
        	c++;
        }
        /*
        List<VideoId> vListOut = new ArrayList<VideoId>();
        for (int i=vList.size()-1;i>=0;i--) {
        	vListOut.add(vList.get(i));
        }
        */
        return vList;
	}
	
	public List<PlaylistId> obtainPlaylist() {
    	JSONObject lYouTubeResponse = null;
    	try {
    		lYouTubeResponse = YouTubeUtility.queryUserPlaylistsData(getId(), 50, 1);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
        if (lYouTubeResponse == null) {        	
        	return null;
        }
        
        List<PlaylistId> vList = YouTubeUtility.getPlaylistList(lYouTubeResponse);
        int c=0;
        Uri next = Uri.parse("");
        JSONObject jnext = lYouTubeResponse;
        while (next != null && c++<5) {
        	next = YouTubeUtility.getNextLink(jnext);
        	if (next != null) {
        		jnext = YouTubeUtility.queryLink(next);
        		if (jnext == null) break;
        		List<PlaylistId> vdlist = YouTubeUtility.getPlaylistList(jnext);
        		for (PlaylistId vid : vdlist) vList.add(vid);
        	}
        	c++;
        }
        /*
        List<VideoId> vListOut = new ArrayList<VideoId>();
        for (int i=vList.size()-1;i>=0;i--) {
        	vListOut.add(vList.get(i));
        }
        */
        return vList;
	}

	public List<UserId> obtainSubscriptions() {
    	JSONObject lYouTubeResponse = null;
    	try {
    		lYouTubeResponse = YouTubeUtility.queryUserSubscriptionsData(getId(), 50, 1);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
        if (lYouTubeResponse == null) {        	
        	return null;
        }
        
        List<UserId> vList = YouTubeUtility.getSubscriptionList(lYouTubeResponse);
        int c=0;
        Uri next = Uri.parse("");
        JSONObject jnext = lYouTubeResponse;
        while (next != null && c++<5) {
        	next = YouTubeUtility.getNextLink(jnext);
        	if (next != null) {
        		jnext = YouTubeUtility.queryLink(next);
        		if (jnext == null) break;
        		List<UserId> vdlist = YouTubeUtility.getSubscriptionList(jnext);
        		for (UserId vid : vdlist) vList.add(vid);
        	}
        	c++;
        }
        /*
        List<VideoId> vListOut = new ArrayList<VideoId>();
        for (int i=vList.size()-1;i>=0;i--) {
        	vListOut.add(vList.get(i));
        }
        */
        return vList;
	}
	
    public static final Parcelable.Creator<UserId> CREATOR
		    = new Parcelable.Creator<UserId>() {
		public UserId createFromParcel(Parcel in) {
			UserId o = new UserId(in.readString());
			o.setTitle(in.readString());
		    return o;
		}
		
		public UserId[] newArray(int size) {
		    return new UserId[size];
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
	}
}
