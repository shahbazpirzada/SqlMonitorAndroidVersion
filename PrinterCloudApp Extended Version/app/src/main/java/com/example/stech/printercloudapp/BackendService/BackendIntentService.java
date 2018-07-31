package com.example.stech.printercloudapp.BackendService;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import com.example.stech.printercloudapp.ipintervalmodel.IpAddressInverval;
import com.example.stech.printercloudapp.ticketalertmodel.Ticket;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.GzipSource;
import okio.Okio;
import okio.Sink;
import okio.Timeout;


import static com.example.stech.printercloudapp.PostGetService.UrlPost;

/**
 * Created by Stech on 4/6/2018.
 */

public class BackendIntentService extends IntentService {



    private int resultActivity = Activity.RESULT_CANCELED;
    String branchName ="";
    String dataString = "";

    int codeData;
    public static final String NOTIFICATION = "com.exampledemo.parsaniahardik.scanbarcodeqrdemonuts";

    public static final String my_intent_service1 = "com.exampledemo.parsaniahardik.scanbarcodeqrdemonuts1";
    public static final String my_intent_service2 = "com.exampledemo.parsaniahardik.scanbarcodeqrdemonuts2";


    public static final String UrlPost = "http://notifier.stech.com.pk:2337/saveTicketAlertData?format=json";
    public final String TenantId = "STECH-TICKET-ALERT-53763ec7-c3c8-4209-b597-a63f4eabdcc4";



    public static final String Result ="";
    private String Ip;
    private JSONArray jsonArray;
    private JSONArray GlobalJsonArray;

    public BackendIntentService() {
        super("BackendIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        GlobalJsonArray = new JSONArray();
        //load database data from database.....
        LoadDatabaseData();


        String getDataFromPrinter = getUrlString();
        SpliterStringMethod(getDataFromPrinter);
        getTableData();

    }

    public String getUrlString(){

        String urlPath= "http://"+Ip+"/login/login.action?username=InstallAdmin&password=ulan&page=/touchscreens/get.qsp?display=1";
        //String urlPath= "http://192.168.1.203:5700/api/values";

        final String[] data = {""};
        SyncHttpClient client = new SyncHttpClient();
        client.get(urlPath, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started

                System.out.println("called before request is started");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                System.out.println("Unable to connect");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                System.out.println("data pkr:"+responseString);
                dataString =  responseString;

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                System.out.println("called when request is retried");
            }
        });

        if (!dataString.isEmpty()){

            resultActivity  = Activity.RESULT_OK;
            String msg = "Succes from Printer";
            printResultToMainAcitivty(my_intent_service2,resultActivity,msg);

        }
        else {
            resultActivity  = Activity.RESULT_OK;
            String msg = "Failed from Printer";
            printResultToMainAcitivty(my_intent_service2,resultActivity,msg);

        }

            return dataString;
    }


    public void LoadDatabaseData() {
        List<IpAddressInverval> listIp = IpAddressInverval.listAll(IpAddressInverval.class);
        for (IpAddressInverval ips : listIp) {
            Ip = ips.getIpAddress();

            System.out.println("Ip " + Ip);

        }
    }


    //spli the strinh into the array
    public void SpliterStringMethod(String values) {


        if (values != null && !values.isEmpty()) {
            //unquote the string for the data..
            values = values.replaceAll("^\"|\"$", "").replace("\n", "").replace("\r", "").trim();


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
         getTableData();
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
             POST(UrlPost);
        }



    }

    public void POST(String url) {

            String result1 = "";
            String json = GlobalJsonArray.toString();
            json = json.replace("\\", "").replace("\"{", "{").replace("}\"", "}");

            String userName = "admin";
            String password = "@Stech78324";
            String base64EncodedCredentials = "Basic " + com.loopj.android.http.Base64.encodeToString(
                    (userName + ":" + password).getBytes(),
                    com.loopj.android.http.Base64.NO_WRAP);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();


        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = new FormBody.Builder()
                .add("TenantId", TenantId)
                .add("JsonData", json)
                .build();


        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization", base64EncodedCredentials)
                .url(UrlPost)
                .post(body)
                .build();

        okhttp3.Response response = null;
        try {
            response = client.newCall(request).execute();

            int responseCode = response.code();

            if (responseCode == 200) {
                //String data = response.body().string();
                boolean responseBody = Boolean.parseBoolean(response.body().string());
                result1="Success";

                resultActivity = Activity.RESULT_OK;
                String message = "Success in cloud";
                printResultToMainAcitivty(my_intent_service1,resultActivity,message);

            }
            else {

                GlobalJsonArray = new JSONArray();
            }




        } catch (Exception e) {
            System.out.println("Exception is: " + e);
            resultActivity = Activity.RESULT_CANCELED;
            String message = "Failed in cloud";
            printResultToMainAcitivty(my_intent_service1, resultActivity,message);

        }
    }

    private void printResultToMainAcitivty(String intentValue,int resultCode, String Msg) {

        Intent intent = new Intent(intentValue);
        intent.putExtra(Result,resultCode);
        intent.putExtra("Name",Msg);
        intent.putExtra("check",intentValue);

        sendBroadcast(intent);

    }





}
