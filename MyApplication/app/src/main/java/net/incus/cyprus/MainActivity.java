package net.incus.cyprus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<ScheduleObject> scheduleList;
    private ScheduleAdapter scheduleSync;
    private DataGrabTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showProgress(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        this.assetINIT();
    }

    private void assetINIT(){
        //<--TODO COLORS-->

        View mProgressView = findViewById(R.id.progressBar);
        ((ProgressBar)mProgressView).getIndeterminateDrawable().setColorFilter(Color.parseColor("#e31837"), PorterDuff.Mode.MULTIPLY);

        //<--TODO FLOATING BUTTON-->

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE); //disable it for now

        //<--TODO IMAGE BUTTON (UPDATEBUTTON)-->

        ((ImageButton)findViewById(R.id.imageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                mTask = new DataGrabTask();
                mTask.execute();
            }
        });

        //<--TODO Start Here-->

        mTask = new DataGrabTask();
        mTask.execute();
    }

    private final String[] getWeekDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private void accumulate() throws JSONException {
        if(DataStore.get("Weekdays") == null || DataStore.get("Schedules") == null)
            return;
        //object initialization
        scheduleList = new ArrayList<ScheduleObject>();
        scheduleSync = new ScheduleAdapter(this, scheduleList);
        ListView listView = (ListView) findViewById(R.id.scheduleView);
        listView.setAdapter(scheduleSync);

        JSONArray weekDays = new JSONArray((String)DataStore.get("Weekdays"));
        JSONArray schedules = new JSONArray((String)DataStore.get("Schedules"));
        JSONObject NS; //nest_schedule --> NS
        HashMap<String, String> MS; //map_schedule --> MS
        HashMap<String, String> _MS; //for multiple schedulings
        //get day of the week first
        for(int i = 0; i < weekDays.length(); i++){
            NS = weekDays.getJSONObject(i);
            for(String day : getWeekDays){
                if(NS.has(day)){
                    DataStore.append(getApplicationContext(), day, NS.getString(day), true);
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        final String dayOfTheWeek;
        if(!DataStore.exists("SelectedDay")){
            dayOfTheWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.getTime());
            DataStore.append(getApplicationContext(), "SelectedDay", dayOfTheWeek, false);
        }else{
            dayOfTheWeek = (String)DataStore.get("SelectedDay");
        }
        ((TextView)findViewById(R.id.textView4)).setText((String)DataStore.get(dayOfTheWeek));
        ((TextView)findViewById(R.id.textView3)).setText(" " + dayOfTheWeek.toUpperCase() + " ");

        //begin datamining
        for(int i = 0; i < schedules.length(); i++){
            NS = schedules.getJSONObject(i);
            MS = new HashMap<String, String>();
            for(String day : getWeekDays){
                if(NS.has(day))
                    if(NS.getString(day).contains("<split>")) {
                        for (int j = 0; j < (NS.getString(day).split("<split>").length - 1); j++) {
                            _MS = new HashMap<String, String>();
                            _MS.put(day, NS.getString(day).split("<split>")[j + 1]);
                            scheduleList.add(new ScheduleObject(NS.getString("name"), _MS, NS.getString("total")));
                        }
                        MS.put(day, NS.getString(day).split("<split>")[0]);
                    }else {
                        MS.put(day, NS.getString(day));
                    }
                else
                    MS.put(day, "");
            }
            scheduleList.add(new ScheduleObject(NS.getString("name"), MS, NS.getString("total")));
        }

        Collections.sort(scheduleList, new Comparator<ScheduleObject>() {
            @Override
            public int compare(ScheduleObject s1, ScheduleObject s2) {
                if(s1.getSchedule(dayOfTheWeek) == "" ||
                        s1.getSchedule(dayOfTheWeek) == null){
                    return 1;
                }else if(s2.getSchedule(dayOfTheWeek) == "" ||
                        s2.getSchedule(dayOfTheWeek) == null){
                    return -1;
                }
                if(s1.getName() == s2.getName())
                    return 1;
                return s1.getName().compareToIgnoreCase(s2.getName());
            }
        });
        showProgress(false);
        scheduleSync.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if(show){
            findViewById(R.id.toolbar).setEnabled(false);
            findViewById(R.id.imageButton).setEnabled(false);
        }else{
            findViewById(R.id.toolbar).setEnabled(true);
            findViewById(R.id.imageButton).setEnabled(true);
        }
        final View mProgressView = findViewById(R.id.progressBar);
        final View mScheduleView = findViewById(R.id.scheduleView);
        final View mDayView = findViewById(R.id.textView3);
        final View mWeekDayView = findViewById(R.id.textView4);
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mScheduleView.setVisibility(show ? View.GONE : View.VISIBLE);
            mScheduleView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mScheduleView.setVisibility(show ? View.GONE : View.VISIBLE);
                    mDayView.setVisibility(show ? View.GONE : View.VISIBLE);
                    mWeekDayView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mScheduleView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDayView.setVisibility(show ? View.GONE : View.VISIBLE);
            mWeekDayView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //if (id == R.id.nav_home){

        switch(id){
            case R.id.nav_sunday:
                reDraw("Sunday");
                break;
            case R.id.nav_monday:
                reDraw("Monday");
                break;
            case R.id.nav_tuesday:
                reDraw("Tuesday");
                break;
            case R.id.nav_wednesday:
                reDraw("Wednesday");
                break;
            case R.id.nav_thursday:
                reDraw("Thursday");
                break;
            case R.id.nav_friday:
                reDraw("Friday");
                break;
            case R.id.nav_saturday:
                reDraw("Saturday");
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void reDraw(String weekDay){
        DataStore.append(getApplicationContext(), "SelectedDay", weekDay, false);
        showProgress(true);
        try {
            accumulate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void errNotify(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public class DataGrabTask extends AsyncTask<Void, Void, Boolean> {

        URL obj;
        HttpURLConnection con;
        BufferedReader in;

        StringBuffer Response;
        String inputLine;

        String errMsg = "Error occured while updating information";

        DataGrabTask() {}

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //instantiate http connection
                obj = new URL("http://centipacapp.azurewebsites.net/cyprus.php");
                con = (HttpURLConnection) obj.openConnection();

                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                Response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    Response.append(inputLine);
                }
                in.close();
                //check if data grabbed, if not fail the data grab
                if(!Response.toString().contains("2017"))
                    return false;
                String[] _temp = Response.toString().split("\\[");
                //store data locally & as cache
                DataStore.append(getApplicationContext(), "Weekdays", '[' + _temp[1].split("]")[0] + "]", true);
                DataStore.append(getApplicationContext(), "Schedules", '[' + _temp[2].split("]")[0] + "]", true);
                // Simulate network access. Also to be a dick to the user
                Thread.sleep(2000);
                return true;
            }catch(Exception e){
                Log.e("GrabActivity", "Exception Caught", e);
                Log.e("GrabActivity", e.getMessage(), e);
                this.errMsg = "Unable to update data (No internet)";
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mTask = null;

            if (success) {
                try {
                    accumulate();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //cache mode
                errNotify(this.errMsg);
                SharedPreferences sharedpreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                if((sharedpreferences.getString("Weekdays", "") != "") &&
                        (sharedpreferences.getString("Schedules", "") != ""))
                    try {
                        DataStore.append(getApplicationContext(), "Weekdays", sharedpreferences.getString("Weekdays", ""), false);
                        DataStore.append(getApplicationContext(), "Schedules", sharedpreferences.getString("Schedules", ""), false);
                        accumulate();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            errNotify("Task cancelled");
        }
    }
}
