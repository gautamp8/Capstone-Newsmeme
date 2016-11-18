package in.newsmeme;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.FileInputStream;

public class CouchViewActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_couch_view);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(Config.YOUTUBE_API_KEY, this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        if (!wasRestored) {
            if(lang.equals(getResources().getResourceName(R.string.englishLang)))
                player.loadPlaylist(getString(R.string.playlist1));
            else
                player.loadPlaylist(getString(R.string.playlist2));
            player.setFullscreen(true);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, R.string.failed_to_initialize, Toast.LENGTH_LONG).show();
    }

}
