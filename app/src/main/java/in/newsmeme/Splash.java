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
            window.setStatusBarColor(Color.parseColor("#512DA8"));
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
            openMainActivity.putStringArrayListExtra("titles", intent.getStringArrayListExtra("titles"));
            openMainActivity.putStringArrayListExtra("descs", intent.getStringArrayListExtra("descs"));
            openMainActivity.putStringArrayListExtra("videoIds", intent.getStringArrayListExtra("videoIds"));
            openMainActivity.putStringArrayListExtra("idsForSDK", intent.getStringArrayListExtra("idsForSDK"));
            openMainActivity.putStringArrayListExtra("links", intent.getStringArrayListExtra("links"));
            openMainActivity.putStringArrayListExtra("shareUrl", intent.getStringArrayListExtra("shareUrl"));
            openMainActivity.putStringArrayListExtra("shortShareUrl", intent.getStringArrayListExtra("shortShareUrl"));
            openMainActivity.putStringArrayListExtra("narrator", intent.getStringArrayListExtra("narrator"));
            startActivity(openMainActivity);
        }
    }
}

