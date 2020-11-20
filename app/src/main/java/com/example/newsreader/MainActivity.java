package com.example.newsreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    ListView news_titles_viewing;
    ArrayList<String> news_titles;
    ArrayList<String> news_urls;
    ArrayAdapter<String> adapter;
    SQLiteDatabase artieclesDB;
    TextView loading;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=new MenuInflater(this);
        inflater.inflate(R.menu.refresh_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        news_titles.clear();
        news_urls.clear();
        loading.setText("REFRESHING FEED");
        loading.setVisibility(VISIBLE);
        adapter.notifyDataSetChanged();
        DownloadTak task=new DownloadTak();
        try{
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
//            Log.i("size",String.valueOf(news_titles.size()));
//            adapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loading=findViewById(R.id.loading);
        artieclesDB=this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        artieclesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHR, content VARCHAR)");
        news_titles_viewing=findViewById(R.id.latest_news);
        news_titles=new ArrayList<>();
        news_urls=new ArrayList<>();
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,news_titles);
        news_titles_viewing.setAdapter(adapter);
        updateListView();
        DownloadTak task=new DownloadTak();
        Log.i("priyansh","priyansh");
        try{
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
//            Log.i("size",String.valueOf(news_titles.size()));
//            adapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
        news_titles_viewing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in=new Intent(MainActivity.this,Viewing_news.class);
                in.putExtra("url",news_urls.get(position));
                startActivity(in);
            }
        });
    }
    public class DownloadTak extends AsyncTask<String,Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            String result="";
            URL url;
            HttpURLConnection urlConnection=null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
                Log.i("apipriyansh",result);
                JSONArray jsonArray=new JSONArray(result);
                if(jsonArray==null)
                    Log.i("josnnull","asdfasdfasdfasdf");
                int count=20;
                if(jsonArray!=null && jsonArray.length()<20)
                    count=jsonArray.length();
                artieclesDB.execSQL("DELETE FROM articles");
                Log.i("count",String.valueOf(count));
                Log.i("adsfasd","asdfasdf");
                for(int i=0;i<count;i++)
                {
                    String id= jsonArray.getString(i);
                    Log.i("ID",id);
                    Log.i("counter",String.valueOf(i));
                    url=new URL("https://hacker-news.firebaseio.com/v0/item/"+id+".json?print=pretty");
                    urlConnection=(HttpURLConnection) url.openConnection();
                    inputStream=urlConnection.getInputStream();
                    inputStreamReader=new InputStreamReader(inputStream);
                    data=inputStreamReader.read();
                    String result1="";
                    while(data!=-1) {
                        char current=(char)data;
                        result1+=current;
                        data=inputStreamReader.read();
                    }
//                    Log.i("Article Info",result1);
                    JSONObject jsonObject=new JSONObject(result1);
                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        String title1=(jsonObject.getString("title"));
                        String url1=(jsonObject.getString("url"));
                        Log.i("title",title1);
                        Log.i("urlcom",url1);
//                        news_titles.add(title1);
//                        news_urls.add(url1);
                        String sql="INSERT INTO articles (articleID, title, content) VALUES (?, ?, ?)";
                        SQLiteStatement sqLiteStatement=artieclesDB.compileStatement(sql);
                        sqLiteStatement.bindString(1, id);
                        sqLiteStatement.bindString(2, title1);
                        sqLiteStatement.bindString(3, url1);
                        sqLiteStatement.execute();
                        Log.i("sizewe",String.valueOf(news_titles.size()));
//                        try {
//                                url = new URL(url1);
//                                urlConnection = (HttpURLConnection) url.openConnection();
//                                inputStream = urlConnection.getInputStream();
//                                inputStreamReader = new InputStreamReader(inputStream);
//                                data=inputStreamReader.read();
//                                String result2 = "";
//                                while(data!=-1) {
//                                    char current=(char)data;
//                                    result2+=current;
//                                    data=inputStreamReader.read();
//                                }
//
//                        }catch (Exception e){
//                                e.printStackTrace();
//                                Log.i("Problem in html","asdf");
//                        }
                    }
                }
                Log.i("URL Content",result);
//                adapter.notifyDataSetChanged();
                return result;
            }
            catch (Exception e){
                e.printStackTrace();
                Log.i("Nothing found","nothing");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateListView();
        }
    }
    public void updateListView() {
        Log.i("priyansh","pasdf");
        Cursor c=artieclesDB.rawQuery("SELECT * FROM articles",null);
        int content_index=c.getColumnIndex("content");
        int title_index=c.getColumnIndex("title");
        if(c!=null && c.moveToFirst()) {
            news_titles.clear();
            news_urls.clear();
            do {
                news_titles.add(c.getString(title_index));
                news_urls.add(c.getString(content_index));
            }while(c.moveToNext());
        }
        else {
            Log.i("Found","norhitn");
        }
        Log.i("asdf111",String.valueOf(news_titles.size()));
        adapter.notifyDataSetChanged();
        if(news_titles.size()!=0)
            loading.setVisibility(INVISIBLE);
        else
            loading.setVisibility(VISIBLE);
    }
}