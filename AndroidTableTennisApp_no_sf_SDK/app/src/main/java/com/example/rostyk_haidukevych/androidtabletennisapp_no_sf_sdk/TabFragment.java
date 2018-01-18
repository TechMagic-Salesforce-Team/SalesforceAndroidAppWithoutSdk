package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.TournamentSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Tournament__c;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rostyk_haidukevych on 1/17/18.
 */

public class TabFragment extends Fragment {
    protected Spinner statusSpinner;
    protected Spinner formatSpinner;
    protected Spinner typeSpinner;
    protected EditText nameInput;
    protected TableLayout tableLayout;
    protected Map<String, JSONObject> tournamentsSync = new HashMap<>();



    static class TableRowAndJsonObject {
        TableRow tableRow;
        JSONObject tournament;

        public TableRowAndJsonObject() {
        }
    }

    public void onRowClickForList(List<TableRowAndJsonObject> tableRowsAndJsonObjects) {
        for (TableRowAndJsonObject tableRowAndJsonObject : tableRowsAndJsonObjects) {
            setOnRowClick(getView().getContext(), tableRowAndJsonObject.tableRow, tableRowAndJsonObject.tournament);
        }
    }

    private void setOnRowClick(final Context context, TableRow row, final JSONObject tournament) {
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

}
