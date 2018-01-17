package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Activity;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rostyk_haidukevych on 1/17/18.
 */

public class ForceComApi {

    private OkHttpClient client = new OkHttpClient();
    private String body;


    public void doGetRequest(Map<String, String> headers, String url){
        Request request = new Request.Builder().url(url).build();
        for (String key : headers.keySet()) {
            request.newBuilder().addHeader(key, headers.get(key));
        }

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed on getting tournaments " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                setBody(response.body().string());
            }
        });
        System.out.println("body");
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
