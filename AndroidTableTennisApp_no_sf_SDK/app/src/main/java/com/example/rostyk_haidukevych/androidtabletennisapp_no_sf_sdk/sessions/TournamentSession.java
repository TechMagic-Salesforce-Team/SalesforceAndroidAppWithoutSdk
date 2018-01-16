package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Tournament__c;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rostyslav on 15.01.2018.
 */

public class TournamentSession {
    public static Tournament__c tournamentSelected = null;
    public static Map<String, Tournament__c> allTournamentsSync = new HashMap<>();
}
