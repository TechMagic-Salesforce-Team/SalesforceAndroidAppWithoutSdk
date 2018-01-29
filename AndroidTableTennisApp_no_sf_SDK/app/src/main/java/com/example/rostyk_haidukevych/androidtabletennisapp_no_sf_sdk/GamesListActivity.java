package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by rostyk_haidukevych on 1/16/18.
 */

public class GamesListActivity extends ListActivity {
    private ListView gamesListView;
    private Map<Integer, Game__c> positionIdGame = new HashMap<>();
    private Button applyForTournamentBtn;
    private String btnText = null;
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

        Button loginLogoutButton = findViewById(R.id.login_logout_button);
        applyForTournamentBtn = findViewById(R.id.apply_tournament_button);
        applyForTournamentBtn.setText("");

        if (PlayerSession.currentPlayer != null) {
            if (TournamentSession.tournamentSelected.Status__c.equals("Upcoming")) {
                checkIfPlayerAppliedToTheTournament("SELECT+Id+from+PlayerTournament__c+where"+
                        "+Player__c+=+'"+PlayerSession.currentPlayer.Id+"'+and+Tournament__c+=+'"+
                        TournamentSession.tournamentSelected.Id+"'");
                applyForTournamentBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            applyDisapplyForTournament();
                        }
                    });
            } else {
                System.out.println("Soql: "+soql);
                loadAllGamesOfTournament(soql);
                applyForTournamentBtn.setVisibility(View.GONE);
            }
        } else {
            applyForTournamentBtn.setVisibility(View.GONE);
            if (TournamentSession.tournamentSelected.Status__c.equals("Upcoming")) {

            } else {
                System.out.println("Soql: "+soql);
                loadAllGamesOfTournament(soql);
            }
        }

        if (PlayerSession.currentPlayer == null) {
            loginLogoutButton.setText("Login");
        } else {
            loginLogoutButton.setText("Logout");
        }

    }

    public void onLoginLogoutClick(View v) {
        if (PlayerSession.currentPlayer == null) {
            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginActivity);
        } else {
            PlayerSession.currentPlayer = null;
            finish();
            startActivity(getIntent());;
        }
    }

    private void loadAllGamesOfTournament(String soql) {
        OkHttpClient client = Sf_Rest_Syncronizer.enableTls12OnPreLollipop();//new OkHttpClient();
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
                                            + " : " + game.FirstCompetitorScore__c + "\n\n"
                                            + PlayerSession.allPlayersSync.get(game.SecondCompetitor__c).Name
                                            + " : " + game.SecondCompetitorScore__c;
                                }
                                positionIdGame.put(i, game);
                            }
                            gamesListView = findViewById(android.R.id.list);

                            GamesListActivity.this.setListAdapter(new ArrayAdapter<String>(
                                    GamesListActivity.this, R.layout.mylist,
                                    R.id.Itemname, values));

                            gamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (PlayerSession.currentPlayer == null) return;
                                    Player__c currentPlayer = PlayerSession.currentPlayer;
                                    final Game__c game = positionIdGame.get(position);


                                    if (game.FirstCompetitor__c.equals(currentPlayer.Id)
                                        || game.SecondCompetitor__c.equals(currentPlayer.Id)
                                        ) {
                                        System.out.println("Game selected: " + game.toString());
                                        GameSession.gameSelected = game;

                                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(GamesListActivity.this);
                                        builderSingle.setTitle("Inserting results of the game");
                                        LayoutInflater inflater = GamesListActivity.this.getLayoutInflater();
                                        View dialogView = inflater.inflate(R.layout.game_edit_layout, null);
                                        builderSingle.setView(dialogView);
                                        TextView firstCompName = dialogView.findViewById(R.id.firstCompName);
                                        TextView secondCompName = dialogView.findViewById(R.id.secondCompName);

                                        firstCompName.setText(PlayerSession.allPlayersSync.get(game.FirstCompetitor__c).Name);
                                        secondCompName.setText(PlayerSession.allPlayersSync.get(game.SecondCompetitor__c).Name);

                                        EditText firstCompetitorScore = dialogView.findViewById(R.id.firstCompScore);
                                        firstCompetitorScore.setText(game.FirstCompetitorScore__c);

                                        EditText secondCompetitorScore = dialogView.findViewById(R.id.secondCompScore);
                                        secondCompetitorScore.setText(game.SecondCompetitorScore__c);

                                        if (!(game.FirstCompetitorAccept__c && game.SecondCompetitorAccept__c)) {
                                            if (
                                                    (game.SecondCompetitorAccept__c
                                                            && game.FirstCompetitor__c.equals(PlayerSession.currentPlayer.Id))
                                                    ||
                                                            (game.FirstCompetitorAccept__c && game.SecondCompetitor__c.equals(PlayerSession.currentPlayer.Id))
                                                    ) {
                                                builderSingle.setNeutralButton("submit", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        System.out.println(game.FirstCompetitorAccept__c);
                                                        System.out.println(game.SecondCompetitorAccept__c);
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }

                                            builderSingle.setNegativeButton("insert", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                        } else {
                                            //firstCompetitorScore.setActivated(false);
                                            //secondCompetitorScore.setActivated(false);
                                            firstCompetitorScore.setEnabled(false);
                                            secondCompetitorScore.setEnabled(false);
                                        }
                                        builderSingle.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        builderSingle.show();
                                    }
                                }
                            });
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


    private void checkIfPlayerAppliedToTheTournament(String soql) {
        try {
            OkHttpClient client = Sf_Rest_Syncronizer.enableTls12OnPreLollipop();//new OkHttpClient();
            String url = Sf_Rest_Syncronizer.getInstance().getAuthSettings().getInstance_url() +
                    "/services/data/v" + Sf_Rest_Syncronizer.getInstance().getVersionNumber() + "/query?q="
                    + soql;
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " +
                            Sf_Rest_Syncronizer.getInstance().getAuthSettings().getAccess_token())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("Failed request to check if player is applied to the tournament");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseBody = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("PlayerTournament is " + responseBody);
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                if (jsonObject.getInt("totalSize") > 0) {
                                    btnText = "Disapply";
                                } else {
                                    btnText = "Apply";
                                }
                                applyForTournamentBtn.setText(btnText);
                            } catch (JSONException e) {
                                System.out.println("Exception while getting PlayerTournaments of Player from PlayerSession and Tournament from TournamentSession "+e.getMessage());
                            }
                        }
                    });
                }
            });
        } catch (Exception ex) {
            System.out.println("Exception while getting PlayerTournaments of Player from PlayerSession and Tournament from TournamentSession "+ex.getMessage());
        }
    }

    private void applyDisapplyForTournament() {
        try {
            OkHttpClient client = Sf_Rest_Syncronizer.enableTls12OnPreLollipop();//new OkHttpClient();
            String url = Sf_Rest_Syncronizer.getInstance().getAuthSettings().getInstance_url() +
                    "/services/apexrest/application/apply";

            MediaType encoded = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(encoded,
                    "{ \"tournamentId\" : \""+TournamentSession.tournamentSelected.Id+"\"," +
                            "\"playerId\" : \""+PlayerSession.currentPlayer.Id+"\"}");

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " +
                            Sf_Rest_Syncronizer.getInstance().getAuthSettings().getAccess_token())
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("Failed request to apply/disapply player for a tournament");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseBody = response.body().string();
                    System.out.println("Status is " + responseBody);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (responseBody.substring(1, responseBody.length() - 1).equals("success")) {
                                    System.out.println("Change button text");
                                    if (btnText.equals("Apply")) {
                                        btnText = "Disapply";
                                    } else {
                                        btnText = "Apply";
                                    }
                                    applyForTournamentBtn.setText(btnText);
                                }
                            } catch (Exception ex) {

                            }
                        }
                    });

                }
            });
        } catch (Exception ex) {
            System.out.println("Exception while getting PlayerTournaments of Player from PlayerSession and Tournament from TournamentSession "+ex.getMessage());
        }
    }

}
