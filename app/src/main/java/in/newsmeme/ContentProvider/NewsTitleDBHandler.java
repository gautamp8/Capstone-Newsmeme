package in.newsmeme.ContentProvider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsTitleDBHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "newsManager";

    // Newss table name
    public static final String TABLE_NEWS = "news";

    // Newss Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";

    public NewsTitleDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NEWS_TABLE = "CREATE TABLE " + TABLE_NEWS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT)";
        db.execSQL(CREATE_NEWS_TABLE);
        Log.d("onCreateDB","DB Created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new news
    public void addNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, news.getName()); // News Name

        // Inserting Row
        db.insert(TABLE_NEWS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single news
    public News getNews(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NEWS, new String[] { KEY_ID,
                        KEY_TITLE}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        News news = new News(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        // return news
        return news;
    }

    // Getting All News
    public List<News> getAllNews() {
        List<News> newsList = new ArrayList<News>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NEWS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                News news = new News();
                news.setID(Integer.parseInt(cursor.getString(0)));
                news.setName(cursor.getString(1));
                // Adding news to list
                newsList.add(news);
            } while (cursor.moveToNext());
        }

        // return news list
        return newsList;
    }

    // Updating single news
    public int updateNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, news.getName());

        // updating row
        return db.update(TABLE_NEWS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(news.getID()) });
    }

    // Deleting single news
    public void deleteNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NEWS, KEY_ID + " = ?",
                new String[] { String.valueOf(news.getID()) });
        db.close();
    }


    // Getting news Count
    public int getNewsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NEWS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}