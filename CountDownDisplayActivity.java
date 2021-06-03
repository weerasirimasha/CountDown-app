package com.example.countdown_timer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousChannelGroup;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;

import static com.example.countdown_timer.App.CHANNEL_ID;

public class CountDownDisplayActivity extends AppCompatActivity{

    TextView timerTv;
    NotificationManagerCompat notificationManagerCompat ;
    String TAG = "===>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down_display);

        timerTv = findViewById(R.id.timer);

        //call async task
        BackgroundAsync backgroundAsync = new BackgroundAsync();
        backgroundAsync.execute();

    }

    public class BackgroundAsync extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {

            //file locator url
            String getTime_url = "http://10.0.2.2:8080/phpmyadmin/getTime.php";

            try {
                URL url = new URL(getTime_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);

                InputStream input = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input,"iso-8859-1"));

                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null){
                    result += line ;
                }
                bufferedReader.close();
                input.close();
                connection.disconnect();
                return  result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG,"MalformedURLException "+e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"IOException "+e);
            }

            return null;
        }

        @Override
        protected void onPreExecute()
        {
            Log.d(TAG,"Searching... ");
        }


        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG,"Received Time "+result.toString());
            startTimer(result.toString());
        }
    }


    //set time to countdownTimer
    public void startTimer(String time){

        try {

            //split the time string
            String[] split = time.split(":");
            String seconds = split[1];

            //convert seconds into milliseconds
            final long[] timeInMilliSeconds = {Long.parseLong(seconds) * 1000};
            Log.d(TAG," in milliseconds "+ timeInMilliSeconds[0]);

            new CountDownTimer(timeInMilliSeconds[0], 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    timeInMilliSeconds[0] = millisUntilFinished ;

                    int minutes = (int) (( timeInMilliSeconds[0] / 1000) / 30);
                    int seconds = (int) (( timeInMilliSeconds[0] / 1000) % 30);

                    String timeDisplayed = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
                    timerTv.setText(timeDisplayed);
                }

                @Override
                public void onFinish() {
                    //dispatched a notification on finish count down
                    displayNotification();
                }
            }.start();


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    //dispatch notification
    public void displayNotification(){
        try {
            Notification notification = new NotificationCompat.Builder(this ,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_notifications)
                    .setContentTitle("Time Over")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .build();

            notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(1,notification);

        }catch (Exception e){
            e.printStackTrace();
            Log.d("*********"," notification "+e);
        }
    }
}