package ru.grande.PSLite;

import java.util.Date;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import android.telephony.gsm.SmsManager;
import android.widget.Button;
import android.widget.Toast;

public class TabSMS extends Activity {

    private static final String TODO = " ";
    private TextView tvEnabledGPS;
    private TextView tvStatusGPS;
    private TextView tvLocationGPS;
    private TextView tvEnabledNet;
    private TextView tvStatusNet;
    private TextView tvLocationNet;
    private Button shareIntent;
    private Button send;
    private String smsText = "";
    private LocationManager locationManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_two);
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        send = (Button) findViewById(R.id.send);
        //Настраиваем обработку нажатия кнопки "Отправить":
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = "+79537699596";
                String sms = smsText;



                //С помощью SMS менеджера отправляем сообщение и высвечиваем
                //Toast сообщение об успехе операции:
                if(smsText != "") {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(number, null, sms, null, null);
                        Toast.makeText(getApplicationContext(),
                                "SMS отправлено!", Toast.LENGTH_LONG).show();
                    }
                    //В случае фейла высвечиваем соответствующее сообщение:
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "SMS не отправлено, попытайтесь еще!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    smsText = "";
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            "SMS не отправлено, попытайтесь еще!", Toast.LENGTH_LONG).show();
                }
            }
        });

//        shareIntent = (Button) findViewById(R.id.sendViaIntent);
//        //Настраиваем обработку нажатия кнопки "Перейти в SMS":
//        shareIntent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //С помощью намерения Intent выполняем переход
//                //в стандартное SMS приложение аппарата:
//                try {
//                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
//                    //Передаем в него текст сообщения с нашего приложения:
//                    sendIntent.putExtra("sms_body", messageBody.getText().toString());
//                    sendIntent.setType("vnd.android-dir/mms-sms");
//                    startActivity(sendIntent);
//                }
//                //В случае неудачи - сообщение об ошибке:
//                catch (Exception e) {
//                    Toast.makeText(getApplicationContext(),
//                            "SMS не отправлено, попытайтесь еще!", Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//                }
//            }
//        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };

    //Вывод геолокации
    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
        smsText = formatLocation(location);
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    //Проверка активности систем геолокации
    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    //Нажатие на кнопки настроек
    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };


}