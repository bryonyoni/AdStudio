package com.bry.adcafe.adapters;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Animate;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import butterknife.OnItemLongClick;

/**
 * Created by bryon on 05/09/2017.
 */

@NonReusable
@Layout(R.layout.saved_ads_list_item)
public class SavedAdsCard {
    @View(R.id.SavedImageView) private ImageView imageView;
    @View(R.id.savedErrorImageView) private ImageView errorImageView;
    @View(R.id.savedAdCardAvi) private AVLoadingIndicatorView mAvi;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    public String mId;
    private ProgressDialog mAuthProgressDialog;
    private boolean hasMessageBeenSeen = false;
    private boolean onDoublePressed = false;
    private boolean isBeingShared = false;

    private long noOfDaysDate;
    private SavedAdsCard sac;
    private boolean mIsLastElement;
    private byte[] mImageBytes;
    private boolean hasLoaded =false;


    public SavedAdsCard(Advert advert, Context context, PlaceHolderView placeHolderView,String pinID,long noOfDays,boolean isLastElement) {
        mAdvert = advert;
        mContext = context;
        mPlaceHolderView = placeHolderView;
        noOfDaysDate = noOfDays;
        this.mIsLastElement = isLastElement;
    }

    @Resolve
    private void onResolved() {
        if(mImageBytes!=null) loadImage2();
        else new LongOperationFI().execute("");

//        if(mImageBytes==null) new LongOperationFI().execute("");

       LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
               new IntentFilter("UNREGISTER"));
       LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForAddNewBlank,
                new IntentFilter("ADD_BLANK"+noOfDaysDate+mAdvert.getPushId()));
       sac = this;
    }

    private void setImage() {
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            Log.d("SavedAdsCard---","Image has been converted to bitmap.");
            mImageBytes = bitmapToByte(getResizedBitmap(bm,170));
            mAdvert.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImage2(){
        Glide.with(mContext).load(mImageBytes).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.VISIBLE);
                }catch (Exception e2){
                    e2.printStackTrace();
                }
                Log.d("SavedAdsCard---",e.getMessage());
                if(!isInternetAvailable()&& !hasMessageBeenSeen){
                    hasMessageBeenSeen = true;
                    Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                }
               hasLoaded = true;
                return false;
            }
        }).into(imageView);
    }

    private void loadImage(){
//        mAvi.setVisibility(android.view.View.VISIBLE);
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            mAdvert.setImageBitmap(bm);
            Log.d("SavedAdsCard---","Image has been converted to bitmap and set in model instance.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Glide.with(mContext).load(bitmapToByte(getResizedBitmap(mAdvert.getImageBitmap(),170))).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.VISIBLE);
                }catch (Exception e2){
                    e2.printStackTrace();
                }
                if(mIsLastElement){
                    Intent intent = new Intent("DONE!!");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                Log.d("SavedAdsCard---",e.getMessage());
                if(!isInternetAvailable()&& !hasMessageBeenSeen){
                    hasMessageBeenSeen = true;
                    Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(mIsLastElement){
                    Intent intent = new Intent("DONE!!");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                return false;
            }
        }).into(imageView);
    }

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unregister all receivers");
            try{LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpin);
            }catch (Exception e){
                e.printStackTrace();
            }
            try{LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddNewBlank);
            }catch (Exception e){
                e.printStackTrace();
            }
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForUnpin = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to Unpin ad");
            unPin();
            removeReceiver();
        }
    };

    private BroadcastReceiver mMessageReceiverForUnpin2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to Unpin ad");
            unPin();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForAddNewBlank = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to ad blank item.");
            pushBlank();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    @LongClick(R.id.SavedImageView)
    private void onLongClick(){
        promptUserIfSureToUnpinAd();
    }

    @Click(R.id.SavedImageView)
    private void onClick(){
        if(onDoublePressed){
            shareAd();
        }else{
            viewAd();
        }
        onDoublePressed = true;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                onDoublePressed=false;
            }
        }, 200);
    }

    private void promptUserIfSureToUnpinAd(){
        if(isNetworkConnected(mContext)){
            Variables.adToBeUnpinned = mAdvert;
            Variables.noOfDays = noOfDaysDate;
            Variables.placeHolderView = mPlaceHolderView;
            Variables.position = mPlaceHolderView.getViewResolverPosition(this);
            Intent intent = new Intent("ARE_YOU_SURE2");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnpin2
                    ,new IntentFilter(mAdvert.getPushRefInAdminConsole()));
        }else{
            Toast.makeText(mContext,"You need an internet connection to unpin that.",Toast.LENGTH_SHORT).show();
        }

    }

    private void viewAd() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if(!isBeingShared && hasLoaded) {
                    Log.d("SavedAdsCard", "Setting the ad to be viewed.");
                    Variables.adToBeViewed = mAdvert;
                    Variables.adToBeUnpinned = mAdvert;
                    Variables.noOfDays = noOfDaysDate;
                    Variables.placeHolderView = mPlaceHolderView;
                    Variables.position = mPlaceHolderView.getViewResolverPosition(this);
                    Intent intent = new Intent("VIEW");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    setUpReceiver();
                }
                isBeingShared = false;
            }
        }, 230);
    }

    private void setUpReceiver(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnpin
                ,new IntentFilter(mAdvert.getPushRefInAdminConsole()));
    }

    private void removeReceiver(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpin);
    }

    private void shareAd() {
        isBeingShared = true;
        Log.d("SavedAdsCard","Setting the ad to be shared.");
        Variables.adToBeShared = mAdvert;
        Intent intent = new Intent("SHARE");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }



    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.toString().equals("");
        } catch (Exception e) {
            return false;
        }

    }


    private void unPin(){
        String id;
        try{
            id = mAdvert.getPushId();
        }catch (Exception e){
            e.printStackTrace();
            id = Variables.adToBeUnpinned.getPushId();
        }
        try{
            mPlaceHolderView.removeView(this);
        }catch (Exception e){
            e.printStackTrace();
            Variables.placeHolderView.removeView(this);
        }
        Intent intent = new Intent("EQUATE_PLACEHOLDER_VIEWS");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        Log.d("SAVED_ADS_CARD--","Removing pinned ad"+id);
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(noOfDaysDate)).child(id);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
                Intent intent = new Intent(Constants.REMOVE_PINNED_AD);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent(Constants.UNABLE_TO_REMOVE_PINNED_AD);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
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

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void pushBlank(){
        try{
            mPlaceHolderView.getViewResolverPosition(sac);
            mPlaceHolderView.addViewAfter(sac,new BlankItem(mContext,mPlaceHolderView,noOfDaysDate,"pineapples",false));
        }catch (Exception e){
            e.printStackTrace();
            try{
//                Variables.placeHolderView.getViewResolverPosition(sac);
                mPlaceHolderView = Variables.placeHolderView;
                mPlaceHolderView.getViewResolverPosition(sac);
                try{
                    mPlaceHolderView.addViewAfter(sac,new BlankItem(mContext,mPlaceHolderView,noOfDaysDate,"pineapples",false));
                }catch (Exception e3){
                    e3.printStackTrace();
                    mPlaceHolderView.addViewAfter(mPlaceHolderView.getViewResolverPosition(sac),new BlankItem(mContext,mPlaceHolderView,noOfDaysDate,"pineapples",false));
                }
            }catch (Exception e2){
                e2.printStackTrace();
            }
        }
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
            if(mIsLastElement){
//                Intent intent = new Intent("DONE!!");
//                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }

        @Override
        protected void onPreExecute() {
            mAvi.setVisibility(android.view.View.VISIBLE);
            super.onPreExecute();
        }
    }
}

