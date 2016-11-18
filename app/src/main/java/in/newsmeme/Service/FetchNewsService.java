package in.newsmeme.Service;

import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
    final ParseQuery<ParseObject> query = ParseQuery.getQuery(getString(R.string.parse_query));

    ArrayList<String> Titles = new ArrayList<>();
    ArrayList<String> Descriptions = new ArrayList<>();
    ArrayList<String> VideoIDs = new ArrayList<>();
    ArrayList<String> FullUrls = new ArrayList<>();
    ArrayList<String> IDsForYTsdk = new ArrayList<>();
    ArrayList<String> Narrator = new ArrayList<>();
    ArrayList<String> ShareUrl = new ArrayList<>();
    ArrayList<String> ShortShareUrl = new ArrayList<>();
    public FetchNewsService() {
        super(Resources.getSystem().getString(R.string.fetch_news_service));
    }
    @Override
    protected void onHandleIntent(final Intent intent) {

        boolean isFirstRun = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE).getBoolean(getString(R.string.first_run_boolean), true);
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
                    lang = getResources().getString(R.string.englishLang);
                    query.whereExists(getString(R.string.youtube_id));
                    query.setLimit(30);
                    query.orderByDescending(getString(R.string.created_At));
                    if (lang.equals(getResources().getString(R.string.englishLang)))
                        query.whereEqualTo(getString(R.string.language), getResources().getString(R.string.englishLang));
                    if (lang.equals(getString(R.string.hindiLang)))
                        query.whereEqualTo(getString(R.string.language),getString(R.string.hindiLang));
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> idList, ParseException e) {
                            if (e == null) {
                                for (int i = 0; i < idList.size(); i++) {
                                    VideoIDs.add(i, getString(R.string.youtube_embed_base_url) + idList.get(i).get(getString(R.string.youtube_id)).toString() + getString(R.string.youtube_player_url_extension));
                                    Titles.add(i, idList.get(i).get(getString(R.string.video_title)).toString());
                                    Descriptions.add(i, idList.get(i).get(getString(R.string.video_script)).toString());
                                    if (idList.get(i).get(getString(R.string.share_url)).toString() != null)
                                        ShareUrl.add(i, idList.get(i).get(getString(R.string.share_url)).toString());
                                    else
                                        ShareUrl.add(i, idList.get(i).get(getString(R.string.video_title)).toString() + getString(R.string.newsmeme_dot_in));
                                    if (idList.get(i).get(getString(R.string.short_share_url)).toString() != null)
                                        ShortShareUrl.add(i, idList.get(i).get(getString(R.string.short_share_url)).toString());
                                    else
                                        ShortShareUrl.add(i, idList.get(i).get(getString(R.string.video_title)).toString() + getString(R.string.newsmeme_dot_in));
                                    IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                                    FullUrls.add(i, getString(R.string.youtube_short_url) + IDsForYTsdk.get(i));
                                    if (idList.get(i).get(getString(R.string.narrator_nam)) != null)
                                        Narrator.add(i, idList.get(i).get(getString(R.string.narrator_nam)).toString());
                                    else
                                        Narrator.add(i, getString(R.string.bot));
                                }
                                broadcastIntent.putStringArrayListExtra(getString(R.string.title_intent_extra), Titles);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.descs_intent_extra), Descriptions);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.videoID_intent_extra), VideoIDs);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.links_intent_extra), FullUrls);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.shareUrl_intent_extra), ShareUrl);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra), ShortShareUrl);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.idForSdk_intent_extra), IDsForYTsdk);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.narrator_intent_extra), Narrator);
                                //Toast.makeText(getApplicationContext(), "doInBackground, first run!", Toast.LENGTH_SHORT).show();
                                ParseObject.unpinAllInBackground(getString(R.string.objectsID_parse), idList, new DeleteCallback() {
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
                                Toast.makeText(getApplicationContext(), getString(R.string.fetch_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (Exception fne) {
                    fne.printStackTrace();
                }
                getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
                        .edit()
                        .putBoolean(getString(R.string.first_run_boolean), false)
                        .apply();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.no_network))
                        .setMessage(getString(R.string.network_suggestion))
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
                    query.whereExists(getString(R.string.youtube_id));
                    query.setLimit(30);
                    query.orderByDescending(getString(R.string.created_At));
                    if (lang.equals(getString(R.string.englishLang)))
                        query.whereEqualTo(getString(R.string.language), getString(R.string.englishLang));
                    else if (lang.equals(getString(R.string.hindiLang)))
                        query.whereEqualTo(getString(R.string.language), getString(R.string.hindiLang));
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> idList, ParseException e) {
                            if (e == null) {
                                for (int i = 0; i < idList.size(); i++) {
                                    Titles.add(i, idList.get(i).get(getString(R.string.video_title)).toString());
                                    Descriptions.add(i, idList.get(i).get(getString(R.string.video_script)).toString());
                                    VideoIDs.add(i, getString(R.string.youtube_embed_base_url) + idList.get(i).get(getString(R.string.youtube_id)).toString() + getString(R.string.youtube_player_url_extension));
                                    IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                                    ShareUrl.add(i, idList.get(i).get(getString(R.string.share_url)).toString());
                                    ShortShareUrl.add(i, idList.get(i).get(getString(R.string.short_share_url)).toString());
                                    FullUrls.add(i, getString(R.string.youtube_short_url) + IDsForYTsdk.get(i));
                                    if (idList.get(i).get(getString(R.string.narrator_nam)) != null)
                                        Narrator.add(i, idList.get(i).get(getString(R.string.narrator_nam)).toString());
                                    else
                                        Narrator.add(i, getString(R.string.bot));
                                }
                                //Toast.makeText(getApplicationContext(), "inside onPostExecute, block1", Toast.LENGTH_SHORT).show();
                                broadcastIntent.putStringArrayListExtra(getString(R.string.title_intent_extra), Titles);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.descs_intent_extra), Descriptions);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.videoID_intent_extra), VideoIDs);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.idForSdk_intent_extra), IDsForYTsdk);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.links_intent_extra), FullUrls);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.shareUrl_intent_extra), ShareUrl);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra), ShortShareUrl);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.narrator_intent_extra), Narrator);
                                ParseObject.unpinAllInBackground(getString(R.string.objectsID_parse), idList, new DeleteCallback() {
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
                                Toast.makeText(getApplicationContext(), R.string.fetch_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    query.fromLocalDatastore();
                    query.orderByDescending(getString(R.string.created_At));
                    if (lang.equals(getString(R.string.englishLang)))
                        query.whereEqualTo(getString(R.string.language), getString(R.string.englishLang));
                    else if (lang.equals(getString(R.string.hindiLang)))
                        query.whereEqualTo(getString(R.string.language), getString(R.string.hindiLang));
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(final List<ParseObject> idList, ParseException e) {
                            if (e == null) {
                                for (int i = 0; i < idList.size(); i++) {
                                    Titles.add(i, idList.get(i).get(getString(R.string.video_title)).toString());
                                    Descriptions.add(i, idList.get(i).get(getString(R.string.video_script)).toString());
                                    VideoIDs.add(i, getString(R.string.youtube_embed_base_url) + idList.get(i).get(getString(R.string.youtube_id)).toString() + getString(R.string.youtube_player_url_extension));
                                    IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                                    FullUrls.add(i, getString(R.string.youtube_short_url) + IDsForYTsdk.get(i));
                                    ShareUrl.add(i, idList.get(i).get(getString(R.string.share_url)).toString());
                                    ShortShareUrl.add(i, idList.get(i).get(getString(R.string.short_share_url)).toString());
                                    if (idList.get(i).get(getString(R.string.narrator_nam)) != null)
                                        Narrator.add(i, idList.get(i).get(getString(R.string.narrator_nam)).toString());
                                    else
                                        Narrator.add(i, getString(R.string.bot));
                                }
                                broadcastIntent.putStringArrayListExtra(getString(R.string.title_intent_extra), Titles);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.descs_intent_extra), Descriptions);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.videoID_intent_extra), VideoIDs);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.links_intent_extra), FullUrls);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.short_share_url), ShortShareUrl);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.idForSdk_intent_extra), IDsForYTsdk);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.shareUrl_intent_extra), ShareUrl);
                                broadcastIntent.putStringArrayListExtra(getString(R.string.narrator_intent_extra), Narrator);
                                //Toast.makeText(getApplicationContext(), "inside onPostExecute, block2", Toast.LENGTH_SHORT).show();

                                Toast.makeText(getApplicationContext(), R.string.fetched_data_offline, Toast.LENGTH_SHORT).show();
                                sendBroadcast(broadcastIntent);
                            } else {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), R.string.fetch_error_offline, Toast.LENGTH_SHORT).show();
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
