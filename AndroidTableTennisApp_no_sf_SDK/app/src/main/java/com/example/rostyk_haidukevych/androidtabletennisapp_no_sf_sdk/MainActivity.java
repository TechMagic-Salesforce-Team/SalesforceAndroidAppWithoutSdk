package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.PlayerSession;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sessions.TournamentSession;
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

            addItemsToSpinner(statusSpinner, Arrays.asList("Upcoming", "Current", "Completed"), InputType.S, getActivity());
            addItemsToSpinner(formatSpinner, Arrays.asList("1 x 1", "2 x 2"), InputType.F, getActivity());
            addItemsToSpinner(typeSpinner, Arrays.asList("RR", "SE", "DE"), InputType.T, getActivity());


            setTheSameOnChangeToSpinners(
                    nameInput,
                    typeSpinner,
                    formatSpinner,
                    statusSpinner,
                    tournamentsSync,
                    getActivity(),
                    tableLayout,
                    this,
                    TabFragmentType.TAB_1_FRAGMENT
                    );


//            setTheSameOnChangeToSpinners(typeSpinner, tableLayout);
//            setTheSameOnChangeToSpinners(formatSpinner, tableLayout);

            nameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub
                    clearTable(tableLayout);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        clearTable(tableLayout);
                        List<TableRowAndJsonObject> tableRowsAndJsonObjects =
                                findTournamentsByInputsAndFillTable(nameInput, typeSpinner, formatSpinner, statusSpinner, tournamentsSync, getActivity(), tableLayout);
                        onRowClickForList(tableRowsAndJsonObjects);
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
                    clearTable(tableLayout);
                    tryLoadTournaments();
                }
            });

            clearTable(tableLayout);
            tryLoadTournaments();
            return view;
        }

        public void onRowClickForList(List<TableRowAndJsonObject> tableRowsAndJsonObjects) {
            for (TableRowAndJsonObject tableRowAndJsonObject : tableRowsAndJsonObjects) {
                setOnRowClick(getContext(), tableRowAndJsonObject.tableRow, tableRowAndJsonObject.tournament);
            }
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



        private void loadAllTournamentsToTheTable(String soql) {
            tournamentsSync.clear();
            clearTable(tableLayout);
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
                                    TableRow newTableRow = addTableRow(tournament, getActivity(), tournamentsSync, tableLayout);
                                    setOnRowClick(getContext(), newTableRow, tournament);
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


        enum InputType {
            S, F, T
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

    }



    public static class Tab2Fragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section1_number";
        private ImageView profileImage;
        private Spinner profile_statusSpinner;
        private Spinner profile_formatSpinner;
        private Spinner profile_typeSpinner;
        private EditText profile_nameInput;
        private TableLayout profile_tableLayout;
        private Map<String, JSONObject> profile_tournamentsSync = new HashMap<>();

        public Tab2Fragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.tab2_fragment, container, false);

            if (view == null) {
                try {
                    Thread.sleep(1000);
                    onCreateView(inflater, container, savedInstanceState);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            profileImage = view.findViewById(R.id.profile_image);
            RelativeLayout layout = view.findViewById(R.id.profile_layout);
            RelativeLayout layoutIfNotLoggedIn = view.findViewById(R.id.layout_no_user);


            if (PlayerSession.currentPlayer != null) {
                layout.setVisibility(View.VISIBLE);
                layoutIfNotLoggedIn.setVisibility(View.GONE);

                String IMAGE_URL = PlayerSession.currentPlayer.Image__c;
                        //!= null ?
                        //PlayerSession.currentPlayer.Image__c
                        //:
                        //"https://i.pinimg.com/736x/8d/f7/42/8df742ad90ca58d3068fb3d7d2ba250f--art-clipart-art-images.jpg";
                        //"http://4.bp.blogspot.com/-o9jPOvHK3FM/UH4P8W9PKLI/AAAAAAAABfg/DofoLsf5nHY/s1600/Nature-Blue-water-spiritual.jpg";
                        //"https://static.pexels.com/photos/248797/pexels-photo-248797.jpeg";
                        //"https://techmagic-table-tennis-developer-edition.eu11.force.com/servlet/servlet.FileDownload?file=0150Y000001ajjfQAA";
                //System.out.println("Image: "+PlayerSession.currentPlayer.Image__c);
                AsyncTask<String, Void, Bitmap> task = new BitmapImgAsyncTask().execute(IMAGE_URL);


                profile_tableLayout = view.findViewById(R.id.profile_tournaments_table);
                profile_statusSpinner = view.findViewById(R.id.status_profile_tournament_spinner);
                profile_formatSpinner = view.findViewById(R.id.format_profile_tournament_input);
                profile_typeSpinner = view.findViewById(R.id.type_profile_tournament_input);
                profile_nameInput = view.findViewById(R.id.name_profile_tournament_input);


                addItemsToSpinner(profile_statusSpinner, Arrays.asList("Upcoming", "Current", "Completed"), Tab1Fragment.InputType.S, getActivity());
                addItemsToSpinner(profile_formatSpinner, Arrays.asList("1 x 1", "2 x 2"), Tab1Fragment.InputType.F, getActivity());
                addItemsToSpinner(profile_typeSpinner, Arrays.asList("RR", "SE", "DE"), Tab1Fragment.InputType.T, getActivity());

//                setTheSameOnChangeToSpinners(
//                        profile_nameInput,
//                        profile_typeSpinner,
//                        profile_formatSpinner,
//                        profile_statusSpinner,
//                        profile_tournamentsSync,
//                        getActivity(),
//                        profile_tableLayout,
//                        this,
//                        TabFragmentType.TAB_2_FRAGMENT
//                        );


                try {
                    profileImage.setImageBitmap(task.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                layout.setVisibility(View.GONE);
                layoutIfNotLoggedIn.setVisibility(View.VISIBLE);
//                Intent loginActivity = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
//                startActivity(loginActivity);

            }
            return view;
        }


        private void tryLoadTournamentsByProfile(){

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
        adapter.addFragment(new Tab1Fragment(), "Home");
        adapter.addFragment(new Tab2Fragment(), "Profile");
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


    public static void addItemsToSpinner(Spinner spinner, List<String> items, Tab1Fragment.InputType inputType, Activity activity) {
        List<String> spinnerItems = new ArrayList<String>();
        spinnerItems.add(inputType.toString());
        spinnerItems.addAll(items);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, spinnerItems);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public static void setTheSameOnChangeToSpinners(final TextView nameInput,
                                                    final Spinner typeSpinner,
                                                    final Spinner formatSpinner,
                                                    final Spinner statusSpinner,
                                                    final Map<String, JSONObject> tournamentsSync,
                                                    final Activity activity,
                                                    final TableLayout tableLayout,
                                                    Fragment fragment,
                                                    TabFragmentType tabFragmentType
                                                    ){
        final Tab1Fragment tab1Fragment;
        final Tab2Fragment tab2Fragment;
        if (tabFragmentType.equals(TabFragmentType.TAB_1_FRAGMENT)) {
            tab1Fragment = (Tab1Fragment) fragment;
            tab2Fragment = null;
        } else {
            tab1Fragment = null;
            tab2Fragment = (Tab2Fragment) fragment;
        }
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                try {
                    clearTable(tableLayout);
                    List<TableRowAndJsonObject> tableRowsAndJsonObjects = findTournamentsByInputsAndFillTable(nameInput, typeSpinner, formatSpinner, statusSpinner, tournamentsSync, activity, tableLayout);
                    tab1Fragment.onRowClickForList(tableRowsAndJsonObjects);
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


        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                try {
                    clearTable(tableLayout);
                    List<TableRowAndJsonObject> tableRowsAndJsonObjects = findTournamentsByInputsAndFillTable(nameInput, typeSpinner, formatSpinner, statusSpinner, tournamentsSync, activity, tableLayout);
                    tab1Fragment.onRowClickForList(tableRowsAndJsonObjects);
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


        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                try {
                    clearTable(tableLayout);
                    List<TableRowAndJsonObject> tableRowsAndJsonObjects = findTournamentsByInputsAndFillTable(nameInput, typeSpinner, formatSpinner, statusSpinner, tournamentsSync, activity, tableLayout);
                    tab1Fragment.onRowClickForList(tableRowsAndJsonObjects);
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


    private static List<TableRowAndJsonObject> findTournamentsByInputsAndFillTable(TextView nameInput,
                                                     Spinner typeSpinner,
                                                     Spinner formatSpinner,
                                                     Spinner statusSpinner,
                                                     Map<String, JSONObject> tournamentsSync,
                                                     Activity activity,
                                                     TableLayout tableLayout
                                                     ) throws JSONException, InterruptedException {
        String name = nameInput.getText().toString();

        String type = typeSpinner.getSelectedItem().toString();

        switch (type) {
            case "RR" : type = "Round Robin"; break;
            case "SE" : type = "Single Elimination"; break;
            case "DE" : type = "Double Elimination"; break;
        }

        String format = formatSpinner.getSelectedItem().toString();
        String status = statusSpinner.getSelectedItem().toString();
        List<TableRowAndJsonObject> tableRowsAndTournaments = new ArrayList<>();

        for (String key : tournamentsSync.keySet()) {
            if (tournamentsSync.get(key).get("Name").toString().toUpperCase().contains(name.toUpperCase())
                    &&
                    (tournamentsSync.get(key).get("Type__c").toString().equals(type) ||
                            type.equals(Tab1Fragment.InputType.T.toString()))
                    &&
                    (tournamentsSync.get(key).get("Status__c").toString().equals(status) ||
                            status.equals(Tab1Fragment.InputType.S.toString()))
                    &&
                    (tournamentsSync.get(key).get("Format__c").toString().equals(format) ||
                            format.equals(Tab1Fragment.InputType.F.toString()))
                    ) {
                TableRow tableRow = addTableRow(tournamentsSync.get(key),activity, tournamentsSync, tableLayout);
                TableRowAndJsonObject tableRowAndJsonObject = new TableRowAndJsonObject();
                tableRowAndJsonObject.tableRow = tableRow;
                tableRowAndJsonObject.tournament = tournamentsSync.get(key);
                tableRowsAndTournaments.add(tableRowAndJsonObject);
            }
        }
        return tableRowsAndTournaments;
    }


    private static TableRow addTableRow(JSONObject jsonObject, Activity activity, Map<String, JSONObject> tournamentsSync, TableLayout tableLayout) throws JSONException, InterruptedException {
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
        tournamentsSync.put(jsonObject.get("Id").toString(), jsonObject);
        tableLayout.addView(tableRow);
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

        public TableRowAndJsonObject() {
        }
    }


    enum TabFragmentType {
        TAB_1_FRAGMENT,
        TAB_2_FRAGMENT;
    }

}
