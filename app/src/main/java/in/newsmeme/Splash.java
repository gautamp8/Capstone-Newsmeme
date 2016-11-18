package in.newsmeme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import in.newsmeme.Service.FetchNewsService;

public class Splash extends AppCompatActivity {
    private FetchNewsServiceReceiver receiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent msgIntent = new Intent(Splash.this, FetchNewsService.class);
        startService(msgIntent);

        IntentFilter filter = new IntentFilter(FetchNewsServiceReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new FetchNewsServiceReceiver();
        registerReceiver(receiver, filter);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getString(R.string.status_bar_color)));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    public class FetchNewsServiceReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "in.newsmeme.intent.action.PROCESS_RESPONSE";
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent openMainActivity = new Intent(context, MainActivity.class);
            openMainActivity.putStringArrayListExtra(getString(R.string.title_intent_extra), intent.getStringArrayListExtra(getString(R.string.title_intent_extra)));
            openMainActivity.putStringArrayListExtra(getString(R.string.descs_intent_extra), intent.getStringArrayListExtra(getString(R.string.descs_intent_extra)));
            openMainActivity.putStringArrayListExtra(getString(R.string.videoID_intent_extra), intent.getStringArrayListExtra(getString(R.string.videoID_intent_extra)));
            openMainActivity.putStringArrayListExtra(getString(R.string.idForSdk_intent_extra), intent.getStringArrayListExtra(getString(R.string.idForSdk_intent_extra)));
            openMainActivity.putStringArrayListExtra(getString(R.string.links_intent_extra), intent.getStringArrayListExtra(getResources().getString(R.string.links_intent_extra)));
            openMainActivity.putStringArrayListExtra(getString(R.string.shareUrl_intent_extra), intent.getStringArrayListExtra(getString(R.string.shareUrl_intent_extra)));
            openMainActivity.putStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra), intent.getStringArrayListExtra(getString(R.string.shortShareUrl_intent_extra)));
            openMainActivity.putStringArrayListExtra(getString(R.string.narrator_intent_extra), intent.getStringArrayListExtra(getString(R.string.narrator_intent_extra)));
            startActivity(openMainActivity);
        }
    }
}

