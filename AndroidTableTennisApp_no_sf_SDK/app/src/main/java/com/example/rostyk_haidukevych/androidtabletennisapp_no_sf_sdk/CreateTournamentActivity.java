package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.PlayerSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.TournamentSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Tournament__c;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by rostyk_haidukevych on 1/23/18.
 */

public class CreateTournamentActivity extends Activity {

    EditText nameTournament;
    Spinner statusTournament;
    Spinner typeTournament;
    Spinner formatTournament;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_tournament);


        nameTournament = findViewById(R.id.name_tournament_create);
        statusTournament = findViewById(R.id.status_tournament_create);
        typeTournament = findViewById(R.id.type_tournament_create);
        formatTournament = findViewById(R.id.format_tournament_create);

        addItemsToSpinner(statusTournament, Arrays.asList("Upcoming", "Current", "Completed"), "Status");
        addItemsToSpinner(formatTournament, Arrays.asList("1 x 1", "2 x 2"), "Format");
        addItemsToSpinner(typeTournament, Arrays.asList("Round Robin", "Single Elimination", "Double Elimination"), "Type");


        Button button = findViewById(R.id.create_tournament_button_post);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tournament__c tournament__c = new Tournament__c();
                tournament__c.Name = nameTournament.getText().toString();
                tournament__c.Status__c = statusTournament.getSelectedItem().toString();
                tournament__c.Format__c = formatTournament.getSelectedItem().toString();
                tournament__c.Type__c = typeTournament.getSelectedItem().toString();
                createTournamentHttp(tournament__c);
            }
        });

        Button loginLogoutButton = findViewById(R.id.login_logout_button);
        if (PlayerSession.currentPlayer != null) {
            loginLogoutButton.setText("Logout");
        } else {
            loginLogoutButton.setText("Login");
        }


    }

    public void addItemsToSpinner(Spinner spinner, List<String> items, String spinnerDefaultElement) {
        List<String> spinnerItems = new ArrayList<String>();
        spinnerItems.add(spinnerDefaultElement);
        spinnerItems.addAll(items);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CreateTournamentActivity.this,
                android.R.layout.simple_spinner_item, spinnerItems);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }


    private void createTournamentHttp(final Tournament__c tournament){
        Sf_Rest_Syncronizer restSyncronizer = Sf_Rest_Syncronizer.getInstance();
        OkHttpClient client = Sf_Rest_Syncronizer.enableTls12OnPreLollipop();//new OkHttpClient();
        MediaType Encoded = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(Encoded, new Gson().toJson(tournament));
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("From", restSyncronizer.getClientId())
                .addHeader("Authorization", "Bearer "+restSyncronizer.getAuthSettings().getAccess_token())
                .url(restSyncronizer.getAuthSettings().getInstance_url()+"/services/apexrest/api/post/tournaments/create")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed request to create new tournament "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseBody = response.body().string();

                final AlertDialog.Builder dialog = new AlertDialog.Builder(CreateTournamentActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("Congratulations");
                dialog.setMessage("Tournament " + tournament.Name + " was successfully created");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Response body : "+responseBody);
                        if (response.isSuccessful()) {
                            nameTournament.setText("");
                            statusTournament.setSelection(0);
                            formatTournament.setSelection(0);
                            typeTournament.setSelection(0);
                        } else {
                            dialog.setTitle("Error");
                            dialog.setMessage(responseBody);
                        }
                        dialog.show();
                    }
                });
            }
        });
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
}
