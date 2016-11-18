package in.newsmeme.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import in.newsmeme.R;

/**
 * Created by brainbreaker on 17/11/16.
 */

public class NewsTitleContentProvider extends ContentProvider {
    NewsTitleDBHandler dbHelper ;
    public static final String AUTHORITY = Resources.getSystem().getString(R.string.contentProviderAuthorities);//specific for our our app, will be specified in maninfed 
    public static final Uri CONTENT_URI = Uri.parse(Resources.getSystem().getString(R.string.content_provider_base_URL) + AUTHORITY);

    @Override
    public boolean onCreate() {
        dbHelper = new NewsTitleDBHandler(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String where, String[] args) {
        String table = NewsTitleDBHandler.TABLE_NEWS;
        SQLiteDatabase dataBase=dbHelper.getWritableDatabase();
        return dataBase.delete(table, where, args);
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        String table = NewsTitleDBHandler.TABLE_NEWS;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long value = database.insert(table, null, initialValues);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String table = NewsTitleDBHandler.TABLE_NEWS;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor =database.query(table,  projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause,
                      String[] whereArgs) {
        String table = NewsTitleDBHandler.TABLE_NEWS;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.update(table, values, whereClause, whereArgs);
    }
}
