package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;

/**
 * Created by rostyk_haidukevych on 1/15/18.
 */

public class PlayerSession {
    private Player__c currentPlayer;

    public PlayerSession(Player__c currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Player__c getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player__c currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
