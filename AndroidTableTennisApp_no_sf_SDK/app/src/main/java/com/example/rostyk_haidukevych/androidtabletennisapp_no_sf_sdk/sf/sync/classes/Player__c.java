package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes;

/**
 * Created by rostyk_haidukevych on 1/15/18.
 */

public class Player__c {
    public String id;
    public String name;
    public String email;
    public String password;
    public ROLE role;

    public static enum ROLE {
        USER, ADMIN
    }

}
