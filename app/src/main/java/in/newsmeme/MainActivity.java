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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MainActivity";
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
    final ParseQuery<ParseObject> query = ParseQuery.getQuery("newsmeme");
    ProgressDialog progressDialog = null;
    //SocialNetworkManager socialNetworkManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        Titles = intent.getStringArrayListExtra("titles");
        Descriptions = intent.getStringArrayListExtra("descs");
        VideoIDs = intent.getStringArrayListExtra("videoIds");
        Links = intent.getStringArrayListExtra("links");
        ShareUrl = intent.getStringArrayListExtra("shareUrl");
        ShortShareUrl = intent.getStringArrayListExtra("shortShareUrl");
        videoView = (WebView) findViewById(R.id.videoView);
        IDsForYTsdk = intent.getStringArrayListExtra("idsForSDK");

        Narrator = intent.getStringArrayListExtra("narrator");

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        pager = (ViewPager) findViewById(R.id.viewPager);
        navButton = (ImageButton) findViewById(R.id.nav_icon);
        mFlowingView = (FlowingView) findViewById(R.id.sv);
        pager.setPageMargin(5);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pager.setPageTransformer(true, new ScaleInOutTransformer());        //Rotate, Scale, Flip

        setNavDrawer();

        // FCM
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        Log.e(TAG, "Subscribed to news topic");
        Log.e(TAG, "InstanceID token: " + FirebaseInstanceId.getInstance().getToken());


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            getLoaderManager().initLoader(0, null, this);
        else{
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLoaderManager().initLoader(0, null, this);
                } else {
                    //Toast.makeText(this, "Please provide permissions", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(
                        ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        if(Splash.firstRun){
            ParseObject userEmails = new ParseObject("userData");
            userEmails.put("emails", emails);
            userEmails.saveInBackground();
            Log.e("Splash", "doInBackground: Sent");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
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
            if (position > 0)
                id = IDsForYTsdk.get(position - 1);       //position problems
            else
                id = IDsForYTsdk.get(0);
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
            intent.putExtra(Intent.EXTRA_TEXT, "Hey! You must check this out!: " + ShareUrl.get(position));
            intent.setType("text/*");
            Intent i = Intent.createChooser(intent, "Send via ");
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
            progressDialog.setMessage("Refreshing");
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
                            ShareUrl.add(i, idList.get(i).get("share_url").toString());
                            ShortShareUrl.add(i, idList.get(i).get("short_share_url").toString());
                            VideoIDs.add(i, "https://www.youtube.com/embed/" + idList.get(i).get("youtube_id").toString() + "?rel=0&amp;controls=0&amp;showinfo=0&amp;autoplay=1");
                            IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                            if (idList.get(i).get("narrator_name") != null)
                                Narrator.add(i, idList.get(i).get("narrator_name").toString());
                            else
                                Narrator.add(i, "Bot");
                        }

                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.putStringArrayListExtra("titles", Titles);
                        intent.putStringArrayListExtra("descs", Descriptions);
                        intent.putStringArrayListExtra("videoIds", VideoIDs);
                        intent.putStringArrayListExtra("shareUrl", ShareUrl);
                        intent.putStringArrayListExtra("shortShareUrl", ShortShareUrl);
                        intent.putStringArrayListExtra("idsForSDK", IDsForYTsdk);
                        intent.putStringArrayListExtra("narrator", Narrator);
                        ParseObject.unpinAllInBackground("objectsID", idList, new DeleteCallback() {
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
                        Toast.makeText(getApplicationContext(), "Couldn't fetch data", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        } else {
            Toast.makeText(this, "Please connect to a network.", Toast.LENGTH_SHORT).show();
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
        initShareIntent("kat");
    }

    public void shareFacebookMessenger(View view) {
        try {
            myVib.vibrate(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initShareIntent("orc");
    }

    public void shareTwitter(View view) {
        try {
            myVib.vibrate(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initShareIntent("twi");
    }

    private boolean checkPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
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
                String fname = "screenMeme" + position;
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
                intent.setPackage("com.whatsapp");
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TEXT, "NewsMeme:Video News - the best way to stay updated\nGet today on Play Store https://goo.gl/gNcPbj ");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "App not installed", Toast.LENGTH_SHORT).show();
            }
        } else {
            myVib.vibrate(50);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/*");
            try {
                intent.setPackage("com.whatsapp");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "App not installed", Toast.LENGTH_SHORT).show();
            }
            //TODO: APP CAN CRASH HERE
            if (position > 0) {
                try {
                    intent.putExtra(Intent.EXTRA_TEXT, Titles.get(position - 1) + ": " + Links.get(position - 1));       //position problems
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    intent.putExtra(Intent.EXTRA_TEXT, Titles.get(0) + ": " + Links.get(0));       //position problems
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(this, "Please provide Storage permission", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
    }

    private void initShareIntent(String type) {
        try {
            boolean found = false;
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            // gets the list of intents that can be loaded.
            List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
            if (!resInfo.isEmpty()) {
                for (ResolveInfo info : resInfo) {
                    if (info.activityInfo.packageName.toLowerCase().contains(type) || info.activityInfo.name.toLowerCase().contains(type)) {
                        if (position > 0) {
                            if (type.equals("twi")) {
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(position - 1) + ": " + ShortShareUrl.get(position - 1) + " | via @newsmemedotin");
                                //Toast.makeText(this, "Hello "+Titles.get(position - 1) , Toast.LENGTH_SHORT).show();
                            } else
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(position - 1) + ": " + ShareUrl.get(position - 1));         //position problems
                        } else {
                            if (type.equals("twi"))
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(0) + ": " + ShortShareUrl.get(0) + " | via @newsmemedotin");
                            else
                                share.putExtra(Intent.EXTRA_TEXT, Titles.get(0) + ": " + ShareUrl.get(0));
                        }
                        share.setPackage(info.activityInfo.packageName);
                        found = true;
                        break;
                    }
                }
                if (!found)
                    Toast.makeText(MainActivity.this, "App not installed", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Please connect to a network", Toast.LENGTH_SHORT).show();
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
            progressDialog.setMessage("Updating preferences");
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
                            VideoIDs.add(i, "https://www.youtube.com/embed/" + idList.get(i).get("youtube_id").toString() + "?rel=0&amp;controls=0&amp;showinfo=0&amp;autoplay=1");
                            IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                            ShareUrl.add(i, idList.get(i).get("share_url").toString());
                            ShortShareUrl.add(i, idList.get(i).get("short_share_url").toString());
                            if (idList.get(i).get("narrator_name") != null)
                                Narrator.add(i, idList.get(i).get("narrator_name").toString());
                            else
                                Narrator.add(i, "Bot");
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putStringArrayListExtra("titles", Titles);
                        intent.putStringArrayListExtra("descs", Descriptions);
                        intent.putStringArrayListExtra("videoIds", VideoIDs);
                        intent.putStringArrayListExtra("shareUrl", ShareUrl);
                        intent.putStringArrayListExtra("shortShareUrl", ShortShareUrl);
                        intent.putStringArrayListExtra("idsForSDK", IDsForYTsdk);
                        intent.putStringArrayListExtra("narrator", Narrator);
                        ParseObject.unpinAllInBackground("objectsID", idList, new DeleteCallback() {
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
            Toast.makeText(this, "Preference switching needs network connection", Toast.LENGTH_SHORT).show();
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
            progressDialog.setMessage("Updating preferences");
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
                            ShareUrl.add(i, idList.get(i).get("share_url").toString());
                            ShortShareUrl.add(i, idList.get(i).get("short_share_url").toString());
                            VideoIDs.add(i, "https://www.youtube.com/embed/" + idList.get(i).get("youtube_id").toString() + "?rel=0&amp;controls=0&amp;showinfo=0&amp;autoplay=1");
                            IDsForYTsdk.add(i, YouTubeUrlParser.getVideoId(VideoIDs.get(i)));
                            if (idList.get(i).get("narrator_name") != null)
                                Narrator.add(i, idList.get(i).get("narrator_name").toString());
                            else
                                Narrator.add(i, "Bot");
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putStringArrayListExtra("titles", Titles);
                        intent.putStringArrayListExtra("descs", Descriptions);
                        intent.putStringArrayListExtra("videoIds", VideoIDs);
                        intent.putStringArrayListExtra("shareUrl", ShareUrl);
                        intent.putStringArrayListExtra("shortShareUrl", ShortShareUrl);
                        intent.putStringArrayListExtra("idsForSDK", IDsForYTsdk);
                        intent.putStringArrayListExtra("narrator", Narrator);
                        ParseObject.unpinAllInBackground("objectsID", idList, new DeleteCallback() {
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
                        Toast.makeText(getApplicationContext(), "Couldn't fetch data", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Preference switching needs network connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void couchView(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Intent intent = new Intent(this, CouchViewActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please connect to a network", Toast.LENGTH_SHORT).show();
        }
    }

    public static String FACEBOOK_URL = "https://www.facebook.com/newsmemedotin";
    public static String FACEBOOK_PAGE_ID = "NewsMeme";

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
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
        intent.putExtra(Intent.EXTRA_TEXT, "Hey, check out NewsMeme android app, Fresh news videos updated daily \uD83D\uDE03\uD83D\uDE03 https://play.google.com/store/apps/details?id=in.newsmeme");
        intent.setType("text/*");
        Intent i = Intent.createChooser(intent, "Send via ");
        startActivity(i);
    }

    public void subscribe(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.youtube.com/user/Mr9vine"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Please install YouTube app", Toast.LENGTH_SHORT).show();
        }
    }

    public void feedback(View view) {
        try {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.emailID)});
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.emailSubject));
            intent.putExtra(Intent.EXTRA_BCC, "gautamprajapati06@gmail.com");
            final PackageManager pm = getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
            ResolveInfo best = null;
            for (final ResolveInfo info : matches)
                if (info.activityInfo.packageName.endsWith(".gm") ||
                        info.activityInfo.name.toLowerCase().contains("gmail")) best = info;
            if (best != null)
                intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gmail client not available", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}

