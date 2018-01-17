package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

            return BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

        } catch (IOException e) {
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
}
