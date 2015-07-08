/**
 *
 */
package com.keyes.youtube;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;


public class VideoId extends YouTubeId implements Parcelable {
	protected int plIdx = 0;
	protected int cPos = 0;
	protected String title;

    public VideoId(String pId) {
        super(pId);
        title=pId;
    }
    public void setPosition(int pos) {
    	this.cPos = pos;
    }
    public int getPosition() {
    	return this.cPos;
    }
    
    
    public void setTitle(String t) {
    	this.title = t;
    }
    
    public String getTitle() {
    	return this.title;
    }

    public static final Parcelable.Creator<VideoId> CREATOR
		    = new Parcelable.Creator<VideoId>() {
		public VideoId createFromParcel(Parcel in) {
			VideoId o = new VideoId(in.readString());
			o.setTitle(in.readString());
			o.setPosition(in.readInt());
		    return o;
		}
		
		public VideoId[] newArray(int size) {
		    return new VideoId[size];
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
		dest.writeInt(this.cPos);
	}
}