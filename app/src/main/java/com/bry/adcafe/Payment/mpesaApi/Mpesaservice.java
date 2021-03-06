package com.bry.adcafe.Payment.mpesaApi;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Base64;
import android.util.Log;

import com.bry.adcafe.services.TimeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Mpesaservice {

    private String TAG = Mpesaservice.class.getSimpleName();


    String appKey;
    String appSecret;

    public Mpesaservice(String app_key, String app_secret) {
        appKey = app_key;
        appSecret = app_secret;
    }


    public String authenticate() throws IOException {
        String app_key = appKey;
        String app_secret = appSecret;
        String appKeySecret = app_key + ":" + app_secret;
        byte[] bytes = appKeySecret.getBytes("ISO-8859-1");
        String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);

        String accesstoken = "";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("authorization", "Basic " + encoded)
                .addHeader("cache-control", "no-cache")
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);

        return accesstoken;
    }


    public String C2BSimulation(String shortCode, String commandID, String amount, String MSISDN, String billRefNumber) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ShortCode", shortCode);
        jsonObject.put("CommandID", commandID);
        jsonObject.put("Amount", amount);
        jsonObject.put("Msisdn", MSISDN);
        jsonObject.put("BillRefNumber", billRefNumber);

        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");
        Log.d(TAG,requestJson);
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/safaricom/c2b/v1/simulate")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + authenticate())
                .addHeader("cache-control", "no-cache")
                .build();

//        Response response = client.newCall(request).execute();
//        Log.d(TAG,response.body().string());
//        return response.body().toString();
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                processResponse(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
        return "";
    }


    public void authenticateThenPayouts(final String amount, final String partyB, final Context context){
        String app_key = appKey;
        String app_secret = appSecret;
        String appKeySecret = app_key + ":" + app_secret;
        byte[] bytes = new byte[0];
        try {
            bytes = appKeySecret.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("authorization", "Basic " + encoded)
                .addHeader("cache-control", "no-cache")
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                    JSONObject aT = new JSONObject(jsonData);
//                    JSONObject accessT = aT.getJSONObject("data");
                    String accessToken = aT.getString("access_token");

                    B2CRequest(amount,partyB,accessToken,context);
                    Log.d(TAG,jsonData);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public void authenticateThenPayments(final String amount, final String partyA ){
        String app_key = appKey;
        String app_secret = appSecret;
        String appKeySecret = app_key + ":" + app_secret;
        String shortCode = "550105";
        String passKey  ="102178110d0c3f3a71170a35a7fc85530422a987574e616662a3f77d9d310f69";
        final String timeStamp = TimeManager.getTimeStamp();
        String passWordEncoded = shortCode+passKey+timeStamp;
        byte [] bytesPas = new byte[0];
        try {
            bytesPas = passWordEncoded.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] bytes = new byte[0];
        try {
            bytes = appKeySecret.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
        final String passEncoded = Base64.encodeToString(bytesPas, Base64.NO_WRAP);
        Log.d("Passencoded :",passEncoded);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("authorization", "Basic " + encoded )
                .addHeader("cache-control", "no-cache")
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonData = response.body().string();
                    JSONObject aT = new JSONObject(jsonData);
//                    JSONObject accessT = aT.getJSONObject("data");
                    String accessToken = aT.getString("access_token");

                    STKPushSimulation("550105",passEncoded, timeStamp,
                            "CustomerPayBillOnline",amount,"254702262663",
                            partyA,"550105","https://ilovepancake.github.io/PigDice",
                            "https://adcafe.github.io/CBK/","AdCafe","Adpayment",accessToken);
                    Log.d(TAG+"payments",jsonData);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
    }

    public String B2CRequest(String amount, String partyB ,String bearer,Context context){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        Log.d(TAG,encryptInitiatorPassword(context,"apicallercert.cer","AdcafeTeam#20182"));
        try {
            jsonObject.put("InitiatorName", "payoutsAdcafe");
            jsonObject.put("SecurityCredential",//*"NgjGPoag+85JmfXQHZWd/7IY3CGHI3iJacJRbsmTgz4xz+lpgkAxm9m4EqX2lWcZbLEdYWlNvFgIX9acvr19RIewOHd6nvvq4EVTjRVxuIgfJP/B+WPyJPKg6eQHpGGG8pqFfyJRm1NFDUkpCKG0D+guQPXW4nPI8RGEKABWv3ssh9OK8WjRCzqdorOgBOduvz7+tjfba1KGzPYEPDDuiTfq79uf00hEDnX+ZhYGlml7a+wqBpyQdgrjYE4CcKOScEcdvrVPADUR6op7XaROgkgBSBHYgFOn1G3a27mKDUfkzwdGZcaeGrbjTpY73Kz02AD2RSmI/7yb8Fzv/rVMYA==");
                     encryptInitiatorPassword( context,"apicallercert.cer","AdcafeTeam#20182"));
            jsonObject.put("CommandID", "PromotionPayment");
            jsonObject.put("Amount", amount);
            jsonObject.put("PartyA", "723387");
            jsonObject.put("PartyB", partyB);
            jsonObject.put("Remarks", "testing123");
            jsonObject.put("QueueTimeOutURL", "https://adcafe.github.io/CBK/");
            jsonObject.put("ResultURL", "https://ilovepancake.github.io/PigDice");
            jsonObject.put("Occassion", "ter");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");


        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url(" https://api.safaricom.co.ke/mpesa/b2c/v1/paymentrequest")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + bearer)
                .addHeader("cache-control", "no-cache")
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String jsonData = response.body().string();
                    Log.d(TAG,jsonData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
        return "";
    }

    public String B2BRequest(String initiatorName, String accountReference, String securityCredential, String commandID, String senderIdentifierType, String receiverIdentifierType, float amount, String partyA, String partyB, String remarks, String queueTimeOutURL, String resultURL, String occassion) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Initiator", initiatorName);
        jsonObject.put("SecurityCredential", securityCredential);
        jsonObject.put("CommandID", commandID);
        jsonObject.put("SenderIdentifierType", senderIdentifierType);
        jsonObject.put("RecieverIdentifierType", receiverIdentifierType);
        jsonObject.put("Amount", amount);
        jsonObject.put("PartyA", partyA);
        jsonObject.put("PartyB", partyB);
        jsonObject.put("Remarks", remarks);
        jsonObject.put("AccountReference", accountReference);
        jsonObject.put("QueueTimeOutURL", queueTimeOutURL);
        jsonObject.put("ResultURL", resultURL);


        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");
        Log.d(TAG,requestJson);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/safaricom/b2b/v1/paymentrequest")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + authenticate())
                .addHeader("cache-control", "no-cache")

                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();

    }



    public String STKPushSimulation(String businessShortCode, String password, String timestamp, String transactionType, String amount,
                                    String phoneNumber, String partyA, String partyB, String callBackURL, String queueTimeOutURL,
                                    String accountReference, String transactionDesc,String bearer) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BusinessShortCode", businessShortCode);
        jsonObject.put("Password", password);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("TransactionType", transactionType);
        jsonObject.put("Amount", amount);
        jsonObject.put("PhoneNumber", phoneNumber);
        jsonObject.put("PartyA", partyA);
        jsonObject.put("PartyB", partyB);
        jsonObject.put("CallBackURL", callBackURL);
        jsonObject.put("AccountReference", accountReference);
        jsonObject.put("QueueTimeOutURL", queueTimeOutURL);
        jsonObject.put("TransactionDesc", transactionDesc);


        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + bearer)
                .addHeader("cache-control", "no-cache")
                .build();


//        Response response = client.newCall(request).execute();
//        Log.d(TAG,response.body().string());
//        return response.body().toString();
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                processResponse(response);
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);
        return "";
    }

    public String STKPushTransactionStatus(String businessShortCode, String password, String timestamp, String checkoutRequestID) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BusinessShortCode", businessShortCode);
        jsonObject.put("Password", password);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("CheckoutRequestID", checkoutRequestID);


        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");


        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query")
                .post(body)
                .addHeader("authorization", "Bearer " + authenticate())
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        Log.d(TAG,response.body().string());
        return response.body().toString();

    }

    public String reversal(String initiator, String securityCredential, String commandID, String transactionID, String amount, String receiverParty, String recieverIdentifierType, String resultURL, String queueTimeOutURL, String remarks, String ocassion) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Initiator", initiator);
        jsonObject.put("SecurityCredential", securityCredential);
        jsonObject.put("CommandID", commandID);
        jsonObject.put("TransactionID", transactionID);
        jsonObject.put("Amount", amount);
        jsonObject.put("ReceiverParty", receiverParty);
        jsonObject.put("RecieverIdentifierType", recieverIdentifierType);
        jsonObject.put("QueueTimeOutURL", queueTimeOutURL);
        jsonObject.put("ResultURL", resultURL);
        jsonObject.put("Remarks", remarks);
        jsonObject.put("Occasion", ocassion);


        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");
        System.out.println(requestJson);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/safaricom/reversal/v1/request")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer xNA3e9KhKQ8qkdTxJJo7IDGkpFNV")
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();
        Log.d(TAG,response.body().string());
        return response.body().string();
    }

    public String balanceInquiry(String initiator, String commandID, String securityCredential, String partyA, String identifierType, String remarks, String queueTimeOutURL, String resultURL) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Initiator", initiator);
        jsonObject.put("SecurityCredential", securityCredential);
        jsonObject.put("CommandID", commandID);
        jsonObject.put("PartyA", partyA);
        jsonObject.put("IdentifierType", identifierType);
        jsonObject.put("Remarks", remarks);
        jsonObject.put("QueueTimeOutURL", queueTimeOutURL);
        jsonObject.put("ResultURL", resultURL);


        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");
        Log.d(TAG,requestJson);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/safaricom/accountbalance/v1/query")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer fwu89P2Jf6MB1A2VJoouPg0BFHFM")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "2aa448be-7d56-a796-065f-b378ede8b136")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    public String registerURL(String shortCode, String responseType, String confirmationURL, String validationURL) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ShortCode", shortCode);
        jsonObject.put("ResponseType", responseType);
        jsonObject.put("ConfirmationURL", confirmationURL);
        jsonObject.put("ValidationURL", validationURL);


        jsonArray.put(jsonObject);

        String requestJson = jsonArray.toString().replaceAll("[\\[\\]]", "");
        Log.d(TAG,requestJson);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/mpesa/c2b/v1/registerurl")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + authenticate())
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();
        Log.d(TAG,response.body().string());
        return response.body().string();


    }


    public static String encryptInitiatorPassword(Context context, String securityCertificate, String password) {
        String encryptedPassword = "";
        AssetManager manager = context.getAssets();
        FileInputStream fis;
        InputStream is;
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] input = password.getBytes();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            is = manager.open(securityCertificate);
//            fis = new FileInputStream(new File(securityCertificate)) ;
            AssetFileDescriptor assetFileDescriptor = manager.openFd(securityCertificate);
//            FileDescriptor fileDescriptor = assetFileDescriptor.getFileDescriptor();
            FileInputStream fin =assetFileDescriptor.createInputStream();
//            FileInputStream fis = openFileInput(securityCertificate);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(fin);
            PublicKey pk = certificate.getPublicKey();
            cipher.init(Cipher.ENCRYPT_MODE, pk);

            byte[] cipherText = cipher.doFinal(input);

            // Convert the resulting encrypted byte array into a string using base64 encoding
            encryptedPassword = Base64.encodeToString(cipherText,Base64.NO_WRAP);

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Mpesaservice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encryptedPassword;
    }


    public void processResponse(Response response){
        try {
            String jsonData = response.body().string();
            Log.d(TAG,jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
