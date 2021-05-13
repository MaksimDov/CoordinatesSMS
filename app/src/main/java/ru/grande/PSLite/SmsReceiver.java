package ru.grande.PSLite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    public static String phoneNumber = "0";

    @Override
    public void onReceive(Context context, Intent intent) {
//---получить входящее SMS сообщение---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null) {
//---извлечь полученное SMS ---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += msgs[i].getMessageBody().toString();
            }
            int numberIndex = str.indexOf("79");
            if(numberIndex != -1){
                str = "+" + str.substring(numberIndex, numberIndex+11);
                if(!str.isEmpty()) this.phoneNumber = str;
//---Показать новое SMS сообщение---
                Toast.makeText(context, this.phoneNumber, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

}