package com.bry.adcafe.services;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.AdCoin;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.Message;
import com.bry.adcafe.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import android.telephony.TelephonyManager;
import android.view.View;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bryon on 24/11/2017.
 */

public class DatabaseManager {
    public static final String TAG = DatabaseManager.class.getSimpleName();
    private String mKey = "";
    private int numberOfSubs = 0;
    private int iterations = 0;
    private Context DBContext;
    private List<String> categoryList = new ArrayList<>();
    private boolean isUserAddingANewCategory = false;
    private int iterationsForResettingCPV = 0;
    private int cyclecount = 0;
    private int currentCyclenumber = 0;



    public void setContext(Context context) {
        this.DBContext = context;
    }


    ////Create user methods//////

    public void createUserSpace(final Context mContext) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DBContext = mContext;

        //Creates nodes for totals seen today and sets them to 0;
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0);

        //Creates node for totals amount earned so far and sets it to 0;
        DatabaseReference adRef9 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.REIMBURSEMENT_TOTALS);
        adRef9.setValue(0);


        DatabaseReference adRef10 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.RESET_ALL_SUBS_BOOLEAN);
        adRef10.setValue(false);

        DatabaseReference adRef11 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTIF);
        adRef11.setValue(true);

        DatabaseReference adRef12 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTF_HOUR);
        adRef12.setValue(Variables.preferredHourOfNotf);


        DatabaseReference adRef13 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTF_MIN);
        adRef13.setValue(Variables.preferredMinuteOfNotf);

        DatabaseReference adRef14 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.USER_NICKNAME);
        adRef14.setValue(Variables.userName);

        DatabaseReference adRef15 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.USER_PASSCODE);
        DatabaseReference adRef16 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.IS_PASSWORD_ENCRYPTED);
        adRef15.setValue(Variables.encryptPassword(Variables.getPassword()));
        adRef16.setValue(true);


        //Creates node for indicating users email.
        DatabaseReference adRef8 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child("Email");
        adRef8.setValue(user.getEmail());

        //Creates nodes for totals seen all month and sets them to 0;
        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        adRef2.setValue(0);

        //Creates node for users current subscription being viewed and setting it to 0.
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        adRef3.setValue(0);


        //Creates node for the current ad being seen by user in specific subscription and setting it to 0.
        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        adRef4.setValue(0);

        setIsMakingPayoutInFirebase(false);

        //sets the date for when last used in firebase.
        DatabaseReference adRef7 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef7.setValue(getDate()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(Constants.CREATE_USER_SPACE_COMPLETE);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

        getAndUpdateUsersKnownEULAversion(mContext);
    }

    private void getAndUpdateUsersKnownEULAversion(final Context context){
        DatabaseReference eulaRef = FirebaseDatabase.getInstance().getReference(Constants.EULA_REFERENCE);
        eulaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    int eulaVersion = dataSnapshot.getValue(Integer.class);
                    SharedPreferences pref2 = context.getSharedPreferences(Constants.EULA_REFERENCE, MODE_PRIVATE);
                    SharedPreferences.Editor editor2 = pref2.edit();
                    editor2.clear().putInt(Constants.EULA_REFERENCE, eulaVersion).apply();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void unSubscribeUserFormAdvertCategory(String AdvertCategory, int clusterIDInCategory) {
        FlagSubscriptionThenUnsubscribeUser(AdvertCategory, clusterIDInCategory);
    }

    private void FlagSubscriptionThenUnsubscribeUser(final String AdvertCategory, final int clusterIDInCategory) {
        Log(TAG, "Unsubscribing user from cluster " + AdvertCategory);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(Variables.constantAmountPerView)).child(AdvertCategory);
        DatabaseReference dbref = dbRef.push();
        dbref.setValue(clusterIDInCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                removeSpecificAdCategryFromUserSpaceAndSubscriptions(AdvertCategory, clusterIDInCategory);
            }
        });
    }

    private void removeSpecificAdCategryFromUserSpaceAndSubscriptions(final String AdvertCategory, int Cluster) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log(TAG, "Removing user from subscription");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST)
                .child(Integer.toString(Variables.constantAmountPerView))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.removeValue();

        Log(TAG, "Removing category " + AdvertCategory + " from users categories list.");
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!Variables.didAdCafeRemoveCategory) {
                    if (getPositionOf(AdvertCategory) == Variables.getCurrentSubscriptionIndex()) {
                        Log(TAG, "The category being removed is currently being viewed");
                        if (Variables.getCurrentSubscriptionIndex() > 0)
                            Variables.setCurrentSubscriptionIndex((Variables.getCurrentSubscriptionIndex() - 1));
                        else
                            Variables.setCurrentSubscriptionIndex((Variables.getCurrentSubscriptionIndex() + 1));
                    }
                    String categoryBeingViewed = getSubscriptionValue(Variables.getCurrentSubscriptionIndex());
                    Log(TAG, "the current category being removed is " + categoryBeingViewed);
                    Variables.Subscriptions.remove(AdvertCategory);

                    int newIndex = getPositionOf(categoryBeingViewed);
                    Log(TAG, "Its new index position is : " + newIndex);
                    Variables.setCurrentSubscriptionIndex(newIndex);

                    updateCurrentSubIndex();
                    setUserDataInSharedPrefs(DBContext);
                    Variables.hasChangesBeenMadeToCategories = true;

                    Intent intent = new Intent(Constants.FINISHED_UNSUBSCRIBING);
                    LocalBroadcastManager.getInstance(DBContext).sendBroadcast(intent);
                }
            }
        });

    }


    public void subscribeUserToSpecificCategory(String AdvertCategory) {
        numberOfSubs = 1;
        isUserAddingANewCategory = true;
        generateClusterIDFromCategoryFlaggedClusters(AdvertCategory);
    }

    public void setUpUserSubscriptions(List<String> subscriptions) {
        numberOfSubs = subscriptions.size();
        setUsersPreferredChargePerView();
        Variables.Subscriptions.clear();
        for (String sub : subscriptions) {
            generateClusterIDFromCategoryFlaggedClusters(sub);
        }
    }

    private void setUsersPreferredChargePerView() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CONSTANT_AMMOUNT_PER_VIEW);
        adRef.setValue(Variables.constantAmountPerView);
    }


    private void generateClusterIDFromCategoryFlaggedClusters(final String AdvertCategory) {
        Variables.setMonthAdTotals(mKey, 0);
        Variables.setAdTotal(0, mKey);
        Log(TAG, "--Generating clusterID from flagged ads.");

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(Variables.constantAmountPerView)).child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Log(TAG, "Flagged clusters has got children in it.");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        int clusterIDInCategory = snap.getValue(int.class);
                        Log(TAG, "Cluster id gotten from Flagged cluster is --" + clusterIDInCategory);
//                        User.setID(clusterIDInCategory,mKey);
                        removeIdThenSubscribeUser(snap.getKey(), AdvertCategory, clusterIDInCategory);
                        break;
                    }
                } else {
                    Log(TAG, "--Flagged cluster in Category has got no children in it. Generating normally");
                    generateClusterIDFromCategory(AdvertCategory);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void generateClusterIDFromCategory(final String AdvertCategory) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView))
                .child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This loads the current cluster in AdCategory
                long currentCluster;
                if (dataSnapshot.getChildrenCount() == 0) {
                    currentCluster = dataSnapshot.getChildrenCount() + 1;
                    Log(TAG, "The adCategory is empty.Setting current cluster to 1");
                } else {
                    currentCluster = dataSnapshot.getChildrenCount();
                    Log(TAG, "The latest current cluster in " + AdvertCategory + " category is :" + currentCluster);
                }

                //this loads number of users in the current cluster.
                DataSnapshot UsersInCurrentCluster = dataSnapshot.child(Integer.toString((int) currentCluster));
                long numberOfUsersInCurrentCluster;
                if (UsersInCurrentCluster.getChildrenCount() == 0) {
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount();
                    Log(TAG, "--number of users in current clusters category is --" + numberOfUsersInCurrentCluster);
                } else {
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount() + 1;
                    Log(TAG, "--number of users in current clusters category is --" + numberOfUsersInCurrentCluster);
                }

                //this checks if the number of users in cluster exceeds limit
                int clusterIDForSpecificCategory;
                if (numberOfUsersInCurrentCluster < Constants.NUMBER_OF_USERS_PER_CLUSTER) {
                    Log(TAG, "--Number of users in the current cluster is less than limit.setting AdCategory cluster to --" + currentCluster);
                    clusterIDForSpecificCategory = (int) currentCluster;
                } else {
                    Log(TAG, "--Number of users in the current cluster exceeds limit.setting AdCategory cluster to --" + (currentCluster + 1));
                    clusterIDForSpecificCategory = (int) currentCluster + 1;
                }
                subscribeUserToAdvertCategoryAndAddCategoryToUserList(AdvertCategory, clusterIDForSpecificCategory);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeIdThenSubscribeUser(String key, final String AdvertCategory, final int clusterIDInCategory) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(Variables.constantAmountPerView)).child(AdvertCategory).child(key);
        dbr.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log(TAG, "---Removed ClusterId From Flagged Clusters. Subscribing user to AdCategory.");
                subscribeUserToAdvertCategoryAndAddCategoryToUserList(AdvertCategory, clusterIDInCategory);
            }
        });
    }

    private void subscribeUserToAdvertCategoryAndAddCategoryToUserList(final String AdvertCategory, final int Cluster) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //this subscribes user to Cluster in Advert Category
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.setValue(uid);

        //this ads the advertCategory subscribed to by user to user space
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.setValue(Cluster).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iterations++;
                if (iterations == numberOfSubs) {
                    if (isUserAddingANewCategory) {
                        loadNewSubList();
                        isUserAddingANewCategory = false;
                    } else {
                        setDateInSharedPrefs(getDate(), DBContext);
                        reloadUsersSubscriptions(Constants.SET_UP_USERS_SUBSCRIPTION_LIST);
                    }
                }
            }
        });

        if (!isUserAddingANewCategory) Variables.Subscriptions.put(AdvertCategory, Cluster);

    }


    public void setNumberOfSubscriptionsUserKnowsAbout(int number) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.NO_OF_CATEGORIES_KNOWN);
        adRef.setValue(number);
    }

    ////create User Methods.//////////////////////////////////////////////////////////////////////


    private void doAbsolutelyNothing() {

    }

    ////Load users data methods.///

    public void loadUserData(final Context mContext) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CATEGORY_LIST);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String generalCategory = snap.getKey();
                    List<String> categoriesList = new ArrayList<>();
                    for (DataSnapshot snapMini : snap.getChildren()) {
                        String category = snapMini.getValue(String.class);
                        if(doesCategoryImageExists(mContext,category)){
                            categoryList.add(category);
                            categoriesList.add(category);
                        }
                    }
                    if(!categoriesList.isEmpty())Variables.newCategories.put(generalCategory,categoriesList);
                }
                loadAnyAnnouncements(mContext);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, "Please check your internet connection.", Toast.LENGTH_LONG).show();
                Log(TAG, "There was a database error " + databaseError.getMessage());
            }
        });
    }

    private void loadAnyAnnouncements(final Context context) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.TEXT_ANOUNCEMENTS).child(getDate());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Variables.announcements = dataSnapshot.getValue(String.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Variables.announcements = "";
                }
                loadUserDataNow(context);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadUserDataNow(final Context mContext) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log(TAG, "Starting to load users data");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setIsMakingPayoutInFirebase(false);
                int numberToMinus = 0;
                //this loads month totals
                DataSnapshot monthAdTotalSnap = dataSnapshot.child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
                int monthTotal = monthAdTotalSnap.getValue(int.class);
                Variables.setMonthAdTotals(mKey, monthTotal);
                Log(TAG, "Setting month total to : " + monthTotal);

                //this loads the users reimbursement totals.
                DataSnapshot reimbursementAmountSnap = dataSnapshot.child(Constants.REIMBURSEMENT_TOTALS);
                int reimbursementAmount = reimbursementAmountSnap.getValue(int.class);
                Variables.setTotalReimbursementAmount(reimbursementAmount);
                Log(TAG, "Setting reimbursement total to : " + reimbursementAmount);

                //this loads and sets users AdCoins in shared preferences;
                DataSnapshot myCoinsSnap = dataSnapshot.child(Constants.USERS_COIN_LIST);
                if(myCoinsSnap.exists()){
                    String myCoinsString = myCoinsSnap.getValue(String.class);
                    setUsersCoinsListInSharedPrefs(myCoinsString,mContext);
                }

                //this loads the users preferred amount per ad view.
                DataSnapshot amountPerViewSnap = dataSnapshot.child(Constants.CONSTANT_AMMOUNT_PER_VIEW);
                if (amountPerViewSnap.exists()) {
                    int amountPerView = amountPerViewSnap.getValue(int.class);
                    Variables.constantAmountPerView = amountPerView;
                    Log(TAG, "Setting the amount per ad to total to : " + amountPerView);
                }

                //loads users password for payouts.
                DataSnapshot passwordSnap = dataSnapshot.child(Constants.USER_PASSCODE);
                DataSnapshot isEncryptedSnap = dataSnapshot.child(Constants.IS_PASSWORD_ENCRYPTED);
                if(isEncryptedSnap.exists()){
                    boolean isEncrypted = isEncryptedSnap.getValue(Boolean.class);
                    if(isEncrypted){
                        if (!Variables.isGottenNewPasswordFromLogInOrSignUp) {
                            Variables.setPassword(Variables.decryptPassword(passwordSnap.getValue(String.class)));
                            Variables.isGottenNewPasswordFromLogInOrSignUp = false;
                        }
                    }else{
                        if (!Variables.isGottenNewPasswordFromLogInOrSignUp) {
                            Variables.setPassword(passwordSnap.getValue(String.class));
                            Variables.isGottenNewPasswordFromLogInOrSignUp = false;
                        }
                    }
                }else{
                    if (!Variables.isGottenNewPasswordFromLogInOrSignUp) {
                        Variables.setPassword(passwordSnap.getValue(String.class));
                        Variables.isGottenNewPasswordFromLogInOrSignUp = false;
                    }
                }

                DataSnapshot notPrefSnap = dataSnapshot.child(Constants.PREFERRED_NOTIF);
                Variables.doesUserWantNotifications = notPrefSnap.getValue(Boolean.class);
                Log(TAG, "Set the preferred value for receiving morning notifications to :" + Variables.doesUserWantNotifications);

                //this set the users preferred notification hour
                DataSnapshot notHour = dataSnapshot.child(Constants.PREFERRED_NOTF_HOUR);
                if (notHour.exists()) Variables.preferredHourOfNotf = notHour.getValue(int.class);

                //this set the users preferred notification minute
                DataSnapshot notMin = dataSnapshot.child(Constants.PREFERRED_NOTF_MIN);
                if (notMin.exists()) Variables.preferredMinuteOfNotf = notMin.getValue(int.class);

                DataSnapshot nameSnap = dataSnapshot.child(Constants.USER_NICKNAME);
                if (nameSnap.exists()) Variables.userName = nameSnap.getValue(String.class);

                //this loads the users no Of Categories known
                DataSnapshot subNoKnown = dataSnapshot.child(Constants.NO_OF_CATEGORIES_KNOWN);
                int subNumberKnown = categoryList.size();
                try{
                  subNumberKnown  = subNoKnown.getValue(int.class);
                }catch (Exception e){
                    e.printStackTrace();
                }
                Log.d(TAG, "Current category list size is: " + categoryList.size());
                if (subNumberKnown < categoryList.size()) {
                    Log(TAG, "Number of subs known is less than current category size.");
                    Variables.didAdCafeAddNewCategory = true;
                    setNumberOfSubscriptionsUserKnowsAbout(categoryList.size());
                }

                //this loads the users known categories
                DataSnapshot subsKnown = dataSnapshot.child(Constants.CATEGORIES_KNOWN);
                if (subsKnown.exists() && Variables.didAdCafeAddNewCategory) {
                    List<String> SublistKnown = new ArrayList<>();
                    for (DataSnapshot snapp : subsKnown.getChildren()) {
                        String snap = snapp.getValue(String.class);
                        SublistKnown.add(snap);
                    }
                    Variables.newSubs.clear();
                    for (String sub : categoryList) {
                        if (!SublistKnown.contains(sub)) {
                            Variables.newSubs.add(sub);
                            Variables.newCategoryList.add(sub);
                            Log.d(TAG, "New sub detected: " + sub);
                        }
                    }
                }
                setUserKnownCategories(categoryList);

                //this loads the users subscription list
                DataSnapshot subscriptionListSnap = dataSnapshot.child(Constants.SUBSCRIPTION_lIST);
                Variables.Subscriptions.clear();
                Variables.NSSubs.clear();
                setNumberOfSubscriptionsUserKnowsAbout(categoryList.size());
                for (DataSnapshot snap : subscriptionListSnap.getChildren()) {
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log(TAG, "Key category gotten from firebase is : " + category + " Value : " + cluster);
                    if (categoryList.contains(category)) {
                        Variables.Subscriptions.put(category, cluster);
                    } else {
                        Variables.NSSubs.add(category);
                        //If the users current subscription index is above the index of the category being removed.
                        if (Variables.getCurrentSubscriptionIndex() >= Variables.Subscriptions.size())
                            numberToMinus++;
                        Log(TAG, "removing category: " + category);
                        unSubscribeUserFormAdvertCategory(category, cluster);
                        Variables.didAdCafeRemoveCategory = true;
                    }
                }

                //this loads the users seen ads list.
                DataSnapshot seenAdsListSnap = dataSnapshot.child(Constants.SEEN_AD_IDS);
                if(!Variables.adsSeenSoFar.isEmpty())Variables.adsSeenSoFar.clear();
                if (seenAdsListSnap.exists()) {
                    for (DataSnapshot pushIdSnap : seenAdsListSnap.getChildren()) {
                        String advertiserId = pushIdSnap.getValue(String.class);
                        String pushId = pushIdSnap.getKey();
                        Variables.adsSeenSoFar.put(pushId, advertiserId);
                    }
                }

                //This loads the users Location Markers list.
                DataSnapshot userLocationMarkersSnap = dataSnapshot.child(Constants.FIREBASE_USERS_LOCATIONS);
                if(!Variables.usersLatLongs.isEmpty())Variables.usersLatLongs.clear();
                if (userLocationMarkersSnap.exists()) {
                    for (DataSnapshot userLocationMark : userLocationMarkersSnap.getChildren()) {
                        double lat = userLocationMark.child("lat").getValue(Double.class);
                        double longit = userLocationMark.child("lng").getValue(Double.class);
                        LatLng latLng = new LatLng(lat, longit);
                        Variables.usersLatLongs.add(latLng);
                    }
                    setMarkersInSharedPrefs(mContext);
                }
                //This loads the users Birthday.
                DataSnapshot birthdaySnap = dataSnapshot.child(Constants.DATE_OF_BIRTH);
                if (birthdaySnap.exists()) {
                    int day = birthdaySnap.child("day").getValue(int.class);
                    int month = birthdaySnap.child("month").getValue(int.class);
                    int year = birthdaySnap.child("year").getValue(int.class);
                    SharedPreferences pref = mContext.getSharedPreferences(Constants.DATE_OF_BIRTH, MODE_PRIVATE);
                    pref.edit().putInt("day", day).putInt("month", month).putInt("year", year).apply();
                }
                //This loads the users gender.
                DataSnapshot genderSnap = dataSnapshot.child(Constants.GENDER);
                if (genderSnap.exists()) {
                    String gender = genderSnap.getValue(String.class);
                    SharedPreferences pref = mContext.getSharedPreferences(Constants.GENDER, MODE_PRIVATE);
                    pref.edit().clear().putString(Constants.GENDER, gender).apply();
                }
                //This loads the consent to target.
                DataSnapshot consentToTarget = dataSnapshot.child(Constants.CONSENT_TO_TARGET);
                if (consentToTarget.exists()) {
                    boolean consent = consentToTarget.getValue(boolean.class);
                    SharedPreferences pref = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
                    pref.edit().clear().putBoolean(Constants.CONSENT_TO_TARGET, consent).apply();
                }

                //this loads the last seen date from firebase
                DataSnapshot dateSnap = dataSnapshot.child(Constants.DATE_IN_FIREBASE);
                String date = dateSnap.getValue(String.class);
                Log(TAG, "Date gotten from firebase is : " + date);
                setDateInSharedPrefs(getDate(), mContext);
                boolean isNewDay = false;

                if (date.equals(getDate())) {
                    Log(TAG, "---Date in firebase matches date in system,thus User was last online today");
                    Log(TAG, "Setting all the normal values from firebase");

                    //this loads today's ad totals.
                    DataSnapshot adTotalSnap = dataSnapshot.child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
                    int adTotal = adTotalSnap.getValue(int.class);
                    Variables.setAdTotal(adTotal, mKey);
                    Log(TAG, "Setting ad total to : " + adTotal);

                    //this loads the current category index
                    DataSnapshot currentSubIndexSnap = dataSnapshot.child(Constants.CURRENT_SUBSCRIPTION_INDEX);
                    int currentSubIndex = currentSubIndexSnap.getValue(int.class);
                    Variables.setCurrentSubscriptionIndex(currentSubIndex - numberToMinus);
                    if (Variables.getCurrentSubscriptionIndex() < 0)
                        Variables.setCurrentSubscriptionIndex(0);
                    Log(TAG, "Setting the current Ad category index to :" + currentSubIndex);

                    //this loads the current ad being seen in the category
                    DataSnapshot currentAdInSubSnap = dataSnapshot.child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
                    int currentAdInSubscription = currentAdInSubSnap.getValue(int.class);
                    Variables.setCurrentAdInSubscription(currentAdInSubscription);
                    Log(TAG, "Setting the current ad being seen in subscription to : " + currentAdInSubscription);

                } else {
                    Log(TAG, "---Date in firebase  does not match date in system , thus User was not online last today");
                    Log(TAG, "---Date from firebase is--" + date + "--while date in system is " + getDate());
                    Log(TAG, "Setting ad total, subscription index and current ad subscription to 0.");

                    Variables.setAdTotal(0, mKey);
                    Variables.setCurrentSubscriptionIndex(0);
                    Variables.setCurrentAdInSubscription(0);
                    resetTotalsInFirebase();
                    clearAdsSeenSoFarInFirebase();
                    setAnnouncementBoolean(mContext);
                    isNewDay = true;

                }
                DataSnapshot isNeedToResetSubsSnap = dataSnapshot.child(Constants.RESET_ALL_SUBS_BOOLEAN);
                if (isNewDay && isNeedToResetSubsSnap.getValue(Boolean.class)) {
                    DataSnapshot newConstantCPVSnap = dataSnapshot.child(Constants.NEW_CPV);
                    int newConstantCPV = newConstantCPVSnap.getValue(Integer.class);
                    int oldConstantCPV = Variables.constantAmountPerView;
                    resetUsersSubscriptionsForNewPrice(oldConstantCPV, newConstantCPV);
                } else {
                    setUserDataInSharedPrefs(mContext);
                    if (isNewDay) setNeedToResetBooleanInSharedPrefs(mContext);
                    if (numberToMinus != 0) setUsersCurrentSubIndexInFireBase();
                    Intent intent = new Intent(Constants.LOADED_USER_DATA_SUCCESSFULLY);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent(Constants.FAILED_TO_LOAD_USER_DATA);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

    }


    private void setUsersCurrentSubIndexInFireBase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        adRef3.setValue(Variables.getCurrentSubscriptionIndex());
    }

    private void resetTotalsInFirebase() {
        Log(TAG, "---Resetting adtotal,current subscription and current ad in category in firebase to 0 due to it being a new day.");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0);

        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        adRef3.setValue(0);

        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_AD_IN_SUBSCRIPTION);
        adRef4.setValue(0);

        setLastSeenDateInFirebase();
    }

    private void setLastSeenDateInFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef.setValue(getDate());

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.LAST_SEEN_DATE_IN_DAYS);
        adRef2.setValue(getDateInDays());
    }

    ////load user data methods.//////////////////////////////////////////////////////////////////////////


    ////Other stuff.///
    private void setDateInSharedPrefs(String date, Context context) {
        Log(TAG, "---Setting current date in shared preferences.");
        if (context == null) context = this.DBContext;
        try {
            SharedPreferences prefs = context.getSharedPreferences(Constants.DATE, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("date", date);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setUserDataInSharedPrefs(Context context) {
        if (context == null) context = this.DBContext;
        SharedPreferences pref5 = context.getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        SharedPreferences.Editor editor5 = pref5.edit();
        editor5.clear();
        editor5.putInt("CurrentSubIndex", Variables.getCurrentSubscriptionIndex());
        Log("DatabaseManager---", "Setting the users current subscription index in shared preferences - " + Variables.getCurrentSubscriptionIndex());
        editor5.apply();

        SharedPreferences pref6 = context.getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        SharedPreferences.Editor editor6 = pref6.edit();
        editor6.clear();
        editor6.putInt("CurrentAdInSubscription", Variables.getCurrentAdInSubscription());
        Log("DatabaseManager---", "Setting the current ad in subscription in shared preferences - " + Variables.getCurrentAdInSubscription());
        editor6.apply();

        SharedPreferences pref = context.getSharedPreferences("TodayTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("TodaysTotals", Variables.getAdTotal(mKey));
        Log("DatabaseManager--", "Setting todays ad totals in shared preferences - " + Integer.toString(Variables.getAdTotal(mKey)));
        editor.apply();

        SharedPreferences pref2 = context.getSharedPreferences("MonthTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.putInt("MonthsTotals", Variables.getMonthAdTotals(mKey));
        Log.d("DatabaseManager--", "Setting the month totals in shared preferences - " + Integer.toString(Variables.getMonthAdTotals(mKey)));
        editor2.apply();

        SharedPreferences pref3 = context.getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor3 = pref3.edit();
        editor3.clear();
        editor3.putInt(Constants.REIMBURSEMENT_TOTALS, Variables.getTotalReimbursementAmount());
        Log(TAG, "Setting the Reimbursement totals in shared preferences - " + Integer.toString(Variables.getTotalReimbursementAmount()));
        editor3.apply();

        SharedPreferences pref4 = context.getSharedPreferences(Constants.CONSTANT_AMMOUNT_PER_VIEW, MODE_PRIVATE);
        SharedPreferences.Editor editor4 = pref4.edit();
        editor4.clear();
        editor4.putInt(Constants.CONSTANT_AMMOUNT_PER_VIEW, Variables.constantAmountPerView);
        Log.d(TAG, "Setting the constant amount per view in shared preferences - " + Integer.toString(Variables.constantAmountPerView));
        editor4.apply();

        SharedPreferences pref7 = context.getSharedPreferences(Constants.PREFERRED_NOTIF, MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.putBoolean(Constants.PREFERRED_NOTIF, Variables.doesUserWantNotifications);
        Log(TAG, "Set the users preference for seing notifications to : " + Variables.doesUserWantNotifications);
        editor7.apply();

        SharedPreferences pref8 = context.getSharedPreferences(Constants.PREFERRED_NOTF_HOUR, MODE_PRIVATE);
        SharedPreferences.Editor editor8 = pref8.edit();
        editor8.clear();
        editor8.putInt(Constants.PREFERRED_NOTF_HOUR, Variables.preferredHourOfNotf);
        Log.d(TAG, "Set the users preferred noification hour to : " + Variables.preferredHourOfNotf);
        editor8.apply();

        SharedPreferences pref9 = context.getSharedPreferences(Constants.PREFERRED_NOTF_MIN, MODE_PRIVATE);
        SharedPreferences.Editor editor9 = pref9.edit();
        editor9.clear();
        editor9.putInt(Constants.PREFERRED_NOTF_MIN, Variables.preferredMinuteOfNotf);
        Log(TAG, "Set the users preferred noification minute to : " + Variables.preferredMinuteOfNotf);
        editor9.apply();

        setSubsInSharedPrefs(context);
    }

    public void setAnnouncementBoolean(Context context) {
        SharedPreferences pref7 = context.getSharedPreferences(Constants.TEXT_ANOUNCEMENTS, MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.putBoolean(Constants.TEXT_ANOUNCEMENTS, false);
        Log("DatabaseManager---", "Setting the current announcement boolean to " + false);
        editor7.apply();
    }

    private void setSubsInSharedPrefs(Context context) {
        if (context == null) context = this.DBContext;
        Gson gson = new Gson();
        String hashMapString = gson.toJson(Variables.Subscriptions);

        SharedPreferences prefs = context.getSharedPreferences("Subscriptions", MODE_PRIVATE);
        prefs.edit().clear().putString("hashString", hashMapString).apply();
    }


    private String getDate() {
        return TimeManager.getDate();
    }

    private void setUserKnownCategories(List<String> categories) {
        for (String category : categories) {
            int pos = categories.indexOf(category);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(uid).child(Constants.CATEGORIES_KNOWN).child(String.valueOf(pos));
            adRef.setValue(category);
        }
//        for(int i = cyclecount;i<cyclecount+3;i++){
//            currentCyclenumber++;
//            setCategory(i,categories.get(i),categories);
//        }
    }

    private void setCategory(final int pos, String category, final List<String> categoryList) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CATEGORIES_KNOWN).child(String.valueOf(pos));
        adRef.setValue(category).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                cyclecount++;
                if (cyclecount != categoryList.size()) {
                    if (cyclecount == currentCyclenumber) {
                        setUserKnownCategories(categoryList);
                    }
                }
            }
        });
    }


    private String getSubscriptionValue(int index) {
        LinkedHashMap map = Variables.Subscriptions;
        String Sub = (new ArrayList<String>(map.keySet())).get(index);
        Log("SubscriptionManagerItem", "Subscription gotten from getCurrent Subscription method is :" + Sub);
        return Sub;
    }

    private int getPositionOf(String subscription) {
        LinkedHashMap map = Variables.Subscriptions;
        List<String> indexes = new ArrayList<String>(map.keySet());
        return indexes.indexOf(subscription);
    }

    private void updateCurrentSubIndex() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.CURRENT_SUBSCRIPTION_INDEX);
        Log(TAG, "Setting current subscription index in firebase to :" + Variables.getCurrentSubscriptionIndex());
        adRef3.setValue(Variables.getCurrentSubscriptionIndex());
    }

    private void loadNewSubList() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log(TAG, "Starting to load users data");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String categoryBeingViewed = getSubscriptionValue(Variables.getCurrentSubscriptionIndex());
                Log(TAG, "the current category being added is " + categoryBeingViewed);
                Log(TAG, "its index position is : " + getPositionOf(categoryBeingViewed));
                Variables.Subscriptions.clear();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log(TAG, "Key category gotten from firebase is : " + category + " Value : " + cluster);
                    Variables.Subscriptions.put(category, cluster);
                }
                int newIndex = getPositionOf(categoryBeingViewed);

                Log(TAG, "Its new index position is : " + newIndex);
                Variables.setCurrentSubscriptionIndex(newIndex);
                updateCurrentSubIndex();

                SharedPreferences pref5 = DBContext.getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
                SharedPreferences.Editor editor5 = pref5.edit();
                editor5.clear();
                editor5.putInt("CurrentSubIndex", Variables.getCurrentSubscriptionIndex());
                Log("DatabaseManager---", "Setting the users current subscription index in shared preferences - " + Variables.getCurrentSubscriptionIndex());
                editor5.apply();

                setSubsInSharedPrefs(DBContext);

                Intent intent = new Intent(Constants.SET_UP_USERS_SUBSCRIPTION_LIST);
                LocalBroadcastManager.getInstance(DBContext).sendBroadcast(intent);

//                setUserDataInSharedPrefs(DBContext);
                Variables.hasChangesBeenMadeToCategories = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //called in main activity
    public void checkIfNeedToResetUsersSubscriptions(final Context myContext) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot isNeedToResetSubsSnap = dataSnapshot.child(Constants.RESET_ALL_SUBS_BOOLEAN);
                if (isNeedToResetSubsSnap.getValue(Boolean.class)) {
                    DataSnapshot newConstantCPVSnap = dataSnapshot.child(Constants.NEW_CPV);
                    int newConstantCPV = newConstantCPVSnap.getValue(Integer.class);
                    int oldConstantCPV = Variables.constantAmountPerView;
                    resetUsersSubscriptionsForNewPrice(oldConstantCPV, newConstantCPV);
                } else {
                    setUserDataInSharedPrefs(myContext);
                    Intent intent = new Intent(Constants.LOADED_USER_DATA_SUCCESSFULLY);
                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void resetUsersSubscriptionsForNewPrice(int oldConstant, int newConstant) {
        for (String AdvertCategory : Variables.Subscriptions.keySet()) {
            int clusterIDInCategory = Variables.Subscriptions.get(AdvertCategory);
            removeSpecificCategoryForResettingNewPrice(newConstant, oldConstant, AdvertCategory, clusterIDInCategory);
        }
    }

    //This will flag the category first...
    private void removeSpecificCategoryForResettingNewPrice(final int newConstant, final int oldConstant, final String AdvertCategory, final int clusterIDInCategory) {
        Log(TAG, "Unsubscribing user from cluster " + AdvertCategory);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(oldConstant)).child(AdvertCategory);
        DatabaseReference dbref = dbRef.push();
        dbref.setValue(clusterIDInCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                removeSpecificAdCategryFromUserSpaceAndSubscriptionsForReset(newConstant, oldConstant, AdvertCategory, clusterIDInCategory);
            }
        });
    }

    //this then removes from cluster and users cluster list...
    private void removeSpecificAdCategryFromUserSpaceAndSubscriptionsForReset(final int newConstant, final int oldConstant, final String AdvertCategory, int Cluster) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log(TAG, "Removing user from subscription");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST)
                .child(Integer.toString(oldConstant))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.removeValue();

        Log(TAG, "Removing category " + AdvertCategory + " from users categories list.");
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iterationsForResettingCPV++;
                if (iterationsForResettingCPV == Variables.Subscriptions.size()) {
                    startResubscriptionForAllUsersSubscriptions(newConstant);
                }
            }
        });
    }


    //this will start resubscription for users subscriptions
    private void startResubscriptionForAllUsersSubscriptions(final int newConstant) {
        List<String> subList = new ArrayList<>();
        subList.addAll(Variables.Subscriptions.keySet());
        setUpUsersNewSubList(subList, newConstant);
    }

    private void setUpUsersNewSubList(List<String> subscriptions, int newConstant) {
        numberOfSubs = subscriptions.size();
        iterations = 0;
        Variables.Subscriptions.clear();
        for (String sub : subscriptions) {
            generateClusterIDFromCategoryFlaggedClustersForCategroyReset(sub, newConstant);
        }
    }

    private void generateClusterIDFromCategoryFlaggedClustersForCategroyReset(final String AdvertCategory, final int newConstant) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(newConstant)).child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Log(TAG, "Flagged clusters has got children in it.");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        int clusterIDInCategory = snap.getValue(int.class);
                        Log(TAG, "Cluster id gotten from Flagged cluster is --" + clusterIDInCategory);
                        removeIdThenSubscribeUserForCategoryReset(newConstant, snap.getKey(), AdvertCategory, clusterIDInCategory);
                        break;
                    }
                } else {
                    Log(TAG, "--Flagged cluster in Category has got no children in it. Generating normally");
                    generateClusterIDFromCategoryForCategoryReset(newConstant, AdvertCategory);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void generateClusterIDFromCategoryForCategoryReset(final int newConstant, final String AdvertCategory) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(newConstant))
                .child(AdvertCategory);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This loads the current cluster in AdCategory
                long currentCluster;
                if (dataSnapshot.getChildrenCount() == 0) {
                    currentCluster = 1;
                    Log(TAG, "The adCategory is empty.Setting current cluster to 1");
                } else {
                    currentCluster = dataSnapshot.getChildrenCount();
                    Log(TAG, "The latest current cluster in " + AdvertCategory + " category is :" + currentCluster);
                }

                //this loads number of users in the current cluster.
                DataSnapshot UsersInCurrentCluster = dataSnapshot.child(Long.toString(currentCluster));
                long numberOfUsersInCurrentCluster;
                if (UsersInCurrentCluster.getChildrenCount() == 0) {
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount();
                    Log(TAG, "--number of users in current clusters category is --" + numberOfUsersInCurrentCluster);
                } else {
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount() + 1;
                    Log(TAG, "--number of users in current clusters category is --" + numberOfUsersInCurrentCluster);
                }

                //this checks if the number of users in cluster exceeds limit
                int clusterIDForSpecificCategory;
                if (numberOfUsersInCurrentCluster < 1000) {
                    Log(TAG, "--Number of users in the current cluster is less than limit.setting AdCategory cluster to --" + currentCluster);
                    clusterIDForSpecificCategory = (int) currentCluster;
                } else {
                    Log(TAG, "--Number of users in the current cluster exceeds limit.setting AdCategory cluster to --" + (currentCluster + 1));
                    clusterIDForSpecificCategory = (int) currentCluster + 1;
                }
                subscribeUserToAdvertCategoryAndAddCategoryToUserListForCategoryReset(newConstant, AdvertCategory, clusterIDForSpecificCategory);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeIdThenSubscribeUserForCategoryReset(final int newConstant, String key, final String AdvertCategory, final int clusterIDInCategory) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(Constants.FLAGGED_CLUSTERS)
                .child(Integer.toString(newConstant)).child(AdvertCategory).child(key);
        dbr.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log(TAG, "---Removed ClusterId From Flagged Clusters. Subscribing user to AdCategory.");
                subscribeUserToAdvertCategoryAndAddCategoryToUserListForCategoryReset(newConstant, AdvertCategory, clusterIDInCategory);
            }
        });
    }

    private void subscribeUserToAdvertCategoryAndAddCategoryToUserListForCategoryReset(final int newConstant, String AdvertCategory, int Cluster) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //this subscribes user to Cluster in Advert Category
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(newConstant))
                .child(AdvertCategory)
                .child(Integer.toString(Cluster)).child(uid);
        dbRef.setValue(uid);

        //this ads the advertCategory subscribed to by user to user space
        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(AdvertCategory);
        dbRefUser.setValue(Cluster).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iterations++;
                if (iterations == numberOfSubs) {
                    setDateInSharedPrefs(getDate(), DBContext);
                    Variables.constantAmountPerView = newConstant;
                    setNewConstantAsConstantInDatabase(DBContext);
                    reloadUsersSubscriptions(Constants.LOADED_USER_DATA_SUCCESSFULLY);
                }
            }
        });
    }


    private void setNewConstantAsConstantInDatabase(Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setUsersPreferredChargePerView();

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.RESET_ALL_SUBS_BOOLEAN);
        adRef.setValue(false);

        SharedPreferences prefs = context.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.IS_CHANGING_CPV, false);
        editor.apply();

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.NEW_CPV);
        adRef2.setValue(Variables.constantAmountPerView);
    }

    private void reloadUsersSubscriptions(final String intentString) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User.setUid(uid);
        Log(TAG, "Starting to load users data");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST);
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!Variables.Subscriptions.isEmpty()) Variables.Subscriptions.clear();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String category = snap.getKey();
                    Integer cluster = snap.getValue(Integer.class);
                    Log(TAG, "Key category gotten from firebase is : " + category + " Value : " + cluster);
                    Variables.Subscriptions.put(category, cluster);
                }
                try {
                    setUserDataInSharedPrefs(DBContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(intentString);
                LocalBroadcastManager.getInstance(DBContext).sendBroadcast(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setBooleanForResetSubscriptions(int newValue, final Context myContext) {
        SharedPreferences prefs3 = myContext.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE);
        boolean hasChangedPrev = prefs3.getBoolean(Constants.IS_CHANGING_CPV, false);

        boolean newBool = true;

        if (hasChangedPrev && newValue == Variables.constantAmountPerView) {
            newBool = false;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.RESET_ALL_SUBS_BOOLEAN);
        adRef.setValue(newBool);

        SharedPreferences prefs = myContext.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.IS_CHANGING_CPV, newBool);
        editor.apply();

        SharedPreferences prefs2 = myContext.getSharedPreferences(Constants.NEW_CPV, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        editor2.putInt(Constants.NEW_CPV, newValue);
        editor2.apply();

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.NEW_CPV);
        adRef2.setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(Constants.FINISHED_SETUP_FOR_RESETTING_SUBS);
                LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);
            }
        });

    }

    private long getDateInDays() {
        return TimeManager.getDateInDays();
    }


    //These methods will check if all users subscriptions are ok.This is to prevent clusters from having more users than intended.
    public void checkIfClustersAreOk(final LinkedHashMap<String, Integer> subscriptions, final Context context, final String intentFilter) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView));
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedHashMap<String, Integer> subscriptionsToBeReset = new LinkedHashMap<>();

                for (String category : subscriptions.keySet()) {
                    Integer clusterId = subscriptions.get(category);
                    if (dataSnapshot.child(category).child(Integer.toString(clusterId)).getChildrenCount() > Constants.NUMBER_OF_USERS_PER_CLUSTER) {
                        subscriptionsToBeReset.put(category, clusterId);
                    }
                }

                if (!subscriptionsToBeReset.isEmpty()) {
                    resetUsersSubscriptions(subscriptionsToBeReset, context, intentFilter);
                } else {
                    Intent intent = new Intent(intentFilter);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void resetUsersSubscriptions(final LinkedHashMap<String, Integer> subscriptionsToBeReset, final Context context, final String intentFilter) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        numberOfSubs = subscriptionsToBeReset.size();
        iterations = 0;
        for (final String category : subscriptionsToBeReset.keySet()) {
            Integer clusterId = subscriptionsToBeReset.get(category);

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                    .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView))
                    .child(category)
                    .child(Integer.toString(clusterId))
                    .child(uid);
            dbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    getNewClusterId(category, context, intentFilter);
                }
            });
        }
    }

    private void getNewClusterId(final String category, final Context context, final String intentFilter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView))
                .child(category);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This loads the current cluster in AdCategory
                long currentCluster;
                if (dataSnapshot.getChildrenCount() == 0) {
                    currentCluster = dataSnapshot.getChildrenCount() + 1;
                    Log(TAG, "The adCategory is empty.Setting current cluster to 1");
                } else {
                    currentCluster = dataSnapshot.getChildrenCount();
                    Log(TAG, "The latest current cluster in " + category + " category is :" + currentCluster);
                }

                //this loads number of users in the current cluster.
                DataSnapshot UsersInCurrentCluster = dataSnapshot.child(Integer.toString((int) currentCluster));
                long numberOfUsersInCurrentCluster;
                if (UsersInCurrentCluster.getChildrenCount() == 0) {
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount();
                    Log(TAG, "--number of users in current clusters category is --" + numberOfUsersInCurrentCluster);
                } else {
                    numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount() + 1;
                    Log(TAG, "--number of users in current clusters category is --" + numberOfUsersInCurrentCluster);
                }

                //this checks if the number of users in cluster exceeds limit
                int clusterIDForSpecificCategory;
                if (numberOfUsersInCurrentCluster < Constants.NUMBER_OF_USERS_PER_CLUSTER) {
                    Log(TAG, "--Number of users in the current cluster is less than limit.setting AdCategory cluster to --" + currentCluster);
                    clusterIDForSpecificCategory = (int) currentCluster;
                } else {
                    Log(TAG, "--Number of users in the current cluster exceeds limit.setting AdCategory cluster to --" + (currentCluster + 1));
                    clusterIDForSpecificCategory = (int) currentCluster + 1;
                }
                subscribeToNewClusterAndAddClusterToUserList(category, clusterIDForSpecificCategory, context, intentFilter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void subscribeToNewClusterAndAddClusterToUserList(final String category, final int clusterId, final Context context, final String intentFilter) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //this subscribes user to Cluster in Advert Category
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS)
                .child(Constants.CLUSTERS_LIST).child(Integer.toString(Variables.constantAmountPerView))
                .child(category)
                .child(Integer.toString(clusterId)).child(uid);
        dbRef.setValue(uid);

        DatabaseReference dbRefUser = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.SUBSCRIPTION_lIST).child(category);
        dbRefUser.setValue(clusterId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iterations++;
                if (iterations == numberOfSubs) {
                    DBContext = context;
                    reloadUsersSubscriptions(intentFilter);
                }
            }
        });
    }

    public void setUsersNewPassword(String password) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.USER_PASSCODE);
        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.IS_PASSWORD_ENCRYPTED);
        adRef2.setValue(true);
        adRef.setValue(password);
    }

    ////Other stuff.////////////////////////////////////////////////////////////////////////////////////////


    private void setNeedToResetBooleanInSharedPrefs(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.IS_CHANGING_CPV, false);
        editor.apply();
    }

    private void Log(String tag, String message) {
        try {
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (user.equals(Constants.ADMIN_ACC)) Log.d(tag, message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateValueForUnneededPayoutAmount(final Advert ad) {
        if (Variables.adsSeenSoFar.size() == Constants.MAX_AMMOUNT_FOR_SHARING_PAYOUT_AMOUNT) {
            clearAdsSeenSoFarInFirebase();
            Variables.adsSeenSoFar.clear();
        }
        Variables.adsSeenSoFar.put(ad.getPushRefInAdminConsole(), ad.getAdvertiserUid());
        adToAdsSeenSoFarInFirebase(ad);
        double size = Variables.adsSeenSoFar.size();
        double previousSize = Variables.adsSeenSoFar.size() - 1;
        final double previousAmount = previousSize > 1 ? ((previousSize - 1) * ((double) Constants.MPESA_CHARGES)) / previousSize : 0.0;
        final double newAmount = size > 1 ? ((size - 1) * ((double) Constants.MPESA_CHARGES)) / size : 0.0;
        final double newAddValue = newAmount - previousAmount;

        Log(TAG, "Updating values for unneeded payout amount that was paid : previousAmount=" + previousAmount
                + " newAmount=" + newAmount + " and newAddValue=" + newAddValue);

        String dateInDays = isAlmostMidNight() ? Long.toString(-(TimeManager.getDateInDays() + 1)) : Long.toString(-TimeManager.getDateInDays());
        String day = isAlmostMidNight() ? TimeManager.getNextDayDay() : TimeManager.getDay();
        String month = isAlmostMidNight() ? TimeManager.getNextDayMonth() : TimeManager.getMonth();
        String year = isAlmostMidNight() ? TimeManager.getNextDayYear() : TimeManager.getYear();
        String datte = isAlmostMidNight() ? TimeManager.getNextDay() : getDate();

        long theDateInDays = ((ad.getDateInDays()+1)*-1);
        String dateInDays2 = Long.toString(theDateInDays);

        for (final String pushRef : Variables.adsSeenSoFar.keySet()) {
            String advertiserUid = Variables.adsSeenSoFar.get(pushRef);

            final DatabaseReference mref2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(advertiserUid).child(Constants.UPLOAD_HISTORY).child(dateInDays2)
                    .child(pushRef).child(Constants.UNNEEDED_REIMBURSAL_AMM);

            final DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                    .child(year).child(month).child(day).child(pushRef).child(Constants.UNNEEDED_REIMBURSAL_AMM);

            final DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                    .child(datte).child(pushRef).child(Constants.UNNEEDED_REIMBURSAL_AMM);
            mref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double value = 0;
                    if (dataSnapshot.exists()) {
                        value = dataSnapshot.getValue(double.class);
                    }
                    if (pushRef.equals(ad.getPushRefInAdminConsole())) {
                        double newValue = value + newAmount;
                        mref.setValue(newValue);
                        mref2.setValue(newValue);
                        adminRef.setValue(newValue);
                    } else {
                        double newValue = value + newAddValue;
                        mref.setValue(newValue);
                        mref2.setValue(newValue);
                        adminRef.setValue(newValue);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void adToAdsSeenSoFarInFirebase(Advert ad) {
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.SEEN_AD_IDS);
        DatabaseReference pushRef = adRef.push();
        pushRef.setValue(ad.getPushRefInAdminConsole());
    }

    public void clearAdsSeenSoFarInFirebase() {
        Variables.adsSeenSoFar.clear();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.SEEN_AD_IDS);
        adRef.setValue(null);
    }



    private boolean isAlmostMidNight() {
        return TimeManager.isAlmostMidNight();
    }

    public void loadUsersPassword() {
        try {
            if (Variables.getPassword().equals("")) {
                String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                        .child(user).child(Constants.USER_PASSCODE);
                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String str = dataSnapshot.getValue(String.class);
                        Variables.setPassword(str);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(user).child(Constants.USER_PASSCODE);
            dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String str = dataSnapshot.getValue(String.class);
                    Variables.setPassword(str);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void loadAnyAnnouncementsFromMainActivity() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.TEXT_ANOUNCEMENTS).child(getDate());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Variables.announcements = dataSnapshot.getValue(String.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else Variables.announcements = "";
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void syncUserDataInFirebase() {
        resetTotalsInFirebase();
    }




    private void setMarkersInSharedPrefs(Context mContext) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.USER_MARKERS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt(Constants.USER_MARKERS_SIZE, Variables.usersLatLongs.size());

        for (int i = 0; i < Variables.usersLatLongs.size(); i++) {
            editor.putFloat("lat" + i, (float) Variables.usersLatLongs.get(i).latitude);
            editor.putFloat("long" + i, (float) Variables.usersLatLongs.get(i).longitude);
        }
        editor.apply();
    }

    public void setIsLoggedOnInFirebase(boolean val){
       DatabaseReference dbref  = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
               .child(Constants.ONLINE_NESS);
       dbref.setValue(val);
    }

    public void setIsMakingPayoutInFirebase(boolean val){
        DatabaseReference dbref  = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constants.IS_MAKING_PAYOUT);
        dbref.setValue(val);
    }



    private boolean doesCategoryImageExists(Context mContext,String category){
        String filename;
        filename = category.replaceAll(" ","_");
        int res = mContext.getResources().getIdentifier(filename, "drawable", mContext.getPackageName());
        if(res==0)Log.e(TAG,"Category image for "+category+" does not exist");
        return res != 0;
    }

    private void removeFromNewCategoriesList(String category){
        Log.d(TAG,"Attempting to remove known category "+category);
        for(int i = 0; i<Variables.newCategories.size();i++){
            String generalCategory = (new ArrayList<>(Variables.newCategories.keySet())).get(i);
            Log.d(TAG,"removing known category "+category);
            Variables.newCategories.get(generalCategory).remove(category);
        }
//        for(List<String>categoryList:Variables.newCategories.values()){
//            if(categoryList.contains(category)){
//
//                break;
//            }
//        }
    }


    public void addCoinToUsersCoinList(final AdCoin coin){
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.USERS_COIN_LIST);
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AdCoin> usersAddCoin = new ArrayList<>();
                try {
                    if (dataSnapshot.exists()) {
                        Gson gson = new Gson();
                        String coinListString = dataSnapshot.getValue(String.class);
                        java.lang.reflect.Type type = new TypeToken<List<AdCoin>>() {}.getType();
                        usersAddCoin = gson.fromJson(coinListString, type);
                    }
                    usersAddCoin.add(coin);

                    DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                            .child(uid).child(Constants.USERS_COIN_LIST);
                    Gson gson = new Gson();
                    String userListString = gson.toJson(usersAddCoin);
                    mRef2.setValue(userListString);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUsersCoinsListInSharedPrefs(String coinsString,Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.USERS_COIN_LIST, MODE_PRIVATE);
        prefs.edit().clear().putString(Constants.USERS_COIN_LIST, coinsString).apply();
    }

}