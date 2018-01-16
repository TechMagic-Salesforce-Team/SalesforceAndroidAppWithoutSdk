package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.mail.GMailSender;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.mail.SendEmailMainThread;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Tournament__c;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPageAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);
        //mViewPager.setAdapter(mSectionsPagerAdapter);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Sf_Rest_Syncronizer.currentActivity = this;
        Sf_Rest_Syncronizer sf_rest_syncronizer = Sf_Rest_Syncronizer.getInstance();

        Button loginLogoutButton = findViewById(R.id.login_logout_button);;

        if (PlayerSession.currentPlayer == null) {
                loginLogoutButton.setText("Login");
        } else {
                loginLogoutButton.setText("Logout");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class Tab1Fragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private Spinner statusSpinner;
        private Spinner formatSpinner;
        private Spinner typeSpinner;
        private EditText nameInput;
        private TableLayout tableLayout;
        private Map<String, JSONObject> tournamentsSync = new HashMap<>();

        public Tab1Fragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Tab1Fragment newInstance(int sectionNumber) {
            Tab1Fragment fragment = new Tab1Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.tab1_fragment, container, false);

            if (view == null) {
                try {
                    Thread.sleep(1000);
                    onCreateView(inflater, container, savedInstanceState);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            tableLayout = view.findViewById(R.id.home_layout_table);
            statusSpinner = view.findViewById(R.id.status_tournament_spinner);
            typeSpinner = view.findViewById(R.id.type_tournament_input);
            formatSpinner = view.findViewById(R.id.format_tournament_input);
            nameInput = view.findViewById(R.id.name_tournament_input);

            addItemsToSpinner(statusSpinner, Arrays.asList("Upcoming", "Current", "Completed"), InputType.S);
            addItemsToSpinner(formatSpinner, Arrays.asList("1 x 1", "2 x 2"), InputType.F);
            addItemsToSpinner(typeSpinner, Arrays.asList("RR", "SE", "DE"), InputType.T);


            setTheSameOnChangeToSpinners(statusSpinner);
            setTheSameOnChangeToSpinners(typeSpinner);
            setTheSameOnChangeToSpinners(formatSpinner);

            nameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub
                    clearTable();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        findTournamentsByInputsAndFillTable();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            Button button = (Button) view.findViewById(R.id.load_tournaments_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    clearTable();
                    tryLoadTournaments();
                }
            });

            tryLoadTournaments();
            return view;
        }

        public void setTheSameOnChangeToSpinners(Spinner spinner){
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // your code here
                    try {
                        clearTable();
                        findTournamentsByInputsAndFillTable();
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

        public void tryLoadTournaments() {
            if (Sf_Rest_Syncronizer.getInstance().getAuthSettings() != null && Sf_Rest_Syncronizer.getInstance().getVersionNumber() != null) {
                loadAllTournamentsToTheTable("SELECT+Id,Name,Status__c,Format__c,Type__c+" +
                        "from+Tournament__c");
            } else {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setMessage(getString(R.string.bad_wifi_connection))
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                            }
//                        });
//                builder.create();
//                builder.show();
                try {
                    Thread.sleep(1000);
                    tryLoadTournaments();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void addItemsToSpinner(Spinner spinner, List<String> items, InputType inputType) {
            List<String> spinnerItems = new ArrayList<String>();
            spinnerItems.add(inputType.toString());
            spinnerItems.addAll(items);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, spinnerItems);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);
        }


        private void clearTable(){
            tableLayout.removeViews(4,tableLayout.getChildCount()-4);
        }

        private void loadAllTournamentsToTheTable(String soql) {
            tournamentsSync.clear();
            OkHttpClient client = new OkHttpClient();
            String url = Sf_Rest_Syncronizer.getInstance().getAuthSettings().getInstance_url() +
                    "/services/data/v" + Sf_Rest_Syncronizer.getInstance().getVersionNumber() + "/query?q="
                    + soql;
            final Request request = new Request.Builder()
                    .url(url).addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer "+ Sf_Rest_Syncronizer.getInstance().getACCESS_TOKEN())
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject tournamentsContainer = new JSONObject(body);
                                JSONArray tournaments = tournamentsContainer.getJSONArray("records");

                                for (int i = 0; i < tournaments.length(); i++) {
                                    JSONObject tournament = tournaments.getJSONObject(i);
                                    tournamentsSync.put(tournament.getString("Id"), tournament);
                                    addTableRow(tournament);
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

        private void addTableRow(JSONObject jsonObject) throws JSONException, InterruptedException {
            TableRow tableRow = new TableRow(getActivity());
            tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tableRow.addView(makeColumn((String) jsonObject.get("Name")),0);

            TextView typeText = makeColumn((String) ""+jsonObject.get("Type__c").toString().split(" ")[0].charAt(0)+
                    jsonObject.get("Type__c").toString().split(" ")[1].charAt(0));

            tableRow.addView(typeText,1);
            tableRow.addView(makeColumn((String) jsonObject.get("Format__c")),2);
            tableRow.addView(makeColumn((String) jsonObject.get("Status__c")),3);
            tableRow.setPadding(0,10,0,0);
            setOnRowClick(tableRow, jsonObject);
            tournamentsSync.put(jsonObject.get("Id").toString(), jsonObject);
            tableLayout.addView(tableRow);
        }


        private TextView makeColumn(String text) {
            TextView textView = new TextView(getActivity());
            textView.setText(text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setPadding(10,10,0,0);
            return textView;
        }

        private void setOnRowClick(TableRow row, final JSONObject tournament){
            row.setClickable(true);  //allows you to select a specific row
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TournamentSession.tournamentSelected = (Tournament__c)
                            new Gson().fromJson(String.valueOf(tournament), Tournament__c.class);
                    Intent tournamentActivity = new Intent(getContext(), LoginActivity.class);
                    startActivity(tournamentActivity);
                }
            });
        }

        private void findTournamentsByInputsAndFillTable() throws JSONException, InterruptedException {
            String name = nameInput.getText().toString();

            String type = typeSpinner.getSelectedItem().toString();

            switch (type) {
                case "RR" : type = "Round Robin"; break;
                case "SE" : type = "Single Elimination"; break;
                case "DE" : type = "Double Elimination"; break;
            }

            String format = formatSpinner.getSelectedItem().toString();
            String status = statusSpinner.getSelectedItem().toString();

            for (String key : tournamentsSync.keySet()) {
                if (tournamentsSync.get(key).get("Name").toString().toUpperCase().contains(name.toUpperCase())
                        &&
                        (tournamentsSync.get(key).get("Type__c").toString().equals(type) ||
                                type.equals(InputType.T.toString()))
                        &&
                        (tournamentsSync.get(key).get("Status__c").toString().equals(status) ||
                                status.equals(InputType.S.toString()))
                        &&
                        (tournamentsSync.get(key).get("Format__c").toString().equals(format) ||
                                format.equals(InputType.F.toString()))
                        ) {
                    addTableRow(tournamentsSync.get(key));
                }
            }
        }


        enum InputType {
            S, F, T
        }

    }



    public static class Tab2Fragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section1_number";

        public Tab2Fragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Tab1Fragment newInstance(int sectionNumber) {
            Tab1Fragment fragment = new Tab1Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.tab2_fragment, container, false);
            return view;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPageAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "Tab1");
        adapter.addFragment(new Tab2Fragment(), "Tab2");
        viewPager.setAdapter(adapter);
    }

    public void onLoginLogoutClick(View v) {
        System.out.println("btn access token clicked: "+Sf_Rest_Syncronizer.getInstance().
                getAuthSettings()+", and vn: "+Sf_Rest_Syncronizer.getInstance().getVersionNumber()+
                ", player in session: "+PlayerSession.currentPlayer);

        //new Thread(new SendEmailMainThread(new String[] {"rgaidukevich9@gmail.com", "rostyslav.haydukevych@techmagic.co"})).start();

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
