package ru.grande.PSLite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class WifiPosition extends Activity implements View.OnClickListener {

    Context context;
    WifiManager wifiManager;
    Button Scan, Get, Send;
    ListView lv;
    List<ScanResult> results, resSend;
    String sense[], prom[];
    String mac;
    String uemac;
    String sAnswer = "", tosend;
    static String address = "http://195.149.207.161:8080/api/v1/";
    String recordTime;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;

    GoogleSignInClient mGoogleSignInClient;

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_one);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();
        uemac = getMacAddr();
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        //signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(this);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd.MM.yy HH-mm-ss");
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Scan = findViewById(R.id.Scan);
        Get = findViewById(R.id.Get);
        Send = findViewById(R.id.Send);
        lv = findViewById(R.id.list);
        Scan.setOnClickListener(this);
        Get.setOnClickListener(this);
        Send.setOnClickListener(this);
        onRequestPermissionsResult();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onRequestPermissionsResult() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
        };
        if (!hasPermissions(this, PERMISSIONS)) {
            while (!hasPermissions(this, PERMISSIONS))
                //  onRequestPermissionsResult();
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private void scanSuccess() {
        results = wifiManager.getScanResults();
        int r = results.size();
        int i = 0, v = 0;
        prom = new String[r];
        int freq;
        while (i < r) {
            if (results.get(i).level >= -75) {
                freq = results.get(i).frequency;
                mac = results.get(i).BSSID;
                prom[v] = ("SSID: " + results.get(i).SSID + "\r\nMAC: " + mac +
                        "\r\nRSSI: " + results.get(i).level + "\r\nFreq: " + freq);
                i++;
                v++;
            } else i++;
        }
        sense = new String[v];
        i = 0;
        while (i < v) {
            sense[i] = (prom[i]);
            i++;
        }
        lv.setAdapter(new ArrayAdapter<>(this, R.layout.row, sense));
    }

    private void scanSend() {
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    sendSuccess();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (success) {
            sendSuccess();
        }
    }

    private void sendSuccess() {
        resSend = wifiManager.getScanResults();
        int r = resSend.size();
        int i = 0, v = 0; tosend = "";
        while (i < r) {
            if (resSend.get(i).level >= -75) {
                mac = resSend.get(i).BSSID;
                tosend = tosend + mac + "," + resSend.get(i).level + ";";
                i++;
                v++;
            } else i++;
        }
        tosend = tosend.substring(0, tosend.length() - 1);
    }

    private void scanFailure() {
        if (results != null) {
            if (results.isEmpty())
                Toast.makeText(context,"Нет результатов", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context,"Нет новых результатов, подождите", Toast.LENGTH_SHORT).show();
        }
        else  Toast.makeText(context,"Нет результатов", Toast.LENGTH_SHORT).show();
    }

    private void sendFailure() {
        if (resSend != null) {
            if (resSend.isEmpty())
                Toast.makeText(context,"Нет результатов", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context,"Нет новых результатов, подождите", Toast.LENGTH_SHORT).show();
        }
        else  Toast.makeText(context,"Нет результатов", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public class Message {

        @SerializedName("key")
        @Expose
        private String key = "";
        @SerializedName("val")
        @Expose
        private String val = "";

        public void setKey(String keys) {
            this.key = keys;
        }

        public void setVal(String vals) {
            this.val = vals;
        }


    }

    public class Answer {
        @SerializedName("result")
        @Expose
        private String results = "";

        public String getResults() { return results; }
    }

    public interface MessagesApi {

        @GET("get/vector")
        Call<Answer> getAnswerWithMac(@Query("mac") String uemac);

        @POST("set/vector")
        Call<Message> setRecord(@Body Message data);
    }

    public static class RestClient {
        public static OkHttpClient getClient() {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            //logging.level(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            return client;

        }
    }

    public static class NetworkCall {
        private static NetworkCall mInstance;

        Gson gson = new GsonBuilder()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(address)
                .client(RestClient.getClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        public static NetworkCall getInstance() {
            if (mInstance == null) {
                mInstance = new NetworkCall();
            }
            return mInstance;
        }

        public MessagesApi getMessagesApi() {
            return retrofit.create(MessagesApi.class);
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.Scan) {
            BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (success) {
                        scanSuccess();
                    } else {
                        scanFailure();
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(wifiScanReceiver, intentFilter);
            boolean success = wifiManager.startScan();
            if (success) {
                scanSuccess();
            } else scanFailure();
        }

        if (id == R.id.Get) {
            NetworkCall.getInstance()
                    .getMessagesApi()
                    .getAnswerWithMac(uemac)
                    .enqueue(new Callback<Answer>() {
                        @Override
                        public void onResponse(@NonNull Call<Answer> call, @NonNull Response<Answer> response) {
                            Answer answer = response.body();
                            if (answer != null) {
                                sAnswer = answer.getResults();
                                Toast.makeText(context, sAnswer, Toast.LENGTH_SHORT).show(); }
                            else {
                                sAnswer = "Ничего не получили!";
                                Toast.makeText(context, sAnswer, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Answer> call, @NonNull Throwable t) {
                            sAnswer = "Получили ничего!";
                            Toast.makeText(context, sAnswer, Toast.LENGTH_SHORT).show();
                            t.printStackTrace();
                        }
                    });
        }

        if (id == R.id.Send) {
            BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (success) {
                        sendSuccess();
                    } else {
                        sendFailure();
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(wifiScanReceiver, intentFilter);
            boolean success = wifiManager.startScan();
            if (success) {
                sendSuccess();
            } else sendFailure();

            Message data = new Message();
            data.setKey(uemac);
            data.setVal(tosend);
            NetworkCall.getInstance()
                    .getMessagesApi()
                    .setRecord(data)
                    .enqueue(new Callback<Message>() {
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Отправили", Toast.LENGTH_SHORT).show(); }
                            else {
                                String err = "";
                                try {
                                    err = response.errorBody().toString();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(context, "Какая-то ошибка: " + err, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                            Toast.makeText(context, "Не отправили", Toast.LENGTH_SHORT).show();
                            t.printStackTrace();
                        }
                    });
        }
        if(id == R.id.sign_in_button)  signIn();
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignored) {
        }
        return "02:00:00:00:00:00";
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.v("MSG:", "работает");
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.v("MSG:", "Или работает");
        } catch (ApiException e) {
            Log.v("MSG:", "Не работает");
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personInfo = personName +" "+ personGivenName + " " +personFamilyName;
            Log.v("MSG:", personInfo);
        }
    }
}
