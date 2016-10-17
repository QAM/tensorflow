package org.tensorflow.demo.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.*;
import android.database.*;
import android.util.Log;

import org.tensorflow.demo.Model.ModelPicture.PictureEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by liach on 10/14/2016.
 */

public class PictureDBHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PictureEntry.TABLE_NAME + " (" +
                    PictureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PictureEntry.COLUMN_NAME_PIC_NAME + TEXT_TYPE + COMMA_SEP +
                    PictureEntry.COLUMN_NAME_PIC_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    PictureEntry.COLUMN_NAME_PATH + TEXT_TYPE + COMMA_SEP +
                    PictureEntry.COLUMN_NAME_CREATED_TIME + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PictureEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TensorFlow.db";

    private SQLiteDatabase sqliteDBInstance = null;


    public PictureDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void openDB() throws SQLException
    {
        Log.i("openDB", "Checking sqliteDBInstance...");
        if(this.sqliteDBInstance == null)
        {
            Log.i("openDB", "Creating sqliteDBInstance...");
            this.sqliteDBInstance = this.getWritableDatabase();
        }else{
            if(!this.sqliteDBInstance.isOpen())
                this.sqliteDBInstance = this.getWritableDatabase();
        }
    }

    public void closeDB()
    {
        if(this.sqliteDBInstance != null && this.sqliteDBInstance.isOpen())
            this.sqliteDBInstance.close();
    }

    public long insertPicture(String name, String category, String path)
    {
        ContentValues values  = new ContentValues();
        values.put(PictureEntry.COLUMN_NAME_PIC_NAME, name);
        values.put(PictureEntry.COLUMN_NAME_PIC_CATEGORY, category);
        values.put(PictureEntry.COLUMN_NAME_PATH, path);
        values.put(PictureEntry.COLUMN_NAME_CREATED_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // Insert the new row, returning the primary key value of the new row
        return sqliteDBInstance.insert(PictureEntry.TABLE_NAME, null, values);
    }

    public List<String> getCategoriesList(){
        return getRecordsOneColumn(PictureEntry.COLUMN_NAME_PIC_CATEGORY, null, null);
    }

    public Cursor getCategoriesCursor(){
        return getCursorColumns(new String[]{"_id", PictureEntry.COLUMN_NAME_PIC_CATEGORY}, null, null);
    }

    public List<String> getPathsList(String category){
        String where = PictureEntry.COLUMN_NAME_PIC_CATEGORY + " = ?";
        String[] whereargs = {
                category
        };
        return getRecordsOneColumn(PictureEntry.COLUMN_NAME_PATH, where, whereargs);
    }

    public Cursor getPathsCursor(String category){
        String where = PictureEntry.COLUMN_NAME_PIC_CATEGORY + " = ?";
        String[] whereargs = {
                category
        };
        return getCursorColumns(new String[]{"_id", PictureEntry.COLUMN_NAME_PATH}, where, whereargs);
    }

    public Cursor getCursorColumns(String[] select, String where, String[] whereargs){

        return sqliteDBInstance.query(
                true,
                PictureEntry.TABLE_NAME,
                select,
                where,
                whereargs,
                null,
                null,
                null,
                null);
    }


    public List<String> getRecordsOneColumn(String select, String where, String[] whereargs){
        List<String> categories = new ArrayList<>();

        Cursor cursor = getCursorColumns(new String[]{select}, where, whereargs);
        if(cursor.getCount() >0)
        {
            while (cursor.moveToNext())
            {
                categories.add(cursor.getString(cursor.getColumnIndex(select)));
            }
            cursor.close();
            return categories;
        }
        else
        {
            cursor.close();
            return categories;
        }
    }

}
