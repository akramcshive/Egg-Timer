package com.eggtimer.timer;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;


import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Locale;

import static android.content.ContentValues.TAG;

public class Soft extends Fragment {

    //Declare a variable to hold count down timer's paused status
    private boolean isCanceled = false;
    private long SoftValue = 360000;
    private long START_TIME_IN_MILLIS = SoftValue;
    static int count=1;

    private TextView mTextViewCountDown;
    private Button mButtonStart;
    private Button mButtonStop;

    private CountDownTimer mCountDownTimer = null;

    private boolean mTimerRunning;

    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View soft =  inflater.inflate(R.layout.soft_layout, container, false);

        mAdView = soft.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

       prepareAd();





        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        mTextViewCountDown = soft.findViewById(R.id.text_view_countdown);


        mButtonStart = soft.findViewById(R.id.button_start);
        mButtonStop = soft.findViewById(R.id.button_stop);
        mButtonStop = soft.findViewById(R.id.button_stop);
        updateCountDownText();


        mButtonStart.setOnClickListener(new View.OnClickListener() {
            //Disable the start and pause button

            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                stopTimer();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        return soft;
    }





    private void startTimer() {
        Log.d("Start","starting timer");

        if(null != mCountDownTimer) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("Count", "onTick: " + (millisUntilFinished / 1000) / 60);
                mButtonStop.setVisibility(View.VISIBLE);
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                mButtonStart.setText("Start");
                mButtonStart.setVisibility(View.INVISIBLE);
            }


            @Override
            public void onFinish() {
                mTimeLeftInMillis = 0;
                updateCountDownText();
                mTimerRunning = false;
                mButtonStart.setText("Start");
                addNotification();
                MediaPlayer mplayer = MediaPlayer.create(getActivity(), R.raw.ding_dong);
                mplayer.start();
                mButtonStart.setVisibility(View.VISIBLE);
                mButtonStop.setVisibility(View.INVISIBLE);
                START_TIME_IN_MILLIS = SoftValue;
                mTimeLeftInMillis = SoftValue;
                updateCountDownText();
            }

        }.start();

    }


    private void stopTimer(){
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        if(null!=mCountDownTimer){
            mCountDownTimer.cancel();
            mCountDownTimer = null;
            mButtonStart.setVisibility(View.VISIBLE);
            mButtonStop.setVisibility(View.INVISIBLE);
            START_TIME_IN_MILLIS = SoftValue;
            mTimeLeftInMillis = SoftValue;

        }

        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        } else {
            Toast.makeText(getContext(), "The interstitial ad wasn't ready yet.", Toast.LENGTH_SHORT).show();

        }
    }


    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);

    }


    private final static String NOTIFICATION_CHANNEL = "channel_name";
    private final static String CHANNEL_DESCRIPTION = "channel_description";
    public final static int NOTIFICATION_ID = 0;


    private void addNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(),"ChannelID");
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Your Eggs Are Ready");
        builder.setContentText("Crack them open and go eat em");

        Intent notificationIntent = new Intent(getActivity(), Soft.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());

        //For versions greater then Android O
        // Intent
        Intent intentAction = new Intent(getActivity(), MainActivity.class);
        //intentAction.putExtra("sysModel", sysModel);

        // Notification Manager
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);

        // Pending Intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 1, intentAction, PendingIntent.FLAG_ONE_SHOT);

        // Create the notification channel if android >= 8
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Notification builder.. Note that I added the notification channel on this constructor
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity(), NOTIFICATION_CHANNEL);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_background);
        notificationBuilder.setContentTitle("Your Eggs Are Ready");
        notificationBuilder.setContentText("Crack them open and go eat em");
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.addAction(R.drawable.ic_launcher_foreground, getActivity().getString(R.string.launch_app), pendingIntent);
        notificationBuilder.setAutoCancel(true);

        // Notify
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());


    }


//    private void addNotification() {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(),"ChannelID");
//        builder.setSmallIcon(R.drawable.ic_launcher_background);
//        builder.setContentTitle("Your Eggs Are Ready");
//        builder.setContentText("Crack them open and go eat em");
//
//        Intent notificationIntent = new Intent(getActivity(), Soft.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, notificationIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(contentIntent);
//
//        // Add as notification
//        NotificationManager manager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
//        manager.notify(0, builder.build());
//    }



    public void prepareAd(){

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getActivity().getApplicationContext(),"ca-app-pub-2876518515969882/2161396117", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }



}