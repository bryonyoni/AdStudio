package com.bry.adcafe;

/**
 * Created by bryon on 6/4/2017.
 */

public class Constants {
    public static final String ADVERT_CARD_BROADCAST_TO_AD_COUNTER = "ADVERT_CARD_BROADCAST_TO_AD_COUNTER";
    public static final String ADVERT_CARD_BROADCAST = "ADVERT_CARD_BROADCAST";
    public static final String AD_TIMER_BROADCAST = "AD_TIMER_BROADCAST";
    public static final String NUMBER_OF_ADS  = "NUMBER_OF_ADS";
    public static final String TIMER_HAS_ENDED = "TIMER_HAS_ENDED";


    public static String AD_TOTAL = "AD_TOTAL";
    public static String STOP_TIMER = "STOP_TIMER";
    public static String ADVERT_CARD_BROADCAST_TO_START_TIMER = "ADVERT_CARD_BROADCAST_TO_START_TIMER";
    public static String ADD_TO_SHARED_PREFERENCES = "ADD_TO_SHARED_PREFERENCES";
    public static String LAST = "LAST";


    public static String NOT_LAST = "NOT_LAST";
    public static String UNREGISTER_ALL_RECEIVERS = "UNREGISTER_ALL_RECEIVERS";
    public static String CONNECTION_OFFLINE = "CONNECTION_OFFLINE";
    public static String PIN_AD = "PIN_AD";
    public static String CONNECTION_ONLINE = "CONNECTION_ONLINE";


    public static String PINNING_FAILED = "PINNING_FAILED";
    public static String PINNING_SUCCESS = "PINNING_SUCCESS";
    public static String REMOVE_PINNED_AD = "REMOVE_PINNED_AD";
    public static String UNABLE_TO_REMOVE_PINNED_AD = "UNABLE_TO_REMOVE_PINNED_AD";
    public static String PINNED_AD_LIST = "pinnedAdList";


    public static String FIREBASE_CHILD_ADS = "savedAds";
    public static String FIREBASE_CHILD_USERS = "users";
    public static String TOTAL_NO_OF_ADS_SEEN_TODAY = "TodaysTotalAds";
    public static String DATE_IN_FIREBASE = "date";
    public static String TOTAL_NO_OF_ADS_SEEN_All_MONTH = "allTimeTotals";
    public static String CLUSTER_ID = "clusterID";
    public static String CLUSTERS = "clusters";
    public static String CLUSTERS_LIST = "cluster_list";
    public static String CLUSTER_LIST_PUSHREF_ID = "cluster_list_pushref_id";
    public static String FLAGGED_CLUSTERS = "FlaggedClusters";
    public static String CLUSTER_TO_START_FROM = "cluster_to_start_from";
    public static String ADVERTS = "Adverts";
    public static String REPORTED_ADS = "Flagged_ads";
    public static String HAS_USER_MADE_PAMENTS = "has_payed";

    public static String NO_ADS = "NO_ADS";
    public static String DATE = "date";
    public static String LAST_AD_SEEN = "LastAdSeen";
    public static String LOAD_MORE_ADS = "LoadMoreAds";
    public static String ANNOUNCEMENTS = "Announcements";
    public static String ADS_FOR_CONSOLE = "AdsForConsole";
    public static String UPLOADED_AD_LIST = "UploadedAdList";
    public static String FEEDBACK = "Feedback";

    public static double CONSTANT_AMOUNT_PER_AD = 4;
    public static double CONSTANT_AMMOUNT_FOR_USER = 2;
    public static String TOTAL_ALL_TIME_ADS = "AllUserTotals";
    public static String SUBSCRIPTION_lIST = "UserSubscriptionList";
    public static String CREATE_USER_SPACE_COMPLETE = "UserSpaceCompleted";
    public static String SET_UP_USERS_SUBSCRIPTION_LIST = "setUpUsersSubscriptionList";

    public static String CURRENT_SUBSCRIPTION_INDEX = "currentSub";
    public static String CURRENT_AD_IN_SUBSCRIPTION = "currentAdInSubscription";
    public static String LOADED_USER_DATA_SUCCESSFULLY = "LoadedUserDataSuccessfully";
    public static String FAILED_TO_LOAD_USER_DATA = "FailedToLoadUserData";

    public static String CATEGORY_LIST = "categoryList";
    public static String FINISHED_UNSUBSCRIBING = "finishedUnsubscribing";

    public static String STARTING_UPDATING = "startingUpdating";
    public static String FINISHED_UPDATING = "FinishedUpdating";
    public static String CONFIRM_START = "ConfirmStart";
    public static String ALL_CLEAR = "AllClear";
    public static String CANCELLED = "canceled";


    public static String NO_OF_CATEGORIES_KNOWN = "NoOfCategoriesKnown";
    public static String IS_AD = "Is_ad";
    public static String IS_ANNOUNCEMENT = "Is_announcement";
    public static String PINNED_AD_POOL = "PinnedAdPool";
    public static String NO_OF_TIMES_PINNED = "NumberOfTimesPinned";



    public static int NO_OF_ADS_TO_LOAD = 3;
    public static int NO_OF_ADS_TO_LOAD2 = 2;
    public static String REIMBURSEMENT_TOTALS = "ReimbursementTotals";
    public static String CONSTANT_AMMOUNT_PER_VIEW = "AmountPerView";
    public static String FINISHED_SETUP_FOR_RESETTING_SUBS = "FINISHED_SETUP_FOR_RESETTING_SUBS";
    public static String RESET_ALL_SUBS_BOOLEAN = "ResetAllSubs";
    public static String NEW_CPV = "NewCPV";
    public static String PREFERRED_NOTIF = "NotificationsPref";

    public static String PREFERRED_NOTF_HOUR = "PreferredNotfHour";
    public static String PREFERRED_NOTF_MIN = "PreferredNotfMin";
    public static String USER_NICKNAME = "UserName";
    public static String LAST_SEEN_DATE_IN_DAYS = "LSDate";
    public static int NUMBER_OF_USERS_PER_CLUSTER = 100;
    public static double PAYMENT_TRANSFER_PERENTAGE = 0.035;
    public static double PAYOUT_TRANSFER_FEE = 0.015;
    public static double TOTAL_PAYOUT_PERCENTAGE = 0.04;
    public static double TOTAL_MPESA_PAYOUT_PERCENTAGE = 0.03;

    public static String USER_PASSCODE = "Password";
    public static String PAUSE_TIMER = "PAUSE_TIMER";
    public static String RESUME_TIMER = "RESUME_TIMER";

    public static long HRS_3_IN_MILLS = (1000*60*60*3);
    public static long HRS_24_IN_MILLS = (1000*60*60*24);
    public static String LOAD_TIME = "LOAD_TIME";

    public static String MPESA_OPTION = "MPESA";
    public static String BANK_OPTION = "CREDIT_DEBIT";
    public static String PAY_POOL = "Pay_pool";
    public static String REIMBURSEMENT_HISTORY = "ReimbursementHistory";
    public static String ADVERT_REIMBURSEMENT_HISTORY = "AdvertReimbursementHistory";
    public static String ADCAFE_TOTALS = "TakeoutTotals";
    public static int TOTAL_AMOUNT_PER_VIEW_FOR_ADMIN = 2;

    public static String ADD_CATEGORY_AUTOMATICALLY = "ADD_CATEGORY_AUTOMATICALLY";
    public static String STOP_LISTENING = "STOP_LISTENING";
    public static String UPLOAD_HISTORY = "UploadHistory";

    public static String CATEGORIES_KNOWN = "CategoriesKnown";
    public static String EULA = "https://adcafe.github.io/Eula/index.html";
    public static String ADVERTISING_PROCESS = "https://adcafe.github.io/AdvertiserTutorial/";
    public static String USER_TUTORIAL = "https://adcafe.github.io/UserTutorial/";
    public static String IS_CHANGING_CPV = "is_changing_cpv";
    public static String REMOVE_SELF_LISTENER = "REMOVE_SELF_LISTENER";
    public static String REMOVE_REMOVE_SELF_LISTENER = "REMOVE_REMOVE_SELF_LISTENER";
    public static String SHOW_DELETE_ICON = "SHOW_DELETE_ICON";

    public static String PHONE_NUMBER = "PHONE_NUMBER";
    public static String PHONE_NUMBER2 ="PHONE_NUMBER2";
    public static int MAX_NUMBER_FOR7 = 14;

    //////payment variables
    public static int live = 1;
    public static String vid = "ctl";
    public static String curr = "KES";
    public static String p1 = "";
    public static String p2 = "";
    public static String p3 = "";
    public static String p4 = "";
    public static String cbk = "";
    public static int cst = 1;
    public static int crl = 2;
    public static String country = "Kenya";


    /////////Mpesa payment variables
    public static String key = "Jbc98YbjL9H9riwA/eweVXF8HhVYmPYjrwLdgp2k1rigPu04E6oqEBe1RP/xq3pDRRrQj+/v+m0D3OGhbJUKfkw0wdxFzrs1FbJ8r9pgdU8iMX9RisnulUwxvYXbdEf1WTHWY4HIekJDnORheEl/8sBHUenWw01WT9twSbUyahx9k/Vx+vXFFITJJ8ldpIRQt/7q7+Ci" +
            "uGgwUcWcAmXyBoAGtHNtBUkACWO6fGiJ8fm7nVXzPTMfkI7HIccWepcCWs0XBLQolNhawgkrQLdvNXUcN7GvHwiOC2GNicrN/a7fvLGdB5eNnMaOZDv9e+SmZ90a+fdY+hSLH6Rbv9Wuog==";
    public static String appKey = "HJju1PU5BthG4QTecPWduXuFz22XTFfe";
    public static String appSecret = "o2ZUZQgi8eDQYKj7";
    public static String payoutAppKey = "Xna3G2ahKqwsXmciMfmAtmxqv9GjShqx";
    public static String payoutAppSecret = "xSkVFsFMUFJ2OEAA";


    ////////////////////////


    public static String ADMIN_MONEY = "AdminTotal";
    public static String TEXT_ANOUNCEMENTS = "Text_Announcements";
//    public static String ADMIN_ACC = "bryonyoni@gmail.com";
    public static String ADMIN_ACC = "bryonyoni456465489416514651ds16f1asdf65a4fs6f1ds6f4aw6f1s6f4s8fa1ew65f1awe84f6ds5f1a65fe8w1f6s5fa84f65sad1f6a8ef165as1fa6sfa6f51s65fse8f16f16s5f1s1df1a65fsfjgoddgohiuhgiuskfaerferfrf8ds4f65sfiuiuewrytiwgkjbfvlcxvui,btrejhfdyvuhljfnmrtef;dsp;fs.ntwrhfdviuhbrntrgfdhvciuxhjbdgtrhdfuvihfdkjgbtrngfdhvcxujkbgdlhidflkjbvkilusehgrkjnfdvkjchifkgsbrluidfvhkjbngrkhldflkhoiguqteproiyt98074398iryhhdslvkjbvilusrehtiuhrjkdfbvkjdznv.kbglihwialhlHLGSHJKERHIUBGLhgiuwhrgkh`bjshgiagfdfbvjlisighreohgahab;va[ga][yr9e8ugdfhvzbvseufhesiuhgebrgliuvdhlreigalbvbvhjlsherluigufdkjbnmcbvselihgjrkf.dbvdiluhklebgsrkdfjvuuuu8";
    public static String CUSTOM_STARTING_POINT_ENABLED = "CUSTOM_STARTING_POINT_ENABLED";
    public static String CUSTOM_STARTING_POINT_VALUE = "CUSTOM_STARTING_POINT_VALUE";
    public static String HISTORY_UPLOADS = "AdminUploadHistory";

    public static String HISTORY_TRANSACTIONS ="AllTransactionsHistory";
    public static int MPESA_CHARGES = 16;
    public static String SEEN_AD_IDS = "SeenAds";
    public static int MAX_AMMOUNT_FOR_SHARING_PAYOUT_AMOUNT = 1500000000;
    public static String UNNEEDED_REIMBURSAL_AMM = "payoutReimbursalAmount";

    public static int ANIMATION_DURATION = 350;
    public static float MAX_DISTANCE_IN_METERS = 1000f;
    public static String USER_MARKERS = "USER_MARKERS";
    public static String USER_MARKERS_SIZE = "USER_MARKERS_SIZE";
    public static String FIREBASE_USERS_LOCATIONS = "UserLocations";

    public static String MALE = "Male";
    public static String FEMALE = "Female";
    public static String GENDER = "Gender";
    public static String DATE_OF_BIRTH = "DateOfBirth";

    public static String CONSENT_TO_TARGET = "CanUsePersonalData";
    public static String ONLINE_NESS = "ONLINE_NESS";
    public static String USER_ID = "USER_ID";

    public static String IS_MAKING_PAYOUT = "IsMakingPayout";
    public static String BOI_IS_DA_KEY = "SessionKey";
    public static String TRANSACTIONS = "Transactions";
    public static String PAYMENTS = "Payments";
    public static String MPESA_PAYMENTS = "PayViaSafMpesa";
    public static String PAYOUTS = "Payouts";
    public static String MPESA_PAYOUTS = "PayoutViaSafMpesa";

    public static String AD_PAYOUTS = "AdPayouts";
    public static Double VAT_CONSTANT = 0.16;
    public static String TARGET_USER_DATA = "TargetUserData";
    public static String SET_LOCATION_DATA = "SET_LOCATION_DATA";

    public static String HIGH_END_DEVICE = "HighEndDevice";
    public static String MID_RANGE_DEVICE = "MidRangeDevice";
    public static String LOW_END_DEVICE = "LowEndDevice";

    public static String DEVICE_CATEGORY = "DeviceCategory";
    public static String SET_MULTI_CATEGORY_DATA = "SET_MULTI_CATEGORY_DATA";
    public static int HIGH_DENSITY_THRESHOLD = 510;
    public static int MID_DENSITY_THRESHOLD = 300;
    public static String RESET_CATEGORIES_SELECTED = "RESET_CATEGORIES_SELECTED";
    public static String UNREGISTER_LISTENERS = "UNREGISTER_LISTENERS";

    public static String ADVERTISER_PHONE_NO = "PhoneNumber";
    public static String ADVERTISER_LOCATION = "Advertiserlocation";
    public static String EULA_REFERENCE = "eula";
    public static String IS_PASSWORD_ENCRYPTED = "IsPasswordEncypted";

    public static String TEXT_MESSAGE = "TEXT_MESSAGE";
    public static String IMAGE_MESSAGE = "IMAGE_MESSAGE";
    public static String FIREBASE_MESSAGES = "Messages";
    public static String MESSAGES_LIST = "MessagesList";
    public static String NEW_MESSAGE_NOTIFIER_INTENT = "NEW_MESSAGE_NOTIFIER_INTENT";

    public static String FIREBASE_IMAGE_MESSAGES = "ImageMessages";

    public static String COIN_TYPE_IMPRESSION = "CoinTypeImpression";
    public static String COIN_TYPE_WEBCLICK = "CoinTypeWebClick";
    public static String USERS_COIN_LIST = "UsersCoinList";

    public static String COIN_VALUE_SYSTEM = "IsUsingCoinSystem12399";
    public static String USERS_THAT_HAVE_SEEN = "UsersThatHaveSeenIt";
    public static String USERS_THAT_HAVE_CLICKED_IT = "UsersThatHaveClickedIt";

    public static String CATEGORY_EVERYONE = "everyone";
    public static String CATEGORY_EVERYONE_CONTAINER = "all-users";
    public static String TOMORROWS_ADS = "TOMORROWS_ADS";
    public static String TODAYS_ADS = "TODAYS_ADS";
    public static String YESTERDAYS_ADS = "YESTERDAYS_ADS";
    public static String OLDER_UPLOADS = "OLDER_UPLOADS";

    public static String ALL_AD_IMAGES = "AdImages";
    public static int INACTIVE_USER_THRESHOLD = 30;
    public static String AD_PINS = "AdPins";

}