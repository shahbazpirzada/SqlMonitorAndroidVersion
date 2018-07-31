package com.example.stech.printercloudapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.multidex.MultiDex;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stech.printercloudapp.BackendService.BackendIntentService;
import com.example.stech.printercloudapp.JobService.JobSchedularService;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import io.hypertrack.smart_scheduler.SmartScheduler;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    public String str;
    JobScheduler mJobScheduler;

   public static TextView PrinterDataText;

   TextView textView,CMsg,PMsg,TextViewStatus,TextViewStatus2;
    FirebaseJobDispatcher dispatcher;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MultiDex.install(getApplicationContext());
        PrinterDataText = (TextView) findViewById(R.id.textView);
        CMsg = (TextView)findViewById(R.id.CMsg);
        PMsg = (TextView)findViewById(R.id.PMsg);
        TextViewStatus = (TextView)findViewById(R.id.textView9);
        TextViewStatus2 = (TextView)findViewById(R.id.textView8);



        //nevigations
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        textView = (TextView) findViewById(R.id.textStatus);
        // Create a new dispatcher using the Google Play driver.
         dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        if(!getSharedPreferences("APP_PREFERENCE", Activity.MODE_PRIVATE).getBoolean("IS_ICON_CREATED", false)){
            criarAtalho();
            getSharedPreferences("APP_PREFERENCE", Activity.MODE_PRIVATE).edit().putBoolean("IS_ICON_CREATED", true).commit();
        }

    }

    public void gotoServicePage(View view) {

        startService(new Intent(this, PostGetService.class));

//        mJobScheduler = (JobScheduler)
//                getSystemService(getApplication().JOB_SCHEDULER_SERVICE);
//        JobInfo.Builder builder = new JobInfo.Builder(1,
//                new ComponentName(getPackageName(),
//                        JobSchedularService.class.getName()));
//        builder.setPeriodic(3000);
//        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

//        if (mJobScheduler.schedule(builder.build()) <= 0) {
//            Log.e(TAG, "onCreate: Some error while scheduling the job");
//        }


//        Job myJob = dispatcher.newJobBuilder()
//                .setService(JobSchedularService.class) // the JobService that will be called
//                .setTag("my-unique-tag")
//                 .setRecurring(true)
//                .setTrigger(Trigger.executionWindow(0,1))
//                .build();
//
//        dispatcher.mustSchedule(myJob);


    }


    //nevigation
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.newItem1) {
            Intent intent = new Intent(this, SecondActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onResetSchedulerClick(MenuItem item) {
        SmartScheduler smartScheduler = SmartScheduler.getInstance(getApplicationContext());
        smartScheduler.removeJob(PostGetService.JOB_ID);
        Toast.makeText(this, "Service has been Stoped!", Toast.LENGTH_SHORT).show();
        //        Button btnstart = (Button) findViewById(R.id.button);
        //        btnstart.setVisibility(View.VISIBLE);


        //        smartJobButton.setText(getString(R.string.schedule_job_btn));
        //        smartJobButton.setEnabled(true);
        //        smartJobButton.setAlpha(1.0f);
    }

    @Override
    protected void onResume() {

       // Action2 to filter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackendIntentService.my_intent_service1);
        intentFilter.addAction(BackendIntentService.my_intent_service2);
//        IntentFilter service1 = new IntentFilter(BackendIntentService.NOTIFICATION);

         registerReceiver(receiver, intentFilter);

       // registerReceiver(receiver, new IntentFilter(BackendIntentService.NOTIFICATION));

        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
        unregisterReceiver(receiver);

    }





    //boradCast Receiver....

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(PostGetService.my_intent_service1)){
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        int resultCode = bundle.getInt(PostGetService.Result);
                        String Message = bundle.getString("Name");

                        if (resultCode == RESULT_OK) {

                            textView.setText("Connected");
                            textView.setTextColor(Color.parseColor("#008000"));
                            CMsg.setText(Message);


                        } else {

                            textView.setText("Disconnected");
                            textView.setTextColor(Color.parseColor("#FF0000"));
                            CMsg.setText(Message);

                        }
                    }

                }
                else if(intent.getAction().equals(PostGetService.my_intent_service2)){

                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        int resultCode = bundle.getInt(PostGetService.Result);
                        String Message = bundle.getString("Name");

                        if (resultCode == RESULT_OK) {

                            TextViewStatus.setText("Connected");
                            TextViewStatus.setTextColor(Color.parseColor("#008000"));
                            PMsg.setText(Message);


                        } else {

                            TextViewStatus.setText("Disconnected");
                            TextViewStatus.setTextColor(Color.parseColor("#FF0000"));
                            PMsg.setText(Message);

                        }
                    }

                }


        }
    };



    //end receiver


    private void criarAtalho() {
        Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", false);  //may it's already there so don't duplicate
        getApplicationContext().sendBroadcast(addIntent);
    }




}
