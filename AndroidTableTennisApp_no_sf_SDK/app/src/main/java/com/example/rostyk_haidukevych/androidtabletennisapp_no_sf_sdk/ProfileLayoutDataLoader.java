package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.PlayerSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.TournamentSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Tournament__c;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rostyk_haidukevych on 1/19/18.
 */

public class ProfileLayoutDataLoader {
    private static final String ARG_SECTION_NUMBER = "section1_number";
    private ImageView profileImage;
    private Spinner profile_statusSpinner;
    private Spinner profile_formatSpinner;
    private Spinner profile_typeSpinner;
    private EditText profile_nameInput;
    private TableLayout profile_tableLayout;
    private Map<String, JSONObject> profile_tournamentsSync = new HashMap<>();
    private Activity activity;
    private Fragment fragment;
    private Button syncButton;
    private Player__c player;
    private RelativeLayout layout;
    private RelativeLayout layoutIfNotLoggedIn;
    private TextView nameField;

    public ProfileLayoutDataLoader(ImageView profileImage,
                                   Spinner profile_statusSpinner,
                                   Spinner profile_formatSpinner,
                                   Spinner profile_typeSpinner,
                                   EditText profile_nameInput,
                                   TableLayout profile_tableLayout,
                                   Activity activity,
                                   Fragment fragment,
                                   Player__c player,
                                   RelativeLayout layout,
                                   RelativeLayout layoutIfNotLoggedIn,
                                   Button syncButton
                                   ) {
        this.profileImage = profileImage;
        this.profile_statusSpinner = profile_statusSpinner;
        this.profile_formatSpinner = profile_formatSpinner;
        this.profile_typeSpinner = profile_typeSpinner;
        this.profile_nameInput = profile_nameInput;
        this.profile_tableLayout = profile_tableLayout;
        this.activity = activity;
        this.fragment = fragment;
        this.player = player;
        this.layout = layout;
        this.layoutIfNotLoggedIn = layoutIfNotLoggedIn;
        this.syncButton = syncButton;
    }



    public void loadAndInitData(Boolean tab1Fragment) {
        addItemsToSpinner(profile_statusSpinner, Arrays.asList("Upcoming", "Current", "Completed"), MainActivity.Tab1Fragment.InputType.S, activity);
        addItemsToSpinner(profile_formatSpinner, Arrays.asList("1 x 1", "2 x 2"), MainActivity.Tab1Fragment.InputType.F, activity);
        addItemsToSpinner(profile_typeSpinner, Arrays.asList("RR", "SE", "DE"), MainActivity.Tab1Fragment.InputType.T, activity);

        setTheSameOnChangeToSpinners();


        profile_nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearTable(profile_tableLayout);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    List<MainActivity.TableRowAndJsonObject> tableRowAndJsonObjects =
                            findTournamentsByInputsAndFillTable();
                    for (MainActivity.TableRowAndJsonObject obj : tableRowAndJsonObjects) {
                        setOnRowClick(activity.getApplicationContext(), obj.tableRow, obj.tournament);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (!tab1Fragment) {
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTable(profile_tableLayout);
                    tryLoadTournamentsByProfile();
                }
            });
            tryLoadTournamentsByProfile();
        } else {
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    clearTable(profile_tableLayout);
                    tryLoadTournaments();
                }
            });
            tryLoadTournaments();
        }
    }

    public static void addItemsToSpinner(Spinner spinner, List<String> items, MainActivity.Tab1Fragment.InputType inputType, Activity activity) {
        List<String> spinnerItems = new ArrayList<String>();
        spinnerItems.add(inputType.toString());
        spinnerItems.addAll(items);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, spinnerItems);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void setTheSameOnChangeToSpinners() {
                profile_typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        try {
                            clearTable(profile_tableLayout);
                            List<MainActivity.TableRowAndJsonObject> tableRowsAndJsonObjects =
                                    findTournamentsByInputsAndFillTable();
                           onRowClickForList(tableRowsAndJsonObjects);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });


                profile_statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        try {
                            clearTable(profile_tableLayout);
                            List<MainActivity.TableRowAndJsonObject> tableRowsAndJsonObjects =
                                    findTournamentsByInputsAndFillTable();
                            onRowClickForList(tableRowsAndJsonObjects);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }
                });

                profile_formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        try {
                            clearTable(profile_tableLayout);
                            List<MainActivity.TableRowAndJsonObject> tableRowsAndJsonObjects =
                                    findTournamentsByInputsAndFillTable();
                            onRowClickForList(tableRowsAndJsonObjects);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
    }

    private static void clearTable(TableLayout tableLayout){
        tableLayout.removeViews(4,tableLayout.getChildCount()-4);
    }


    private List<MainActivity.TableRowAndJsonObject> findTournamentsByInputsAndFillTable() throws JSONException, InterruptedException {
        String name = profile_nameInput.getText().toString();
        String type = profile_typeSpinner.getSelectedItem().toString();

        switch (type) {
            case "RR" : type = "Round Robin"; break;
            case "SE" : type = "Single Elimination"; break;
            case "DE" : type = "Double Elimination"; break;
        }

        String format = profile_formatSpinner.getSelectedItem().toString();
        String status = profile_statusSpinner.getSelectedItem().toString();
        List<MainActivity.TableRowAndJsonObject> tableRowsAndTournaments = new ArrayList<>();

        for (String key : profile_tournamentsSync.keySet()) {
            if (profile_tournamentsSync.get(key).get("Name").toString().toUpperCase().contains(name.toUpperCase())
                    &&
                    (profile_tournamentsSync.get(key).get("Type__c").toString().equals(type) ||
                            type.equals(MainActivity.Tab1Fragment.InputType.T.toString()))
                    &&
                    (profile_tournamentsSync.get(key).get("Status__c").toString().equals(status) ||
                            status.equals(MainActivity.Tab1Fragment.InputType.S.toString()))
                    &&
                    (profile_tournamentsSync.get(key).get("Format__c").toString().equals(format) ||
                            format.equals(MainActivity.Tab1Fragment.InputType.F.toString()))
                    ) {
                TableRow tableRow = addTableRow(profile_tournamentsSync.get(key));
                MainActivity.TableRowAndJsonObject tableRowAndJsonObject = new MainActivity.TableRowAndJsonObject();
                tableRowAndJsonObject.tableRow = tableRow;
                tableRowAndJsonObject.tournament = profile_tournamentsSync.get(key);
                tableRowsAndTournaments.add(tableRowAndJsonObject);
            }
        }
        return tableRowsAndTournaments;
    }


    private TableRow addTableRow(JSONObject jsonObject) throws JSONException, InterruptedException {
        TableRow tableRow = new TableRow(activity);
        tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(makeColumn((String) jsonObject.get("Name"), activity),0);

        TextView typeText = makeColumn((String) ""+jsonObject.get("Type__c").toString().split(" ")[0].charAt(0)+
                jsonObject.get("Type__c").toString().split(" ")[1].charAt(0), activity);

        tableRow.addView(typeText,1);
        tableRow.addView(makeColumn((String) jsonObject.get("Format__c"), activity),2);
        tableRow.addView(makeColumn((String) jsonObject.get("Status__c"), activity),3);
        tableRow.setPadding(0,10,0,0);
        profile_tournamentsSync.put(jsonObject.get("Id").toString(), jsonObject);
        profile_tableLayout.addView(tableRow);
        return tableRow;
    }


    private static TextView makeColumn(String text, Activity activity) {
        TextView textView = new TextView(activity);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setPadding(10,10,0,0);
        return textView;
    }

    static class TableRowAndJsonObject {
        TableRow tableRow;
        JSONObject tournament;

        public TableRowAndJsonObject() {}
    }


    enum TabFragmentType {
        TAB_1_FRAGMENT,
        TAB_2_FRAGMENT;
    }


    private void setOnRowClick(final Context context, TableRow row, final JSONObject tournament){
        row.setClickable(true);  //allows you to select a specific row
        row.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TournamentSession.tournamentSelected = (Tournament__c)
                        new Gson().fromJson(String.valueOf(tournament), Tournament__c.class);
                Intent tournamentInfoActivity = new Intent(context, GamesListActivity.class);
                context.startActivity(tournamentInfoActivity);
            }
        });
    }


    private void tryLoadTournamentsByProfile(){
        if (player==null) {
            tryLoadTournaments();
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Sf_Rest_Syncronizer restSyncronizer = Sf_Rest_Syncronizer.getInstance();
        String url = restSyncronizer.getAuthSettings().getInstance_url()
                +"/services/apexrest/api/get/tournaments/player?playerId="
                + player.Id;
        Request request = new Request.Builder().url(url)
                .addHeader("Authorization","Bearer "
                        +restSyncronizer.getAuthSettings().getAccess_token())
                .addHeader("Accept", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Error while getting tournaments by profile");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                profile_tournamentsSync.clear();
                final String responseBody = response.body().string();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Response body (getting tournaments by profile : "
                                + responseBody);
                        try {
                            JSONArray tournamentsJSON = new JSONArray(responseBody.substring
                                    (1,responseBody.length()-1));
                            for (int i = 0; i < tournamentsJSON.length(); i++){
                                JSONObject tournamentJSON = tournamentsJSON.getJSONObject(i);
                                TableRow tableRow = addTableRow(
                                        tournamentJSON);
                                setOnRowClick(activity.getApplicationContext(), tableRow, tournamentJSON);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }

    public void onRowClickForList(List<MainActivity.TableRowAndJsonObject> tableRowsAndJsonObjects) {
        for (MainActivity.TableRowAndJsonObject tableRowAndJsonObject : tableRowsAndJsonObjects) {
            setOnRowClick(activity.getApplicationContext(), tableRowAndJsonObject.tableRow, tableRowAndJsonObject.tournament);
        }
    }




    //for tab1 fragment
    private void loadAllTournamentsToTheTable(String soql) {
        profile_tournamentsSync.clear();
        clearTable(profile_tableLayout);
        Sf_Rest_Syncronizer restSyncronizer = Sf_Rest_Syncronizer.getInstance();
        OkHttpClient client = new OkHttpClient();
        String url = restSyncronizer.getAuthSettings().getInstance_url() +
                "/services/data/v" + restSyncronizer.getVersionNumber() + "/query?q="
                + soql;
        final Request request = new Request.Builder()
                .url(url).addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer "+ restSyncronizer.getACCESS_TOKEN())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed on getting tournaments "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String body = response.body().string();
                System.out.println(body);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject tournamentsContainer = new JSONObject(body);
                            JSONArray tournaments = tournamentsContainer.getJSONArray("records");

                            for (int i = 0; i < tournaments.length(); i++) {
                                JSONObject tournament = tournaments.getJSONObject(i);
                                profile_tournamentsSync.put(tournament.getString("Id"), tournament);
                                TournamentSession.allTournamentsSync.put(tournament.getString("Id"), new Gson().fromJson(tournament.toString(), Tournament__c.class));
                                TableRow newTableRow = addTableRow(tournament);
                                setOnRowClick(activity.getApplicationContext(), newTableRow, tournament);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void tryLoadTournaments() {
        Sf_Rest_Syncronizer restSyncronizer = Sf_Rest_Syncronizer.getInstance();
        if (restSyncronizer.getAuthSettings() != null && restSyncronizer.getVersionNumber() != null) {
            loadAllTournamentsToTheTable("SELECT+Id,Name,Status__c,Format__c,Type__c+" +
                    "from+Tournament__c");
        } else {
            try {
                Thread.sleep(1000);
                tryLoadTournaments();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
