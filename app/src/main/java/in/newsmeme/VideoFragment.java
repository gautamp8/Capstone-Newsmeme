package in.newsmeme;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import me.grantland.widget.AutofitTextView;

public class VideoFragment extends Fragment {

    WebView videoView;
    AutofitTextView titleTV, descTV;
    TextView narratorTV;
    String video, title, desc, narratorName;
    int narratorImage;
    CircularImageView narratorImageView;
    static View v;
    //private Tracker mTracker;

    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            v = inflater.inflate(R.layout.fragment_video, container, false);
            video = getArguments().getString(getString(R.string.link));                //"iS1g8G_njx8";
            title = getArguments().getString(getString(R.string.title_));
            desc = getArguments().getString(getString(R.string.desc));
            narratorName = getResources().getString(R.string.narratorName_By_Text) + " " + getArguments().getString(getString(R.string.narrator_intent_extra));
            narratorImage = getArguments().getInt(getString(R.string.narrator_image));

            titleTV = (AutofitTextView) v.findViewById(R.id.title);
            Typeface face = Typeface.createFromAsset(getContext().getAssets(), getString(R.string.typeface_tisapro_location));
            titleTV.setTypeface(face);
            titleTV.setText(title);

            descTV = (AutofitTextView) v.findViewById(R.id.description);
            face = Typeface.createFromAsset(getContext().getAssets(), getString(R.string.typeface_tisapro_regular_location));
            descTV.setTypeface(face);
            descTV.setText(desc);

            narratorTV = (TextView) v.findViewById(R.id.narratorNameTV);
            narratorTV.setTypeface(face);
            narratorTV.setText(narratorName);

            narratorImageView = (CircularImageView) v.findViewById(R.id.narratorImageIV);
            narratorImageView.setImageResource(narratorImage);

            videoView = (WebView) v.findViewById(R.id.videoView);
            videoView.getSettings().setJavaScriptEnabled(true);
            WebSettings webSettings = videoView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            videoView.loadUrl(video);
          /*  videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("YouTube Video Played")
                            .setLabel("Video Played")
                            .build());
                }
            }); */

        } else {
            v = inflater.inflate(R.layout.fragment_layout_offline, container, false);
            title = getArguments().getString(getString(R.string.title_));
            desc = getArguments().getString(getString(R.string.desc));
            narratorName = getResources().getString(R.string.narratorName_By_Text) + " " + getArguments().getString(getString(R.string.narrator_intent_extra));
            narratorImage = getArguments().getInt(getString(R.string.narrator_image));

            titleTV = (AutofitTextView) v.findViewById(R.id.title);
            Typeface face = Typeface.createFromAsset(getContext().getAssets(), getString(R.string.typeface_tisapro_location));
            titleTV.setTypeface(face);
            titleTV.setText(title);

            descTV = (AutofitTextView) v.findViewById(R.id.description);
            face = Typeface.createFromAsset(getContext().getAssets(), getString(R.string.typeface_tisapro_regular_location));
            descTV.setTypeface(face);
            descTV.setText(desc);
/*
            narratorTV = (TextView) v.findViewById(R.id.narratorNameTV);
            narratorTV.setTypeface(face);
            narratorTV.setText(narratorName);

            narratorImageView = (ImageView) v.findViewById(R.id.narratorImageIV);
            narratorImageView.setImageResource(narratorImage);
            */
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public static VideoFragment newInstance(String title, String desc, String link, String narrator, int narratorImage) {

        VideoFragment f = new VideoFragment();
        Bundle b = new Bundle();
        b.putString(Resources.getSystem().getString(R.string.title_), title);
        b.putString(Resources.getSystem().getString(R.string.desc), desc);
        b.putString(Resources.getSystem().getString(R.string.link), link);
        b.putString(Resources.getSystem().getString(R.string.narrator_intent_extra), narrator);
        b.putInt(Resources.getSystem().getString(R.string.narrator_image), narratorImage);
        f.setArguments(b);

        return f;
    }

    public static Bitmap getScreenShot() {
        View screenView = v.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}