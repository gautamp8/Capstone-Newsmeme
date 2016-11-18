package in.newsmeme;
import android.content.res.Resources;
public final class Config {

    private Config() {
    }
    public static final String YOUTUBE_API_KEY = Resources.getSystem().getString(R.string.youtube_api_key);
}
