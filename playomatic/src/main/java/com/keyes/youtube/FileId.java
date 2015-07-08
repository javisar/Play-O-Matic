package com.keyes.youtube;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with IntelliJ IDEA.
 * User: kslazarev
 * Date: 03.11.12
 * Time: 8:55
 * To change this template use File | Settings | File Templates.
 */

public class FileId extends YouTubeId implements Parcelable {
    public FileId(String pId) {
        super(pId);
    }

    public static final Parcelable.Creator<FileId> CREATOR
		    = new Parcelable.Creator<FileId>() {
		public FileId createFromParcel(Parcel in) {
			FileId o = new FileId(in.readString());
		    return o;
		}
		
		public FileId[] newArray(int size) {
		    return new FileId[size];
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
	}
}