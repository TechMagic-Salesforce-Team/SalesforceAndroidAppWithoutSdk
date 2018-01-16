package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.R;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.GameSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.PlayerSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.TournamentSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Game__c;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rostyk_haidukevych on 1/16/18.
 */

public class GamesListActivity extends ListActivity {
    private ListView gamesListView;
    private Map<Integer, Game__c> positionIdGame = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.games_list_view);

        String soql = "SELECT+Id,FirstCompetitor__c,SecondCompetitor__c,FirstCompetitorAccept__c," +
                "SecondCompetitorAccept__c,FirstCompetitorScore__c,SecondCompetitorScore__c,Stage__c,"+
                "WinningGroup__c+from+Game__c+where+Tournament__c+=+'"+
                TournamentSession.tournamentSelected.Id+"'";

//        String []values = new String[] {"Hello1","Hello2","Hello3"};
//        gamesListView = findViewById(android.R.id.list);
//        GamesListActivity.this.setListAdapter(new ArrayAdapter<String>(
//                GamesListActivity.this, R.layout.mylist,
//                R.id.Itemname, values));

        System.out.println("Soql: "+soql);
        loadAllGamesOfTournament(soql);

    }



    private void loadAllGamesOfTournament(String soql) {
        OkHttpClient client = new OkHttpClient();
        String url = Sf_Rest_Syncronizer.getInstance().getAuthSettings().getInstance_url() +
                "/services/data/v" + Sf_Rest_Syncronizer.getInstance().getVersionNumber() + "/query?q="
                + soql;
        System.out.println("Url: "+url);
        final Request request = new Request.Builder()
                .url(url).addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer "+ Sf_Rest_Syncronizer.getInstance().getACCESS_TOKEN())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed on getting games " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray records = new JSONObject(responseBody).getJSONArray("records");
                            String[] values = new String[records.length()];
                            for (int i = 0; i < records.length(); i++) {
                                JSONObject gameJson = records.getJSONObject(i);
                                Game__c game = (Game__c) new Gson().fromJson(gameJson.toString(), Game__c.class);
                                if (PlayerSession.allPlayersSync.containsKey(game.FirstCompetitor__c)
                                        && PlayerSession.allPlayersSync.containsKey(game.SecondCompetitor__c)) {
                                    values[i] = PlayerSession.allPlayersSync.get(game.FirstCompetitor__c).Name
                                            + " : " + game.FirstCompetitorScore__c + "\n"
                                            + PlayerSession.allPlayersSync.get(game.SecondCompetitor__c).Name
                                            + " : " + game.SecondCompetitorScore__c;
                                }
                                positionIdGame.put(i, game);
                            }
                            gamesListView = findViewById(android.R.id.list);

                            GamesListActivity.this.setListAdapter(new ArrayAdapter<String>(
                                    GamesListActivity.this, R.layout.mylist,
                                    R.id.Itemname, values));

//                            gamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                                @Override
//                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                    Game__c game = positionIdGame.get(position);
//                                    System.out.println("Game selected: " + game.toString());
//                                    GameSession.gameSelected = game;
//                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                    startActivity(intent);
//                                }
//                            });
                        } catch (JSONException ex) {

                        } catch (NullPointerException e) {
                            System.out.println("Exception npe: "+e.getMessage());
                        }

                    }
                });
                System.out.println("finished call");
            }
        });
    }
}
