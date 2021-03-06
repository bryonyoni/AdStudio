package com.bry.adcafe.ui;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.services.SliderPrefManager;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.crashlytics.android.Crashlytics;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class Splash extends AppCompatActivity {
    private int SPLASH_DISPLAY_LENGTH = 3021;
    private SliderPrefManager myPrefManager;
    private boolean isUserSeeingAcivity;
    private boolean isClearToMoveToNextActivity;
    private TextView LSEText;
    private TextView LogoText;
    private static int NOTIFICATION_ID2 = 1880;

    private LinearLayout logoPart;
    private int duration = 300;
    private View whiteView;
    private ImageView adImageBoi;
    private boolean isGoingToNextAct = false;
    private View whiteViewBack;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_splash);
        hideNavBars();
        if(new SliderPrefManager(getApplicationContext()).isFirstTimeLaunch()) SPLASH_DISPLAY_LENGTH = 6021;

        isUserSeeingAcivity=true;
        isClearToMoveToNextActivity = false;
        LSEText = findViewById(R.id.LSEText);
        LogoText = findViewById(R.id.logoText);
        logoPart = findViewById(R.id.logoPart);
        whiteView = findViewById(R.id.whiteView);
        adImageBoi = findViewById(R.id.adImageBoi);
        whiteViewBack = findViewById(R.id.whiteViewBack);

        try{
            int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minutes = Calendar.getInstance().get(Calendar.MINUTE);
//            if(hours==19 && minutes==26)LSEText.setText("M..M");
        }catch (Exception e){
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isClearToMoveToNextActivity = true;
                if(isUserSeeingAcivity) goToNextActivity();
            }
        },SPLASH_DISPLAY_LENGTH);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        try{
            notificationManager.cancel(NOTIFICATION_ID2);
        }catch (Exception e){
            e.printStackTrace();
        }

        adImageBoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextActivity();
                isGoingToNextAct = true;
            }
        });

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) whiteView.getLayoutParams();
        params.width = Utils.getDisplaySize(getWindowManager()).y;
        params.height = Utils.getDisplaySize(getWindowManager()).y;
        whiteView.setLayoutParams(params);

//        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) whiteViewBack.getLayoutParams();
//        params2.width = Utils.getDisplaySize(getWindowManager()).y;
//        params2.height = Utils.getDisplaySize(getWindowManager()).y;
//        whiteView.setLayoutParams(params2);
    }

    @Override
    protected void onPause() {
        isUserSeeingAcivity = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        isUserSeeingAcivity = true;
        if(isClearToMoveToNextActivity) goToNextActivity();
        super.onResume();
    }

    private void goToNextActivity2(){
        //for background window
        ObjectAnimator colorFade = ObjectAnimator.ofObject(findViewById(R.id.pageID)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade.setDuration(600);

        //for the line
        ObjectAnimator colorFade2 = ObjectAnimator.ofObject(findViewById(R.id.lineView)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.white) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*to color*/);
        colorFade2.setDuration(600);

        //for the bottom text
        ObjectAnimator colorFade3 = ObjectAnimator.ofObject(LSEText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.white) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*to color*/);
        colorFade3.setDuration(600);

        //for the logotext
        ObjectAnimator colorFade4 = ObjectAnimator.ofObject(LogoText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.white) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*to color*/);
        colorFade4.setDuration(600);

        colorFade.start();
        colorFade2.start();
        colorFade3.start();
        colorFade4.start();

        startNextActivity();
//        long currentTimeMillis = System.currentTimeMillis();
//        long extraTimeFromMidnight = currentTimeMillis%(1000*60*60*24);
//        long currentDay = (currentTimeMillis-extraTimeFromMidnight)/(1000*60*60*24);
//        getDateFromDays(currentDay);
    }

    private void startNextActivity(){
        //for the line
        long duration = 200;
        ObjectAnimator colorFade2 = ObjectAnimator.ofObject(findViewById(R.id.lineView)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade2.setDuration(duration);
        colorFade2.setStartDelay(600);

        //for the bottom text
        ObjectAnimator colorFade3 = ObjectAnimator.ofObject(LSEText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade3.setDuration(duration);
        colorFade3.setStartDelay(600);

        //for the logotext
        ObjectAnimator colorFade4 = ObjectAnimator.ofObject(LogoText
                , "textColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade4.setDuration(duration);
        colorFade4.setStartDelay(600);

        colorFade2.start();
        colorFade3.start();
        colorFade4.start();

        NowReallyStartNextActivity();
    }

    private void NowReallyStartNextActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myPrefManager = new SliderPrefManager(getApplicationContext());
                if (myPrefManager.isFirstTimeLaunch()){
                    Intent intent = new Intent(Splash.this,TutorialActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(Splash.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },800);
    }

    private void hideNavBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    private void goToNextActivity3(){
        logoPart.setRotationY(0);
        logoPart.animate().setDuration(duration).translationY(0)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
               doTheThingAfterTheThing();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        ObjectAnimator colorFade = ObjectAnimator.ofObject(findViewById(R.id.pageID)
                , "backgroundColor" /*view attribute name*/,
                new ArgbEvaluator(),
                getApplicationContext().getResources().getColor(R.color.colorPrimaryDark2) /*from color*/
                , getApplicationContext().getResources().getColor(R.color.white) /*to color*/);
        colorFade.setDuration(duration-10);
        colorFade.start();
    }

    private void goToNextActivity(){
        if(!isGoingToNextAct) {
            isGoingToNextAct = true;
            whiteView.setVisibility(View.VISIBLE);
//            whiteViewBack.setVisibility(View.VISIBLE);

            whiteView.animate().translationY(0).scaleX(2.7f).scaleY(1.5f).setDuration(500).setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            doTheThingAfterTheThing();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();

//            whiteViewBack.animate().translationY(0).setDuration(500).setInterpolator(new LinearOutSlowInInterpolator()).start();

//            ValueAnimator animator = ValueAnimator.ofFloat(0.05f,1f,1.5f);
//            animator.setInterpolator(new LinearOutSlowInInterpolator());
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    whiteView.setScaleY((Float) valueAnimator.getAnimatedValue());
//                    whiteView.setScaleX((Float) valueAnimator.getAnimatedValue());
////                    whiteViewBack.setScaleY((Float) valueAnimator.getAnimatedValue());
////                    whiteViewBack.setScaleX((Float) valueAnimator.getAnimatedValue());
//                }
//            });
//            animator.setDuration(250);
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    doTheThingAfterTheThing();
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//            animator.start();

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
////                    doTheThingAfterTheThing();
//                }
//            },230);

        }
    }


    private void doTheThingAfterTheThing() {
        myPrefManager = new SliderPrefManager(getApplicationContext());
        if (myPrefManager.isFirstTimeLaunch()){
            Intent intent = new Intent(Splash.this,TutorialActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }else{
            Intent intent = new Intent(Splash.this,LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }
    }


}
