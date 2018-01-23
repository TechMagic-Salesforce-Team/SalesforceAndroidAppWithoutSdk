package com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.InputFilter;
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
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Player__c;
import com.example.rostyk_haidukevych.androidtabletennisapp_no_sf_sdk.sf.sync.classes.Tournament__c;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static Sf_Rest_Syncronizer restSyncronizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.


        // Set up the ViewPager with the sections adapter.


//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                TableLayout playersTable = findViewById(R.id.players_table);
//                System.out.println("Child count : "+playersTable.getChildCount());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });


        Sf_Rest_Syncronizer.currentActivity = this;
        restSyncronizer = Sf_Rest_Syncronizer.getInstance();

        while (Sf_Rest_Syncronizer.playersSyncCount == null || Sf_Rest_Syncronizer.playersSyncCount > PlayerSession.allPlayersSync.size()) {}

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Button loginLogoutButton = findViewById(R.id.login_logout_button);;

        if (PlayerSession.currentPlayer == null) {
            loginLogoutButton.setText("Login");
        } else {
            loginLogoutButton.setText("Logout");
        }

        System.out.println("Main activity is created");


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

        public Tab1Fragment() {}

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

            RelativeLayout layout = (RelativeLayout)  view.findViewById(R.id.home_content_layout);
            Button button = layout.findViewById(R.id.load_tournaments_button);

//            while (
//                    tableLayout==null
//                    || statusSpinner == null
//                    || typeSpinner == null
//                    || formatSpinner == null
//                    || nameInput == null
//                    || button==null) {}

            ProfileLayoutDataLoader profileLayoutDataLoader = new ProfileLayoutDataLoader(
                    null,
                    statusSpinner,
                    formatSpinner,
                    typeSpinner,
                    nameInput,
                    tableLayout,
                    getActivity(),
                    null,
                    null,
                    null,
                    null,
                    button
            );
            profileLayoutDataLoader.loadAndInitData(true);

//            Button button = (Button) view.findViewById(R.id.load_tournaments_button);
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v)
//                {
//                    clearTable(tableLayout);
//                    tryLoadTournaments();
//                }
//            });
//
//            clearTable(tableLayout);
//            tryLoadTournaments();

            System.out.println("Tab 1 is created");
            return view;
        }

        public void onRowClickForList(List<TableRowAndJsonObject> tableRowsAndJsonObjects) {
            for (TableRowAndJsonObject tableRowAndJsonObject : tableRowsAndJsonObjects) {
                setOnRowClick(getContext(), tableRowAndJsonObject.tableRow, tableRowAndJsonObject.tournament);
            }
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



    public static class Tab2Fragment extends Fragment implements View.OnClickListener {
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
        private TextView nameField;
        public Tab2Fragment() {}


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.tab2_fragment, container, false);


            if (view == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            nameField = view.findViewById(R.id.player_name);
            profileImage = view.findViewById(R.id.profile_image);
            RelativeLayout layout = view.findViewById(R.id.profile_layout);
            RelativeLayout layoutIfNotLoggedIn = view.findViewById(R.id.layout_no_user);



            if (PlayerSession.currentPlayer != null) {
                layout.setVisibility(View.VISIBLE);
                layoutIfNotLoggedIn.setVisibility(View.GONE);
                nameField.setVisibility(View.VISIBLE);
                nameField.setText("Name: "+PlayerSession.currentPlayer.Name);

                String IMAGE_URL = PlayerSession.currentPlayer.Image__c;

                //!= null ?
                //PlayerSession.currentPlayer.Image__c
                //:
                //"https://i.pinimg.com/736x/8d/f7/42/8df742ad90ca58d3068fb3d7d2ba250f--art-clipart-art-images.jpg";
                //"http://4.bp.blogspot.com/-o9jPOvHK3FM/UH4P8W9PKLI/AAAAAAAABfg/DofoLsf5nHY/s1600/Nature-Blue-water-spiritual.jpg";
                //"https://static.pexels.com/photos/248797/pexels-photo-248797.jpeg";
                //"https://techmagic-table-tennis-developer-edition.eu11.force.com/servlet/servlet.FileDownload?file=0150Y000001ajjfQAA";
                //System.out.println("Image: "+PlayerSession.currentPlayer.Image__c);



                profile_tableLayout = view.findViewById(R.id.profile_tournaments_table);
                profile_statusSpinner = view.findViewById(R.id.status_profile_tournament_spinner);
                profile_formatSpinner = view.findViewById(R.id.format_profile_tournament_input);
                profile_typeSpinner = view.findViewById(R.id.type_profile_tournament_input);
                profile_nameInput = view.findViewById(R.id.name_profile_tournament_input);
                Button button = layout.findViewById(R.id.load_profile_tournaments_button);


//                while (
//                        profile_tableLayout==null
//                                || profile_statusSpinner == null
//                                || profile_typeSpinner == null
//                                || profile_formatSpinner == null
//                                || profile_nameInput == null
//                                || button==null) {}


                System.out.println("Profile image == null =>"+(profileImage==null)+
                        ", status spinner == null => "+(profile_statusSpinner==null)+
                        ", type spinner == null =>"+(profile_typeSpinner==null)+
                        ", format spinner == null =>"+(profile_formatSpinner==null)+
                        ", name input == null =>"+(profile_nameInput==null)+
                        ", button == null =>"+(button==null)
                );

                ProfileLayoutDataLoader profileLayoutDataLoader = new ProfileLayoutDataLoader(
                        profileImage,
                        profile_statusSpinner,
                        profile_formatSpinner,
                        profile_typeSpinner,
                        profile_nameInput,
                        profile_tableLayout,
                        getActivity(),
                        null,
                        PlayerSession.currentPlayer,
                        null,
                        null,
                        button
                );

                profileLayoutDataLoader.loadAndInitData(false);

                if (IMAGE_URL != null) {
                    AsyncTask<String, Void, Bitmap> task = new BitmapImgAsyncTask().execute(IMAGE_URL);
                    if (PlayerSession.playerBitmaps.get(PlayerSession.currentPlayer.Id) == null) {
                        System.out.println("NOT NULL");
                        try {
                            profileImage.setImageBitmap(task.get());
                            while (task.get() == null) {
                            }
                            PlayerSession.playerBitmaps.put(PlayerSession.currentPlayer.Id, task.get());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    } else {
                        profileImage.setImageBitmap(PlayerSession.playerBitmaps.get(PlayerSession.currentPlayer.Id));
                    }
                } else {
                    profileImage.setImageResource(R.drawable.default_player);
                }
            } else {
                layout.setVisibility(View.GONE);
                layoutIfNotLoggedIn.setVisibility(View.VISIBLE);
                nameField.setVisibility(View.GONE);
            }
            return view;
        }



        @Override
        public void onClick(View v) {
            System.out.println("Clicked");
        }
    }



    public static class Tab3Fragment extends Fragment {
        private TableLayout playersTable;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view =  inflater.inflate(R.layout.tab3_fragment, container, false);

            while (view == null
                    || Sf_Rest_Syncronizer.playersSyncCount==null
                    || Sf_Rest_Syncronizer.playersSyncCount > PlayerSession.playerBitmaps.size()) {}

            playersTable = view.findViewById(R.id.players_table);
            int counter = 0;

            if (PlayerSession.allPlayersSync != null) {
                for (Player__c player : PlayerSession.allPlayersSync.values()) {
                    //if (counter >= 10) break;
                    addTableRow(player);
                    counter++;
                    System.out.println("counter : " + counter);
                }
            }

            System.out.println("Children count: "+playersTable.getChildCount());
            return view;
        }


        private void addTableRow(final Player__c player){
            TableRow tableRow = new TableRow(getActivity());
            //final String DEFAULT_IMAGE_ADDRESS = "https://cdn3.iconfinder.com/data/icons/rcons-user-action/32/boy-512.png";

            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    150,
                    getResources().getDisplayMetrics());
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    120,
                    getResources().getDisplayMetrics());

            TableRow.LayoutParams lp = new TableRow.LayoutParams(width, height);
            lp.setMargins(0,10,0,0);


            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(R.drawable.all_players);
            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setLayoutParams(lp);
            imageView.setImageResource(R.drawable.default_player);

//            if (PlayerSession.playerBitmaps.get(player.Id)==null) {
//                imageView.setImageResource(R.drawable.default_player);
//            } else {
//                imageView.setImageBitmap(PlayerSession.playerBitmaps.get(player.Id));
//            }

            System.out.println("Player name : "+player.Name+", image : "+PlayerSession.playerBitmaps.get(player.Id));



            tableRow.addView(imageView);
            TextView textView = new TextView(getActivity());
            textView.setText("\n\n     " + player.Name);
            textView.setTextSize(15);
            tableRow.addView(textView);


            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println(player.Name);
                    PlayerSession.playerSelected = player;
                    Intent playerActivity = new Intent(getContext(), PlayerProfileActivity.class);
                    startActivity(playerActivity);
                }
            });

            playersTable.addView(tableRow);
        }

    }

    public static class Tab4Fragment extends Fragment {
        private ViewPager viewPager;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tab4_fragment, container, false);

            if (view == null) {
                try {
                    Thread.sleep(1000);
                    onCreateView(inflater, container, savedInstanceState);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            viewPager = (ViewPager) view.findViewById(R.id.admin_container);
            viewPager.setOffscreenPageLimit(2);
            setupViewPagerForAdminTab(viewPager);


            TabLayout tabLayout = (TabLayout) view.findViewById(R.id.admin_tabs);
            tabLayout.setupWithViewPager(viewPager);

            return view;
        }


        private void setupViewPagerForAdminTab(ViewPager viewPager) {
            SectionsPageAdapter adapter = new SectionsPageAdapter(getChildFragmentManager());
            adapter.addFragment(new AdminPlayersFragment(), "Players");
            adapter.addFragment(new AdminTournamentsFragment(), "Tournaments");
            viewPager.setAdapter(adapter);
        }


        public static class AdminPlayersFragment extends Fragment {
            private TableLayout tableLayout;

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.players_admin_fragment, container, false);

                if (view==null) {
                    try {
                        Thread.sleep(1000);
                        onCreateView(inflater,container,savedInstanceState);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                tableLayout = view.findViewById(R.id.admin_players_table);

                Button savePlayersAccessChanges = view.findViewById(R.id.save_players_changes);

                savePlayersAccessChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("Changes");
                    }
                });

                for (Player__c player : PlayerSession.allPlayersSync.values()) {
                    addTableRow(player);
                }

                return view;
            }

            private void addTableRow(Player__c player){
                TableRow tableRow = new TableRow(getActivity());

                TextView textView = new TextView(getActivity());
                textView.setText(player.Name);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,15);
                tableRow.addView(textView);

                Spinner spinner = new Spinner(getActivity());
                List<String> spinnerItems = new ArrayList<String>();
                if (player.IsManager__c) {
                    spinnerItems.add("Manager");
                    spinnerItems.add("Simple");
                } else {
                    spinnerItems.add("Simple");
                    spinnerItems.add("Manager");
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, spinnerItems);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);
                tableRow.addView(spinner);

                tableLayout.addView(tableRow);
            }

        }

        public static class AdminTournamentsFragment extends Fragment {
            private static TableLayout tableLayout;

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.tournaments_admin_fragment, container, false);

                if (view==null) {
                    try {
                        Thread.sleep(1000);
                        onCreateView(inflater,container,savedInstanceState);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



                System.out.println("Fragment AdminTournamentsFragment was created"+TournamentSession.allTournamentsSync.get("a010Y00000YXbJPQA1").Status__c);

                tableLayout = view.findViewById(R.id.admin_layout_tournaments_table);
                System.out.println("Size: "+tableLayout.getChildCount());

                for (Tournament__c tournament : TournamentSession.allTournamentsSync.values()) {
                    if (tournament.Status__c.equals("Upcoming")) {
                        addTableRow(tournament);
                    }
                }

                Button createTournamentBtn = view.findViewById(R.id.create_tournament_button);
                createTournamentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent createTournamentActivity = new Intent(getActivity(), CreateTournamentActivity.class);
                        startActivity(createTournamentActivity);
                    }
                });

                return view;
            }


            private void addTableRow(Tournament__c tournament){
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        40, getActivity().getResources().getDisplayMetrics());

                TableLayout.LayoutParams lp = new TableLayout.LayoutParams
                        (TableLayout.LayoutParams.WRAP_CONTENT, height);
                lp.setMargins(0,20,0,0);
                TableRow tableRow = new TableRow(getActivity());
                tableRow.setLayoutParams(lp);
                tableRow.addView(makeColumn(tournament.Name, getActivity()));
                String type = ""+tournament.Type__c.split(" ")[0].charAt(0)
                        +tournament.Type__c.split(" ")[1].charAt(0);
                tableRow.addView(makeColumn(type, getActivity()));
                tableRow.addView(makeColumn(tournament.Format__c, getActivity()));
                tableRow.addView(makeButtonColumn(tournament.Id, ButtonType.START, getActivity(), tableRow));
                tableRow.addView(makeButtonColumn(tournament.Id, ButtonType.DELETE, getActivity(), tableRow));
                tableLayout.addView(tableRow);
            }

            private static TextView makeColumn(String text, Activity activity) {
                TextView textView = new TextView(activity);
                textView.setText(text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setPadding(10,10,0,0);
                return textView;
            }

            private static TextView makeButtonColumn(final String tournamentId, ButtonType type, final Activity activity, final TableRow tableRow) {
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        35, activity.getResources().getDisplayMetrics());

                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        80, activity.getResources().getDisplayMetrics());


                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams
                        (width, height);
                layoutParams.setMargins(10,0,0,0);
                Button button = new Button(activity);
                button.setLayoutParams(layoutParams);
                button.setText(type.toString());
                button.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10);
                button.setTextColor(Color.WHITE);
                button.setBackgroundResource
                        (type == ButtonType.START ? R.color.sf__start_button : R.color.sf__warning_color);

                if (type.equals(ButtonType.START)) {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println("Start tournament "+tournamentId);
                            OkHttpClient client = new OkHttpClient();
                            Sf_Rest_Syncronizer restSyncronizer = Sf_Rest_Syncronizer.getInstance();
                            MediaType Encoded = MediaType.parse("application/json");
                            RequestBody body = RequestBody.create(Encoded,
                                    "{\"tournamentId\":\""+tournamentId+"\"}");
                            final Request request =
                                    new Request.Builder()
                                            .url(restSyncronizer.getAuthSettings().getInstance_url()
                                                    +"/services/apexrest/api/post/tournaments/start"
                                                    )
                                            .addHeader("Content-Type", "application/json")
                                            .addHeader("From", restSyncronizer.getClientId())
                                            .addHeader("Authorization", "Bearer "
                                                    + restSyncronizer.getAuthSettings().getAccess_token()
                                            )
                                            .post(body)
                                            .build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    System.out.println("Error while trying to start tournament "+e.getMessage());
                                }

                                @Override
                                public void onResponse(Call call, final Response response) throws IOException {
                                    final String[] responseBody = {response.body().string()};
                                    System.out.println("Response body (after trying to start tournament) => "+ responseBody[0]);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (response.code()==200) {
                                                responseBody[0] = responseBody[0].substring(1, responseBody[0].length()-1);
                                                try {
                                                    System.out.println("rb: "+ responseBody[0]);
                                                    System.out.println("Code == 200");
                                                    JSONObject jsonObject = new JSONObject(responseBody[0]);
                                                    //if (jsonObject.get("status")=="Current") {
                                                        tableLayout.removeView(tableRow);
                                                    System.out.println("Children count (before childAt) : "+tableLayout.getChildCount());
                                                        if (tableLayout.getChildCount()==1) {
                                                            tableLayout.removeView(tableLayout.getChildAt(0));
                                                        }
//                                                    Tournament__c tournament__c = TournamentSession.allTournamentsSync.get(tournamentId);
//                                                        tournament__c.Status__c = "Current";
//                                                    TournamentSession.allTournamentsSync.put(tournamentId, tournament__c);
                                                    //}
                                                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                                                    dialog.setCancelable(false);
                                                    dialog.setTitle("Congratulations");
                                                    dialog.setMessage("Tournament '"+
                                                            TournamentSession.allTournamentsSync.get(tournamentId).Name+"' has been started");
                                                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                                                            activity.startActivity(intent);
                                                            dialog.cancel();
                                                        }
                                                    });
                                                    dialog.show();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });

                                }
                            });
                        }
                    });
                } else {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println("Start tournament "+tournamentId);
                        }
                    });
                }

                return button;
            }


            enum ButtonType {
                START,DELETE
            }

        }


    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class SectionsPageAdapter extends FragmentPagerAdapter {

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
        adapter.addFragment(new Tab3Fragment(), "Players");
        if (PlayerSession.currentPlayer != null && PlayerSession.currentPlayer.IsManager__c) {
            adapter.addFragment(new Tab4Fragment(), "Admin");
        }
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


    private static void clearTable(TableLayout tableLayout){
        tableLayout.removeViews(4,tableLayout.getChildCount()-4);
    }


    static class TableRowAndJsonObject {
        TableRow tableRow;
        JSONObject tournament;

        public TableRowAndJsonObject() {}
    }

    enum TabFragmentType {
        TAB_1_FRAGMENT,
        TAB_2_FRAGMENT;
    }

}
