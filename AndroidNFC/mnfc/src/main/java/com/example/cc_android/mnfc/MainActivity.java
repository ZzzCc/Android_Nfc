package com.example.cc_android.mnfc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Context mContext = this;

    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mPendingIntent = null;
    private IntentFilter[] mIntentFilter = null;
    private String[][] mTechLists = null;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.tv);
        mTextView.setText("Scan a TAG!");

        NfcCheck();

        mPendingIntent = PendingIntent.getActivity(this,0,new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
    }

    private void NfcCheck(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter == null){
            //NFC功能存在
            Toast.makeText(MainActivity.this, "NFC功能不存在！", Toast.LENGTH_SHORT).show();
            return;
        }else{
            if (!mNfcAdapter.isEnabled()){
                //NFC功能未被打开，跳转到NFC功能设置页面
                Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(setNfc);
            }else{
                Toast.makeText(MainActivity.this, "NFC功能已开启！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(mContext,"MainActivity获取到了焦点。。。", Toast.LENGTH_SHORT).show();
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent,null,null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //解析NFC扫描到的数据
        MyNfc myNfc = new MyNfc();
        String[][] strArray = myNfc.nfcData(intent);
        String data = "";
        for (int i = 0; i < strArray.length; i++){
            for (int j = 0; j < strArray[i].length; j++){
                data += strArray[i][j];
            }
        }
        Log.i("Nfc", data);
        String text = strArray[0][3] + strArray[0][2] +strArray[0][1] +strArray[0][0];
        mTextView.setText(text);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

}
