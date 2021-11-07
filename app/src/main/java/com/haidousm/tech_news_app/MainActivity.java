package com.haidousm.tech_news_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int MAX_NUMBER_OF_STORIES = 1;
    private final String apiRootUrl = "https://hacker-news.firebaseio.com/v0";
    private final String topStoriesAPIUrl = String.format("%s/topstories.json", apiRootUrl);
    private final String unformattedStoryDetailAPIUrl = String.format("%s/item", apiRootUrl) + "/%s.json";

    enum DOWNLOAD_TYPE {
        STORY_IDS,
        STORY_DETAILS
    }

    private DOWNLOAD_TYPE currentDownloadType = DOWNLOAD_TYPE.STORY_IDS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadJSONData jsonDownloader = new DownloadJSONData();
        jsonDownloader.execute(topStoriesAPIUrl);

    }

    private void parseJSON(String jsonRes) {

        if (this.currentDownloadType == DOWNLOAD_TYPE.STORY_IDS) {

            ArrayList<Long> listOfIDs = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(jsonRes);
                for (int i = 0; i < MAX_NUMBER_OF_STORIES; i++) {
                    listOfIDs.add(jsonArray.getLong(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            this.currentDownloadType = DOWNLOAD_TYPE.STORY_DETAILS;

          // TODO: maybe batch download task them
            for (Long storyID:
                 listOfIDs) {
                DownloadJSONData jsonDownloader = new DownloadJSONData();
                jsonDownloader.execute(String.format(unformattedStoryDetailAPIUrl, storyID));
            }

        } else if (this.currentDownloadType == DOWNLOAD_TYPE.STORY_DETAILS) {
            Log.i("TAG", jsonRes);
        }

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
}

