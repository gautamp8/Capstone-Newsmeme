package in.newsmeme.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.thefinestartist.ytpa.utils.YouTubeUrlParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import in.newsmeme.R;
import in.newsmeme.Splash;

/**
 * Created by brainbreaker on 16/11/16.
 */

public class FetchNewsService extends IntentService {

    String lang;
    static Boolean firstRun;
    final ParseQuery<ParseObject> query = ParseQuery.getQuery("newsmeme");

    ArrayList<String> Titles = new ArrayList<>();
    ArrayList<String> Descriptions = new ArrayList<>();
    ArrayList<String> VideoIDs = new ArrayList<>();
    ArrayList<String> FullUrls = new ArrayList<>();
    ArrayList<String> IDsForYTsdk = new ArrayList<>();
    ArrayList<String> Narrator = new ArrayList<>();
    ArrayList<String> ShareUrl = new ArrayList<>();
    ArrayList<String> ShortShareUrl = new ArrayList<>();
    public FetchNewsService() {
        super("FetchNewsService");
    }
    @Override
    protected void onHandleIntent(final Intent intent) {

        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        firstRun = isFirstRun;
        if (isFirstRun) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            final Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(Splash.FetchNewsServiceReceiver.PROCESS_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

            if (networkInfo != null && networkInfo.isConnected()) {
                try {
                    FileOutputStream fos = openFileOutput(getResources().getString(R.string.FILENAME), Context.MODE_PRIVATE);
                    fos.write(getResources().getString(R.string.ENGLISH).getBytes());
                    fos = openFileOutput(getResources().getString(R.string.storeEnObjFile), Context.MODE_PRIVATE);
                    fos.write(getResources().getString(R.string.objEN).getBytes());
                    fos = openFileOutput(getResources().getString(R.string.storeHiObjFile), Context.MODE_PRIVATE);
                    fos.write(getResources().getString(R.string.objHI).getBytes());
                    fos.close();
                    lang = "en";
                    query.whereExists("youtube_id");
                    query.setLimit(30);
                    query.orderByDescending("createdAt");
                    if (lang.equals("en"))
                        query.whereEqualTo("language", "en");
                    if (lang.equals("hi"))
                        query.whereEqualTo("language", "hi");
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> idList, ParseException e) {
                            if (e == null) {
                                for (int i = 0; i < idList.size(); i++) {
                                    VideoIDs.add(i, "https://www.youtube.com/embed/" + idList.get(i).get("youtube_id").toString() + "?rel=0&amp;controls=0&amp;showinfo=0&amp;autoplay=1");
                                    Titles.add(i, idList.get(i).get("video_title").toString());
                                    Descriptions.add(i, idList.get(i).get("video_script").toString());
                                    if (idList.get(i).get("share_url").toString() != null)
                                        ShareUrl.add(i, idList.get(i).get("share_url").toString());
                                    else
                                        ShareUrl.add(i, idList.get(i).get("video_title").toString() + " @newsmemedotin");
                                    if (idList.get(i).get("short_share_url").toString() != null)
                                        ShortShareUrl.add(i, idList.get(i).get("short_share_url").toString());
                                    else
                                        ShortShareUrl.add(i, idList.get(i).get("video_title").toString() + " @newsmemedotin");
                                    IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                                    FullUrls.add(i, "https://youtu.be/" + IDsForYTsdk.get(i));
                                    if (idList.get(i).get("narrator_name") != null)
                                        Narrator.add(i, idList.get(i).get("narrator_name").toString());
                                    else
                                        Narrator.add(i, "Bot");
                                }
                                broadcastIntent.putStringArrayListExtra("titles", Titles);
                                broadcastIntent.putStringArrayListExtra("descs", Descriptions);
                                broadcastIntent.putStringArrayListExtra("videoIds", VideoIDs);
                                broadcastIntent.putStringArrayListExtra("links", FullUrls);
                                broadcastIntent.putStringArrayListExtra("shareUrl", ShareUrl);
                                broadcastIntent.putStringArrayListExtra("shortShareUrl", ShortShareUrl);
                                broadcastIntent.putStringArrayListExtra("idsForSDK", IDsForYTsdk);
                                broadcastIntent.putStringArrayListExtra("narrator", Narrator);
                                //Toast.makeText(getApplicationContext(), "doInBackground, first run!", Toast.LENGTH_SHORT).show();
                                ParseObject.unpinAllInBackground("objectsID", idList, new DeleteCallback() {
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            return;
                                        }
                                        ParseObject.pinAllInBackground(idList);
                                    }
                                });
                                sendBroadcast(broadcastIntent);

                            } else {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Couldn't fetch data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (Exception fne) {
                    fne.printStackTrace();
                }
                getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                        .edit()
                        .putBoolean("isFirstRun", false)
                        .apply();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("No Network")
                        .setMessage("Please connect to a network")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        }
        else {
            try {
                FileInputStream fis = openFileInput(getResources().getString(R.string.FILENAME));
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String c;
                lang = "";
                while ((c = br.readLine()) != null) {
                    lang = lang + c;
                }
                br.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(Splash.FetchNewsServiceReceiver.PROCESS_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            //final ParseQuery<ParseObject> query = ParseQuery.getQuery("newsmeme");          //GET PARSEOBJECTS LIST FROM SERVER
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    query.fromLocalDatastore();                                             //so user doesn't have to wait
                    query.whereExists("youtube_id");
                    query.setLimit(30);
                    query.orderByDescending("createdAt");
                    if (lang.equals("en"))
                        query.whereEqualTo("language", "en");
                    else if (lang.equals("hi"))
                        query.whereEqualTo("language", "hi");
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> idList, ParseException e) {
                            if (e == null) {
                                for (int i = 0; i < idList.size(); i++) {
                                    Titles.add(i, idList.get(i).get("video_title").toString());
                                    Descriptions.add(i, idList.get(i).get("video_script").toString());
                                    VideoIDs.add(i, "https://www.youtube.com/embed/" + idList.get(i).get("youtube_id").toString() + "?rel=0&controls=0&showinfo=0&autoplay=1&modestbranding=1");
                                    IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                                    ShareUrl.add(i, idList.get(i).get("share_url").toString());
                                    ShortShareUrl.add(i, idList.get(i).get("short_share_url").toString());
                                    FullUrls.add(i, "https://youtu.be/" + IDsForYTsdk.get(i));
                                    if (idList.get(i).get("narrator_name") != null)
                                        Narrator.add(i, idList.get(i).get("narrator_name").toString());
                                    else
                                        Narrator.add(i, "Bot");
                                }
                                //Toast.makeText(getApplicationContext(), "inside onPostExecute, block1", Toast.LENGTH_SHORT).show();
                                broadcastIntent.putStringArrayListExtra("titles", Titles);
                                broadcastIntent.putStringArrayListExtra("descs", Descriptions);
                                broadcastIntent.putStringArrayListExtra("videoIds", VideoIDs);
                                broadcastIntent.putStringArrayListExtra("idsForSDK", IDsForYTsdk);
                                broadcastIntent.putStringArrayListExtra("links", FullUrls);
                                broadcastIntent.putStringArrayListExtra("shareUrl", ShareUrl);
                                broadcastIntent.putStringArrayListExtra("shortShareUrl", ShortShareUrl);
                                broadcastIntent.putStringArrayListExtra("narrator", Narrator);
                                ParseObject.unpinAllInBackground("objectsID", idList, new DeleteCallback() {
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            return;
                                        }
                                        ParseObject.pinAllInBackground(idList);
                                    }
                                });
                                sendBroadcast(broadcastIntent);
                            } else {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Couldn't fetch data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    query.fromLocalDatastore();
                    query.orderByDescending("createdAt");
                    if (lang.equals("en"))
                        query.whereEqualTo("language", "en");
                    else if (lang.equals("hi"))
                        query.whereEqualTo("language", "hi");
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> idList, ParseException e) {
                            if (e == null) {
                                for (int i = 0; i < idList.size(); i++) {
                                    Titles.add(i, idList.get(i).get("video_title").toString());
                                    Descriptions.add(i, idList.get(i).get("video_script").toString());
                                    VideoIDs.add(i, "https://www.youtube.com/embed/" + idList.get(i).get("youtube_id").toString() + "?rel=0&amp;controls=0&amp;showinfo=0&amp;autoplay=1");
                                    IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                                    FullUrls.add(i, "https://youtu.be/" + IDsForYTsdk.get(i));
                                    ShareUrl.add(i, idList.get(i).get("share_url").toString());
                                    ShortShareUrl.add(i, idList.get(i).get("short_share_url").toString());
                                    if (idList.get(i).get("narrator_name") != null)
                                        Narrator.add(i, idList.get(i).get("narrator_name").toString());
                                    else
                                        Narrator.add(i, "Bot");
                                }
                                broadcastIntent.putStringArrayListExtra("titles", Titles);
                                broadcastIntent.putStringArrayListExtra("descs", Descriptions);
                                broadcastIntent.putStringArrayListExtra("videoIds", VideoIDs);
                                broadcastIntent.putStringArrayListExtra("links", FullUrls);
                                broadcastIntent.putStringArrayListExtra("shortShareUrl", ShortShareUrl);
                                broadcastIntent.putStringArrayListExtra("idsForSDK", IDsForYTsdk);
                                broadcastIntent.putStringArrayListExtra("shareUrl", ShareUrl);
                                broadcastIntent.putStringArrayListExtra("narrator", Narrator);
                                //Toast.makeText(getApplicationContext(), "inside onPostExecute, block2", Toast.LENGTH_SHORT).show();

                                Toast.makeText(getApplicationContext(), "Fetched saved data", Toast.LENGTH_SHORT).show();
                                sendBroadcast(broadcastIntent);
                            } else {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Couldn't fetch saved data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
