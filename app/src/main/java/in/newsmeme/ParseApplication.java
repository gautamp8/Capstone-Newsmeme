package in.newsmeme;

import android.app.Application;

import com.parse.Parse;
/**
 * Created by Gautam Prajaapti on 16-Nov-16.
 */
public class ParseApplication extends Application {

    public static final String APP_ID = "2wzSL2IYgy38Q378nNoKSJ23qqqSy5Uu1BW7Slax";
    public static final String CLIENT_KEY = "DceJGwZ5lMYnXMvtLHXkYjVlkDbx5YD6cpDNMG0k";
    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, APP_ID, CLIENT_KEY);

    }
}
