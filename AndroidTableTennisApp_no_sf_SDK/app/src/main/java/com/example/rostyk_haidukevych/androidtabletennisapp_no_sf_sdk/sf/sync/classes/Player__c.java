package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes;

/**
 * Created by rostyk_haidukevych on 1/15/18.
 */

public class Player__c {
    public String Id;
    public String Name;
    public String Email__c;
    public String Password__c;
    public Boolean IsManager__c;
    public String Status__c;
    public ROLE role;
    public String Image__c;

    public static enum ROLE {
        USER, ADMIN
    }

}
