package com.example.stech.printercloudapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stech.printercloudapp.ipintervalmodel.IpAddressInverval;
import com.example.stech.printercloudapp.ticketalertmodel.Ticket;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.Base64;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;
import io.hypertrack.smart_scheduler.Job;
import io.hypertrack.smart_scheduler.SmartScheduler;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

    public class MainActivity extends AppCompatActivity implements SmartScheduler.JobScheduledCallback,NavigationView.OnNavigationItemSelectedListener{


        String str;
        ProgressBar pb;
        TextView textView;

        TextView updateText;

        private static final int JOB_ID = 1;
        private static final String TAG = MainActivity.class.getSimpleName();
        private static final String JOB_PERIODIC_TASK_TAG = "io.hypertrack.android_scheduler_demo.JobPeriodicTask";

        String Ip="";
        String Interval="";

        private String intervalInMillisEditText;

        JSONArray jsonArray;

        static final String UrlPost = "http://notifier.stech.com.pk:2337/saveTicketAlertData?format=json";
        final String TenantId = "STECH-TICKET-ALERT-53763ec7-c3c8-4209-b597-a63f4eabdcc4";

        private Activity activity;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //Progress bar code is here
            pb = (ProgressBar) findViewById(R.id.progressBar);
            pb.setVisibility(View.GONE);

            //for the textView of loading...
            textView = (TextView) findViewById(R.id.textView);
            updateText = (TextView) findViewById(R.id.textView6);

          //  updateText.setVisibility(View.GONE);




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



            //get the interval values from Database
            LoadDatabaseData();
        }


        public void LoadDatabaseData(){
            List<IpAddressInverval> listIp = IpAddressInverval.listAll(IpAddressInverval.class);
            for (IpAddressInverval ips:listIp) {
                Ip= ips.getIpAddress();
                Interval = ips.getInterval();
                System.out.println("Ip "+Ip);
                System.out.println("Interval is "+Interval);
            }
        }



        public  void gotoServicePage(View view){
            //get the interval values from Database
            LoadDatabaseData();

    //        Button btnstart = (Button) findViewById(R.id.button);
    //        btnstart.setVisibility(View.GONE);

            if(Ip.matches("")  || Interval.matches("")) {

                Toast.makeText(this, "Please Fill the Parametters", Toast.LENGTH_SHORT).show();
            }

    //        if(Interval.matches("")){
    //
    //            Toast.makeText(this, "Please Fill the Parametters", Toast.LENGTH_SHORT).show();
    //
    //        }
            else
            {

           // CallApi();

            SmartScheduler jobScheduler = SmartScheduler.getInstance(this);

            // Check if any periodic job is currently scheduled
            if (jobScheduler.contains(JOB_ID)) {
                removePeriodicJob();
                return;
            }

            // Create a new job with specified params
            Job job = createJob();
            if (job == null) {
                Toast.makeText(MainActivity.this, "Invalid paramteres specified. " +
                        "Please try again with correct job params.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Schedule current created job
            if (jobScheduler.addJob(job)) {
                Toast.makeText(MainActivity.this, "Job successfully added!", Toast.LENGTH_SHORT).show();

            }

            }


        }


        //call the api and get the data

        public void CallApi(){

            //String AddressIp = "http://"+Ip+":5700/api/values";
            String AddressIp = "http://"+Ip+"/login/login.action?username=InstallAdmin&password=ulan&page=/touchscreens/get.qsp?display=1";


            new AsyncHttpClient().get(AddressIp, new TextHttpResponseHandler() {


                @Override
                public void onStart() {
                  //  pb.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("Loading....");
                    //updateText.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {


                    str= responseString;
                     //   Toast.makeText(MainActivity.this, "Data Received", Toast.LENGTH_SHORT).show();

                    try {
                        SpliterStringMethod(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                  //  pb.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }




            });


        }

        //spli the strinh into the array
        public  void SpliterStringMethod(String values) throws IOException, JSONException {


            if(values != null && !values.isEmpty()) {

                //unquote the string for the data..
                values = values.replaceAll("^\"|\"$", "").replace("\n", "").replace("\r", "").trim();;

                //split on the baseis of '#'
                String[] lineString = values.split("#");

                for (int i = 0; i < lineString.length; i++) {
                    double idTicket = 0;
                    System.out.println(lineString[i]);

                    String[] valuesString = lineString[i].split(",");
                    System.out.println("inner loop");

                    //fETCH THE ID FROM DB if ticket exist then update else insert into it
                    Ticket ticket = new Ticket();
                    List<Ticket> num = ticket.find(Ticket.class, "TICKETNO = " + valuesString[3]);

                    if (num.size() != 0) {


                        for (Ticket item : num) {
                            System.out.println("Fetching the id= " + item.getId());
                            idTicket = item.getId();

                        }
                    }


                    if (idTicket == 0) {
//                    Ticket newTicket = new Ticket(Integer.parseInt(valuesString[2]), Integer.parseInt(valuesString[4])
//                            , Integer.parseInt(valuesString[3]), Integer.parseInt(valuesString[5]),
//                            Integer.parseInt(valuesString[1]));
                        Ticket newTicket = new Ticket();
                        newTicket.setTrack_no(Integer.parseInt(valuesString[2]));
                        newTicket.setTime_no(Integer.parseInt(valuesString[4]));
                        newTicket.setTicket_no(Integer.parseInt(valuesString[3]));
                        newTicket.setBranch_id(Integer.parseInt(valuesString[5]));
                        newTicket.setPosition_in_queue(Integer.parseInt(valuesString[1]));



                        newTicket.save();
                        System.out.println("Ticket Saved!..");

                    } else {
                        //update queue id
                        Ticket newqueuid = Ticket.findById(Ticket.class, (int) idTicket);
                        newqueuid.setPosition_in_queue(Integer.parseInt(valuesString[1])); // modify the values
                        newqueuid.save(); // updates the previous entry with new values.
                        System.out.println("Ticket Updated!..");
                        idTicket = 0;

                    }
                }
            }




           // getTableData();
        }


            //fetch the data from the Database

            public  void getTableData(){
            //call the ticket table to get the values


            List<Ticket> ticketAll = Ticket.listAll(Ticket.class);

            jsonArray = new JSONArray();
            for(int i=0;i<ticketAll.size();i++)
            {
                // JSONObject jGroup1 = new JSONObject();
                Map jGroup1 = new LinkedHashMap();
                jGroup1.put("track_no",ticketAll.get(i).getTrack_no());
                jGroup1.put("time_no",ticketAll.get(i).getTime_no());
                jGroup1.put("ticket_no",ticketAll.get(i).getTicket_no());
                jGroup1.put("branch_id",ticketAll.get(i).getBranch_id());
                jGroup1.put("position_in_queue",ticketAll.get(i).getPosition_in_queue());

                jsonArray.put(new Gson().toJson(jGroup1, Map.class));

            }
                String str1 = POST(UrlPost,"");

            }





        public String POST(String url, String json){
            enableStrictMode();
            HttpResponse response = null;
            HttpClient httpclient= null;
            httpclient = new DefaultHttpClient();

            InputStream inputStream = null;
            String result = "";
            try {

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost();

                // 4. convert JSONObject to JSON to String
                json = jsonArray.toString();
                json = json.replace("\\","").replace("\"{" ,"{").replace("}\"" ,"}");

                String userName ="admin";
                String password = "@Stech78324";
                String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                        (userName + ":" + password).getBytes(),
                        Base64.NO_WRAP);


                httpPost.setHeader("Authorization", base64EncodedCredentials);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("TenantId", TenantId));
                nameValuePairs.add(new BasicNameValuePair("JsonData", json));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                httpPost.setURI(new URI(UrlPost));

                response = httpclient.execute(httpPost);
                boolean responseBody = Boolean.parseBoolean(EntityUtils.toString(response.getEntity()));
                if ( (response.getStatusLine().getStatusCode()== HttpsURLConnection.HTTP_OK)  && responseBody==true ){

                    //Toast.makeText(MainActivity.this, "Updated Data", Toast.LENGTH_SHORT).show();


                    updateText.setVisibility(View.VISIBLE);
                    updateText.setText("Updated....");

                }
                updateText.setVisibility(View.GONE);
               // pb.setVisibility(View.GONE);



                System.out.println(responseBody+" values is");

                //updateText.setVisibility(View.GONE);


            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }




            // enableStrictMode();


            // 11. return result
         return result;
        }


        public void enableStrictMode()
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
        }





        //for the time schedular functions

        private Job createJob() {
            int jobType = 3;
            boolean isPeriodic = true;
            Long miliSecondsInterval = Long.parseLong(Interval)*1000;
            intervalInMillisEditText=Long.toString(miliSecondsInterval) ;

            String intervalInMillisString = intervalInMillisEditText;
            if (TextUtils.isEmpty(intervalInMillisString)) {
                return null;
            }

            Long intervalInMillis = Long.parseLong(intervalInMillisString);
            Job.Builder builder = new Job.Builder(JOB_ID, this, jobType, JOB_PERIODIC_TASK_TAG)

                    .setIntervalMillis(intervalInMillis);

            if (isPeriodic) {
                builder.setPeriodic(intervalInMillis);
            }

            return builder.build();
        }

        private void removePeriodicJob() {

            SmartScheduler jobScheduler = SmartScheduler.getInstance(this);
            if (!jobScheduler.contains(JOB_ID)) {
                Toast.makeText(MainActivity.this, "No job exists with JobID: " + JOB_ID, Toast.LENGTH_SHORT).show();
                return;
            }

            if (jobScheduler.removeJob(JOB_ID)) {
                Toast.makeText(MainActivity.this, "Job successfully removed!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onJobScheduled(Context context, final Job job) {
            if (job != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
    //                    System.out.println("Shahbaz is running");
    //                    Toast.makeText(MainActivity.this, "Job: " + job.getJobId() + " scheduled!", Toast.LENGTH_SHORT).show();
                        CallApi();
                        getTableData();
                    }
                });
                Log.d(TAG, "Job: " + job.getJobId() + " scheduled!");


            }
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
                Intent intent = new Intent(this,SecondActivity.class);
                startActivity(intent);
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        public void onResetSchedulerClick(MenuItem item) {
            SmartScheduler smartScheduler = SmartScheduler.getInstance(getApplicationContext());
            smartScheduler.removeJob(JOB_ID);
            Toast.makeText(this, "Service has been Stoped!", Toast.LENGTH_SHORT).show();
    //        Button btnstart = (Button) findViewById(R.id.button);
    //        btnstart.setVisibility(View.VISIBLE);


    //        smartJobButton.setText(getString(R.string.schedule_job_btn));
    //        smartJobButton.setEnabled(true);
    //        smartJobButton.setAlpha(1.0f);
        }

    }
