/*
Assignment: In Class Assignment 04 Homework
File name: MainActivity.java
Full name:
Akhil Madhamshetty-801165622
Tarun thota-801164383
 */
package com.example.inclass05;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "demo";
    Button button_go;
    TextView search_keyword;
    ArrayList<String> results = new ArrayList<>();
    ImageView left_image_view;
    ImageView right_image_view;
    ProgressBar progressBar;
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Activity");

        button_go = findViewById(R.id.button_go);
        left_image_view = findViewById(R.id.left_image_view);
        right_image_view = findViewById(R.id.right_image_view);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        left_image_view.setVisibility(View.INVISIBLE);
        right_image_view.setVisibility(View.INVISIBLE);

        if(isConnected()){
            button_go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new getKeywords().execute();
                }
            });
            left_image_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(index == 0){
                        index = results.size() - 1;
                        ImageView imageView = findViewById(R.id.show_image);
                        new GetImageAsync(imageView).execute(results.get(index));
                    }
                    else{
                        index = index - 1;
                        ImageView imageView = findViewById(R.id.show_image);
                        new GetImageAsync(imageView).execute(results.get(index));
                    }
                }
            });
            right_image_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(index == results.size() -1 ){
                        index = 0;
                        ImageView imageView = findViewById(R.id.show_image);
                        new GetImageAsync(imageView).execute(results.get(index));
                    }
                    else{
                        index = index + 1;
                        ImageView imageView = findViewById(R.id.show_image);
                        new GetImageAsync(imageView).execute(results.get(index));
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "Check Internet Connection",Toast.LENGTH_SHORT).show();
        }

    }
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    class getKeywords extends AsyncTask<Void, Void, String[]>{

        @Override
        protected String[] doInBackground(Void... voids) {
            StringBuilder stringBuilder = new StringBuilder();
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String result = null;
            ArrayList<String> results = new ArrayList<>();
            String[] keywordStrings =null;
            try {
                URL url = new URL("https://dev.theappsdr.com/apis/photos/keywords.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    result = stringBuilder.toString();

                    Log.d(TAG, "keywords: " + result);

                    keywordStrings = result.split(";");

                    for (String s: keywordStrings) {
                        Log.d(TAG, "Strings: " + s);
                    }
                }
                else{
                    Log.d(TAG, "else called");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return keywordStrings;
        }

        @Override
        protected void onPostExecute(final String[] strings) {
            super.onPostExecute(strings);
            Log.d(TAG, "onPostExecute: " + strings);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose a Keyword")
                    .setItems(strings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String keyword = strings[which];
                            search_keyword = findViewById(R.id.search_keyword);
                            search_keyword.setText(keyword);
                            new getImageURLS().execute(keyword);
                        }
                    });
            builder.create().show();
        }
    }

    class getImageURLS extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            String strUrl = null;
            HttpURLConnection connection = null;
            String result = null;

            try {
                strUrl = "https://dev.theappsdr.com/apis/photos/index.php" + "?" +
                        "keyword=" + URLEncoder.encode(strings[0], "UTF-8");
                URL url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = IOUtils.toString(connection.getInputStream(), "UTF8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Log.d(TAG, "Image URLS: " + string);
            results.clear();
            if(string != ""){
                left_image_view.setVisibility(View.VISIBLE);
                right_image_view.setVisibility(View.VISIBLE);
                String[] substrings = string.split("\n");
                for (String s : substrings) {
                    results.add(s);
                }
                ImageView imageView = findViewById(R.id.show_image);
                index = 0;
                new GetImageAsync(imageView).execute(results.get(index));
            }
            else {
                progressBar.setVisibility(View.INVISIBLE);
                ImageView imageView = findViewById(R.id.show_image);
                imageView.setImageResource(R.mipmap.ic_launcher);
                left_image_view.setVisibility(View.INVISIBLE);
                right_image_view.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this,"No Images Found", Toast.LENGTH_SHORT).show();
            }


        }
    }
    private class GetImageAsync extends AsyncTask<String, Void, Void> {
        ImageView imageView;
        Bitmap bitmap = null;

        public GetImageAsync(ImageView iv) {
            imageView = iv;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection connection = null;
            bitmap = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Log.d(TAG, "Response code " + connection.getResponseCode());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                }
                else{
                    bitmap = null;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //Close open connection
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (bitmap != null && imageView != null) {
                progressBar.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            }
            else{
                bitmap = null;
                progressBar.setVisibility(View.INVISIBLE);
                ImageView imageView = findViewById(R.id.show_image);
                imageView.setImageResource(R.mipmap.ic_launcher);
                imageView.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"Error in loading the Image", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
