package com.bry.adcafe.models;

/**
 * Created by bryon on 6/4/2017.
 */

public class User {
    private static String mKey = "";
    private static int mClusterID;
    private static String mUid;

    public static void setID(int number,String Key){
        if(Key == mKey){
            mClusterID = number;
        }
    }

     public static int getClusterID(String Key){
         if(Key == mKey){
             return mClusterID;
         }else{
             return 0;
         }
     }

     public static void setUid(String uid){
         mUid = uid;
     }

     public static String getUid(){
         return mUid;
     }
}
