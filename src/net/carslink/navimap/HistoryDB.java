package net.carslink.navimap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wonghoukit on 2015/4/1 0001.
 */
public class HistoryDB extends SQLiteOpenHelper {
    private final static String TABLE_NAME = "search_history";
    private final static String DB_NAME = "dashCam.db";

    public HistoryDB(Context context){
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE search_history (id INTEGER primary key autoincrement, " +
                "title varchar(20) not null, " +
                "snippet varchar(30), " +
                "latitude double, " +
                "longitude double)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    public Cursor select() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, "id desc");
    }
    //增加操作
    public long insert(String title,String snippet, double lat, double lng)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        /* ContentValues */
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("snippet", snippet);
        cv.put("latitude", lat);
        cv.put("longitude", lng);
        return db.insert(TABLE_NAME, null, cv);
    }
    //删除操作
    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "id = ?";
        String[] whereValue ={ Integer.toString(id) };
        db.delete(TABLE_NAME, where, whereValue);
    }

    public void truncate(){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM " + TABLE_NAME;
        db.execSQL(sql);
    }

}
