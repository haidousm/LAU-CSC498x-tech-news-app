package com.haidousm.tech_news_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.haidousm.tech_news_app.models.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    enum DOWNLOAD_TYPE {
        ARTICLE_IDS,
        ARTICLE_DETAILS
    }

    private DOWNLOAD_TYPE currentDownloadType = DOWNLOAD_TYPE.ARTICLE_IDS;

    private final int MAX_NUMBER_OF_ARTICLES = 20;
    private final String apiRootUrl = "https://hacker-news.firebaseio.com/v0";
    private final String topStoriesAPIUrl = String.format("%s/topstories.json", apiRootUrl);
    private final String unformattedArticleDetailAPIUrl = String.format("%s/item", apiRootUrl) + "/%s.json";

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.db = new DatabaseHelper(this);
//        this.db.onUpgrade(this.db.getWritableDatabase(), 1, 1);
//        DownloadJSONData jsonDownloader = new DownloadJSONData();
//        jsonDownloader.execute(topStoriesAPIUrl);
    }

    private void parseJSON(String jsonRes) {

        if (this.currentDownloadType == DOWNLOAD_TYPE.ARTICLE_IDS) {

            ArrayList<Long> listOfIDs = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(jsonRes);
                for (int i = 20; i < MAX_NUMBER_OF_ARTICLES + 20; i++) {
                    listOfIDs.add(jsonArray.getLong(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            this.currentDownloadType = DOWNLOAD_TYPE.ARTICLE_DETAILS;

            // TODO: maybe batch download task them
            for (Long articleID :
                    listOfIDs) {
                DownloadJSONData jsonDownloader = new DownloadJSONData();
                jsonDownloader.execute(String.format(unformattedArticleDetailAPIUrl, articleID));
            }

        } else if (this.currentDownloadType == DOWNLOAD_TYPE.ARTICLE_DETAILS) {
            try {
                JSONObject jsonObject = new JSONObject(jsonRes);
                if(jsonObject.has("url")){
                    String contentUrl = jsonObject.getString("url");
                    contentUrl = contentUrl.replace("http://", "");
                    contentUrl = "https://" + contentUrl.replace("https://", "");
                    Article newArticle = new Article(jsonObject.getLong("id"), jsonObject.getString("title"));
                    new DownloadHTMLContent(newArticle).execute(contentUrl);
                }else if(jsonObject.has("text")){
                    Article newArticle = new Article(jsonObject.getLong("id"), jsonObject.getString("title"), jsonObject.getString("text"));
                    this.saveArticle(newArticle);
                }else{
                    Log.i("TAG", "parseJSON: ");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void saveArticle(Article article) {
        this.db.insertArticle(article);
    }

    @Override
    protected void onDestroy() {
        this.db.close();
        super.onDestroy();
    }

    public class DownloadJSONData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String res = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    res += inputLine;
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MainActivity.this.parseJSON(s);
        }
    }

    public class DownloadHTMLContent extends AsyncTask<String, Void, String> {
        private Article article;

        public DownloadHTMLContent(Article article) {
            this.article = article;
        }

        @Override
        protected String doInBackground(String... urls) {
            String res = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    res += inputLine;
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String htmlContent) {
            super.onPostExecute(htmlContent);
            article.setContent(htmlContent);
            MainActivity.this.saveArticle(article);

        }
    }
}

