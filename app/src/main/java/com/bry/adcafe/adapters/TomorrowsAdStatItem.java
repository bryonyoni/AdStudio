package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.TimeManager;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by bryon on 20/12/2017.
 */

@NonReusable
@Layout(R.layout.tomorrows_ads_item)
public class TomorrowsAdStatItem {
    @View(R.id.adImage) private ImageView mImage;
    @View(R.id.EmailText) private TextView mEmail;
    @View(R.id.TargetedNumber) private TextView mTargetedNumber;
    @View(R.id.AmountToReimburse) private TextView mAmountToReimburse;
    @View(R.id.category) private TextView mCategory;
    @View(R.id.isFlagged) private TextView mFlagged;
    @View(R.id.takeDownButton) private Button mTakeDown;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    private byte[] mImageBytes;

    public TomorrowsAdStatItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    private void onResolved(){
        if(mImageBytes==null) new  LongOperationFI().execute("");
        else Glide.with(mContext).load(mImageBytes).into(mImage);

        //dddddddddddd
        mEmail.setText("Uploaded by : "+mAdvert.getUserEmail());
        if(mAdvert.getUserEmail().equals(Constants.ADMIN_ACC))mEmail.setText("Uploaded by : me@myGeemail.com");
        double vat = (mAdvert.getNumberOfUsersToReach()*Variables.getUserCpvFromTotalPayPerUser(mAdvert.getAmountToPayPerTargetedView()))
                *Constants.VAT_CONSTANT;

        mTargetedNumber.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));
        mCategory.setText("Category : "+mAdvert.getCategory());
        double incentiveAmm = 0;
        if(mAdvert.didAdvertiserAddIncentive()){
            incentiveAmm = (mAdvert.getWebClickIncentive()*(mAdvert.getNumberOfUsersToReach()-mAdvert.getWebClickNumber()) );
            Log.d("TomorrowsAdsItem","incentive amount: "+incentiveAmm);
        }
        double am = (mAdvert.getNumberOfUsersToReach()*(mAdvert.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES))+vat+incentiveAmm;

        am = round(am);
        String amount = Double.toString(am);
        mAmountToReimburse.setText(String.format("Paid amount : %s Ksh.", amount));
        if(mAdvert.isFlagged()) {
            mTakeDown.setText("Put Up.");
            mFlagged.setText("Status : Taken Down.");
        } else {
            mTakeDown.setText("Take Down.");
            mFlagged.setText("Status : Uploaded.");
        }
        loadListeners();
    }

    private void setImage(){
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            Log.d("TomorrowsAdItem---","Image has been converted to bitmap.");
            mImageBytes = bitmapToByte(getResizedBitmap(bm,300));
            mAdvert.setImageBitmap(bm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void loadListeners() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay()).child(mAdvert.getPushRefInAdminConsole());
        DatabaseReference dbRef = query.getRef();
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("TOMORROWS_AD_STAT_ITEM","Listener from firebase has responded. Updating data.");
                if(dataSnapshot.getKey().equals("flagged")) {
                    boolean newValue = dataSnapshot.getValue(boolean.class);
                    Log.d("TOMORROWS_AD_STAT_ITEM", "New value gotten from firebase --" + newValue);
                    mAdvert.setFlagged(newValue);
                    if (mAdvert.isFlagged()) {
                        mTakeDown.setText("Put Up.");
                        mFlagged.setText("Status : Taken Down.");
                    } else {
                        mTakeDown.setText("Take Down.");
                        mFlagged.setText("Status : Uploaded.");
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Click(R.id.viewCard)
    private void onClick(){
//        if(!mAdvert.isAdminFlagged()) {
//            Variables.adToBeFlagged = mAdvert;
//            Intent intent = new Intent("TAKE_DOWN");
//            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
//            if (!mAdvert.isFlagged()) {
//                Variables.areYouSureTakeDownText = "Are you sure you want to take down your ad?";
//            } else {
//                Variables.areYouSureTakeDownText = "Are you sure you want to put back up your ad?";
//            }
//        }else{
//            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("CANT_TAKE_DOWN_AD"));
//        }

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("VIEW_AD_TELEMETRIES"));
        Variables.adToBeViewedInTelemetries = mAdvert;
    }




    private String getNextDay(){
        return TimeManager.getNextDay();

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }




    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private class LongOperationFI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try{
                setImage();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(mImageBytes!=null) Glide.with(mContext).load(mImageBytes).into(mImage);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
