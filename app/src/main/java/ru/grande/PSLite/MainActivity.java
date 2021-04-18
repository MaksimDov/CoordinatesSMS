package ru.grande.PSLite;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends TabActivity{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        Resources res = getResources();
        TabHost th = getTabHost();

        th.addTab(th.newTabSpec("").setIndicator("tab1").
                setContent(new Intent(this, WifiPosition.class)));
        th.addTab(th.newTabSpec("").setIndicator("tab2")
                .setContent(new Intent(this, TabSMS.class)));

        th.setCurrentTab(1);
    }
}

