package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rostyk_haidukevych on 1/15/18.
 */

public class PlayerSession {
    public static Player__c currentPlayer = null;
    public static Map<String, Player__c> allPlayersSync = new HashMap<>();
}
