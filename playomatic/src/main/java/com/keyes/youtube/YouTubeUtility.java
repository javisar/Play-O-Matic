package com.keyes.youtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class YouTubeUtility {

    //public static List<String> videoList = new ArrayList<String>();
    public static PlaylistId currentPlaylist = null;
    protected static String lYouTubeFmtQuality = null;
    protected static String currentPlayer = "default";
    //public static int currentPos = 0;
    public static boolean cancelled = true;
    public static String playMode = "show";

    public static HashMap<String, PlaylistId> videosOfPlaylists = new HashMap<String, PlaylistId>();

    public static void addPlaylist(String plid, PlaylistId pl) {
        videosOfPlaylists.put(plid, pl);
    }

    public static PlaylistId removePlaylist(String plid) {
        PlaylistId rpl = videosOfPlaylists.get(plid);
        videosOfPlaylists.remove(plid);
        return rpl;
    }

    public static PlaylistId getPlaylist(String plid) {
        return videosOfPlaylists.get(plid);
    }

    public static PlaylistId getCurrentPlaylist() {
        return currentPlaylist;
    }

    public static void setCurrentPlaylist(PlaylistId pl) {
        currentPlaylist = pl;
        initCurrentPlaylist();
    }

    public static void initCurrentPlaylist() {
        if (currentPlaylist == null) return;
        for (VideoId vd : currentPlaylist.getVideoList()) {
            vd.setPosition(0);
        }
        currentPlaylist.setCurrentVideo(0);
    }

    public static void resetCurrentPlaylist() {
        initCurrentPlaylist();
        currentPlaylist = null;
    }
   
    /*
    public static String queryLatestPlaylistVideo(PlaylistId pPlaylistId) {
    	    	
    	if (getCurrentPlaylist() != null) {    		
    		return getCurrentPlaylist().getCurrentVideo().mId;
    	}
    	    	
    	PlaylistId videoList = YouTubeUtility.getPlaylist(pPlaylistId.mId);
    	JSONObject lYouTubeResponse = queryPlaylistData(pPlaylistId);
        if (lYouTubeResponse == null) return null;
        videoList.setData(lYouTubeResponse);
        
        setCurrentPlaylist(videoList);
        
        String firstVideo=PlaylistId.getVideolist(lYouTubeResponse).get(0);
        //return lVideoId;
        return firstVideo;
    }
    */

    public static String httpQuery(String url) throws ClientProtocolException, IOException {
        ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
        HttpClient lClient = new DefaultHttpClient();
        HttpGet lGetMethod = new HttpGet(url);
        HttpResponse lResp = null;
        String lInfoStr = null;

        lResp = lClient.execute(lGetMethod);
        lResp.getEntity().writeTo(lBOS);
        lInfoStr = lBOS.toString("UTF-8");
        return lInfoStr;
    }

    public static JSONObject queryLink(Uri link) {


        String lInfoStr = null;
        JSONObject lYouTubeResponse = null;


        try {
            lInfoStr = httpQuery(link.toString());
            lYouTubeResponse = new JSONObject(lInfoStr);
            return lYouTubeResponse;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject queryPlaylistData(PlaylistId pPlaylistId, int maxResults, int startIndex) {


        String lInfoStr = null;
        JSONObject lYouTubeResponse = null;


        try {
            //lInfoStr = httpQuery(YouTubePlayerActivity.YOUTUBE_PLAYLIST_ATOM_FEED_URL + pPlaylistId.getId() + "?v=2&start-index="+startIndex+"&max-results="+maxResults+"&alt=json");
            lInfoStr = httpQuery("http://javisar.net/mediaparser/mediaparser.php?user="+Playomatic.dataConfig.get("mediaparser_key")+"&action=episodes&provider=youtube&href=www.youtube.com%2Fplaylist%3Flist%3D" + pPlaylistId.getId());
            lYouTubeResponse = new JSONObject(lInfoStr);
            return lYouTubeResponse;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<VideoId> getVideoList(JSONObject lYouTubeResponse) {
        List<VideoId> vlist = new ArrayList<VideoId>();

        try {
            JSONArray lEntryArr = lYouTubeResponse.getJSONArray("results");
            for (int k = 0; k < lEntryArr.length(); k++) {
                JSONObject item = lEntryArr.getJSONObject(k);
                String title = item.getString("title");
                String lVideoId = item.getString("id");

                VideoId vd = new VideoId(lVideoId);
                vd.title = title;
                vd.plIdx = vlist.size();
                vlist.add(vd);
            }
        } catch (JSONException e) {
            Log.i(YouTubeUtility.class.getSimpleName(), "Error retrieving content from YouTube", e);
        }
        return vlist;
    }

    public static List<VideoId> getVideoListOld(JSONObject lYouTubeResponse) {
        String lVideoId = null;
        List<VideoId> vlist = new ArrayList<VideoId>();

        try {
            JSONArray lEntryArr = lYouTubeResponse.getJSONObject("feed").getJSONArray("entry");
            for (int k = 0; k < lEntryArr.length(); k++) {
                //JSONArray lLinkArr = lEntryArr.getJSONObject(lEntryArr.length() - 1).getJSONArray("link");
                JSONArray lLinkArr = lEntryArr.getJSONObject(k).getJSONArray("link");
                JSONObject title = (JSONObject) lEntryArr.getJSONObject(k).get("title");
                int plIdx = 0;
                for (int i = 0; i < lLinkArr.length(); i++) {
                    JSONObject lLinkObj = lLinkArr.getJSONObject(i);
                    ;
                    String lRelVal = lLinkObj.optString("rel", null);
                    if (lRelVal != null && lRelVal.equals("alternate")) {

                        String lUriStr = lLinkObj.optString("href", null);
                        Uri lVideoUri = Uri.parse(lUriStr);
                        lVideoId = lVideoUri.getQueryParameter("v");
                        //break;
                        //videoList.add(lVideoId.replaceAll("[", "").replaceAll("]", ""));
                        VideoId vd = new VideoId(lVideoId);
                        vd.title = title.getString("$t");
                        vd.plIdx = vlist.size();
                        vlist.add(vd);
                    }
                }
            }
        } catch (JSONException e) {
            Log.i(YouTubeUtility.class.getSimpleName(), "Error retrieving content from YouTube", e);
        }
        return vlist;
    }

    public static String getPlaylistDescription(JSONObject lYouTubeResponse) {
        String lVideoId = null;
        String desc = null;
        List<VideoId> vlist = new ArrayList<VideoId>();

        try {
            JSONArray lEntryArr = lYouTubeResponse.getJSONObject("feed").getJSONArray("entry");
            for (int k = 0; k < lEntryArr.length(); k++) {
                //JSONArray lLinkArr = lEntryArr.getJSONObject(lEntryArr.length() - 1).getJSONArray("link");
                JSONArray lLinkArr = lEntryArr.getJSONObject(k).getJSONArray("link");
                JSONObject title = (JSONObject) lEntryArr.getJSONObject(k).get("title");
                int plIdx = 0;
                for (int i = 0; i < lLinkArr.length(); i++) {
                    JSONObject lLinkObj = lLinkArr.getJSONObject(i);
                    ;
                    String lRelVal = lLinkObj.optString("rel", null);
                    if (lRelVal != null && lRelVal.equals("alternate")) {

                        String lUriStr = lLinkObj.optString("href", null);
                        Uri lVideoUri = Uri.parse(lUriStr);
                        lVideoId = lVideoUri.getQueryParameter("v");
                        //break;
                        //videoList.add(lVideoId.replaceAll("[", "").replaceAll("]", ""));
                        VideoId vd = new VideoId(lVideoId);
                        vd.title = title.getString("$t");
                        vd.plIdx = vlist.size();
                        vlist.add(vd);
                    }
                }
            }
        } catch (JSONException e) {
            Log.i(YouTubeUtility.class.getSimpleName(), "Error retrieving content from YouTube", e);
        }
        return desc;
    }


    public static List<PlaylistId> getPlaylistList(JSONObject lYouTubeResponse) {
        String lVideoId = null;
        List<PlaylistId> vlist = new ArrayList<PlaylistId>();

        try {
            JSONArray lEntryArr = lYouTubeResponse.getJSONObject("feed").getJSONArray("entry");
            for (int k = 0; k < lEntryArr.length(); k++) {
                //JSONArray lLinkArr = lEntryArr.getJSONObject(lEntryArr.length() - 1).getJSONArray("link");
                //JSONArray lLinkArr = lEntryArr.getJSONObject(k).getJSONArray("link");
                JSONObject title = (JSONObject) lEntryArr.getJSONObject(k).get("title");
                JSONObject playlistId = (JSONObject) lEntryArr.getJSONObject(k).get("yt$playlistId");
                PlaylistId plid = new PlaylistId(playlistId.getString("$t"));
                plid.setTitle(title.getString("$t"));
                vlist.add(plid);
            }
        } catch (JSONException e) {
            Log.i(YouTubeUtility.class.getSimpleName(), "Error retrieving content from YouTube", e);
        }
        return vlist;
    }


    public static List<UserId> getSubscriptionList(JSONObject lYouTubeResponse) {
        String lVideoId = null;
        List<UserId> vlist = new ArrayList<UserId>();

        try {
            JSONArray lEntryArr = lYouTubeResponse.getJSONObject("feed").getJSONArray("entry");
            for (int k = 0; k < lEntryArr.length(); k++) {
                //JSONArray lLinkArr = lEntryArr.getJSONObject(lEntryArr.length() - 1).getJSONArray("link");
                //JSONArray lLinkArr = lEntryArr.getJSONObject(k).getJSONArray("link");
                //JSONObject title = (JSONObject) lEntryArr.getJSONObject(k).get("title");
                JSONObject userId = (JSONObject) lEntryArr.getJSONObject(k).get("yt$username");
                UserId plid = new UserId(userId.getString("$t"));
                plid.setTitle(userId.getString("display"));
                vlist.add(plid);
            }
        } catch (JSONException e) {
            Log.i(YouTubeUtility.class.getSimpleName(), "Error retrieving content from YouTube", e);
        }
        return vlist;
    }

    public static Uri getNextLink(JSONObject lYouTubeResponse) {

        try {
            JSONArray lEntryArr = lYouTubeResponse.getJSONObject("feed").getJSONArray("link");
            for (int k = 0; k < lEntryArr.length(); k++) {
                //JSONArray lLinkArr = lEntryArr.getJSONObject(lEntryArr.length() - 1).getJSONArray("link");

                JSONObject lLinkObj = lEntryArr.getJSONObject(k);
                ;
                String lRelVal = lLinkObj.optString("rel", null);
                if (lRelVal != null && lRelVal.equals("next")) {

                    String lUriStr = lLinkObj.optString("href", null);
                    Uri lVideoUri = Uri.parse(lUriStr);
                    return lVideoUri;
                }

            }
        } catch (JSONException e) {
            Log.i(YouTubeUtility.class.getSimpleName(), "Error retrieving next link from YouTube data", e);
        }
        return null;
    }

    public static JSONObject queryUserUploadsData(String username, int maxResults, int startIndex) {

        String lInfoStr = null;
        JSONObject lYouTubeResponse = null;

        try {
            lInfoStr = httpQuery(YouTubePlayerActivity.YOUTUBE_USER_ATOM_FEED_URL +
                    username + "/uploads?v=2&start-index=" + startIndex + "&max-results=" + maxResults + "&alt=json");
            lYouTubeResponse = new JSONObject(lInfoStr);
            return lYouTubeResponse;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject queryUserPlaylistsData(String username, int maxResults, int startIndex) {

        String lInfoStr = null;
        JSONObject lYouTubeResponse = null;

        try {
            lInfoStr = httpQuery(YouTubePlayerActivity.YOUTUBE_USER_ATOM_FEED_URL +
                    username + "/playlists?v=2&start-index=" + startIndex + "&max-results=" + maxResults + "&alt=json");
            lYouTubeResponse = new JSONObject(lInfoStr);
            return lYouTubeResponse;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject queryUserSubscriptionsData(String username, int maxResults, int startIndex) {

        String lInfoStr = null;
        JSONObject lYouTubeResponse = null;

        try {
            lInfoStr = httpQuery(YouTubePlayerActivity.YOUTUBE_USER_ATOM_FEED_URL +
                    username + "/subscriptions?v=2&start-index=" + startIndex + "&max-results=" + maxResults + "&alt=json");
            lYouTubeResponse = new JSONObject(lInfoStr);
            return lYouTubeResponse;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calculate the YouTube URL to load the video.  Includes retrieving a token that YouTube
     * requires to play the video.
     *
     * @param pYouTubeFmtQuality quality of the video.  17=low, 18=high
     * @param pFallback          whether to fallback to lower quality in case the supplied quality is not available
     * @param pYouTubeVideoId    the id of the video
     * @return the url string that will retrieve the video
     * @throws IOException
     * @throws ClientProtocolException
     * @throws UnsupportedEncodingException
     */
    public static String calculateYouTubeUrl(String pYouTubeFmtQuality, boolean pFallback,
                                             String pYouTubeVideoId) throws IOException,
            ClientProtocolException, UnsupportedEncodingException {

        String lUriStr = null;
        HttpClient lClient = new DefaultHttpClient();

        HttpGet lGetMethod = new HttpGet(YouTubePlayerActivity.YOUTUBE_VIDEO_INFORMATION_URL +
                pYouTubeVideoId);

        HttpResponse lResp = null;

        lResp = lClient.execute(lGetMethod);

        ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
        String lInfoStr = null;

        lResp.getEntity().writeTo(lBOS);
        lInfoStr = new String(lBOS.toString("UTF-8"));

        String[] lArgs = lInfoStr.split("&");
        Map<String, String> lArgMap = new HashMap<String, String>();
        for (int i = 0; i < lArgs.length; i++) {
            String[] lArgValStrArr = lArgs[i].split("=");
            if (lArgValStrArr != null) {
                if (lArgValStrArr.length >= 2) {
                    lArgMap.put(lArgValStrArr[0], URLDecoder.decode(lArgValStrArr[1]));
                }
            }
        }

        //Find out the URI string from the parameters

        //Populate the list of formats for the video
        String lFmtList = URLDecoder.decode(lArgMap.get("fmt_list"));
        ArrayList<Format> lFormats = new ArrayList<Format>();
        if (null != lFmtList) {
            String lFormatStrs[] = lFmtList.split(",");

            for (String lFormatStr : lFormatStrs) {
                Format lFormat = new Format(lFormatStr);
                lFormats.add(lFormat);
            }
        }

        //Populate the list of streams for the video
        String lStreamList = lArgMap.get("url_encoded_fmt_stream_map");
        if (null != lStreamList) {
            String lStreamStrs[] = lStreamList.split(",");
            ArrayList<VideoStream> lStreams = new ArrayList<VideoStream>();
            for (String lStreamStr : lStreamStrs) {
                VideoStream lStream = new VideoStream(lStreamStr);
                lStreams.add(lStream);
            }

            //Search for the given format in the list of video formats
            // if it is there, select the corresponding stream
            // otherwise if fallback is requested, check for next lower format
            int lFormatId = Integer.parseInt(pYouTubeFmtQuality);

            Format lSearchFormat = new Format(lFormatId);
            while (!lFormats.contains(lSearchFormat) && pFallback) {
                int lOldId = lSearchFormat.getId();
                int lNewId = getSupportedFallbackId(lOldId);

                if (lOldId == lNewId) {
                    break;
                }
                lSearchFormat = new Format(lNewId);
            }

            int lIndex = lFormats.indexOf(lSearchFormat);
            if (lIndex >= 0) {
                VideoStream lSearchStream = lStreams.get(lIndex);
                lUriStr = lSearchStream.getUrl();
            }

        }
        //Return the URI string. It may be null if the format (or a fallback format if enabled)
        // is not found in the list of formats for the video
        return lUriStr;
    }

    public static boolean hasVideoBeenViewed(Context pCtxt, String pVideoId) {
        SharedPreferences lPrefs = PreferenceManager.getDefaultSharedPreferences(pCtxt);

        String lViewedVideoIds = lPrefs.getString("com.keyes.screebl.lastViewedVideoIds", null);

        if (lViewedVideoIds == null) {
            return false;
        }

        String[] lSplitIds = lViewedVideoIds.split(";");
        if (lSplitIds == null || lSplitIds.length == 0) {
            return false;
        }

        for (int i = 0; i < lSplitIds.length; i++) {
            if (lSplitIds[i] != null && lSplitIds[i].equals(pVideoId)) {
                return true;
            }
        }

        return false;

    }

    public static void markVideoAsViewed(Context pCtxt, String pVideoId) {

        SharedPreferences lPrefs = PreferenceManager.getDefaultSharedPreferences(pCtxt);

        if (pVideoId == null) {
            return;
        }

        String lViewedVideoIds = lPrefs.getString("com.keyes.screebl.lastViewedVideoIds", null);

        if (lViewedVideoIds == null) {
            lViewedVideoIds = "";
        }

        String[] lSplitIds = lViewedVideoIds.split(";");
        if (lSplitIds == null) {
            lSplitIds = new String[]{};
        }

        // make a hash table of the ids to deal with duplicates
        Map<String, String> lMap = new HashMap<String, String>();
        for (int i = 0; i < lSplitIds.length; i++) {
            lMap.put(lSplitIds[i], lSplitIds[i]);
        }

        // recreate the viewed list
        String lNewIdList = "";
        Set<String> lKeys = lMap.keySet();
        Iterator<String> lIter = lKeys.iterator();
        while (lIter.hasNext()) {
            String lId = lIter.next();
            if (!lId.trim().equals("")) {
                lNewIdList += lId + ";";
            }
        }

        // add the new video id
        lNewIdList += pVideoId + ";";

        Editor lPrefEdit = lPrefs.edit();
        lPrefEdit.putString("com.keyes.screebl.lastViewedVideoIds", lNewIdList);
        lPrefEdit.commit();

    }

    public static int getSupportedFallbackId(int pOldId) {
        final int lSupportedFormatIds[] = {
                13,  //3GPP (MPEG-4 encoded) Low quality
                17,  //3GPP (MPEG-4 encoded) Medium quality
                18,  //MP4  (H.264 encoded) Normal quality
                22,  //MP4  (H.264 encoded) High quality
                37   //MP4  (H.264 encoded) High quality
        };
        int lFallbackId = pOldId;
        for (int i = lSupportedFormatIds.length - 1; i >= 0; i--) {
            if (pOldId == lSupportedFormatIds[i] && i > 0) {
                lFallbackId = lSupportedFormatIds[i - 1];
            }
        }
        return lFallbackId;
    }

    public static void setCurrentVideo(VideoId plID) {
        Iterator<VideoId> it = getCurrentPlaylist().vList.iterator();
        int count = 0;
        while (it.hasNext()) {
            //if (it.next().equals(plID)) getCurrentPlaylist().setCurrentVideo(plID.plIdx);
            if (it.next().mId.equals(plID.mId)) {
                getCurrentPlaylist().setCurrentVideo(count);
            }
            count++;
        }


    }

    public static void toggleQuality(Context ctx) {
        if (YouTubeUtility.lYouTubeFmtQuality.equals("17"))
            YouTubeUtility.lYouTubeFmtQuality = "18";
        else if (YouTubeUtility.lYouTubeFmtQuality.equals("18"))
            YouTubeUtility.lYouTubeFmtQuality = "22";
        else if (YouTubeUtility.lYouTubeFmtQuality.equals("22"))
            YouTubeUtility.lYouTubeFmtQuality = "37";
        else if (YouTubeUtility.lYouTubeFmtQuality.equals("37"))
            YouTubeUtility.lYouTubeFmtQuality = "17";
        Toast.makeText(ctx, "Quality changed to: " + YouTubeUtility.lYouTubeFmtQuality, Toast.LENGTH_SHORT).show();
    }

    public static void togglePlayer(Context ctx) {
        if (YouTubeUtility.currentPlayer.equals("default"))
            YouTubeUtility.currentPlayer = "mxplayer";
        else if (YouTubeUtility.currentPlayer.equals("mxplayer"))
            YouTubeUtility.currentPlayer = "default";
        Toast.makeText(ctx, "Video Player changed to: " + YouTubeUtility.currentPlayer, Toast.LENGTH_SHORT).show();
    }
}
