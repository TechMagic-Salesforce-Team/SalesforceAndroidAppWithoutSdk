package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.R;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.PlayerSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by rostyk_haidukevych on 1/19/18.
 */

public class PlayerProfileActivity extends Activity {
    private Player__c playerSelected = null;
    private static final String ARG_SECTION_NUMBER = "section1_number";
    private Spinner profile_statusSpinner;
    private Spinner profile_formatSpinner;
    private Spinner profile_typeSpinner;
    private EditText profile_nameInput;
    private TableLayout profile_tableLayout;
    private Map<String, JSONObject> profile_tournamentsSync = new HashMap<>();
    private TextView nameField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_profile);

        ImageView playerImg = findViewById(R.id.profile_image);
        playerSelected = PlayerSession.playerSelected;

        profile_tableLayout =  findViewById(R.id.profile_tournaments_table);
        profile_statusSpinner = findViewById(R.id.status_profile_tournament_spinner);
        profile_formatSpinner = findViewById(R.id.format_profile_tournament_input);
        profile_typeSpinner = findViewById(R.id.type_profile_tournament_input);
        profile_nameInput = findViewById(R.id.name_profile_tournament_input);
        nameField = findViewById(R.id.player_name);
        nameField.setText("Name: "+playerSelected.Name);

        if (playerSelected.Image__c==null) {
            PlayerSession.playerBitmaps.put(playerSelected.Id, null);
            playerImg.setImageResource(R.drawable.default_player);
        } else {
            if (PlayerSession.playerBitmaps.get(playerSelected.Id) == null) {
                try {
                    AsyncTask<String, Void, Bitmap> task = new BitmapImgAsyncTask();
                    task.execute(playerSelected.Image__c);
                    PlayerSession.playerBitmaps.put(playerSelected.Id, task.get());
                    playerImg.setImageBitmap(PlayerSession.playerBitmaps.get(playerSelected.Id));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                playerImg.setImageBitmap(PlayerSession.playerBitmaps.get(playerSelected.Id));
            }
        }

        ProfileLayoutDataLoader profileLayoutDataLoader = new ProfileLayoutDataLoader(
                playerImg,
                profile_statusSpinner,
                profile_formatSpinner,
                profile_typeSpinner,
                profile_nameInput,
                profile_tableLayout,
                this,
                null,
                playerSelected,
                null,
                null,
                (Button) findViewById(R.id.load_profile_tournaments_button)
        );
        profileLayoutDataLoader.loadAndInitData(false);

    }
}
