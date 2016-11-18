package in.newsmeme;

import android.app.Application;
import android.content.res.Resources;

import com.parse.Parse;
/**
 * Created by Gautam Prajapati on 16-Nov-16.
 */
public class ParseApplication extends Application {

    public static String APP_ID = Resources.getSystem().getString(R.string.parse_app_id);
    public static String CLIENT_KEY = Resources.getSystem().getString(R.string.parse_client_key);
    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, APP_ID, CLIENT_KEY);

    }
}
