package com.example.music;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import android.content.Context;
public class MyDBHelper extends SQLiteOpenHelper {
    //public static String name="name";
    public static final String create_music="create table music ( "
            +"name text primary key ) ";
    private Context mContext;
    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext=context;
    }
    public void onCreate(SQLiteDatabase db){
        db.execSQL(create_music);
        Toast.makeText(mContext,"successed",Toast.LENGTH_LONG).show();
    }
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }
}
