package com.example.stech.printercloudapp;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stech.printercloudapp.ipintervalmodel.IpAddressInverval;
import com.example.stech.printercloudapp.ticketalertmodel.Ticket;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.Base64;
import com.loopj.android.http.HttpGet;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpRequestRetryHandler;
import cz.msebera.android.httpclient.impl.client.DefaultUserTokenHandler;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;
import io.hypertrack.smart_scheduler.Job;
import io.hypertrack.smart_scheduler.SmartScheduler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostGetService extends Service implements SmartScheduler.JobScheduledCallback {


    public String str;

    public TextView textView;

    public TextView updateText;

    public static final int JOB_ID = 1;
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String JOB_PERIODIC_TASK_TAG = "io.hypertrack.android_scheduler_demo.JobPeriodicTask";

    public String Ip = "";
    public String Interval = "";

    public String intervalInMillisEditText;

    public JSONArray jsonArray;

    public JSONArray GlobalJsonArray;

    public static final String UrlPost = "http://notifier.stech.com.pk:2337/saveTicketAlertData?format=json";
    public final String TenantId = "STECH-TICKET-ALERT-53763ec7-c3c8-4209-b597-a63f4eabdcc4";

    private Activity activity;

    public OkHttpClient client1;

    public String dataGet;


    public PostGetService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //get the interval values from Database
        LoadDatabaseData();
        GlobalJsonArray = new JSONArray();
        if (Ip.matches("") || Interval.matches("")) {

            Toast.makeText(this, "Please Fill the Parametters", Toast.LENGTH_SHORT).show();
        } else {

            SmartScheduler jobScheduler = SmartScheduler.getInstance(this);

            // Check if any periodic job is currently scheduled
            if (jobScheduler.contains(JOB_ID)) {
                removePeriodicJob();
                //return;
            }

            // Create a new job with specified params
            Job job = createJob();
            if (job == null) {
//                Toast.makeText(MainActivity.this, "Invalid paramteres specified. " +
//                        "Please try again with correct job params.", Toast.LENGTH_SHORT).show();

                System.out.println("Invalid paramteres specified. Please try again with correct job params.");


                // return;
            }

            // Schedule current created job
            if (jobScheduler.addJob(job)) {
                Toast.makeText(getApplicationContext(), "Job successfully added!", Toast.LENGTH_SHORT).show();
                System.out.println("Job successfully added!");

            }

        }


        return START_STICKY;
    }


    public void LoadDatabaseData() {
        List<IpAddressInverval> listIp = IpAddressInverval.listAll(IpAddressInverval.class);
        for (IpAddressInverval ips : listIp) {
            Ip = ips.getIpAddress();
            Interval = ips.getInterval();
            System.out.println("Ip " + Ip);
            System.out.println("Interval is " + Interval);
        }
    }


    //call the api and get the data

    public void CallApi() {

        //String AddressIp = "http://"+Ip+":5700/api/values";
        final String AddressIp = "http://" + Ip + "/login/login.action?username=InstallAdmin&password=ulan&page=/touchscreens/get.qsp?display=1";

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String response = GET(AddressIp);
                    SpliterStringMethod(response);
                    //Parse the response string here
                    // Log.d("Response", response);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();


    }

    public String GET(String url) throws IOException {

        new SyncHttpClient().get(url, new TextHttpResponseHandler() {


            @Override
            public void onStart() {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                System.out.println("Error message" + responseString);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                dataGet = responseString;
            }
        });
        return dataGet;



//        String REQUEST_METHOD = "GET";
//        int READ_TIMEOUT = 15000;
//        int CONNECTION_TIMEOUT = 15000;
//
//        String stringUrl =url;
//        String result ="";
//
//        try {
//            //Create a URL object holding our url
//            URL myUrl = new URL(stringUrl);
//            //Create a connection
//            HttpURLConnection connection =(HttpURLConnection)
//                    myUrl.openConnection();
//            //Set methods and timeouts
//            connection.setRequestMethod(REQUEST_METHOD);
//            connection.setReadTimeout(READ_TIMEOUT);
//            connection.setConnectTimeout(CONNECTION_TIMEOUT);
//
//            //Connect to our url
//            connection.connect();
//        }
//        catch (Exception ex){
//            System.out.println("new exceptions: "+ex);
//
//        }
//        return result;

//        String result123 = "";
//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//        client.readTimeoutMillis();
//
//
//        OkHttpClient client2 = new OkHttpClient.Builder()
//                .connectTimeout(100, TimeUnit.SECONDS)
//                .readTimeout(100,TimeUnit.SECONDS)
//
//                .build();
//
//
//        Response response = client.newCall(request).execute();
//        client.retryOnConnectionFailure();
//        return result123;
    }


    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    //spli the strinh into the array
    public void SpliterStringMethod(String values) throws IOException, JSONException {


        if (values != null && !values.isEmpty()) {
            //unquote the string for the data..
            values = values.replaceAll("^\"|\"$", "").replace("\n", "").replace("\r", "").trim();
            ;

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

    public void getTableData() {
        //call the ticket table to get the values
        List<Ticket> ticketAll = Ticket.listAll(Ticket.class);

        jsonArray = new JSONArray();
        for (int i = 0; i < ticketAll.size(); i++) {
            // JSONObject jGroup1 = new JSONObject();
            Map jGroup1 = new LinkedHashMap();
            jGroup1.put("track_no", ticketAll.get(i).getTrack_no());
            jGroup1.put("time_no", ticketAll.get(i).getTime_no());
            jGroup1.put("ticket_no", ticketAll.get(i).getTicket_no());
            jGroup1.put("branch_id", ticketAll.get(i).getBranch_id());
            jGroup1.put("position_in_queue", ticketAll.get(i).getPosition_in_queue());

            jsonArray.put(new Gson().toJson(jGroup1, Map.class));

        }

        boolean checkCondition = jsonArray.toString().equals(GlobalJsonArray.toString());
       if (checkCondition == false)
      {
            GlobalJsonArray = jsonArray;

            // send Dts to server
            //HttpDataPost();
          String str = POST(UrlPost, "");
      }



    }

    //call the http client to execute
    public void HttpDataPost() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                String str = POST(UrlPost, "");
                return null;
            }

            protected void onProgressUpdate(Void progress) {

            }

            protected void onPostExecute(Void params) {
            }


        }.execute();

    }


    public String POST(String url, String json) {

        // Convert JSONObject to JSON to String

        HttpResponse response = null;
        HttpClient httpclient = null;
        httpclient = new DefaultHttpClient();

        InputStream inputStream = null;
        String result = "";
        try {

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost();

            // 4. convert JSONObject to JSON to String
            json = GlobalJsonArray.toString();
            json = json.replace("\\", "").replace("\"{", "{").replace("}\"", "}");

            String userName = "admin";
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
            if ((response.getStatusLine().getStatusCode() == HttpsURLConnection.HTTP_OK) && responseBody == true) {
                System.out.println("Data Updated" + responseBody);

            }
            else
            {
                GlobalJsonArray= new JSONArray();

            }
        } catch (Exception e) {

        }

        // enableStrictMode();
        return result;


    }


    //for the time schedular functions

    public Job createJob() {
        int jobType = 3;
        boolean isPeriodic = true;
        Long miliSecondsInterval = Long.parseLong(Interval) * 1000;
        intervalInMillisEditText = Long.toString(miliSecondsInterval);

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

    public void removePeriodicJob() {

        SmartScheduler jobScheduler = SmartScheduler.getInstance(this);
        if (!jobScheduler.contains(JOB_ID)) {
            //  Toast.makeText(MainActivity.this, "No job exists with JobID: " + JOB_ID, Toast.LENGTH_SHORT).show();

            System.out.println("No job exists with JobID: " + JOB_ID);
            return;
        }

        if (jobScheduler.removeJob(JOB_ID)) {
//            Toast.makeText(MainActivity.this, "Job successfully removed!", Toast.LENGTH_SHORT).show();
            System.out.println("Job successfully removed!");
        }
    }

    @Override
    public void onJobScheduled(Context context, final Job job) {
        if (job != null) {
            new MainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    CallApi();
                    getTableData();

                }
            });

        }
    }

}
