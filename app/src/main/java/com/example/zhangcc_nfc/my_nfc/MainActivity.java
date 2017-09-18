package com.example.zhangcc_nfc.my_nfc;

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

//    //NDEF类型标签，数据类型为字符串时的处理方法。
//    private boolean readFromTag(Intent intent){
//        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//        NdefMessage mNdefMsg = (NdefMessage)rawArray[0];
//        NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
//        try {
//            if(mNdefRecord != null){
//                String readResult = new String(mNdefRecord.getPayload(), "UTF-8");
//                Toast.makeText(MainActivity.this, readResult, Toast.LENGTH_SHORT);
//                mTextView.setText("标签信息：" + readResult);
//                return true;
//            }
//        }
//        catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        };
//        return false;
//    }
//
//    //非NDEF类型标签的，数据处理方法
//    private void resolveIntent(Intent intent){
//        String action = intent.getAction();
//        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        boolean isAuth = false;
//        if(supportedTechs(tag.getTechList())){
//            MifareClassic mfc = MifareClassic.get(tag);
//            if (mfc != null){
//                try {
//                    mfc.connect();
//                    int sectorCount = mfc.getSectorCount();
//                    //扇形区的数量
//                    Log.i("ccLog", "sectorCount:" + sectorCount);
//                    for (int i = 0; i < sectorCount; i++){
//                        if (mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)){
//                            isAuth = true;
//                        }else {
//                            isAuth = false;
//                        }
//                        if (isAuth){
//                            int nBlock = mfc.getBlockCountInSector(i);
//                            //数据块的数量
//                            Log.i("ccLog", "nBlock = " + nBlock);
////                            for (int j = 0; j < nBlock; j++){
////                                byte[] data = mfc.readBlock(j);
////                                //String str = new String(data, "UTF-8");
////                                printHexString(data);
////                                //Log.i("ccLog", "标签数据：str => " + str);
////                            }
//                            byte[] data  = mfc.readBlock(0);
//                            String tagData = printHexString(data);
//                            Log.i("ccLog", "标签数据：tagData => " + tagData);
//                            mTextView.setText("RFID: " + tagData);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }else{
//                Log.i("ccLog", "您的标签不匹配");
//            }
//        }
//    }
//
//    private boolean supportedTechs(String[] techList) {
//        boolean isSupport = false;
//        for (String s : techList){
//            Log.i("ccLog","All SupportedTechs:" + s);
//        }
//        for (String s : techList){
//            if (s.equals("android.nfc.tech.MifareClassic")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.MifareClassic");
//            }else if (s.equals("android.nfc.tech.MifareUltralight")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.MifareUltralight");
//            }else if (s.equals("android.nfc.tech.Ndef")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.Ndef");
//            }else if (s.equals("android.nfc.tech.IsoDep")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.IsoDep");
//            }else if (s.equals("android.nfc.tech.NdefFormatable")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.NdefFormatable");
//            }else if (s.equals("android.nfc.tech.NfcA")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.NfcA");
//            }else if (s.equals("android.nfc.tech.NfcB")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.NfcB");
//            }else if (s.equals("android.nfc.tech.NfcF")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.NfcF");
//            }else if (s.equals("android.nfc.tech.NfcV")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.NfcV");
//            }else if (s.equals("android.nfc.tech.TagTechnology")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.TagTechnology");
//            }else if (s.equals("android.nfc.tech.NfcBarcode")){
//                isSupport = true;
//                Log.i("ccLog","android.nfc.tech.NfcBarcode");
//            }
//        }
//
//        return isSupport;
//    }
//
//    //将指定byte数组以16进制的形式打印到控制台
//    public String printHexString( byte[] b) {
//        String[] strs = new String[4];
//        for (int i = 0; i < 4; i++) {
//            String hex = Integer.toHexString(b[i] & 0xFF);
//            if (hex.length() == 1) {
//                hex = '0' + hex;
//            }
//            strs[i] = hex.toUpperCase();
//            Log.i("ccLog", hex.toUpperCase());
//        }
//        String data = strs[3] + strs[2] + strs[1] + strs[0];
//        return data;
//    }
}
