package com.example.stech.printercloudapp.JobService;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.example.stech.printercloudapp.BackendService.BackendIntentService;

/**
 * Created by Stech on 4/6/2018.
 */


public class JobSchedularService extends JobService {


    private static final String TAG = "JobSchedulerService";
    public JobSchedularService() {
        MultiDex.install(this);

    }



    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i(TAG, "onStartJob:");
        //MultiDex.install(this);
        startServiceIntent();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob:");
        return false;
    }


    public void startServiceIntent(){
        Intent intent
                = new Intent(this,BackendIntentService.class);

        startService(intent);

    }

}
