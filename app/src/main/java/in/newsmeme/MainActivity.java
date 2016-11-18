package in.newsmeme;

import android.Manifest;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.ScaleInOutTransformer;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mxn.soul.flowingdrawer_core.FlowingView;
import com.mxn.soul.flowingdrawer_core.LeftDrawerLayout;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.thefinestartist.ytpa.YouTubePlayerActivity;
import com.thefinestartist.ytpa.enums.Orientation;
import com.thefinestartist.ytpa.utils.YouTubeUrlParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import in.newsmeme.ContentProvider.News;
import in.newsmeme.ContentProvider.NewsTitleContentProvider;
import in.newsmeme.ContentProvider.NewsTitleDBHandler;
import in.newsmeme.Widget.ListProvider;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String TAG = getClass().getSimpleName();
    LeftDrawerLayout leftDrawerLayout = null;
    ViewPager pager;
    int position;
    String title, desc, link, lang, id, narratorName;
    int narratorImage;
    static ImageButton navButton;
    Boolean isOutSideClicked = true;
    FlowingView mFlowingView;
    private Vibrator myVib;
    WebView videoView;
    ArrayList<String> Titles = new ArrayList<>();
    ArrayList<String> Descriptions = new ArrayList<>();
    ArrayList<String> VideoIDs = new ArrayList<>();
    ArrayList<String> Links = new ArrayList<>();
    ArrayList<String> IDsForYTsdk = new ArrayList<>();
    ArrayList<String> Narrator = new ArrayList<>();
    ArrayList<String> ShareUrl = new ArrayList<>();
    ArrayList<String> ShortShareUrl = new ArrayList<>();
    List<String> emails = new ArrayList<String>();
    final ParseQuery<ParseObject> query = ParseQuery.getQuery(getString(R.string.newsmeme));
    ProgressDialog progressDialog = null;
    //SocialNetworkManager socialNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NewsTitleDBHandler newsTitleDBHandler = new NewsTitleDBHandler(this);

        Intent intent = getIntent();
        Titles = intent.getStringArrayListExtra(getString(R.string.title_intent_extra));
        Descriptions = intent.getStringArrayListExtra(getString(R.string.descs_intent_extra));
        VideoIDs = intent.getStringArrayListExtra(getString(R.string.videoID_intent_extra));
        Links = intent.getStringArrayListExtra(getString(R.string.links_intent_extra));
        ShareUrl = intent.getStringArrayListExtra(getString(R.string.shareUrl_intent_extra));
        ShortShareUrl = intent.getStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra));
        videoView = (WebView) findViewById(R.id.videoView);
        IDsForYTsdk = intent.getStringArrayListExtra(getString(R.string.idForSdk_intent_extra));

        if (Titles!=null){
            for (int i = 0; i < Titles.size(); i++) {
                newsTitleDBHandler.addNews(new News(i, Titles.get(i)));
            }
        }

        Narrator = intent.getStringArrayListExtra(getString(R.string.narrator_intent_extra));

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        pager = (ViewPager) findViewById(R.id.viewPager);
        navButton = (ImageButton) findViewById(R.id.nav_icon);
        mFlowingView = (FlowingView) findViewById(R.id.sv);
        pager.setPageMargin(5);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pager.setPageTransformer(true, new ScaleInOutTransformer());        //Rotate, Scale, Flip

        setNavDrawer();
        getLoaderManager().initLoader(0, null, this);

        // FCM
        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.news));
        Log.e(TAG, getString(R.string.subscribed_news));
        Log.e(TAG, getString(R.string.instance_token) + FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //Toast.makeText(this, "Please provide permissions", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        String[] projection = { NewsTitleDBHandler.KEY_ID, NewsTitleDBHandler.KEY_TITLE};
        CursorLoader cursorLoader = new CursorLoader(this,
                NewsTitleContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor.moveToFirst()){
            do{
                String data = cursor.getString(cursor.getColumnIndex(NewsTitleDBHandler.KEY_TITLE));
                ListProvider.populateListItem(data);
            } while(cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            position = pos;
            if (position > 0) {
                id = IDsForYTsdk.get(position - 1);
            }//position problems
            else {
                id = IDsForYTsdk.get(0);
            }
            narratorName = Narrator.get(position);
            switch (narratorName) {
                case "Sumaiya":
                    narratorImage = R.drawable.narrator_sumaiya_image;
                    break;
                case "Surbhi":
                    narratorImage = R.drawable.narrator_surbhi_image;
                    break;
                case "Dipika":
                    narratorImage = R.drawable.narrator_dipika_image;
                    break;
                case "Bot":
                    narratorImage = R.drawable.narrator_bot_image;
                    break;
                case "Garima":
                    narratorImage = R.drawable.narrator_garima_image;
                    break;
                case "Kritika":
                    narratorImage = R.drawable.narrator_kritika_image;
                    break;
                case "Harsimran":
                    narratorImage = R.drawable.narrator_harsimran_image;
                    break;
                case "Simer":
                    narratorImage = R.drawable.narrator_simer_image;
                    break;
                case "Sayli":
                    narratorImage = R.drawable.narrator_sayli_image;
                    break;
                case "Anand":
                    narratorImage = R.drawable.narrator_anand_image;
                    break;
                case "Simran":
                    narratorImage = R.drawable.narrator_simran_image;
                    break;
                default:
                    narratorImage = R.drawable.drawericon;
                    break;
            }
            title = Titles.get(position);
            desc = Descriptions.get(position);
            link = VideoIDs.get(position);
            return VideoFragment.newInstance(title, desc, link, narratorName, narratorImage);
        }

        @Override
        public int getCount() {
            return 30;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDrawer(View view) {
        if (leftDrawerLayout.isShownMenu()) {
            leftDrawerLayout.closeDrawer();
        } else {
            leftDrawerLayout.openDrawer();
        }
    }

    private void setNavDrawer() {
        leftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);
        FragmentManager fm = getSupportFragmentManager();
        MyMenuFragment mMenuFragment = (MyMenuFragment) fm.findFragmentById(R.id.id_container_menu);
        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new MyMenuFragment()).commit();
        }
        leftDrawerLayout.setFluidView(mFlowingView);
        leftDrawerLayout.setMenuFragment(mMenuFragment);
    }

    public void fabClick(View view) {
        try {
            myVib.vibrate(50);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.check_out) + ShareUrl.get(position));
            intent.setType(getString(R.string.text_intent_type));
            Intent i = Intent.createChooser(intent, getString(R.string.send_via));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshContent(View view) {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.refresh_dialogue));
            progressDialog.show();

            try {
                FileInputStream fis = openFileInput(getResources().getString(R.string.FILENAME));
                int c;
                lang = "";
                while ((c = fis.read()) != -1) {
                    lang = lang + Character.toString((char) c);
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //query.fromLocalDatastore();
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
                            ShareUrl.add(i, idList.get(i).get(getString(R.string.share_url)).toString());
                            ShortShareUrl.add(i, idList.get(i).get(getString(R.string.short_share_url)).toString());
                            VideoIDs.add(i, getString(R.string.youtube_embed_base_url) + idList.get(i).get(getString(R.string.youtube_id)).toString() + getString(R.string.youtube_player_url_extension));
                            IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                            if (idList.get(i).get(getString(R.string.narrator_nam)) != null)
                                Narrator.add(i, idList.get(i).get(getString(R.string.narrator_nam)).toString());
                            else
                                Narrator.add(i, getString(R.string.bot));
                        }

                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.putStringArrayListExtra(getString(R.string.title_intent_extra), Titles);
                        intent.putStringArrayListExtra(getString(R.string.descs_intent_extra), Descriptions);
                        intent.putStringArrayListExtra(getString(R.string.videoID_intent_extra), VideoIDs);
                        intent.putStringArrayListExtra(getString(R.string.shareUrl_intent_extra), ShareUrl);
                        intent.putStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra), ShortShareUrl);
                        intent.putStringArrayListExtra(getString(R.string.idForSdk_intent_extra), IDsForYTsdk);
                        intent.putStringArrayListExtra(getString(R.string.narrator_intent_extra), Narrator);
                        ParseObject.unpinAllInBackground(getString(R.string.objectsID_parse), idList, new DeleteCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    return;
                                }
                                ParseObject.pinAllInBackground(idList);
                            }
                        });
                        progressDialog.dismiss();
                        startActivity(intent);
                        finish();
                    } else {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), R.string.fetch_error, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        } else {
            Toast.makeText(this, R.string.network_suggestion, Toast.LENGTH_SHORT).show();
        }
    }

    public void toFirstFragment(View view) {
        pager.setCurrentItem(0);
    }

    public void shareFacebook(View view) {
        try {
            myVib.vibrate(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initShareIntent(getString(R.string.facebook_app_share_kat));
    }

    public void shareFacebookMessenger(View view) {
        try {
            myVib.vibrate(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initShareIntent(getString(R.string.facebook_package_orc));
    }

    public void shareTwitter(View view) {
        try {
            myVib.vibrate(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initShareIntent(getString(R.string.twitter_package_string));
    }

    private boolean checkPermission() {
        String permission = getString(R.string.external_storage_permission);
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        if (res != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void shareWhatsapp(View view) {
        if (checkPermission()) {
            try {
                myVib.vibrate(50);
                Bitmap bm = VideoFragment.getScreenShot();
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root);
                if (!myDir.exists()) {
                    Boolean made = myDir.mkdirs();
                    //Toast.makeText(this, "Directory made:"+made, Toast.LENGTH_SHORT).show();
                }
                String fname = getString(R.string.screenMeme) + position;
                File file = new File(myDir, fname);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.fromFile(file);
                Intent intent = new Intent();
                intent.setPackage(getString(R.string.whatsapp_package_name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType(getString(R.string.image_intent_type));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.whatsapp_share_text));
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, R.string.app_not_installed, Toast.LENGTH_SHORT).show();
            }
        } else {
            myVib.vibrate(50);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getString(R.string.text_intent_type));
            try {
                intent.setPackage(getString(R.string.whatsapp_package_name));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, R.string.app_not_installed, Toast.LENGTH_SHORT).show();
            }
            //TODO: APP CAN CRASH HERE
            if (position > 0) {
                try {
                    intent.putExtra(Intent.EXTRA_TEXT, Titles.get(position - 1) + getString(R.string.colon_space) + Links.get(position - 1));       //position problems
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    intent.putExtra(Intent.EXTRA_TEXT, Titles.get(0) + getString(R.string.colon_space) + Links.get(0));       //position problems
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(this, R.string.storage_permission, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
    }

    private void initShareIntent(String type) {
        try {
            boolean found = false;
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(getString(R.string.intent_text_plain_type));
            // gets the list of intents that can be loaded.
            List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
            if (!resInfo.isEmpty()) {
                for (ResolveInfo info : resInfo) {
                    if (info.activityInfo.packageName.toLowerCase().contains(type) || info.activityInfo.name.toLowerCase().contains(type)) {
                        if (position > 0) {
                            if (type.equals(getString(R.string.twitter_package_string))) {
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(position - 1) + getString(R.string.colon_space) + ShortShareUrl.get(position - 1) + getString(R.string.newsmeme_dot_in));
                                //Toast.makeText(this, "Hello "+Titles.get(position - 1) , Toast.LENGTH_SHORT).show();
                            } else
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(position - 1) + getString(R.string.colon_space) + ShareUrl.get(position - 1));         //position problems
                        } else {
                            if (type.equals(getString(R.string.twitter_package_string)))
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(0) + getString(R.string.colon_space) + ShortShareUrl.get(0) + getString(R.string.newsmeme_dot_in));
                            else
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(0) + getString(R.string.colon_space) + ShareUrl.get(0));
                        }
                        share.setPackage(info.activityInfo.packageName);
                        found = true;
                        break;
                    }
                }
                if (!found)
                    Toast.makeText(MainActivity.this, R.string.app_not_installed, Toast.LENGTH_SHORT).show();
                startActivity(share);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playVideoInFullScreen(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Intent intent = new Intent(MainActivity.this, YouTubePlayerActivity.class);
            intent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, id);
            intent.putExtra(YouTubePlayerActivity.EXTRA_PLAYER_STYLE, YouTubePlayer.PlayerStyle.DEFAULT);
            intent.putExtra(YouTubePlayerActivity.EXTRA_ORIENTATION, Orientation.ONLY_LANDSCAPE);
            intent.putExtra(YouTubePlayerActivity.EXTRA_SHOW_AUDIO_UI, true);
            intent.putExtra(YouTubePlayerActivity.EXTRA_HANDLE_ERROR, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.network_suggestion, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Rect rect = null, toolbarViewRect = null;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (leftDrawerLayout.isShownMenu()) {

                View content = findViewById(R.id.id_drawerlayout);
                int[] contentLocation = new int[2];
                if (content != null) {
                    content.getLocationOnScreen(contentLocation);
                    rect = new Rect(contentLocation[0],
                            contentLocation[1],
                            contentLocation[0] + content.getWidth(),
                            contentLocation[1] + content.getHeight());
                }

                View toolbarView = findViewById(R.id.toolbar);
                int[] toolbarLocation = new int[2];
                if (toolbarView != null) {
                    toolbarView.getLocationOnScreen(toolbarLocation);
                    toolbarViewRect = new Rect(toolbarLocation[0],
                            toolbarLocation[1],
                            toolbarLocation[0] + toolbarView.getWidth(),
                            toolbarLocation[1] + toolbarView.getHeight());
                }

                try {
                    if (!(rect.contains((int) event.getX(), (int) event.getY())) && !toolbarViewRect.contains((int) event.getX(), (int) event.getY())) {
                        isOutSideClicked = true;
                    } else {
                        isOutSideClicked = false;
                    }
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }

            } else {
                return super.dispatchTouchEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && isOutSideClicked) {
            isOutSideClicked = false;
            return super.dispatchTouchEvent(event);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && isOutSideClicked) {
            return super.dispatchTouchEvent(event);
        }

        if (isOutSideClicked) {
            //make http call/db request
            //Toast.makeText(this, "Hello..", Toast.LENGTH_SHORT).show();

        }
        return super.dispatchTouchEvent(event);
    }

    public void changeToEnglish(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                FileOutputStream fos = openFileOutput(getResources().getString(R.string.FILENAME), Context.MODE_PRIVATE);
                fos.write(getResources().getString(R.string.ENGLISH).getBytes());
                fos.close();
            } catch (Exception fne) {
                fne.printStackTrace();
            }
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.update_pref_dialogue));
            progressDialog.show();
            try {
                FileInputStream fis = openFileInput(getResources().getString(R.string.FILENAME));
                int c;
                lang = "";
                while ((c = fis.read()) != -1) {
                    lang = lang + Character.toString((char) c);
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //query.fromLocalDatastore();
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
                            if (idList.get(i).get(getString(R.string.narrator_nam)) != null)
                                Narrator.add(i, idList.get(i).get(getString(R.string.narrator_nam)).toString());
                            else
                                Narrator.add(i, getString(R.string.bot));
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putStringArrayListExtra(getString(R.string.title_intent_extra), Titles);
                        intent.putStringArrayListExtra(getString(R.string.descs_intent_extra), Descriptions);
                        intent.putStringArrayListExtra(getString(R.string.videoID_intent_extra), VideoIDs);
                        intent.putStringArrayListExtra(getString(R.string.shareUrl_intent_extra), ShareUrl);
                        intent.putStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra), ShortShareUrl);
                        intent.putStringArrayListExtra(getString(R.string.idForSdk_intent_extra), IDsForYTsdk);
                        intent.putStringArrayListExtra(getString(R.string.narrator_intent_extra), Narrator);
                        ParseObject.unpinAllInBackground(getString(R.string.objectsID_parse), idList, new DeleteCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    return;
                                }
                                ParseObject.pinAllInBackground(idList);
                            }
                        });
                        progressDialog.dismiss();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        //overridePendingTransition(0, 0);                                    //CHANGES FOR TRANSITION
                        startActivity(intent);
                        finish();
                    } else {
                        e.printStackTrace();
                        //Toast.makeText(getApplicationContext(), "Couldn't fetch data", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.pref_connection_suggestion, Toast.LENGTH_SHORT).show();
        }
    }

    public void changeToHindi(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                FileOutputStream fos = openFileOutput(getResources().getString(R.string.FILENAME), Context.MODE_PRIVATE);
                fos.write(getResources().getString(R.string.HINDI).getBytes());
                fos.close();
            } catch (Exception fne) {
                fne.printStackTrace();
            }
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.update_pref_dialogue));
            progressDialog.show();
            try {
                FileInputStream fis = openFileInput(getResources().getString(R.string.FILENAME));
                int c;
                lang = "";
                while ((c = fis.read()) != -1) {
                    lang = lang + Character.toString((char) c);
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //query.fromLocalDatastore();
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
                            ShareUrl.add(i, idList.get(i).get(getString(R.string.share_url)).toString());
                            ShortShareUrl.add(i, idList.get(i).get(getString(R.string.short_share_url)).toString());
                            VideoIDs.add(i, getString(R.string.youtube_embed_base_url) + idList.get(i).get(getString(R.string.youtube_id)).toString() + getString(R.string.youtube_player_url_extension));
                            IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                            if (idList.get(i).get(getString(R.string.narrator_nam)) != null)
                                Narrator.add(i, idList.get(i).get(getString(R.string.narrator_nam)).toString());
                            else
                                Narrator.add(i, getString(R.string.bot));
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putStringArrayListExtra(getString(R.string.title_intent_extra), Titles);
                        intent.putStringArrayListExtra(getString(R.string.descs_intent_extra), Descriptions);
                        intent.putStringArrayListExtra(getString(R.string.videoID_intent_extra), VideoIDs);
                        intent.putStringArrayListExtra(getString(R.string.shareUrl_intent_extra), ShareUrl);
                        intent.putStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra), ShortShareUrl);
                        intent.putStringArrayListExtra(getString(R.string.idForSdk_intent_extra), IDsForYTsdk);
                        intent.putStringArrayListExtra(getString(R.string.narrator_intent_extra), Narrator);
                        ParseObject.unpinAllInBackground(getString(R.string.objectsID_parse), idList, new DeleteCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    return;
                                }
                                ParseObject.pinAllInBackground(idList);
                            }
                        });
                        progressDialog.dismiss();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    } else {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), R.string.fetch_error, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void couchView(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Intent intent = new Intent(this, CouchViewActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.network_suggestion, Toast.LENGTH_SHORT).show();
        }
    }

    public static String FACEBOOK_URL = Resources.getSystem().getString(R.string.facebook_newsmeme);
    public static String FACEBOOK_PAGE_ID = Resources.getSystem().getString(R.string.app_name);

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo(getString(R.string.facebook_package_name), 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return getString(R.string.facebook_url) + FACEBOOK_URL;
            } else { //older versions of fb app
                return getString(R.string.facebook_page_url) + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

    public void likeOnFacebook(View view) {
        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        String facebookUrl = getFacebookPageURL(this);
        facebookIntent.setData(Uri.parse(facebookUrl));
        startActivity(facebookIntent);
    }

    public void shareLinkToApp(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_link));
        intent.setType(getString(R.string.text_intent_type));
        Intent i = Intent.createChooser(intent, getResources().getString(R.string.send_via));
        startActivity(i);
    }

    public void subscribe(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.youtube_subscribe)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.youtube_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    public void feedback(View view) {
        try {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getString(R.string.intent_text_plain_type));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.emailID)});
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.emailSubject));
            intent.putExtra(Intent.EXTRA_BCC, getString(R.string.my_email));
            final PackageManager pm = getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
            ResolveInfo best = null;
            for (final ResolveInfo info : matches)
                if (info.activityInfo.packageName.endsWith(getString(R.string.gmail_package_end)) ||
                        info.activityInfo.name.toLowerCase().contains(getString(R.string.gmail))) best = info;
            if (best != null)
                intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.gmail_not_found, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}

