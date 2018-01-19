package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.PlayerSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by rostyk_haidukevych on 1/15/18.
 */

public class Sf_Rest_Syncronizer {
    public static Activity currentActivity = null;
    private final String ORG_USERNAME = "rgaidukevych@gmail.com";
    private final String ORG_PASSWORD = "hjcnbrgo919979";
    private final String ORG_SECURITY_TOKEN = "eKNzvIdrCnrBIM4chDlV8FAQE";
    private String ACCESS_TOKEN = "";

    private static final String LOGIN_URL = "https://login.salesforce.com/services/oauth2/token";
    private static final String CLIENT_ID =
            "3MVG9HxRZv05HarQDQcBkT_chF.RWVZHSA8FLCkfvwl8OYnEHdvQFtc3lBrlRYzmIRPqz5qzTqPMUUJq1q.xz";
    private static final String CLIENT_SECRET = "6823072746201649040";
    private static String VERSION_NUMBER = null;

    private RestAuthSfSettings authSettings = null;

    private static Sf_Rest_Syncronizer instance = null;

    public static Integer playersSyncCount = null;

    private Sf_Rest_Syncronizer(){}


    public static Sf_Rest_Syncronizer getInstance(){
        if (instance==null) {
            instance = new Sf_Rest_Syncronizer();
            instance.auth();
//            while (instance.getAuthSettings()==null) {}
//            instance.getVersionNumberRestApi();
//            while (instance.getVersionNumber()==null) {}
//            instance.getAllPlayersSync("SELECT+Id,Name,Email__c,Password__c,IsManager__c+from+Player__c");
        }
        return instance;
    }

    public void auth() {
        OkHttpClient client = new OkHttpClient();

        MediaType Encoded = MediaType.parse("application/x-www-form-urlencoded");

        String bodyParams = "grant_type=password&username="+ORG_USERNAME+"&password="+ORG_PASSWORD
                + ORG_SECURITY_TOKEN+"&client_id="+CLIENT_ID+"&client_secret="+CLIENT_SECRET;

        System.out.println("Body parameters: "+bodyParams);

        RequestBody body = RequestBody.create(Encoded, bodyParams);

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                //.addHeader("Content-Type","application/x-www-form-urlencoded")
                .build();

        ACCESS_TOKEN = "access_token";

        client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.println("Call failed "+e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseSBody = response.body().string();
                        System.out.println("Response body: "+responseSBody);
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseSBody);
                                //ACCESS_TOKEN = jsonObject.getString("access_token");
                                setACCESS_TOKEN_From_Async_Response(
                                        jsonObject.getString("access_token"));
                                setAuthSettings(jsonObject);
                                instance.getVersionNumberRestApi();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
        });
    }





    private void getVersionNumberRestApi() {
//        if (instance.getAuthSettings()==null) {
//            try {
//                Thread.sleep(1000);
//                getVersionNumber();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        try {
            OkHttpClient client = new OkHttpClient();
            String url = authSettings.getInstance_url() + "/services/data/";
            System.out.println("url : "+url);

            final Request request = new Request.Builder()
                    .url(url).addHeader("Accept", "application/json")
                    .build();


            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("Failed");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //System.out.println("Response body => " + response.body().string());
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        if (jsonArray.length() > 0) {
                            setVersionNumber(jsonArray.getJSONObject(jsonArray.length()-1)
                                    .getString("version"));
                            instance.getAllPlayersSync("SELECT+Id,Name,Email__c,Password__c,IsManager__c,Image__c+from+Player__c");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getAllPlayersSync(String soql) {
        try {
            OkHttpClient client = new OkHttpClient();
            String url = authSettings.getInstance_url() + "/services/data/v"+getVersionNumber()+"/query?q="+soql;
            System.out.println("url : "+url);

            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer "+authSettings.getAccess_token())
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("Failed");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("records");
                        playersSyncCount = jsonArray.length();
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject playerJson = jsonArray.getJSONObject(i);
                                Player__c player = (Player__c) new Gson().fromJson(playerJson.toString(), Player__c.class);
                                PlayerSession.allPlayersSync.put(player.Id, player);
                                PlayerSession.playerBitmaps.put(player.Id, null);
//                                if (player.Image__c==null) {
//                                    PlayerSession.playerBitmaps.put(player.Id, null);
//                                } else {
//                                    AsyncTask<String, Void, Bitmap> task = new BitmapImgAsyncTask();
//                                    task.execute(player.Image__c);
//                                    //while (task.get() == null) {}
//                                    PlayerSession.playerBitmaps.put(player.Id, task.get());
//                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception ex) {

                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void setVersionNumber(String versionNumber) {
        VERSION_NUMBER = versionNumber;
    }

    public String getVersionNumber() {
        return VERSION_NUMBER;
    }

    private void setACCESS_TOKEN_From_Async_Response(String access_token) {
        ACCESS_TOKEN = access_token;
        System.out.println("Set access token: "+instance.ACCESS_TOKEN);
    }

    private void setAuthSettings(JSONObject jsonSettings){
        authSettings = new Gson().fromJson(jsonSettings.toString(), RestAuthSfSettings.class);
        System.out.println(authSettings);
    }

    public String getACCESS_TOKEN() {
        return ACCESS_TOKEN;
    }


    public RestAuthSfSettings getAuthSettings() {
        return authSettings;
    }

    public class RestAuthSfSettings {
        String access_token;
        String instance_url;
        String id;
        String token_type;
        String issued_at;
        String signature;

        @Override
        public String toString() {
            return "RestAuthSfSettings{" +
                    "access_token='" + access_token + '\'' +
                    ", instance_url='" + instance_url + '\'' +
                    ", id='" + id + '\'' +
                    ", token_type='" + token_type + '\'' +
                    ", issued_at='" + issued_at + '\'' +
                    ", signature='" + signature + '\'' +
                    '}';
        }

        public String getAccess_token() {
            return access_token;
        }

        public String getInstance_url() {
            return instance_url;
        }

        public String getId() {
            return id;
        }

        public String getToken_type() {
            return token_type;
        }

        public String getIssued_at() {
            return issued_at;
        }

        public String getSignature() {
            return signature;
        }
    }


}
