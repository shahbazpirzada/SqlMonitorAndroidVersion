package com.example.stech.printercloudapp.printerApi;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Stech on 11/14/2017.
 */

public class PrinterApi {

    private static String Base_URL = "http://localhost:62082/api/values";

    private  static AsyncHttpClient client = new AsyncHttpClient();
    public  static  void getAllData(RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler){
        client.get(Base_URL,params, asyncHttpResponseHandler);

    }
}
