package com.bry.adcafe.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.MyAdStatsItem;
import com.bry.adcafe.adapters.TomorrowsAdStatItem;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AdStats extends AppCompatActivity {
    private static final String TAG = "AdStats";
    private List<String> mAdList = new ArrayList<>();
    private List<Advert> mUploadedAds = new ArrayList<>();

    private List<String> mAdList2 = new ArrayList<>();
    private List<Advert> mUploadedAds2 = new ArrayList<>();

    private List<String> mAdList3 = new ArrayList<>();
    private List<Advert> mUploadedAds3= new ArrayList<>();

    private Context mContext;
    @Bind(R.id.adStatsScrollView) ScrollView mAdStatsScrollView;
    @Bind(R.id.PlaceHolderViewInfo) PlaceHolderView DataListsView;

    @Bind(R.id.TomorrowsTitle) TextView TomorrowsAdsTitle;
    @Bind(R.id.PlaceHolderViewInfoTomorrow) PlaceHolderView tomorrowsPlaceHolderView;

    @Bind(R.id.YesterdaysTitle) TextView YesterdayAdsTitle;
    @Bind(R.id.PlaceHolderViewInfoPrevious) PlaceHolderView yesterdayPlaceHolderView;

    private int cycleCount = 0;
    private int cycleCount2 = 0;
    private int cycleCount3 = 0;

    private ProgressDialog mAuthProgressDialog;
    private int numberOfClusters =0;
    private int runCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_stats);
        mContext = this.getApplicationContext();
        ButterKnife.bind(this);

        if(isNetworkConnected(mContext)){
            DataListsView.setVisibility(View.GONE);
            yesterdayPlaceHolderView.setVisibility(View.GONE);
            tomorrowsPlaceHolderView.setVisibility(View.GONE);

            DataListsView.setNestedScrollingEnabled(false);
            yesterdayPlaceHolderView.setNestedScrollingEnabled(false);
            tomorrowsPlaceHolderView.setNestedScrollingEnabled(false);

            findViewById(R.id.topText).setVisibility(View.GONE);
            YesterdayAdsTitle.setVisibility(View.GONE);
            TomorrowsAdsTitle.setVisibility(View.GONE);

            findViewById(R.id.LoadingViews).setVisibility(View.VISIBLE);
            registerReceivers();
            createProgressDialog();
//            loadAdsThatHaveBeenUploaded();
            loadTomorrowsUploadedAds();
        }else{
            showNoConnectionView();
        }

        mAdStatsScrollView.setSmoothScrollingEnabled(true);
    }

    @Override
    protected void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForTakeDownAd,
                new IntentFilter("TAKE_DOWN"));

    }

    private void unregisterReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForTakeDownAd);
    }

    private void showNoConnectionView() {
        DataListsView.setVisibility(View.GONE);
        yesterdayPlaceHolderView.setVisibility(View.GONE);
        YesterdayAdsTitle.setVisibility(View.GONE);

        tomorrowsPlaceHolderView.setVisibility(View.GONE);
        TomorrowsAdsTitle.setVisibility(View.GONE);

        findViewById(R.id.topText).setVisibility(View.GONE);
        findViewById(R.id.droppedInternetLayoutForAdStats).setVisibility(View.VISIBLE);
    }




    private void loadTomorrowsUploadedAds() {
        Log.d(TAG,"Loading ads uploaded by user for tomorrow.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getNextDay());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList3.add(pushValue);
                    }
                    loadNextDaysAds();
                }else{
                    loadAdsThatHaveBeenUploaded();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadNextDaysAds() {
        for(int i = 0; i<mAdList3.size(); i++){
            String adToBeLoaded = mAdList3.get(i);
            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getNextDay()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                    DataSnapshot clusters = dataSnapshot.child("clustersToUpLoadTo");
                    for(DataSnapshot clusterSnap : clusters.getChildren()){
                        int cluster = Integer.parseInt(clusterSnap.getKey());
                        int pushId = clusterSnap.getValue(int.class);
                        adUploadedByUser.clusters.put(cluster,pushId);
                    }
                    Log.d(TAG,"Gotten one ad from firebase. : "+adUploadedByUser.getPushRefInAdminConsole());
                    mUploadedAds3.add(adUploadedByUser);
                    cycleCount3++;
                    if(cycleCount3 == mAdList3.size()){
                        Log.d(TAG,"All the ads have been handled.");
                        loadStats3();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loadStats3() {
        tomorrowsPlaceHolderView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        for(int i = 0; i<mUploadedAds3.size();i++){
            tomorrowsPlaceHolderView.addView(new TomorrowsAdStatItem(mContext,tomorrowsPlaceHolderView,mUploadedAds3.get(i)));
        }
        loadAdsThatHaveBeenUploaded();
    }


    private void loadAdsThatHaveBeenUploaded() {
        Log.d(TAG,"Loading ads uploaded by user.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getDate());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"Children have been gotten from firebase");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList.add(pushValue);
                    }
                    loadAdsUploadedByUser();
                    Log.d(TAG,"Number of children is : "+mAdList.size());
                }else{
                    loadPreviousDaysAds();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAdsUploadedByUser() {
        for(int i = 0; i<mAdList.size(); i++){
            String adToBeLoaded = mAdList.get(i);

            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getDate()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                    Log.d(TAG,"Gotten one ad from firebase. : "+adUploadedByUser.getPushRefInAdminConsole());
                    mUploadedAds.add(adUploadedByUser);
                    cycleCount++;
                    if(cycleCount == mAdList.size()){
                        Log.d(TAG,"All the ads have been handled.");
                        loadStats();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });

        }

    }

    private void loadStats() {
        DataListsView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        for(int i = 0; i<mUploadedAds.size();i++){
            DataListsView.addView(new MyAdStatsItem(mContext,DataListsView,mUploadedAds.get(i)));
        }

        loadPreviousDaysAds();
    }




    private void loadPreviousDaysAds() {
        Log.d(TAG,"Loading ads uploaded by user.");
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getPreviousDay());
        DatabaseReference dbref = query.getRef();
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG,"Children have been gotten from firebase");
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        String pushValue = snap.getValue(String.class);
                        mAdList2.add(pushValue);
                    }
                    Log.d(TAG,"Number of children is : "+mAdList2.size());
                    loadAdsUploadedByUser2();
                }else{
                    findViewById(R.id.topText).setVisibility(View.VISIBLE);
                    findViewById(R.id.LoadingViews).setVisibility(View.GONE);
                    if(mUploadedAds3.size()>0) TomorrowsAdsTitle.setVisibility(View.VISIBLE);
                    tomorrowsPlaceHolderView.setVisibility(View.VISIBLE);
                    DataListsView.setVisibility(View.VISIBLE);
                    findViewById(R.id.noAdsUploadedText).setVisibility(View.GONE);
//                    if(DataListsView.getChildCount()>0) {
//                        Log.d(TAG,"User had uploaded ads for today.");
//                        findViewById(R.id.topText).setVisibility(View.VISIBLE);
//                        findViewById(R.id.LoadingViews).setVisibility(View.GONE);
//                        findViewById(R.id.noAdsUploadedText).setVisibility(View.GONE);
//                        DataListsView.setVisibility(View.VISIBLE);
//                    }
//                    if(tomorrowsPlaceHolderView.getChildCount()>0){
//                        Log.d(TAG,"User had uploaded ads for tomorrow.");
//                        findViewById(R.id.topText).setVisibility(View.VISIBLE);
//                        findViewById(R.id.LoadingViews).setVisibility(View.GONE);
//                        tomorrowsPlaceHolderView.setVisibility(View.VISIBLE);
//                        TomorrowsAdsTitle.setVisibility(View.VISIBLE);
//                    }
                    if(mUploadedAds3.size()==0 && mUploadedAds.size()==0){
                        Log.d(TAG,"UUUUser had uploaded no ads.");
                        findViewById(R.id.noAdsUploadedText).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                Toast.makeText(mContext,"We're having a hard time with your connection...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAdsUploadedByUser2() {
        for(int i = 0; i<mAdList2.size(); i++){
            String adToBeLoaded = mAdList2.get(i);

            Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(getPreviousDay()).child(adToBeLoaded);
            DatabaseReference dbRef = query.getRef();
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Advert adUploadedByUser = dataSnapshot.getValue(Advert.class);
                    Log.d(TAG,"Gotten one ad from firebase. : "+adUploadedByUser.getPushRefInAdminConsole());
                    mUploadedAds2.add(adUploadedByUser);
                    cycleCount2++;
                    if(cycleCount2 == mAdList2.size()){
                        Log.d(TAG,"All the ads have been handled.");
                        loadStats2();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG,"Database error in loading ; "+databaseError.getMessage());
                    Toast.makeText(mContext,"We're having a few connectivity issues...",Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void loadStats2() {
        DataListsView.setVisibility(View.VISIBLE);
        yesterdayPlaceHolderView.setVisibility(View.VISIBLE);
        YesterdayAdsTitle.setVisibility(View.VISIBLE);

        tomorrowsPlaceHolderView.setVisibility(View.VISIBLE);
        if(mUploadedAds3.size()>0) TomorrowsAdsTitle.setVisibility(View.VISIBLE);

        findViewById(R.id.topText).setVisibility(View.VISIBLE);
        findViewById(R.id.LoadingViews).setVisibility(View.GONE);

        yesterdayPlaceHolderView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,2));
        for(int i = 0; i<mUploadedAds2.size();i++){
            yesterdayPlaceHolderView.addView(new MyAdStatsItem(mContext,yesterdayPlaceHolderView,mUploadedAds2.get(i)));
        }
    }





    private String getDate(){
        long date = System.currentTimeMillis();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        String MonthString = sdfMonth.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String dayString = sdfDay.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String yearString = sdfYear.format(date);

        Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dd+":"+mm+":"+yy);

        return todaysDate;
    }

    private String getPreviousDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d(TAG,"Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

    private String getNextDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d(TAG,"Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private BroadcastReceiver mMessageReceiverForTakeDownAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Message received for taking down ad.");
            showConfirmSubscribeMessage();
        }
    };

    private void createProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("AdCafe.");
        mAuthProgressDialog.setMessage("Taking down the ad...");
        mAuthProgressDialog.setCancelable(false);
    }

    private void showConfirmSubscribeMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AdCafe.");
        builder.setMessage(Variables.areYouSureTakeDownText)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        takeDownAd2();
                    }
                })
                .setNegativeButton("No.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void takeDownAd() {
        mAuthProgressDialog.show();
        Advert ad = Variables.adToBeFlagged;
        if(ad.isFlagged())mAuthProgressDialog.setMessage("Restoring the ad...");
        boolean bol = !ad.isFlagged();

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay())
                .child(ad.getPushRefInAdminConsole())
                .child("flagged");
        mRef.setValue(bol);

        Log.d(TAG,"Flagging ad : "+ad.getPushRefInAdminConsole());
        numberOfClusters = ad.clusters.size();
        for(Integer cluster : ad.clusters.keySet()){
            int pushId = ad.clusters.get(cluster);
            DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                    .child(getNextDay())
                    .child(ad.getCategory())
                    .child(Integer.toString(cluster))
                    .child(Integer.toString(pushId))
                    .child("flagged");
            mRef3.setValue(bol).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    runCount++;
                    if(runCount==numberOfClusters){
                        runCount = 0;
                        numberOfClusters = 0;
                        mAuthProgressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void takeDownAd2(){
        mAuthProgressDialog.show();
        Advert ad = Variables.adToBeFlagged;
        if(ad.isFlagged())mAuthProgressDialog.setMessage("Restoring the ad...");
        boolean bol = !ad.isFlagged();

        DatabaseReference  mRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay())
                .child(ad.getPushRefInAdminConsole())
                .child("flagged");
        mRef.setValue(bol);

        Log.d(TAG,"Flagging ad : "+ad.getPushRefInAdminConsole());
        numberOfClusters = ad.clusters.size();
        int nextCluster = getClusterValue(runCount,ad);
        int nextPushId = getPushIdValue(runCount,ad);
        flagSpecific(nextCluster,nextPushId,ad,bol);
    }

    private void flagSpecific(int cluster, int pushId, final Advert ad, final boolean bol){
        DatabaseReference  mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay())
                .child(ad.getCategory())
                .child(Integer.toString(cluster))
                .child(Integer.toString(pushId))
                .child("flagged");
        mRef3.setValue(bol).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                runCount++;
                if(runCount<numberOfClusters){
                    int nextCluster = getClusterValue(runCount,ad);
                    int nextPushId = getPushIdValue(runCount,ad);
                    flagSpecific(nextCluster,nextPushId,ad,bol);
                }else{
                    runCount = 0;
                    numberOfClusters = 0;
                    mAuthProgressDialog.dismiss();
                }
            }
        });
    }

    private int getClusterValue(int index,Advert ad) {
        LinkedHashMap map = ad.clusters;
        int cluster = (new ArrayList<Integer>(map.keySet())).get(index);
        Log.d(TAG, "Cluster gotten from ad is : " + cluster);
        return cluster;
    }

    private int getPushIdValue(int index,Advert ad) {
        LinkedHashMap map = ad.clusters;
        int cluster = (new ArrayList<Integer>(map.values())).get(index);
        Log.d(TAG, "Cluster gotten from ad is : " + cluster);
        return cluster;
    }

}
