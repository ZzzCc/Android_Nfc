package com.example.cc_android.timetest;

import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView tvTime;
    private Button btnNowTime;
    private Button mBtnTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTime = findViewById(R.id.tv_time);
        btnNowTime = findViewById(R.id.btn_getNowTime);
        mBtnTime = findViewById(R.id.btn_time);

        btnNowTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                tvTime.setText(date);
            }
        });

        mBtnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isLog = isLoging("2017-12-11 12:00:00", tvTime.getText().toString());
                Toast.makeText(MainActivity.this, isLog + "", Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean isLoging(String logTime, String nowTime){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d1 = df.parse(logTime);
            Date d2 = df.parse(nowTime);
            long diff = d2.getTime() - d1.getTime();//这样得到的差值是微秒级别
            if (diff > (1000 * 60 * 60 * 24)){
                return false;
            } else {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
