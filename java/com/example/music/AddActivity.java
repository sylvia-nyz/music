package com.example.music;

import android.content.ContentValues;
import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.Arrays;
import android.content.res.*;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity {
    ArrayList<String>list_name;
    ArrayList<String>list_name_db;
    ArrayList<String>list;
    MyDBHelper myDBHelper;
    ListView listView;
    Button finish;
    //显示可以增加的歌单增加以后就可以跳转：
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        myDBHelper=new MyDBHelper(this,"musicdb.db",null,1);  //数据库名称错了以后读取数据为空；
        listView=(ListView)findViewById(R.id.listView_zengjia);
        finish=(Button)findViewById(R.id.finish);

        Intent intent=getIntent();
        list_name=showAssetsList("");//(ArrayList<String>)intent.getSerializableExtra("reuslt");   //传送过来的数据为空；
        list_name_db=getAll("music");
        list=new ArrayList<String>();
        //确定不在数据库中歌曲：
        if(list_name_db.size()==0){
            list=list_name;

        }else{
            for(int i=0;i<list_name.size();i++)
                for(int j=0;j<list_name_db.size();j++)
                    if(list_name.get(i).equals(list_name_db.get(j))) {
                        if(i<list_name.size()-1)i++;//
                        //j=0;
                    }
                    else {
                        if(j==(list_name_db.size()-1))
                            list.add(list_name.get(i));
                    }
        }

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.item_add,list);
        listView.setAdapter(adapter);

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AddActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        this.registerForContextMenu(listView);
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        getMenuInflater().inflate(R.menu.menu2,menu);
        super.onCreateContextMenu(menu,v,menuInfo);
    }
    public boolean onContextItemSelected(MenuItem item) {
        TextView textView=null;
        View itemView=null;
        AdapterView.AdapterContextMenuInfo info = null;
        switch (item.getItemId()){
            case R.id.zengjia:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();//提供上下文菜单的信息
                itemView = info.targetView;  //上下文菜单的子视图
                textView = (TextView) itemView.findViewById(R.id.song_name_add);
                if(textView!=null){
                    String textName=textView.getText().toString();
                    SQLiteDatabase db=myDBHelper.getWritableDatabase();
                    ContentValues values=new ContentValues();
                    values.put("name",textName);
                    db.insert("music",null,values);
                    values.clear();
                    Toast.makeText(this,"添加成功",Toast.LENGTH_LONG).show();
                }
                break;
        }
    return true;
    }
    public  ArrayList<String> showAssetsList(String fileAssetsName){//
        int j=0;
        ArrayList<String>list=new ArrayList<>();
        String []list2=null;
        AssetManager assetManager=getResources().getAssets();
        try{
            String []list1=assetManager.list("");
            list2=new String[list1.length-3];
            for(int i=0;i<list1.length;i++) {
                if (list1[i].endsWith(".mp3")){
                    list2[j]=list1[i];
                    j++;}
            }
            list=new ArrayList<String>(Arrays.asList(list2));
        }catch(Exception e){
            e.printStackTrace();
        };
        return list;
    }
    public ArrayList<String> getAll(String tablename){
        ArrayList<String> list=new ArrayList<>();
        SQLiteDatabase db=myDBHelper.getReadableDatabase();
        Cursor cursor=db.query(tablename,null,null,null,null,null,null);
        int columns= cursor.getColumnCount();
        while(cursor.moveToNext()){
            //ArrayList<String>list1=new ArrayList<>();
            for(int i=0;i<columns;i++){
                String name= cursor.getColumnName(i);   //为表中的列名，id,,,name;
                String values= cursor.getString(cursor.getColumnIndex(name));
                list.add(values);
            }
        }
        return list;
    }
}
/*
Attempt to invoke virtual method 'void android.widget.ListView.setAdapter(android.widget.ListAdapter)' on a null object reference
listView没有定义，find...ById();
 */