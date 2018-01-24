package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * Created by rostyk_haidukevych on 1/17/18.
 */

public class BitmapImgAsyncTask extends AsyncTask<String, Void, Bitmap> {
    public static Boolean isWorking = false;
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
            System.setProperty("https.protocols", "“TLSv1,TLSv1.1,TLSv1.2”");
            Bitmap bitmap = null;
//            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//            System.out.println("Path : "+path);
//            File file = new File(
//                    path + "/images/");
//            if(file.exists()){
//                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//            }
//            return bitmap;
            //return BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            //return downloadBitmap(args[0]);
            return downloadBitmapOkHttp(args[0]);
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
            //URL uri = new URL(url);
            urlConnection = enableTls12ForHttpUrlCon(url);
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


    public static Bitmap downloadBitmapOkHttp(String url){
        isWorking = true;
        OkHttpClient client = Sf_Rest_Syncronizer.enableTls12OnPreLollipop();
        final Bitmap[] bitmapToGet = {null};
        final Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed while trying to load image : "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    System.out.println("Input stream => "+inputStream);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        System.out.println("Bitmap decoded => "+bitmap);
                        //while (bitmap==null) {}
                        bitmapToGet[0] = bitmap;
                        System.out.println("Bitmap to get[0] => "+bitmapToGet[0]);
                    }
                }
            }
        });

        while (bitmapToGet[0] == null) {}
        isWorking = false;
        System.out.println("Bitmap to get[0] (before returning) => "+bitmapToGet[0]);
        return bitmapToGet[0];
    }



    private static HttpsURLConnection enableTls12ForHttpUrlCon(String url) {
        URL uri = null;
        try {
            uri = new URL(url);
            if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 21) {
                try {
                    SSLContext sc = SSLContext.getInstance("TLSv1.2");
                    sc.init(null, null, null);
                    //client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                    ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build();
                    List<ConnectionSpec> specs = new ArrayList<>();
                    specs.add(cs);
                    specs.add(ConnectionSpec.COMPATIBLE_TLS);
                    specs.add(ConnectionSpec.CLEARTEXT);
                    HttpsURLConnection urlConnection = (HttpsURLConnection) uri.openConnection();
                    urlConnection.setSSLSocketFactory(sc.getSocketFactory());
                    urlConnection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String arg0, SSLSession arg1) {
                            return true;
                        }
                    });
                    return urlConnection;
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else try {
                return (HttpsURLConnection) uri.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
