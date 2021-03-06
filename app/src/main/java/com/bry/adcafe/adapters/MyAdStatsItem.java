package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
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
import com.google.firebase.auth.FirebaseAuth;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bryon on 19/11/2017.
 */


@NonReusable
@Layout(R.layout.my_ad_stat_item)
public class MyAdStatsItem {
    @View(R.id.adImage) private ImageView mAdImage;
    @View(R.id.EmailText) private TextView mEmail;
    @View(R.id.TargetedNumber) private TextView mTargetedNumber;
    @View(R.id.usersReachedSoFar) private TextView mUsersReachedSoFar;
    @View(R.id.AmountToReimburse) private TextView mAmountToReimburse;
    @View(R.id.hasBeenReimbursed) private TextView mHasBeenReimbursed;
    @View(R.id.dateUploaded) private TextView mDateUploaded;
//    @View(R.id.reimburseBtn) private Button mReimburseButton;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    private DatabaseReference dbRef;
    private byte[] mImageBytes;
    private boolean isClickable = false;

    public MyAdStatsItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    public void onResolved(){
        if(mImageBytes==null) new LongOperationFI().execute("");
        else loadImage2();

        mEmail.setText(String.format("Uploaded by : %s", mAdvert.getUserEmail()));
        if(mAdvert.getUserEmail().equals(Constants.ADMIN_ACC))mEmail.setText("Uploaded by : me@myGeemail.com");
        mTargetedNumber.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));
        mDateUploaded.setText(String.format("Uploaded on %s", getDateFromDays(mAdvert.getDateInDays())));
        if(!mAdvert.isFlagged()){
            mUsersReachedSoFar.setText("Users reached : "+mAdvert.getNumberOfTimesSeen());
        }else{
            mUsersReachedSoFar.setText("Users reached : "+mAdvert.getNumberOfTimesSeen()+" (Taken Down).");
        }

        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
        int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd*
                (mAdvert.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);
        double vat = (numberOfUsersWhoDidntSeeAd*(Variables.getUserCpvFromTotalPayPerUser(mAdvert.getAmountToPayPerTargetedView())))
                *Constants.VAT_CONSTANT;

        double incentiveAmm = 0;
        if(mAdvert.didAdvertiserAddIncentive()){
            incentiveAmm = (mAdvert.getWebClickIncentive()* (mAdvert.getNumberOfUsersToReach()-mAdvert.getWebClickNumber()) );
        }

        double totalReimbursalPlusPayout = (double)ammountToBeRepaid+mAdvert.getPayoutReimbursalAmount()+vat+incentiveAmm;
        String number = Double.toString(round(totalReimbursalPlusPayout));

        mAmountToReimburse.setText("Reimbursing amount: "+number+" Ksh");
        try{
            if(totalReimbursalPlusPayout==0){
                mHasBeenReimbursed.setText("Status: All Users Reached.");
                mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
            }else {
                if (mAdvert.isHasBeenReimbursed()) {
                    mHasBeenReimbursed.setText("Status: Reimbursed.");
                    mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
                } else {
                    mHasBeenReimbursed.setText("Status: NOT Reimbursed.");
                    mAmountToReimburse.setText("Reimbursing ammt: " + number + "Ksh");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!mAdvert.isHasBeenReimbursed() && isCardForYesterdayAds() && ammountToBeRepaid !=0 ){
//            mReimburseButton.setVisibility(android.view.View.VISIBLE);
            isClickable = true;
            addListenerForPayoutSessions();
        }else{
            isClickable = false;
//            if(isCardForYesterdayAds()) mReimburseButton.setVisibility(android.view.View.VISIBLE);
//            mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
        }
        loadListeners();
    }

    private void setImage() {
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            Log("SavedAdsCard---","Image has been converted to bitmap.");
            mImageBytes = bitmapToByte(getResizedBitmap(bm,250));
            mAdvert.setImageBitmap(bm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadImage2(){
        Glide.with(mContext).load(mImageBytes).into(mAdImage);
    }

    private void loadImage(){
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            mAdvert.setImageBitmap(bm);
            Log("MY_AD_STAT_ITEM---","Image has been converted to bitmap and set in model instance.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Glide.with(mContext).load(bitmapToByte(getResizedBitmap(mAdvert.getImageBitmap(),150))).into(mAdImage);
    }

    @Click(R.id.viewCard)
    private void onClick(){
//        if(isClickable) {
//            Variables.adToBeReimbursed = mAdvert;
//            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("START_ADVERTISER_PAYOUT"));
//        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("VIEW_AD_TELEMETRIES"));
        Variables.adToBeViewedInTelemetries = mAdvert;
    }


    private void loadListeners() {
        String tme = getDate();
        if(isCardForYesterdayAds())tme = TimeManager.getPreviousDay();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(tme).child(mAdvert.getPushRefInAdminConsole());
        Log.d("MyAdStatItem","Adding listener for : "+mAdvert.getPushRefInAdminConsole());
        Log.d("MyAdStatItem","Query set up is: "+Constants.ADS_FOR_CONSOLE+" : "+getDate()+" : "
            +mAdvert.getPushRefInAdminConsole());
        dbRef = query.getRef();
        dbRef.addChildEventListener(chil);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemovingEventListeners
                ,new IntentFilter("REMOVE-LISTENERS"));
    }

    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log("MY_AD_STAT_ITEM","Listener from firebase has responded.Updating users reached so far");
            if(dataSnapshot.getKey().equals("flagged")){
                mAdvert.setFlagged(dataSnapshot.getValue(Boolean.class));
                if(!mAdvert.isFlagged()){
                    mUsersReachedSoFar.setText("Users reached : "+mAdvert.getNumberOfTimesSeen());
                }else{
                    mUsersReachedSoFar.setText("Users reached : "+mAdvert.getNumberOfTimesSeen()+" (Taken Down).");
                }
            }else
            if(dataSnapshot.getKey().equals("websiteLink")){
                mAdvert.setWebsiteLink(dataSnapshot.getValue(String.class));
            }else
            if(dataSnapshot.getKey().equals("advertiserPhoneNo")){
                mAdvert.setAdvertiserPhoneNo(dataSnapshot.getValue(String.class));
            }else
            if(dataSnapshot.getKey().equals(Constants.USERS_THAT_HAVE_CLICKED_IT)){

            }else
            if(dataSnapshot.getKey().equals(Constants.USERS_THAT_HAVE_SEEN)){

            }else
            if(dataSnapshot.getKey().equals("numberOfPins")){
                mAdvert.setNumberOfPins(dataSnapshot.getValue(Integer.class));
            }else
            if(dataSnapshot.getKey().equals("webClickNumber")){
                mAdvert.setWebClickNumber(dataSnapshot.getValue(Integer.class));

                int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
                int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd*
                        (mAdvert.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);
                double vat = (mAdvert.getNumberOfUsersToReach()*Variables.getUserCpvFromTotalPayPerUser(
                        mAdvert.getAmountToPayPerTargetedView())) *Constants.VAT_CONSTANT;

                double incentiveAmm = 0;
                if(mAdvert.didAdvertiserAddIncentive()){
                    incentiveAmm = (mAdvert.getWebClickIncentive()* (mAdvert.getNumberOfUsersToReach()-mAdvert.getWebClickNumber()) );
                }

                double totalReimbursalPlusPayout = (double)ammountToBeRepaid+mAdvert.getPayoutReimbursalAmount()+vat+incentiveAmm;
                String number = Double.toString(round(totalReimbursalPlusPayout));

                mAmountToReimburse.setText("Reimbursing ammt: " + number + "Ksh");
                if(totalReimbursalPlusPayout==0){
                    mHasBeenReimbursed.setText("Status: All Users Reached.");
                    mAmountToReimburse.setText("Reimbursing :  0 Ksh");
                    isClickable = false;
//                    mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
                }
            }else
            if(dataSnapshot.getKey().equals("payoutReimbursalAmount")){
                mAdvert.setPayoutReimbursalAmount(dataSnapshot.getValue(double.class));

                int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
                int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd*
                        (mAdvert.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);
                double vat = (mAdvert.getNumberOfUsersToReach()*Variables.getUserCpvFromTotalPayPerUser(
                        mAdvert.getAmountToPayPerTargetedView())) *Constants.VAT_CONSTANT;

                double incentiveAmm = 0;
                if(mAdvert.didAdvertiserAddIncentive()){
                    incentiveAmm = (mAdvert.getWebClickIncentive()* (mAdvert.getNumberOfUsersToReach()-mAdvert.getWebClickNumber()) );
                }

                double totalReimbursalPlusPayout = (double)ammountToBeRepaid+mAdvert.getPayoutReimbursalAmount()+vat+incentiveAmm;
                String number = Double.toString(round(totalReimbursalPlusPayout));

                mAmountToReimburse.setText("Reimbursing ammt: " + number + "Ksh");
                if(totalReimbursalPlusPayout==0){
                    mHasBeenReimbursed.setText("Status: All Users Reached.");
                    mAmountToReimburse.setText("Reimbursing :  0 Ksh");
                    isClickable = false;
//                    mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
                }
            }else{
                if(dataSnapshot.getKey().equals("numberOfTimesSeen")) {
                    try {
                        int newValue = dataSnapshot.getValue(int.class);
                        Log("MY_AD_STAT_ITEM", "New value gotten from firebase --" + newValue);
                        mAdvert.setNumberOfTimesSeen(newValue);
                        mUsersReachedSoFar.setText("Users reached so far : " + newValue);
                        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach() - newValue;

                        int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd *
                                (mAdvert.getAmountToPayPerTargetedView() + Constants.MPESA_CHARGES);
                        double vat = (mAdvert.getNumberOfUsersToReach() * Variables.getUserCpvFromTotalPayPerUser(
                                mAdvert.getAmountToPayPerTargetedView())) * Constants.VAT_CONSTANT;

                        double incentiveAmm = 0;
                        if (mAdvert.didAdvertiserAddIncentive()) {
                            incentiveAmm = (mAdvert.getWebClickIncentive() * (mAdvert.getNumberOfUsersToReach() - mAdvert.getWebClickNumber()));
                        }

                        double totalReimbursalPlusPayout = (double) ammountToBeRepaid + mAdvert.getPayoutReimbursalAmount() + vat + incentiveAmm;
                        String number = Double.toString(round(totalReimbursalPlusPayout));

                        mAmountToReimburse.setText("Reimbursing ammt: " + number + "Ksh");
                        if (totalReimbursalPlusPayout == 0) {
                            mHasBeenReimbursed.setText("Status: All Users Reached.");
                            mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
                            isClickable = false;
//                        mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(dataSnapshot.getKey().equals("hasBeenReimbursed")) {
                    try {
                        boolean newValue = dataSnapshot.getValue(boolean.class);
                        Log("ADMIN_STAT_ITEM", "New value gotten from firebase --" + newValue);
                        mAdvert.setHasBeenReimbursed(newValue);
                        try {
                            if (mAdvert.isHasBeenReimbursed()) {
                                mHasBeenReimbursed.setText("Status: Reimbursed.");
                                mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
                                if (isCardForYesterdayAds())
//                                mReimburseButton.setVisibility(android.view.View.GONE);
                                    isClickable = false;
                                removeListenerForPayoutSessions();
//                                mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
                            } else {
                                mHasBeenReimbursed.setText("Status: NOT Reimbursed.");

                                int numberOfUsersWhoDidntSeeAd = (mAdvert.getNumberOfUsersToReach() - mAdvert.getNumberOfTimesSeen());
                                int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd *
                                        (mAdvert.getAmountToPayPerTargetedView() + Constants.MPESA_CHARGES);
                                double vat = (mAdvert.getNumberOfUsersToReach() * Variables.getUserCpvFromTotalPayPerUser(
                                        mAdvert.getAmountToPayPerTargetedView())) * Constants.VAT_CONSTANT;

                                double incentiveAmm = 0;
                                if (mAdvert.didAdvertiserAddIncentive()) {
                                    incentiveAmm = (mAdvert.getWebClickIncentive() * (mAdvert.getNumberOfUsersToReach() - mAdvert.getWebClickNumber()));
                                }

                                double totalReimbursalPlusPayout = (double) ammountToBeRepaid + mAdvert.getPayoutReimbursalAmount() + vat + incentiveAmm;
                                String number = Double.toString(round(totalReimbursalPlusPayout));

                                mAmountToReimburse.setText("Reimbursing ammt: " + number + "Ksh");
                                if (totalReimbursalPlusPayout == 0) {
                                    mHasBeenReimbursed.setText("Status: All Users Reached.");
                                    mAmountToReimburse.setText("Reimbursing amount:  0 Ksh");
                                    isClickable = false;
//                                mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
                                    removeListenerForPayoutSessions();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
    };

    private BroadcastReceiver mMessageReceiverForRemovingEventListeners = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(dbRef!=null) dbRef.removeEventListener(chil);
            removeListenerForPayoutSessions();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForHideBtnsBecauseOfPayoutSessionStart = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hidePayoutButtons();
        }
    };

    private void addListenerForPayoutSessions(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForHideBtnsBecauseOfPayoutSessionStart,
                new IntentFilter(Constants.IS_MAKING_PAYOUT+"true"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForShowBtnsBecauseOfPayoutSessionStop,
                new IntentFilter(Constants.IS_MAKING_PAYOUT+"false"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForCollapsedCard,
                new IntentFilter("COLLAPSED_CARD"));
    }

    private void removeListenerForPayoutSessions(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForHideBtnsBecauseOfPayoutSessionStart);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowBtnsBecauseOfPayoutSessionStop);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForCollapsedCard);
    }

    private BroadcastReceiver mMessageReceiverForShowBtnsBecauseOfPayoutSessionStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPayoutButtons();
        }
    };

    private BroadcastReceiver mMessageReceiverForCollapsedCard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    private String getDate(){
        return TimeManager.getDate();
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        Bitmap newBm = getResizedBitmap(bitm,500);
        return newBm;
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

    private String getDateFromDays(long days){
        long currentTimeInMills = days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log("Splash","Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        String yearName;

        if(year == year2){
            Log("My_ad_stat_item","Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log("My_ad_stat_item","Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        return dayOfMonth+" "+monthName+yearName;
    }




    private String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
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
            if(mImageBytes!=null) loadImage2();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private boolean isCardForYesterdayAds(){
        return mAdvert.getDateInDays()+1 < getDateInDays();
    }

    private long getDateInDays(){
        return TimeManager.getDateInDays();
    }

    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals(Constants.ADMIN_ACC)) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void hidePayoutButtons(){
//        mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
        isClickable = false;
    }

    private void showPayoutButtons(){
//        mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        isClickable = true;
    }

    public static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}
