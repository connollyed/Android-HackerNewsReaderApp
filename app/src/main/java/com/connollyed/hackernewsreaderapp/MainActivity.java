package com.connollyed.hackernewsreaderapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    LinkedList<Article> stories;


    public class TaskDownloader extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection con;
            StringBuffer result = new StringBuffer();

            try{
                url = new URL(params[0]);
                con = (HttpURLConnection) url.openConnection();

                InputStream in = con.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while(data != -1){
                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }

                return result.toString();

            } catch(Exception e) {
                e.printStackTrace();
                return "FAILED";
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stories = new LinkedList<>();


        TaskDownloader topstorydownloader = new TaskDownloader();
        try {

            String topstories = topstorydownloader.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            JSONArray jsonArray = new JSONArray(topstories);

//            Log.i("TOP", topstories);

            for(int i=0; i<20; i++){
//                Log.i("Article_ID", jsonArray.getString(i));
                TaskDownloader articledownloader = new TaskDownloader();
                String article_text = articledownloader.execute("https://hacker-news.firebaseio.com/v0/item/"+jsonArray.getString(i)+".json?print=pretty").get();
//                Log.i("ART", article_text);

                JSONObject jsonObject = new JSONObject(article_text);
                String title = jsonObject.getString("title");
                String link = jsonObject.getString("url");
//                Log.i("TITLE", title);
//                Log.i("LINK", link);

                //Add story information to stories LinkedList
                stories.add(new Article(title, link));
            }

            ListView listView= (ListView) findViewById(R.id.stories);
            ArrayList<String> titles = new ArrayList<>();
            for(int i=0; i<stories.size(); i++){
                titles.add(stories.get(i).getTitle());
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
            listView.setAdapter(arrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), StoryActivity.class);
                    intent.putExtra("title", stories.get(position).title);
                    intent.putExtra("link", stories.get(position).link);

                    startActivity(intent);

                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}

class Article{
    String title;
    String link;

    public Article(String title, String link){
        this.title = title;
        this.link = link;
    }

    public String getTitle(){
        return title;
    }
    public String getLink(){
        return link;
    }

}
