package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.mail;

import android.util.Log;

/**
 * Created by rostyk_haidukevych on 1/16/18.
 */

public class SendEmailMainThread implements Runnable{
    private String recipients = "";
    private final String senderEmail = "techmagic.tennis@gmail.com";
    private final String senderPassword = "hjcnbrgo9";

    public SendEmailMainThread(String [] recipientsArr) {
        for (String recipient : recipientsArr) {
            recipients+=recipient+",";
        }
        if (recipients.length() > 0) {
            recipients = recipients.substring(0, recipients.length()-1);
        }
        System.out.println("Recipients : "+recipients);
    }

    @Override
    public void run() {
        try {
            GMailSender sender = new GMailSender(this.senderEmail,
                    this.senderPassword);
            sender.sendMail("Hello from JavaMail", "Body from JavaMail",
                    this.senderEmail, this.recipients);
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }
}
