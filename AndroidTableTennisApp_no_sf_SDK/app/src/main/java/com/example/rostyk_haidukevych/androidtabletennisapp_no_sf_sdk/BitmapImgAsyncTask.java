package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by rostyk_haidukevych on 1/17/18.
 */

public class BitmapImgAsyncTask extends AsyncTask<String, Void, Bitmap> {

    public BitmapImgAsyncTask() {

        //mListener = listener;
    }

//    public interface Listener{
//
//        void onImageLoaded(Bitmap bitmap);
//        void onError();
//    }

    //private Listener mListener;


    @Override
    protected Bitmap doInBackground(String... args) {
        try {
            Bitmap bitmap = null;
//            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//            System.out.println("Path : "+path);
//            File file = new File(
//                    path + "/images/");
//            if(file.exists()){
//                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//            }
//            return bitmap;
            return BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
            //return downloadBitmap(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            //mListener.onImageLoaded(bitmap);
        } else {
            //mListener.onError();
        }
    }

    private Bitmap downloadBitmap(String url) {
        HttpsURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpsURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != 200) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (Exception e) {
            urlConnection.disconnect();
            Log.w("ImageDownloader", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
