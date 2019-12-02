package com.example.music;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Button;
import java.io.IOException;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import android.media.*;
import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.AdapterView;
import android.view.Menu;
import java.util.Random;
public class MainActivity extends AppCompatActivity {
    //private LyricView lyricView;
    private static final String TAG = "myTag";
    private MediaPlayer mediaPlay;
    private SeekBar sb;
    MyDBHelper myDBHelper;
    ListView listView;
    TextView textView;
    TextView txtLoopState;
    Button buttonStart;    //开始
    Button buttonPause;   //暂停
    Button buttonStop;       //停止
    Button buttonLoop;        //循环
    Button buttonSuiji;             //随机播放按钮；
    Button create;
    Button next;
    Button last;
    ArrayList<String> list_name;//歌单名链表；
    ArrayList<String> list_name_db;
    String title = null;
    String artist = null;
    MediaMetadataRetriever mmr;
    public static final String name = "name";//列名
    String geming = "";
    public Handler handler;
    public int UPDATE=0x101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlay = new MediaPlayer();
        txtLoopState = (TextView) findViewById(R.id.txtLoopState);
        buttonStart = (Button) findViewById(R.id.buttonStart);     //开始
        buttonPause = (Button) findViewById(R.id.buttonPause);     //暂停
        buttonStop = (Button) findViewById(R.id.buttonStop);       //停止
        buttonLoop = (Button) findViewById(R.id.buttonLoop);        //循环
        buttonSuiji=(Button)findViewById(R.id.suiji);
        //create = (Button) findViewById(R.id.create);
        next = (Button) findViewById(R.id.next);
        last = (Button) findViewById(R.id.last);
        textView = (TextView) findViewById(R.id.textview);

        listView = (ListView) findViewById(R.id.listView);
        sb = (SeekBar) findViewById(R.id.seekbar);
        myDBHelper = new MyDBHelper(this, "musicdb.db", null, 1);
        //创建数据库：表名：music:
        /*
        myDBHelper = new MyDBHelper(this, "musicdb.db", null, 1);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDBHelper.getWritableDatabase();
            }
        });
         */
        //
        //往数据库中添加歌曲：
        //打印歌单----assets中的歌单（直接读取出来的）

        list_name = showAssetsList("");  //获取assets中的歌单
        //adddatabase();
        list_name_db = new ArrayList<>();
        list_name_db = getAll("music");//获取数据库中的歌单
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, list_name_db);//可以打印assets读出来的数据与数据库中读出来的数据
        listView.setAdapter(adapter);
        // new String[]{name},new int[]{R.id.song_name});
        //SimpleAdapter simpleAdapter=new SimpleAdapter(this,R.layout.item,new String[]{name},new int[]{R.id.song_name});

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //if(is)
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int musicMax = mediaPlay.getDuration(); //得到该首歌曲最长秒数
                // int seekBarMax = seekBar.getMax();                musicService.player                        .seekTo(musicMax * progress / seekBarMax);
                int seekBarMax = seekBar.getMax();//100
                int progress = seekBar.getProgress();//12
                if (seekBarMax != 0) {
                    mediaPlay.seekTo(musicMax * progress / seekBarMax);
                }
                //  mediaPlay.seekTo(sb.getProgress());
            }
        });
        buttonStart.setOnClickListener(new MyClickListener());
        buttonPause.setOnClickListener(new MyClickListener());
        buttonStop.setOnClickListener(new MyClickListener());
        buttonLoop.setOnClickListener(new MyClickListener());
        buttonSuiji.setOnClickListener(new MyClickListener());
        next.setOnClickListener(new MyClickListener());
        last.setOnClickListener(new MyClickListener());

        buttonPause.setEnabled(false);
        buttonStop.setEnabled(false);
        buttonLoop.setEnabled(false);

        //注册上下文菜单：
        this.registerForContextMenu(listView);
        mediaPlay.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {   //监听完成自动播放下一首歌曲
            @Override
            public void onCompletion(MediaPlayer mp) {
                int temp=0;
                String str = artist + " - " + title + ".mp3";
                for (int i = 0; i < list_name_db.size(); i++)
                    if (str.equals(list_name_db.get(i)))
                        temp = i;
                if(mediaPlay.getCurrentPosition()==sb.getMax()) {
                    if (txtLoopState.getText() == "一次播放")
                        if (temp < list_name_db.size() - 1)
                            bofang0(temp + 1);
                        else
                            bofang0(0);
                    else if (txtLoopState.getText() == "随机播放") {
                        Random random = new Random(list_name_db.size());
                        bofang0(random.nextInt());
                    }
                }
            }
        });


    }
    public void bofang0(int temp) {
        try {
            mediaPlay.reset();
            AssetManager assetManager = getAssets();
            //list_name.get(0)
            //String s="代悦 - 三生执念.mp3";
            //String s1=list_name.get(0);
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(list_name_db.get(temp));//"代悦 - 三生执念.mp3");

            //sb.setMax(mediaPlay.getDuration());    ////获取音乐的总长：//去掉后就迅速跑满整个进度条：
            mediaPlay.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());//利用mediaplayer加载指定的声音文件
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            //File file=new File(Environment.getExternalStorageDirectory(),"music_mp3");
            //mediaPlay.setDataSource(file.getPath());
            geming = list_name_db.get(temp);
            textView.setText(geming);
            buttonPause.setEnabled(true);
            buttonStop.setEnabled(true);
            buttonLoop.setEnabled(true);
            mediaPlay.prepare();
            mediaPlay.start();

            new MyThread().start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void adddatabase() {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i = 0; i < list_name.size(); i++) {
            values.put("name", list_name.get(i));
            db.insert("music", null, values);
            values.clear();
        }
    }

    public ArrayList<String> showAssetsList(String fileAssetsName) {//
        int j = 0;
        ArrayList<String> list = new ArrayList<>();
        String[] list2 = null;
        AssetManager assetManager = getResources().getAssets();
        try {
            String[] list1 = assetManager.list("");
            list2 = new String[list1.length - 3];  //-3为image,sounds,webkit
            for (int i = 0; i < list1.length; i++) {
                if (list1[i].endsWith(".mp3")) {   //带歌词为.Irc文件，MP3中没有歌词
                    list2[j] = list1[i];
                    j++;
                }
            }
            list = new ArrayList<String>(Arrays.asList(list2));
            //String []list=assetManager.list("");

        } catch (Exception e) {
            e.printStackTrace();
        }
        ;
        return list;
    }

    public ArrayList<String> getAll(String tablename) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        Cursor cursor = db.query(tablename, null, null, null, null, null, null);
        int columns = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            //ArrayList<String>list1=new ArrayList<>();
            for (int i = 0; i < columns; i++) {
                String name = cursor.getColumnName(i);   //为表中的列名，id,,,name;
                String values = cursor.getString(cursor.getColumnIndex(name));
                list.add(values);
            }
        }
        return list;
    }

    //上下文菜单：
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu1, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        TextView textView = null;
        View itemView = null;
        AdapterView.AdapterContextMenuInfo info = null;
        switch (item.getItemId()) {
            case R.id.bo:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();//提供上下文菜单的信息
                itemView = info.targetView;  //上下文菜单的子视图
                textView = (TextView) itemView.findViewById(R.id.song_name);
                if (textView != null) {
                    String textName = textView.getText().toString();
                    bofang1(textName);
                }
                break;
            case R.id.delete:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();//提供上下文菜单的信息
                itemView = info.targetView;  //上下文菜单的子视图
                textView = (TextView) itemView.findViewById(R.id.song_name);
                if (textView != null) {
                    String textName = textView.getText().toString();
                    SQLiteDatabase db = myDBHelper.getWritableDatabase();
                    db.delete("music", "name = ?", new String[]{textName});
                    //删除后更新界面中的歌单
                    list_name_db = getAll("music");//获取数据库中的歌单
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, list_name_db);//可以打印assets读出来的数据与数据库中读出来的数据
                    listView.setAdapter(adapter);
                    Toast.makeText(this, "删除成功", Toast.LENGTH_LONG).show();
                }
                break;

        }
        return true;
    }

    public void bofang1(String temp) {
        try {
            mediaPlay.reset();
            AssetManager assetManager = getAssets();
            //list_name.get(0)
            //String s="代悦 - 三生执念.mp3";
            //String s1=list_name.get(0);
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(temp);//"代悦 - 三生执念.mp3");

            //sb.setMax(mediaPlay.getDuration());    ////获取音乐的总长：//去掉后就迅速跑满整个进度条：
            mediaPlay.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());//利用mediaplayer加载指定的声音文件
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            //File file=new File(Environment.getExternalStorageDirectory(),"music_mp3");
            //mediaPlay.setDataSource(file.getPath());
            geming = temp;
            textView.setText(geming);
            buttonPause.setEnabled(true);
            buttonStop.setEnabled(true);
            buttonLoop.setEnabled(true);
            mediaPlay.prepare();
            mediaPlay.start();

            new MyThread().start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //左上角菜单：
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.zengjia:
                Bundle bundle = new Bundle();
                bundle.putSerializable("result", list_name);
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
        return true;
    }

    class MyClickListener implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonStart: {
                    bofang(0);
                    break;
                }
                case R.id.buttonPause: {
                    if (mediaPlay.isPlaying()) {
                        buttonPause.setText("play");
                        mediaPlay.pause();
                    } else {
                        buttonPause.setText("pause");
                        mediaPlay.start();
                    }
                    break;
                }
                case R.id.buttonStop: {
                    if (mediaPlay.isPlaying())
                        mediaPlay.stop();
                    break;
                }
                case R.id.buttonLoop: {
                    boolean loop = mediaPlay.isLooping();
                    mediaPlay.setLooping(!loop);
                    if (!loop)
                        txtLoopState.setText("循环播放");
                    else
                        txtLoopState.setText("一次播放");
                    break;
                }
                case R.id.suiji:
                    txtLoopState.setText("随机播放");
                    if(mediaPlay.getCurrentPosition()==sb.getMax()) {
                        Random random=new Random(list_name_db.size());
                        bofang(random.nextInt());
                    }
                    break;
                case R.id.last: {
                    int temp = 0;
                    String str = artist + " - " + title + ".mp3";
                    for (int i = 0; i < list_name_db.size(); i++)
                        if (str.equals(list_name_db.get(i)))
                            temp = i;
                    if (temp == 0)
                        bofang(list_name_db.size() - 1);
                    else
                        bofang(temp - 1);

                    break;
                }
                case R.id.next: {
                    int temp = 0;
                    String str = artist + " - " + title + ".mp3";
                    for (int i = 0; i < list_name_db.size(); i++)
                        if (str.equals(list_name_db.get(i)))
                            temp = i;
                    if (temp == list_name_db.size() - 1)
                        bofang(0);
                    else
                        bofang(temp + 1);

                    //mediaPlay.get//获取正在播放的文件名即歌名；
                    break;
                }
            }
        }

        public void bofang(int temp) {
            try {
                mediaPlay.reset();
                AssetManager assetManager = getAssets();
                //list_name.get(0)
                //String s="代悦 - 三生执念.mp3";
                //String s1=list_name.get(0);
                AssetFileDescriptor assetFileDescriptor = assetManager.openFd(list_name_db.get(temp));//"代悦 - 三生执念.mp3");

                //sb.setMax(mediaPlay.getDuration());    ////获取音乐的总长：//去掉后就迅速跑满整个进度条：
                mediaPlay.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());//利用mediaplayer加载指定的声音文件
                mmr = new MediaMetadataRetriever();
                mmr.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                //File file=new File(Environment.getExternalStorageDirectory(),"music_mp3");
                //mediaPlay.setDataSource(file.getPath());
                geming = list_name_db.get(temp);
                textView.setText(geming);
                buttonPause.setEnabled(true);
                buttonStop.setEnabled(true);
                buttonLoop.setEnabled(true);
                mediaPlay.prepare();
                mediaPlay.start();

                MyThread thread=new MyThread();
                handler=new Handler(){
                    public void handleMessage(Message msg){
                        super.handleMessage(msg);
                        int musicMax=mediaPlay.getDuration();
                        if(msg.what==UPDATE){
                            try{
                                sb.setProgress(msg.arg1);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            sb.setProgress(0);
                        }
                    }
                };
                thread.start();

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class MyThread extends Thread {
        //handler=new Handler();


        public void run() {
            //super.run();
            while (!Thread.currentThread().isInterrupted()){//sb.getProgress() <= sb.getMax()) {
                if(mediaPlay!=null&&mediaPlay.isPlaying()) {
                    int currentPosition = mediaPlay.getCurrentPosition();//获取当前音乐播放器的位置
                    int musicMax = mediaPlay.getDuration();
                    int seekBarMax = sb.getMax();
                    int progress = sb.getProgress();

                    Message m = handler.obtainMessage();  //
                    m.arg1 = currentPosition * seekBarMax / musicMax;
                    m.arg2 = currentPosition;
                    m.what = UPDATE;
                    handler.sendMessage(m);

                    //让进度条动起来；mediaPlay.seekTo(musicMax * progress / seekBarMax);

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


//  mediaPlay.seekTo(sb.getProgress());


/*
    private void initMediaPlayer(){
        try{
            File file=new File(Environment.getExternalStorageDirectory(),"music.mp3");
            mediaPlay.setDataSource(file.getPath());//"C:\\Users\\xia\\Music");
            mediaPlay.prepare();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onRequestPermissionResult(int requestCode,String []permission,int []grantResults){
        switch(requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                }else{
                    Toast.makeText(MainActivity.this,"拒绝权限将无法播放",Toast.LENGTH_LONG).show();
                    finish();
                }
        }

    }
    protected void onDestroy(){
        super.onDestroy();
        if(mediaPlay!=null){
            mediaPlay.stop();
            mediaPlay.release();
        }
    }*/

/*
1.java.lang.IllegalStateException: ArrayAdapter requires the resource ID to be a TextView
textView跟文件型的.xml文件
 2.重复定义变量所带来的空指针异常：
 */